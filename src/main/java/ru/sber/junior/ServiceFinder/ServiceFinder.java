package ru.sber.junior.ServiceFinder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.junior.MyAnnotations.Service;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ServiceFinder {
    private static final Logger logger = LogManager.getRootLogger();
    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';
    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final Map<String, Class<?>> foundServices = new HashMap<>();

    public Map<String, Class<?>> findServices(String packageName) {
        logger.debug("Старт метода [ServiceFinder.findServices]. Сканируется пакет: [{}]", packageName);
        String scannedPath = packageName.replace(PKG_SEPARATOR, DIR_SEPARATOR);
        URL scannedUrl = null;
        try {
            Enumeration<URL> resources = ClassLoader.getSystemResources(scannedPath);
            while (resources.hasMoreElements()){
                URL url = resources.nextElement();
                String urlString = url.toString();
                String[] split = urlString.split("/");
                for(String s: split){
                    if(s.equalsIgnoreCase("test-classes")){
                        break;
                    }
                    scannedUrl = url;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(scannedUrl != null){
            File scannedDir = new File(scannedUrl.getFile());
            for (File file : Objects.requireNonNull(scannedDir.listFiles())) {
                findServices(file, packageName);
            }
        }
        logger.debug("Завершение работы метода [ServiceFinder.findServices]");
        return foundServices;
    }

    private void findServices(File file, String scannedPackage) {
        logger.trace("Старт метода [ServiceFinder.findServices] - Файл: [{}]. Пакет: [{}]", file, scannedPackage);
        String resource = scannedPackage + PKG_SEPARATOR + file.getName();
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                findServices(child, resource);
            }
        } else if (resource.endsWith(CLASS_FILE_SUFFIX)) {
            int endIndex = resource.length() - CLASS_FILE_SUFFIX.length();
            String className = resource.substring(0, endIndex);
            try {
                Class<?> myClass = Class.forName(className);
                if (myClass.isAnnotationPresent(Service.class)) {
                    logger.info("Найден сервис: [{}] в пакете: [{}]", myClass.getSimpleName(), scannedPackage);
                    foundServices.put(myClass.getSimpleName(), myClass);
                }
            } catch (ClassNotFoundException e) {
                logger.error("Ошибка в методе [ApplicationContext.findService]. Класс [{}] не найден.", className);
                e.printStackTrace();
            }
        }
        logger.trace("Завершение работы метода [ApplicationContext.findServices]");
    }
}
