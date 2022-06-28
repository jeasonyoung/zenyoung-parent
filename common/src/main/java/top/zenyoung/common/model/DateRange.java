package top.zenyoung.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.zenyoung.common.util.DateUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * 时间范围
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/28 1:45 下午
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateRange implements Serializable {
    /**
     * 开始时间
     */
    private Date start;
    /**
     * 结束时间
     */
    private Date end;

    /**
     * 构建时间范围对象
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 时间范围对象
     */
    public static DateRange of(@Nullable final Date start, @Nullable final Date end) {
        return new DateRange(start, end);
    }

    /**
     * 构建时间范围对象
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 时间范围对象
     */
    public static DateRange of(@Nullable final LocalDate start, @Nullable final LocalDate end) {
        return new DateRange(DateUtils.fromLocalDate(start), DateUtils.fromLocalDate(end));
    }
}
