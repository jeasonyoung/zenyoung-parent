package top.zenyoung.common.util;

import lombok.experimental.UtilityClass;
import top.zenyoung.common.model.DateRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.UnaryOperator;

/**
 * 日期-工具类
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/28 1:43 下午
 **/
@UtilityClass
public class DateUtils {

    /**
     * 将LocalDate转换为Date类型
     *
     * @param localDate LocalDate类型数据
     * @return Date类型数据
     */
    public Date fromLocalDate(@Nullable final LocalDate localDate) {
        if (localDate != null) {
            final ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
            return Date.from(zonedDateTime.toInstant());
        }
        return null;
    }

    /**
     * 将Date日期转换为LocalDate
     *
     * @param date Date类型数据
     * @return LocalDate类型数据
     */
    public LocalDate toLocalDate(@Nullable final Date date) {
        if (date != null) {
            final Instant instant = date.toInstant();
            final ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
            return zonedDateTime.toLocalDate();
        }
        return null;
    }

    /**
     * 创建以当前时间为开始的限期时间段
     *
     * @param endDateHandler 结束时间处理
     * @return 限期时间段
     */
    public DateRange createWithinAfter(@Nonnull final UnaryOperator<LocalDate> endDateHandler) {
        final LocalDate start = LocalDate.now();
        final LocalDate end = endDateHandler.apply(start);
        return DateRange.of(start, end);
    }

    /**
     * 创建以当前时间为结束的限期时间段
     *
     * @param startDateHandler 开始时间处理
     * @return 限期时间段
     */
    public DateRange createWithinBefore(@Nonnull final UnaryOperator<LocalDate> startDateHandler) {
        final LocalDate end = LocalDate.now();
        final LocalDate start = startDateHandler.apply(end);
        return DateRange.of(start, end);
    }

    /**
     * 创建去过一年内
     *
     * @return 去过一年内
     */
    public DateRange createWithinBeforeYear() {
        return createWithinBefore(end -> end.plusYears(-1).plusDays(-1));
    }

    /**
     * 创建将来一年内
     *
     * @return 将来一年内
     */
    public DateRange createWithinAfterYear() {
        return createWithinAfter(start -> start.plusYears(1).plusDays(-1));
    }

    /**
     * 创建去过一月内
     *
     * @return 去过一月内
     */
    public DateRange createWithinBeforeMonth() {
        return createWithinBefore(end -> end.plusMonths(-1).plusDays(-1));
    }

    /**
     * 创建将来一月内
     *
     * @return 将来一月内
     */
    public DateRange createWithinAfterMonth() {
        return createWithinAfter(start -> start.plusMonths(1).plusDays(-1));
    }

    /**
     * 创建去过一周内
     *
     * @return 去过一周内
     */
    public DateRange createWithinBeforeWeek() {
        return createWithinBefore(end -> end.plusWeeks(-1).plusDays(-1));
    }

    /**
     * 创建将来一周内
     *
     * @return 将来一周内
     */
    public DateRange createWithinAfterWeek() {
        return createWithinAfter(start -> start.plusMonths(1).plusDays(-1));
    }

    /**
     * 创建去过一天内
     *
     * @return 去过一天内
     */
    public DateRange createWithinBeforeDay() {
        return createWithinBefore(end -> end.plusDays(-1).plus(Duration.of(-1, ChronoUnit.HOURS)));
    }

    /**
     * 创建将来一天内
     *
     * @return 将来一天内
     */
    public DateRange createWithinAfterDay() {
        return createWithinAfter(start -> start.plusDays(1).plus(Duration.of(-1, ChronoUnit.HOURS)));
    }
}
