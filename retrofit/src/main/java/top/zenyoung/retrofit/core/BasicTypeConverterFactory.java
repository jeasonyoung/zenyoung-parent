package top.zenyoung.retrofit.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 基本类型转换工厂
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BasicTypeConverterFactory extends Converter.Factory {
    public static final BasicTypeConverterFactory INSTANCE = new BasicTypeConverterFactory();

    @Override
    public Converter<?, RequestBody> requestBodyConverter(@Nonnull final Type type, @Nonnull final Annotation[] parameterAnnotations,
                                                          @Nonnull final Annotation[] methodAnnotations, @Nonnull final Retrofit retrofit) {
        return null;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(@Nonnull final Type type, @Nonnull final Annotation[] annotations, @Nonnull final Retrofit retrofit) {
        if (String.class.getTypeName().equals(type.getTypeName())) {
            return new StringResConverter();
        } else if (Integer.class.getTypeName().equals(type.getTypeName())) {
            return new IntegerResConverter();
        } else if (Long.class.getTypeName().equals(type.getTypeName())) {
            return new LongResConverter();
        } else if (Boolean.class.getTypeName().equals(type.getTypeName())) {
            return new BooleanResConverter();
        } else if (Float.class.getTypeName().equals(type.getTypeName())) {
            return new FloatResConverter();
        } else if (Double.class.getTypeName().equals(type.getTypeName())) {
            return new DoubleResConverter();
        } else {
            return null;
        }
    }


    private static final class StringResConverter implements Converter<ResponseBody, String> {

        @Override
        public String convert(final ResponseBody value) throws IOException {
            return value.string();
        }
    }

    private static class IntegerResConverter implements Converter<ResponseBody, Integer> {

        @Override
        public Integer convert(final ResponseBody value) throws IOException {
            return Integer.valueOf(value.string());
        }
    }

    private static class LongResConverter implements Converter<ResponseBody, Long> {
        @Override
        public Long convert(final ResponseBody value) throws IOException {
            return Long.valueOf(value.string());
        }
    }

    private static class BooleanResConverter implements Converter<ResponseBody, Boolean> {
        @Override
        public Boolean convert(final ResponseBody value) throws IOException {
            return Boolean.valueOf(value.string());
        }
    }

    private static class FloatResConverter implements Converter<ResponseBody, Float> {
        @Override
        public Float convert(final ResponseBody value) throws IOException {
            return Float.valueOf(value.string());
        }
    }

    private static class DoubleResConverter implements Converter<ResponseBody, Double> {
        @Override
        public Double convert(final ResponseBody value) throws IOException {
            return Double.valueOf(value.string());
        }
    }
}
