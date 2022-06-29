package top.zenyoung.generator.type;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Mysql数据行类型枚举
 *
 * @author young
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DbColumnType implements ColumnType {
    /**
     * 基本类型-byte
     */
    BASE_BYTE("byte", null),
    /**
     * 基本类型-short
     */
    BASE_SHORT("short", null),
    /**
     * 基本类型-char
     */
    BASE_CHAR("char", null),
    /**
     * 基本类型-int
     */
    BASE_INT("int", null),
    /**
     * 基本类型-long
     */
    BASE_LONG("long", null),
    /**
     * 基本类型-float
     */
    BASE_FLOAT("float", null),
    /**
     * 基本类型-double
     */
    BASE_DOUBLE("double", null),
    /**
     * 基本类型-double
     */
    BASE_BOOLEAN("boolean", null),

    /**
     * 包装类型-Byte
     */
    BYTE("Byte", null),
    /**
     * 包装类型-Short
     */
    SHORT("Short", null),
    /**
     * 包装类型-Character
     */
    CHARACTER("Character", null),
    /**
     * 包装类型-Integer
     */
    INTEGER("Integer", null),
    /**
     * 包装类型-Long
     */
    LONG("Long", null),
    /**
     * 包装类型-Float
     */
    FLOAT("Float", null),
    /**
     * 包装类型-Double
     */
    DOUBLE("Double", null),
    /**
     * 包装类型-Boolean
     */
    BOOLEAN("Boolean", null),
    /**
     * 包装类型-String
     */
    STRING("String", null),

    /**
     * sql包下数据类型-Date
     */
    DATE_SQL("Date", "java.sql.Date"),
    /**
     * sql包下数据类型-Time
     */
    TIME("Time", "java.sql.Time"),
    /**
     * sql包下数据类型-Timestamp
     */
    TIMESTAMP("Timestamp", "java.sql.Timestamp"),
    /**
     * sql包下数据类型-Blob
     */
    BLOB("Blob", "java.sql.Blob"),
    /**
     * sql包下数据类型-Clob
     */
    CLOB("Clob", "java.sql.Clob"),

    /**
     * java8新时间类型-LocalDate
     */
    LOCAL_DATE("LocalDate", "java.time.LocalDate"),
    /**
     * java8新时间类型-LocalTime
     */
    LOCAL_TIME("LocalTime", "java.time.LocalTime"),
    /**
     * java8新时间类型-Year
     */
    YEAR("Year", "java.time.Year"),
    /**
     * java8新时间类型-YearMonth
     */
    YEAR_MONTH("YearMonth", "java.time.YearMonth"),
    /**
     * java8新时间类型-LocalDateTime
     */
    LOCAL_DATE_TIME("LocalDateTime", "java.time.LocalDateTime"),
    /**
     * java8新时间类型-Instant
     */
    INSTANT("Instant", "java.time.Instant"),

    /**
     * byte[]
     */
    BYTE_ARRAY("byte[]", null),
    /**
     * Object
     */
    OBJECT("Object", null),
    /**
     * Date
     */
    DATE("Date", "java.util.Date"),
    /**
     * BigInteger
     */
    BIG_INTEGER("BigInteger", "java.math.BigInteger"),
    /**
     * BigDecimal
     */
    BIG_DECIMAL("BigDecimal", "java.math.BigDecimal");

    private final String type;
    private final String pkg;
}
