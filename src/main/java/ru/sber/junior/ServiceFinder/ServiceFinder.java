package ru.sber.junior.ServiceFinder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.junior.MyAnnotations.Service;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ServiceFinder {
    private static final Logger logger = LogManager.getLogger(ServiceFinder.class);
    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';
    private static final String CLASS_FILE_SUFFIX = ".class";

    /**
     * TODO нужно ли это выносить в отдельное поле?
     * вопрос над мапой foundServices
     * Me: не храним найденные сервисы, передаем в BeanFactory
     * СДЕЛАТЬ
     */

    public Map<String, Class<?>> findServices(String packageName) {
        logger.debug("Старт метода [findServices]. Сканируется пакет: [{}]", packageName);
        Map<String, Class<?>> foundServices = new HashMap<>();
        String scannedPath = packageName.replace(PKG_SEPARATOR, DIR_SEPARATOR);
        URL scannedUrl = null;
        try {
            Enumeration<URL> resources = ClassLoader.getSystemResources(scannedPath);
            while (resources.hasMoreElements()){
                URL url = resources.nextElement();
                String urlString = url.toString();
                String[] split = urlString.split("/");
                boolean isTestPackage = false;
                for(String s: split){
                    if(s.equalsIgnoreCase("test-classes")){
                        isTestPackage = true;
                        break;
                    }
                    // TODO не понял, почему тут 2 цикла без прерываний (внешний и внутренний) на присваивание одной переменной
                    // TODO - DONE. проверка, что URL не тестового пакета. исправил
                }
                if(!isTestPackage){
                    scannedUrl = url;
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка в методе [findServices]. Ошибка при сканировании пакет: [{}]. Ошибка: [{}]",packageName,e.toString());
        }
        if(scannedUrl != null){
            File scannedDir = new File(scannedUrl.getFile());
            for (File file : Objects.requireNonNull(scannedDir.listFiles())) {
                findServices(file, packageName, foundServices);
            }
        }
        logger.debug("Завершение работы метода [findServices]");
        return foundServices;
    }

    private void findServices(File file, String scannedPackage, Map<String, Class<?>> foundServices) {
        logger.trace("Старт метода [findServices] - Файл: [{}]. Пакет: [{}]", file, scannedPackage);
        String resource = scannedPackage + PKG_SEPARATOR + file.getName();
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                findServices(child, resource, foundServices);
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
                logger.error("Ошибка в методе [findService]. Класс [{}] не найден. Ошибка: [{}]", className, e.toString());
            }
        }
        logger.trace("Завершение работы метода [findServices]");
    }
}
