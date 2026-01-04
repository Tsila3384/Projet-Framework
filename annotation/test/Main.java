package test;

import annotations.AnnotationAttribute;
import annotations.AnnotationClass;
import annotations.AnnotationMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Main {

    public static void main(String[] args) {
        Class<Person> clazz = Person.class;
        System.out.println("Classe: " + clazz.getName());

        // Lire l'annotation de classe
        AnnotationClass ann = clazz.getAnnotation(AnnotationClass.class);
        if (ann != null) {
            System.out.println("@AnnotationClass présent:");
            System.out.println("  value        = " + ann.value());
            System.out.println("  description  = " + ann.description());
            System.out.print("  tags         = [");
            String[] tags = ann.tags();

            for (int i = 0; i < tags.length; i++) {
                System.out.print(tags[i]);
                if (i < tags.length - 1) System.out.print(", ");
            }
            System.out.println("]");
        } else {
            System.out.println("@AnnotationClass non présent sur la classe");
        }

        // Lire les annotations de champs
        System.out.println("\nChamps annotés:");
        for (Field f : clazz.getDeclaredFields()) {
            AnnotationAttribute a = f.getAnnotation(AnnotationAttribute.class);
            if (a != null) {
                System.out.println("- " + f.getName() + " : @AnnotationAttribute");
                System.out.println("    label      = " + a.label());
                System.out.println("    required   = " + a.required());
                System.out.println("    maxLength  = " + a.maxLength());
            }
        }

        // Lire les annotations de méthodes
        System.out.println("\nMéthodes annotées:");
        for (Method m : clazz.getDeclaredMethods()) {
            AnnotationMethod am = m.getAnnotation(AnnotationMethod.class);
            if (am != null) {
                System.out.println("- " + m.getName() + "() : @AnnotationMethod");
                System.out.println("    label      = " + am.label());
                System.out.println("    author     = " + am.author());
                System.out.println("    since      = " + am.since());
            }
        }
    }
}
