package top.zenyoung.sms.aliyun;

import com.aliyuncs.AcsRequest;
import com.aliyuncs.AcsResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.sms.exption.SmsException;
import top.zenyoung.sms.vo.BaseQueryVO;
import top.zenyoung.sms.vo.BaseSmsVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 阿里云-短信服务-基类
 *
 * @author yangyong
 */
@Slf4j
abstract class BaseAliSmsService {
    private static final ObjectMapper OBJ_MAPPER = new ObjectMapper();
    private static final ThreadLocal<DateFormat> LOCAL_TIME_FORMAT = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    );
    private static final ThreadLocal<DateFormat> LOCAL_DATE_FORMAT = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyy-MM-dd")
    );

    public static final String SUCCESS = "ok";

    protected <T extends AcsResponse> void handler(@Nonnull final IAcsClient client, @Nonnull final AcsRequest<T> req,
                                                   @Nonnull final Consumer<T> resHandler) throws SmsException {
        try {
            final T res = client.getAcsResponse(req);
            resHandler.accept(res);
        } catch (ClientException e) {
            log.error("handler(req: {})-exp: {}", req, e.getMessage());
            throw new SmsException(e.getMessage());
        }
    }

    protected <T extends BaseSmsVO> void buildStatus(@Nonnull final T vo) {
        vo.setStatus(SUCCESS.equalsIgnoreCase(vo.getCode()));
    }

    protected <T extends BaseQueryVO<?>> void buildStatus(@Nonnull final T vo) {
        vo.setStatus(SUCCESS.equalsIgnoreCase(vo.getCode()));
    }

    protected static <T> String toJson(@Nonnull final T data) throws SmsException {
        try {
            return OBJ_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.warn("toJson(data: {})-exp: {}", data, e.getMessage());
            throw new SmsException(e.getMessage());
        }
    }

    protected static Date parseTime(@Nullable final String time) {
        if (!Strings.isNullOrEmpty(time)) {
            try {
                final DateFormat format = LOCAL_TIME_FORMAT.get();
                return format.parse(time);
            } catch (ParseException e) {
                log.warn("parseTime(time: {})-exp: {}", time, e.getMessage());
            }
        }
        return null;
    }

    protected static Date parseDate(@Nullable final String day) {
        if (!Strings.isNullOrEmpty(day)) {
            try {
                final DateFormat format = LOCAL_DATE_FORMAT.get();
                return format.parse(day);
            } catch (ParseException e) {
                log.warn("parseDate(day: {})-exp: {}", day, e.getMessage());
            }
        }
        return null;
    }

    protected static String dayFormat(@Nullable final Date date) {
        if (Objects.nonNull(date)) {
            final DateFormat format = LOCAL_DATE_FORMAT.get();
            return format.format(date);
        }
        return null;
    }
}
