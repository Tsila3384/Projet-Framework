package annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AnnotationMethod {
	/**
	 * Libellé lisible pour la méthode.
	 */
	String label() default "";

	/**
	 * Auteur de la méthode (annotation documentaire).
	 */
	String author() default "";

	/**
	 * Depuis quelle version (annotation documentaire).
	 */
	String since() default "";
}
