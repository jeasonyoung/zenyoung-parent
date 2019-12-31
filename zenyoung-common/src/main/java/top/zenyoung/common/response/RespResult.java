package top.zenyoung.common.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 响应结果
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/10/21 9:18 下午
 **/
@Data
@Builder
@ApiModel("响应结果")
@NoArgsConstructor
@AllArgsConstructor
public class RespResult<T extends Serializable> implements Serializable {
    /**
     * 状态码(0:成功,非0:失败)
     */
    @JsonIgnore
    private Integer code = 200;
    /**
     * 提示消息
     */
    @ApiModelProperty("提示消息")
    private String msg;
    /**
     * 响应数据
     */
    @ApiModelProperty("响应数据")
    private T data;

    /**
     * 构建响应结果
     *
     * @param code 状态码
     * @param msg  提示消息
     */
    public RespResult<T> buildResult(@Nonnull final Integer code, @Nullable final String msg) {
        this.setCode(code);
        this.setMsg(msg);
        return this;
    }

    /**
     * 构建响应结果
     *
     * @param retCode 结果代码枚举
     * @return 响应结果
     */
    public RespResult<T> buildResult(@Nonnull final ResultCode retCode) {
        buildResult(retCode.getVal(), retCode.getTitle());
        return this;
    }

    /**
     * 构建响应结果
     *
     * @param ret 绑定异常结果
     * @return 响应结果
     */
    private RespResult<T> buildResult(@Nonnull final BindingResult ret) {
        buildResult(ResultCode.Fail);
        final List<FieldError> errors = ret.getFieldErrors();
        if (!CollectionUtils.isEmpty(errors)) {
            setMsg(errors.stream()
                    .map(e-> {
                        String err = e.getDefaultMessage();
                        if(Strings.isNullOrEmpty(err)){
                            err = e.toString();
                        }
                        return err;
                    })
                    .collect(Collectors.joining())
            );
        }
        return this;
    }

    /**
     * 构造响应结果
     *
     * @param e 异常基类
     */
    public RespResult<T> buildResult(@Nonnull final Throwable e) {
        buildResult(ResultCode.Fail);
        if(e instanceof BindingResult){
            return buildResult((BindingResult)e);
        }
        Throwable cause = e.getCause();
        if (cause != null) {
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
        }
        setMsg((cause == null ? e : cause).getMessage());
        return this;
    }

    public Map<String, Object> toMap(){
        return new HashMap<String, Object>(3){
            {
                //状态码(0:成功,非0:失败)
                put("code", getCode());
                //提示消息
                put("msg", getMsg());
                //响应数据
                put("data", getData());
            }
        };
    }
}