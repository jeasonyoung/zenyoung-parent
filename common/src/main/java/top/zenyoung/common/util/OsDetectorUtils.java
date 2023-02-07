package top.zenyoung.common.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OS探测器工具类
 *
 * @author young
 */
@Slf4j
public class OsDetectorUtils {
    private static final String DETECTED_NAME = "os.detected.name";
    private static final String DETECTED_ARCH = "os.detected.arch";
    private static final String DETECTED_BITNESS = "os.detected.bitness";
    private static final String DETECTED_VERSION = "os.detected.version";
    private static final String DETECTED_VERSION_MAJOR = DETECTED_VERSION + ".major";
    private static final String DETECTED_VERSION_MINOR = DETECTED_VERSION + ".minor";
    private static final String DETECTED_CLASSIFIER = "os.detected.classifier";
    private static final String DETECTED_RELEASE = "os.detected.release";
    private static final String DETECTED_RELEASE_VERSION = DETECTED_RELEASE + ".version";
    private static final String DETECTED_RELEASE_LIKE_PREFIX = DETECTED_RELEASE + ".like.";

    private static final String UNKNOWN = "unknown";
    private static final String LINUX_ID_PREFIX = "ID=";
    private static final String LINUX_ID_LIKE_PREFIX = "ID_LIKE=";
    private static final String LINUX_VERSION_ID_PREFIX = "VERSION_ID=";
    private static final String[] LINUX_OS_RELEASE_FILES = {"/etc/os-release", "/usr/lib/os-release"};
    private static final String REDHAT_RELEASE_FILE = "/etc/redhat-release";
    private static final String[] DEFAULT_REDHAT_VARIANTS = {"rhel", "fedora"};

    private static final Pattern VERSION_REGEX = Pattern.compile("((\\d+)\\.(\\d+)).*");
    private static final Pattern REDHAT_MAJOR_VERSION_REGEX = Pattern.compile("(\\d+)");

    private final SystemPropertyOperationProvider propertyOperationProvider;
    private final FileOperationProvider fileOperationProvider;

    private final Properties properties = new Properties();

    private OsDetectorUtils(@Nonnull final SystemPropertyOperationProvider pop, @Nonnull final FileOperationProvider fop) {
        this.propertyOperationProvider = pop;
        this.fileOperationProvider = fop;
        this.detect(properties, Collections.emptyList());
    }

    private static OsDetectorUtils instance = null;
    private static final Object LOCKS = new Object();

    private static String getProperties(@Nonnull final String name) {
        if (!Strings.isNullOrEmpty(name)) {
            if (Objects.isNull(instance)) {
                synchronized (LOCKS) {
                    instance = new OsDetectorUtils(new SimpleSystemPropertyOperations(), new SimpleFileOperations());
                }
            }
            return instance.properties.getProperty(name);
        }
        return null;
    }

    /**
     * 获取OS名称
     *
     * @return OS名称
     */
    public static String getOsName() {
        return getProperties(DETECTED_NAME);
    }

    /**
     * 获取OS架构
     *
     * @return os架构
     */
    public static String getOsArch() {
        return getProperties(DETECTED_ARCH);
    }

    /**
     * 获取Os位数(32/64)
     *
     * @return Os位数(32 / 64)
     */
    public static Integer getOsBitness() {
        final String bit = getProperties(DETECTED_BITNESS);
        if (!Strings.isNullOrEmpty(bit)) {
            return Integer.parseInt(bit);
        }
        return null;
    }

    protected void detect(@Nonnull final Properties props, @Nonnull final List<String> classifierWithLikes) {
        log.info("------------------------------------------------------------------------");
        log.info("Detecting the operating system and CPU architecture");
        log.info("------------------------------------------------------------------------");
        final String osName = propertyOperationProvider.getSystemProperty("os.name");
        final String osArch = propertyOperationProvider.getSystemProperty("os.arch");
        final String osVersion = propertyOperationProvider.getSystemProperty("os.version");
        //
        final String detectedName = normalizeOs(osName), detectedArch = normalizeArch(osArch);
        final int detectedBitness = determineBitness(detectedArch);
        //
        setProperty(props, DETECTED_NAME, detectedName);
        setProperty(props, DETECTED_ARCH, detectedArch);
        setProperty(props, DETECTED_BITNESS, "" + detectedBitness);
        //
        final Matcher versionMatcher = VERSION_REGEX.matcher(osVersion);
        if (versionMatcher.matches()) {
            setProperty(props, DETECTED_VERSION, versionMatcher.group(1));
            setProperty(props, DETECTED_VERSION_MAJOR, versionMatcher.group(2));
            setProperty(props, DETECTED_VERSION_MINOR, versionMatcher.group(3));
        }
        //
        final String failOnUnknownOs = propertyOperationProvider.getSystemProperty("failOnUnknownOS");
        if (!"false".equalsIgnoreCase(failOnUnknownOs)) {
            if (UNKNOWN.equals(detectedName)) {
                throw new RuntimeException("unknown os.name: " + osName);
            }
            if (UNKNOWN.equals(detectedArch)) {
                throw new RuntimeException("unknown os.arch: " + osArch);
            }
        }
        // Assume the default classifier, without any os "like" extension.
        final StringBuilder detectedClassifierBuilder = new StringBuilder();
        detectedClassifierBuilder.append(detectedName);
        detectedClassifierBuilder.append('-');
        detectedClassifierBuilder.append(detectedArch);
        // For Linux systems, add additional properties regarding details of the OS.
        final LinuxRelease linuxRelease = "linux".equals(detectedName) ? getLinuxRelease() : null;
        if (Objects.nonNull(linuxRelease)) {
            setProperty(props, DETECTED_RELEASE, linuxRelease.id);
            if (linuxRelease.version != null) {
                setProperty(props, DETECTED_RELEASE_VERSION, linuxRelease.version);
            }
            // Add properties for all systems that this OS is "like".
            for (final String like : linuxRelease.like) {
                final String propKey = DETECTED_RELEASE_LIKE_PREFIX + like;
                setProperty(props, propKey, "true");
            }
            // If any of the requested classifier likes are found in the "likes" for this system,
            // append it to the classifier.
            for (final String classifierLike : classifierWithLikes) {
                if (linuxRelease.like.contains(classifierLike)) {
                    detectedClassifierBuilder.append('-');
                    detectedClassifierBuilder.append(classifierLike);
                    // First one wins.
                    break;
                }
            }
        }
        setProperty(props, DETECTED_CLASSIFIER, detectedClassifierBuilder.toString());
    }

    private void setProperty(@Nonnull final Properties props, @Nonnull final String name, @Nonnull final String value) {
        props.setProperty(name, value);
        propertyOperationProvider.setSystemProperty(name, value);
    }

    private static String normalizeOs(@Nonnull final String val) {
        final String value = normalize(val);
        if (!Strings.isNullOrEmpty(value)) {
            final Map<List<String>, Function<String, String>> osMapHandlers = new HashMap<List<String>, Function<String, String>>(11) {
                {
                    //1.aix
                    final String aix = "aix";
                    put(Lists.newArrayList(aix), v -> aix);
                    //2.hpux
                    final String hpux = "hpux";
                    put(Lists.newArrayList(hpux), v -> hpux);
                    //3.os400
                    final String os400 = "os400";
                    put(Lists.newArrayList(os400), v -> {
                        final int idx = 5;
                        if (v.length() <= idx || !Character.isDigit(v.charAt(idx))) {
                            return os400;
                        }
                        return null;
                    });
                    //4.linux
                    final String linux = "linux";
                    put(Lists.newArrayList(linux), v -> linux);
                    //5.mac/osx
                    final String mac = "mac", osx = "osx";
                    put(Lists.newArrayList(mac, osx), v -> osx);
                    //6.freebsd
                    final String freebsd = "freebsd";
                    put(Lists.newArrayList(freebsd), v -> freebsd);
                    //7.openbsd
                    final String openbsd = "openbsd";
                    put(Lists.newArrayList(openbsd), v -> openbsd);
                    //8.netbsd
                    final String netbsd = "netbsd";
                    put(Lists.newArrayList(netbsd), v -> netbsd);
                    //9.solaris/sunos
                    final String solaris = "solaris", sunos = "sunos";
                    put(Lists.newArrayList(solaris, sunos), v -> sunos);
                    //10.windows
                    final String windows = "windows";
                    put(Lists.newArrayList(windows), v -> windows);
                    //11.zos
                    final String zos = "zos";
                    put(Lists.newArrayList(zos), v -> zos);
                }
            };
            //
            for (final Map.Entry<List<String>, Function<String, String>> entry : osMapHandlers.entrySet()) {
                final List<String> targets = entry.getKey();
                final Function<String, String> fn = entry.getValue();
                for (final String target : targets) {
                    if (value.startsWith(target)) {
                        final String ret = fn.apply(value);
                        if (!Strings.isNullOrEmpty(ret)) {
                            return ret;
                        }
                    }
                }
            }
        }
        return UNKNOWN;
    }

    private static String normalizeArch(@Nonnull final String val) {
        final String value = normalize(val);
        if (!Strings.isNullOrEmpty(value)) {
            final Map<Function<String, Boolean>, String> archMapHandlers = new HashMap<Function<String, Boolean>, String>() {
                {
                    //1.x86_64
                    final String x8664Regx = "^(x8664|amd64|ia32e|em64t|x64)$", x8664 = "x86_64";
                    put(v -> v.matches(x8664Regx), x8664);
                    //2.x86_32
                    final String x8632Regx = "^(x8632|x86|i[3-6]86|ia32|x32)$", x8632 = "x86_32";
                    put(v -> v.matches(x8632Regx), x8632);
                    //3.itanium_64
                    final String itanium64Regx = "^(ia64w?|itanium64)$", itanium64 = "itanium_64";
                    put(v -> v.matches(itanium64Regx), itanium64);
                    //4.ia64n
                    final String itanium32Regx = "ia64n", itanium32 = "itanium_32";
                    put(v -> v.equalsIgnoreCase(itanium32Regx), itanium32);
                    //5.sparc_32
                    final String sparc32Regx = "^(sparc|sparc32)$", sparc32 = "sparc_32";
                    put(v -> v.matches(sparc32Regx), sparc32);
                    //6.sparc_64
                    final String sparc64Regx = "^(sparcv9|sparc64)$", sparc64 = "sparc_64";
                    put(v -> v.matches(sparc64Regx), sparc64);
                    //7.arm_32
                    final String arm32Regx = "^(arm|arm32)$", arm32 = "arm_32";
                    put(v -> v.matches(arm32Regx), arm32);
                    //8.aarch_64
                    final String aarch64Regx = "aarch64", aarch64 = "aarch_64";
                    put(v -> v.equalsIgnoreCase(aarch64Regx), aarch64);
                    //9.mips_32
                    final String mips32Regx = "^(mips|mips32)$", mips32 = "mips_32";
                    put(v -> v.matches(mips32Regx), mips32);
                    //10.mips_64
                    final String mips64Regx = "mips64", mips64 = "mips_64";
                    put(v -> v.equalsIgnoreCase(mips64Regx), mips64);
                    //11.mipsel_64
                    final String mipsel64Regx = "mips64el", mipsel64 = "mipsel_64";
                    put(v -> v.equalsIgnoreCase(mipsel64Regx), mipsel64);
                    //12.ppc_32
                    final String ppc32Regx = "^(ppc|ppc32)$", ppc32 = "ppc_32";
                    put(v -> v.matches(ppc32Regx), ppc32);
                    //13.ppcle_32
                    final String ppcle32Regex = "^(ppcle|ppc32le)$", ppcle32 = "ppcle_32";
                    put(v -> v.matches(ppcle32Regex), ppcle32);
                    //14.ppc_64
                    final String ppc64Regex = "ppc64", ppc64 = "ppc_64";
                    put(v -> v.equalsIgnoreCase(ppc64Regex), ppc64);
                    //15.ppcle_64
                    final String ppcle64Regex = "ppc64le", ppcle64 = "ppcle_64";
                    put(v -> v.equalsIgnoreCase(ppcle64Regex), ppcle64);
                    //16.s390_32
                    final String s39032Regex = "s390", s39032 = "s390_32";
                    put(v -> v.equalsIgnoreCase(s39032Regex), s39032);
                    //17.s390_64
                    final String s39064Regex = "s390x", s39064 = "s390_64";
                    put(v -> v.equalsIgnoreCase(s39064Regex), s39064);
                    //18.riscv
                    final String riscvRegex = "^(riscv|riscv32)$", riscv = "riscv";
                    put(v -> v.matches(riscvRegex), riscv);
                    //19.riscv64
                    final String riscv64Regex = "riscv64", riscv64 = "riscv64";
                    put(v -> v.equalsIgnoreCase(riscv64Regex), riscv64);
                    //20.e2k
                    final String e2k = "e2k";
                    put(v -> v.equalsIgnoreCase(e2k), e2k);
                    //21.loongarch64
                    final String loongarch64Regex = "loongarch64", loongarch64 = "loongarch_64";
                    put(v -> v.equalsIgnoreCase(loongarch64Regex), loongarch64);
                }
            };
            //
            for (final Map.Entry<Function<String, Boolean>, String> entry : archMapHandlers.entrySet()) {
                final Function<String, Boolean> handler = entry.getKey();
                if (handler.apply(value)) {
                    return entry.getValue();
                }
            }
        }
        return UNKNOWN;
    }

    private static String normalize(@Nonnull final String value) {
        if (!Strings.isNullOrEmpty(value)) {
            return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
        }
        return "";
    }

    private LinuxRelease getLinuxRelease() {
        // First, look for the os-release file.
        for (final String osReleaseFileName : LINUX_OS_RELEASE_FILES) {
            final LinuxRelease res = parseLinuxOsReleaseFile(osReleaseFileName);
            if (res != null) {
                return res;
            }
        }
        // Older versions of redhat don't have /etc/os-release. In this case, try
        // parsing this file.
        return parseLinuxRedhatReleaseFile();
    }

    /**
     * Parses a file in the format of {@code /etc/os-release} and return a {@link LinuxRelease}
     * based on the {@code ID}, {@code ID_LIKE}, and {@code VERSION_ID} entries.
     */
    private LinuxRelease parseLinuxOsReleaseFile(String fileName) {
        BufferedReader reader = null;
        try {
            InputStream in = fileOperationProvider.readFile(fileName);
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String id = null, version = null;
            final Set<String> likeSet = Sets.newLinkedHashSet();
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse the ID line.
                if (line.startsWith(LINUX_ID_PREFIX)) {
                    // Set the ID for this version.
                    id = normalizeOsReleaseValue(line.substring(LINUX_ID_PREFIX.length()));
                    // Also add the ID to the "like" set.
                    likeSet.add(id);
                    continue;
                }
                // Parse the VERSION_ID line.
                if (line.startsWith(LINUX_VERSION_ID_PREFIX)) {
                    // Set the ID for this version.
                    version = normalizeOsReleaseValue(line.substring(LINUX_VERSION_ID_PREFIX.length()));
                    continue;
                }
                // Parse the ID_LIKE line.
                if (line.startsWith(LINUX_ID_LIKE_PREFIX)) {
                    line = normalizeOsReleaseValue(line.substring(LINUX_ID_LIKE_PREFIX.length()));
                    // Split the line on any whitespace.
                    final String[] parts = line.split("\\s+");
                    Collections.addAll(likeSet, parts);
                }
            }
            if (id != null) {
                return new LinuxRelease(id, version, likeSet);
            }
        } catch (IOException ignored) {
            // Just absorb. Don't treat failure to read /etc/os-release as an error.
        } finally {
            IOUtils.closeQuietly(reader, null);
        }
        return null;
    }

    /**
     * Parses the {@code /etc/redhat-release} and returns a {@link LinuxRelease} containing the
     * ID and like ["rhel", "fedora", ID]. Currently only supported for CentOS, Fedora, and RHEL.
     * Other variants will return {@code null}.
     */
    private LinuxRelease parseLinuxRedhatReleaseFile() {
        BufferedReader reader = null;
        try {
            final InputStream in = fileOperationProvider.readFile(REDHAT_RELEASE_FILE);
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            // There is only a single line in this file.
            String line = reader.readLine();
            if (line != null) {
                line = line.toLowerCase(Locale.US);
                final String id;
                String version = null;
                if (line.contains("centos")) {
                    id = "centos";
                } else if (line.contains("fedora")) {
                    id = "fedora";
                } else if (line.contains("red hat enterprise linux")) {
                    id = "rhel";
                } else {
                    // Other variants are not currently supported.
                    return null;
                }
                final Matcher versionMatcher = REDHAT_MAJOR_VERSION_REGEX.matcher(line);
                if (versionMatcher.find()) {
                    version = versionMatcher.group(1);
                }
                final Set<String> likeSet = Sets.newLinkedHashSet(Arrays.asList(DEFAULT_REDHAT_VARIANTS));
                likeSet.add(id);
                return new LinuxRelease(id, version, likeSet);
            }
        } catch (IOException ignored) {
            // Just absorb. Don't treat failure to read /etc/os-release as an error.
        } finally {
            IOUtils.closeQuietly(reader, null);
        }
        return null;
    }

    private static String normalizeOsReleaseValue(String value) {
        // Remove any quotes from the string.
        return value.trim().replace("\"", "");
    }

    private int determineBitness(final String architecture) {
        // try the widely adopted sun specification first.
        String bitness = propertyOperationProvider.getSystemProperty("sun.arch.data.model", "");
        if (!bitness.isEmpty() && bitness.matches("[0-9]+")) {
            return Integer.parseInt(bitness, 10);
        }
        // bitness from sun.arch.data.model cannot be used. Try the IBM specification.
        bitness = propertyOperationProvider.getSystemProperty("com.ibm.vm.bitmode", "");
        if (!bitness.isEmpty() && bitness.matches("[0-9]+")) {
            return Integer.parseInt(bitness, 10);
        }
        // as a last resort, try to determine the bitness from the architecture.
        return guessBitnessFromArchitecture(architecture);
    }

    public static int guessBitnessFromArchitecture(@Nonnull final String arch) {
        if (arch.contains("64")) {
            return 64;
        }
        return 32;
    }

    /**
     * Interface exposing system property operations.
     */
    private interface SystemPropertyOperationProvider {
        /**
         * Gets the system property indicated by the specified name.
         *
         * @param name the name of the system property.
         * @return the string value of the system property, or {@code null} if there is no
         * property with that key.
         */
        String getSystemProperty(final String name);

        /**
         * Gets the system property indicated by the specified name.
         *
         * @param name the name of the system property.
         * @param def  a default value.
         * @return the string value of the system property, or the default value if there is
         * no property with that key.
         */
        String getSystemProperty(final String name, final String def);

        /**
         * Sets the system property indicated by the specified name.
         *
         * @param name  the name of the system property.
         * @param value the value of the system property.
         * @return the previous value of the system property, or {@code null} if it did not have one.
         */
        String setSystemProperty(final String name, final String value);
    }

    /**
     * Interface exposing file operations.
     */
    private interface FileOperationProvider {
        /**
         * Gets a {@link InputStream} for reading the content of the file with the specified path.
         *
         * @param filePath the system-dependent file path.
         * @return the {@link InputStream} that can be read to get the file content.
         * @throws IOException if the file does not exist, is a directory rather than a regular
         *                     file, or for some other reason cannot be opened for reading.
         */
        InputStream readFile(final String filePath) throws IOException;
    }

    private static class SimpleSystemPropertyOperations implements SystemPropertyOperationProvider {

        @Override
        public String getSystemProperty(final String name) {
            return System.getProperty(name);
        }

        @Override
        public String getSystemProperty(final String name, final String def) {
            return System.getProperty(name, def);
        }

        @Override
        public String setSystemProperty(final String name, final String value) {
            return System.setProperty(name, value);
        }
    }

    private static class SimpleFileOperations implements FileOperationProvider {

        @Override
        public InputStream readFile(final String filePath) throws IOException {
            return FileUtils.openInputStream(new File(filePath));
        }
    }

    private static class LinuxRelease {
        final String id;
        final String version;
        final Collection<String> like;

        LinuxRelease(final String id, final String version, final Set<String> like) {
            this.id = id;
            this.version = version;
            this.like = Collections.unmodifiableCollection(like);
        }
    }
}
