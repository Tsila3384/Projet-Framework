package com.tsila.servlet;

import com.tsila.annotations.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FrontServlet extends HttpServlet {
    // Map qui lie les URL aux méthodes correspondantes
    private Map<String, Method> urlMappings = new HashMap<>();
    private Map<String, Class<?>> controllerMappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
        System.out.println(" Initialisation du framework...");

        try {
            // Le package à scanner peut être lu depuis web.xml
            String basePackage = getServletContext().getInitParameter("basePackage");
            if (basePackage == null)
                basePackage = "com.tsila.controllers";

            scanControllers(basePackage);

            System.out.println("Scan terminé !");

            // Construire une map simple URL -> "ClassName#methodName" et la stocker dans le
            // ServletContext
            HashMap<String, String> urlInfo = new HashMap<>();
            urlMappings.forEach((url, method) -> {
                Class<?> controllerClass = controllerMappings.get(url);
                String handler = controllerClass.getName() + "#" + method.getName();
                urlInfo.put(url, handler);
                System.out.println(url + " => " + handler);
            });

            // Exposer la map au contexte pour que d'autres composants puissent y accéder
            getServletContext().setAttribute("urlMappings", urlInfo);

        } catch (Exception e) {
            throw new ServletException("Erreur d'initialisation du FrontServlet", e);
        }
    }

    private void scanControllers(String packageName) throws Exception {
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Enumeration<URL> resources = classLoader.getResources(path);
        Set<String> classNames = new HashSet<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();

            if ("file".equals(protocol)) {
                // exploded classes in filesystem
                File directory = new File(resource.toURI());
                if (directory.exists()) {
                    File[] files = directory.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().endsWith(".class")) {
                                classNames.add(packageName + "." + file.getName().replace(".class", ""));
                            }
                        }
                    }
                }

            } else if ("jar".equals(protocol)) {
                // resource like jar:file:/path/to/jar.jar!/com/tsila/controllers
                String full = resource.getPath();
                int excl = full.indexOf('!');
                String jarPath = full.substring(0, excl);
                if (jarPath.startsWith("file:"))
                    jarPath = jarPath.substring(5);
                jarPath = URLDecoder.decode(jarPath, "UTF-8");

                try (JarFile jar = new JarFile(jarPath)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory()) {
                            String className = name.replace('/', '.').replace(".class", "");
                            classNames.add(className);
                        }
                    }
                }

            } else {
                // fallback: try to treat as file
                try {
                    File directory = new File(resource.toURI());
                    if (directory.exists()) {
                        File[] files = directory.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.getName().endsWith(".class")) {
                                    classNames.add(packageName + "." + file.getName().replace(".class", ""));
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Impossible de scanner la ressource: " + resource + " -> " + ex.getMessage());
                }
            }
        }

        for (String className : classNames) {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Controller.class)) {
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(Url.class)) {
                        String url = m.getAnnotation(Url.class).Value();
                        urlMappings.put(url, m);
                        controllerMappings.put(url, clazz);
                    }
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/plain");

        if (path == null) {
            out.println(" Aucune URL fournie");
            return;
        }

        Method method = urlMappings.get(path);
        if (method == null) {
            out.println("Aucune méthode correspondante pour " + path);
        } else {
            Class<?> controller = controllerMappings.get(path);
            out.println(" Classe: " + controller.getName());
            out.println(" Méthode: " + method.getName());
        }
    }
}
