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
            System.out.print(log);
        }

        @Override
        public void debug(@Nonnull String log) {

        }

        @Override
        public void info(@Nonnull String log) {

        }

        @Override
        public void warn(@Nonnull String log) {

        }

        @Override
        public void error(@Nonnull String log) {

        }

        @Override
        public void setLevel(@Nonnull String level) {

        }
    };

    private JavaRuntime() {
    }

    public static RuntimeLogger getLogger() {
        return LOGGER;
    }
}
