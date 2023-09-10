package cloud.codestore.client.ui;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies a class as JavaFX controller.
 * Every JavaFX controller class MUST be annotated with this annotation.
 * It is used by the dependency injection framework to resolve the components for the DI context.
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface FxController {}
