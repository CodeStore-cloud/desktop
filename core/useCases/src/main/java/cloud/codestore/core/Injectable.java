package cloud.codestore.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a class to be used for dependency injection.
 * This custom annotation avoids adding third party DI libraries to the core functionality.
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface Injectable {}
