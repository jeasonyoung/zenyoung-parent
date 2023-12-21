package javax.annotation.processing;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * The Generated annotation is used to mark source code that has been generated.
 * It uses <code>CLASS</code> retention to allow byte code analysis tools like
 * Jacoco detect this code as being generated.
 */

@Documented
@Retention(CLASS)
@Target(TYPE)
public @interface Generated {
    /**
     * The value element MUST have the name of the code generator.
     * The recommended convention is to use the fully qualified name of the
     * code generator.
     */
    String[] value();
}