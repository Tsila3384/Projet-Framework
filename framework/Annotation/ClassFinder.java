import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClassFinder {

    public static List<Class<?>> findClassesInCurrentDirectory() throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        File currentDir = new File("."); // Répertoire courant
        
        File[] files = currentDir.listFiles();
        if (files == null) {
            return classes;
        }
        
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".class")) {
                // Pour le répertoire courant, on utilise le nom de classe simple
                String className = file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
        
        return classes;
    }

    public static List<Class<?>> findClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        File directory = new File(path);

        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".class")) {
                String className = packageName + '.' +
                        file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }

        return classes;
    }

    public void printAnnotationDetails() {
        try {
            List<Class<?>> classes = findClassesInCurrentDirectory();
            for (Class<?> clazz : classes) {
                for (var method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(MyAnnotation.class)) {
                        MyAnnotation annotation = method.getAnnotation(MyAnnotation.class);
                        System.out.println("Méthode: " + method.getName() +
                                ", Valeur: " + annotation.valeur());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}