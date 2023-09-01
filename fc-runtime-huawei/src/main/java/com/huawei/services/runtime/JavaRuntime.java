package com.huawei.services.runtime;

import javax.annotation.Nonnull;

/**
 * Java运行时
 *
 * @author young
 */
public final class JavaRuntime {

    private static final RuntimeLogger LOGGER = new RuntimeLogger() {
        @Override
        public void log(@Nonnull final String log) {
            System.out.println(log);
        }

        @Override
        public void debug(@Nonnull final String log) {
            System.out.println(log);
        }

        @Override
        public void info(@Nonnull final String log) {
            System.out.println(log);
        }

        @Override
        public void warn(@Nonnull final String log) {
            System.out.println(log);
        }

        @Override
        public void error(@Nonnull final String log) {
            System.out.println(log);
        }

        @Override
        public void setLevel(@Nonnull final String level) {
            
        }
    };

    private JavaRuntime() {
    }

    public static RuntimeLogger getLogger() {
        return LOGGER;
    }
}
