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
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FrontServlet extends HttpServlet {
    private Map<String, Method> urlMappings = new HashMap<>();
    private Map<String, Class<?>> controllerMappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
        System.out.println("Initialisation du framework...");

        try {
            String basePackage = getServletContext().getInitParameter("basePackage");
            if (basePackage == null) {
                basePackage = "com.tsila.controllers";
            }

            scanControllers(basePackage);
            exposeUrlMappings();

            System.out.println("Scan terminé !");
        } catch (Exception e) {
            throw new ServletException("Erreur d'initialisation du FrontServlet", e);
        }
    }

    private void scanControllers(String packageName) throws Exception {
        Set<String> classNames = findClassNames(packageName);
        registerControllers(classNames);
    }

    private Set<String> findClassNames(String packageName) throws Exception {
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);
        Set<String> classNames = new HashSet<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            classNames.addAll(scanResource(resource, path, packageName));
        }

        return classNames;
    }

    private Set<String> scanResource(URL resource, String path, String packageName) {
        try {
            if ("jar".equals(resource.getProtocol())) {
                return scanJar(resource, path);
            } else {
                return scanDirectory(resource, packageName);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du scan de: " + resource + " -> " + e.getMessage());
            return new HashSet<>();
        }
    }

    private Set<String> scanJar(URL resource, String path) throws Exception {
        Set<String> classNames = new HashSet<>();
        String jarPath = extractJarPath(resource);

        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory()) {
                    classNames.add(convertPathToClassName(name));
                }
            }
        }

        return classNames;
    }

    private String extractJarPath(URL resource) throws Exception {
        String fullPath = resource.getPath();
        String jarPath = fullPath.substring(0, fullPath.indexOf('!'));

        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring(5);
        }

        return URLDecoder.decode(jarPath, "UTF-8");
    }

    private Set<String> scanDirectory(URL resource, String packageName) throws Exception {
        Set<String> classNames = new HashSet<>();
        File directory = new File(resource.toURI());

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".class"));

            if (files != null) {
                for (File file : files) {
                    String className = packageName + "." + file.getName().replace(".class", "");
                    classNames.add(className);
                }
            }
        }

        return classNames;
    }

    private String convertPathToClassName(String path) {
        return path.replace('/', '.').replace(".class", "");
    }

    private void registerControllers(Set<String> classNames) throws Exception {
        for (String className : classNames) {
            Class<?> clazz = Class.forName(className);

            if (clazz.isAnnotationPresent(Controller.class)) {
                registerControllerMethods(clazz);
            }
        }
    }

    private void registerControllerMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Url.class)) {
                String url = method.getAnnotation(Url.class).Value();
                urlMappings.put(url, method);
                controllerMappings.put(url, clazz);
            }
        }
    }

    private void exposeUrlMappings() {
        Map<String, String> urlInfo = new HashMap<>();

        urlMappings.forEach((url, method) -> {
            Class<?> controllerClass = controllerMappings.get(url);
            String handler = controllerClass.getName() + "#" + method.getName();
            urlInfo.put(url, handler);
            System.out.println(url + " => " + handler);
        });

        getServletContext().setAttribute("urlMappings", urlInfo);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();

        if (path == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Aucune URL fourniesssssssssssssssss");
            return;
        }

        Method method = urlMappings.get(path);
        if (method == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Aucuneeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee méthode correspondante pour " + path);
            return;
        }

        try {
            // NOUVEAU : Instanciation du contrôleur
            Class<?> controllerClass = controllerMappings.get(path);
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

            // NOUVEAU : Exécution de la méthode
            Object result = method.invoke(controllerInstance);

            // NOUVEAU : Gestion du résultat
            handleMethodResult(result, req, resp);

        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'exécution de la méthode pour " + path, e);
        }
    }

    // CETTE MÉTHODE DOIT ÊTRE EN DEHORS DE doGet, à la racine de la classe
    private void handleMethodResult(Object result, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        if (result == null) {
            out.println("Méthode exécutée avec succès (retour null)");
        } else if (result instanceof String) {
            out.print(result);
        } else {
            out.print(result.toString());
        }
    }
}