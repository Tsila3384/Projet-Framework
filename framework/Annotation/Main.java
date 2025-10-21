import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ClassFinder classFinder = new ClassFinder();
        classFinder.printAnnotationDetails();
    }
}