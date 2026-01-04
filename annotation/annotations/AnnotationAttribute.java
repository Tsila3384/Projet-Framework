package annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AnnotationAttribute {
	/**
	 * Libellé lisible pour l'attribut.
	 */
	String label() default "";

	/**
	 * Indique si la valeur est requise.
	 */
	boolean required() default false;

	/**
	 * Longueur maximale facultative (utile pour des chaînes).
	 */
	int maxLength() default Integer.MAX_VALUE;
}
