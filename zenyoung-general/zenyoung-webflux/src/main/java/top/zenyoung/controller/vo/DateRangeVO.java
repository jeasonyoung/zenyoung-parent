package top.zenyoung.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.zenyoung.common.model.DateRange;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Date;

/**
 * 时间段-数据VO
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/28 1:46 下午
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateRangeVO implements Serializable {
    /**
     * 开始时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date start;
    /**
     * 结束时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date end;

    /**
     * 转换为时间段数据
     *
     * @return 时间段数据
     */
    public DateRange toRange() {
        return new DateRange(start, end);
    }

    /**
     * 创建VO数据
     *
     * @param data 时间段数据
     * @return VO数据
     */
    public static DateRangeVO of(@Nullable final DateRange data) {
        if (data != null) {
            return new DateRangeVO(data.getStart(), data.getEnd());
        }
        return null;
    }

    /**
     * 创建VO数据
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return VO数据
     */
    public static DateRangeVO of(@Nullable final Date start, @Nullable final Date end) {
        return new DateRangeVO(start, end);
    }
}
