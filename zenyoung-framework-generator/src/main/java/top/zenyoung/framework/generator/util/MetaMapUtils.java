package top.zenyoung.framework.generator.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;
import top.zenyoung.framework.generator.Constants;
import top.zenyoung.framework.generator.config.GeneratorProperties;
import top.zenyoung.framework.generator.db.Table;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 元数据工具类
 *
 * @author young
 */
public class MetaMapUtils {
    private static final String IGNORE_SEP = ",";
    private static final String MODULE_NAME_REGEX = "^[a-z]+$";
    private static final Map<String, Pattern> PATTERN_MAPS = Maps.newHashMap();
    private static final Map<String, List<String>> IGNORE_MAPS = Maps.newHashMap();

    private static final List<String> PRIMARY_KEYS = Lists.newArrayList(
            "private", "protected", "public", "native",
            "default", "abstract", "class", "interface",
            "extends", "final", "implements", "new",
            "strictfp", "static", "public", "synchronized",
            "transient", "volatile", "break", "case", "continue",
            "do", "else", "for", "if", "instanceof", "return", "switch");

    private static Pattern getPattern(@Nonnull final String pattern) {
        if (!Strings.isNullOrEmpty(pattern)) {
            return PATTERN_MAPS.computeIfAbsent(pattern, k -> Pattern.compile(pattern));
        }
        return null;
    }

    private static List<String> getIgnores(@Nonnull final String ignores) {
        if (!Strings.isNullOrEmpty(ignores)) {
            return IGNORE_MAPS.computeIfAbsent(ignores, k -> NameUtils.splitter(IGNORE_SEP, ignores));
        }
        return Lists.newArrayList();
    }

    public static String buildModuleName(@Nonnull final GeneratorProperties properties, @Nonnull final String tableName) {
        final String moduleName;
        if (!Strings.isNullOrEmpty(moduleName = properties.getModuleName())) {
            if (moduleName.matches(MODULE_NAME_REGEX) && !checkPrimaryKey(moduleName)) {
                return moduleName;
            }
            if (!Strings.isNullOrEmpty(tableName)) {
                final Pattern pattern = getPattern(moduleName);
                if (Objects.nonNull(pattern)) {
                    final List<String> ignores = getIgnores(properties.getModuleNameIgnores());
                    final Matcher matcher = pattern.matcher(tableName);
                    while (matcher.find()) {
                        String val = matcher.group().toLowerCase();
                        if (!Strings.isNullOrEmpty(val)) {
                            if (checkPrimaryKey(val)) {
                                continue;
                            }
                            if (CollectionUtils.isEmpty(ignores)) {
                                return val;
                            }
                            for (String ignore : ignores) {
                                if (!Strings.isNullOrEmpty(ignore) && val.contains(ignore)) {
                                    val = val.replaceAll(ignore, "").trim();
                                }
                            }
                            if (!Strings.isNullOrEmpty(val) && !checkPrimaryKey(val)) {
                                return val;
                            }
                        }
                    }
                }
                //按下划线进行拆分
                final List<String> vals = NameUtils.underlineToSplit(tableName);
                if (!CollectionUtils.isEmpty(vals)) {
                    for (String val : vals) {
                        if (!checkPrimaryKey(val)) {
                            return val;
                        }
                    }
                }
            }
        }
        return "";
    }

    public static boolean checkPrimaryKey(@Nonnull final String key) {
        return PRIMARY_KEYS.contains(key);
    }

    public static Map<String, Object> getBasic(@Nonnull final GeneratorProperties properties) {
        return new HashMap<String, Object>(7) {
            {
                //1.服务名称
                put(Constants.PARAM_SERVER_NAME, properties.getServerName());
                //2.基础包名
                put(Constants.PARAM_BASE_PACKAGE, properties.getBasePackageName());
                //3.id类型
                put(Constants.PARAM_ID_TYPE, String.class.getSimpleName());
                //4.是否生成 api 以提供服务
                put(Constants.PARAM_IS_PROVIDE_SERVER, properties.getIsProvideService());
                //5.是否生成 含有BaseAPi
                put(Constants.PARAM_HASH_BASE_API, properties.getHasBaseApi());
                //6.是否包含 orm
                put(Constants.PARAM_HAS_ORM, properties.getHasOrm());
                //7.日期
                put(Constants.PARAM_DATE, new Date());
            }
        };
    }

    public static Map<String, Object> getTables(@Nonnull final GeneratorProperties properties,
                                                   @Nonnull final Table table) {
        return new HashMap<String, Object>(22){
            {
                //基础参数数据
                putAll(getBasic(properties));
                //8.模块名称
                put(Constants.PARAM_MODULE_NAME, buildModuleName(properties, table.getName()));
                //字符串下划线转驼峰格式
                final String camel = NameUtils.underlineToCamel(table.getName());
                //首字母大写
                final String upperCase = NameUtils.firstToUpperCase(camel);
                //首字母小写
                final String lowerCase = NameUtils.firstToLowerCase(camel);
                //9.api名称
                put(Constants.PARAM_API_NAME, upperCase);
                //10.controller名称
                put(Constants.PARAM_CONTROLLER_NAME, upperCase);
                //11.业务资源名称
                put(Constants.PARAM_SOURCE_NAME, lowerCase);
                //12.service名称
                put(Constants.PARAM_SERVICE_NAME, upperCase);
                //13.service名称 首字母小写
                put(Constants.PARAM_SERVICE_NAME_FIRST_LOWER, lowerCase);
                //14.serviceImpl名称
                put(Constants.PARAM_SERVICE_IMPL_NAME, upperCase);
                //15.mapper名称
                put(Constants.PARAM_MAPPER_NAME, upperCase);
                //16.mapper名称 首字母小写
                put(Constants.PARAM_MAPPER_NAME_FIRST_LOWER, lowerCase);
                //17.mapper xml
                put(Constants.PARAM_MAPPER_XML_NAME, upperCase);
                //18.vo名称
                put(Constants.PARAM_VO_NAME, upperCase);
                //19.po名称
                put(Constants.PARAM_PO_NAME, upperCase);
                //20.dto名称
                put(Constants.PARAM_DTO_NAME, upperCase);
                //21.表信息
                put(Constants.PARAM_TABLE, table);
            }
        };
    }
}
