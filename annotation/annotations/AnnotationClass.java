package annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnotationClass {
	/**
	 * Nom court ou identifiant de la classe annotée.
	 */
	String value() default "";

	/**
	 * Description libre de la classe.
	 */
	String description() default "";

	/**
	 * Étiquettes optionnelles.
	 */
	String[] tags() default {};
}
