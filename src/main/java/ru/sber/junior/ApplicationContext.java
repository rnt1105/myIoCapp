package ru.sber.junior;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.junior.BeanFactory.BeanFactory;
import ru.sber.junior.MyAnnotations.Timed;
import ru.sber.junior.Proxy.TimedProxy;
import ru.sber.junior.ServiceFinder.ServiceFinder;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationContext {
    private static final Logger logger = LogManager.getLogger(ApplicationContext.class);
    private final BeanFactory beanFactory = new BeanFactory(this);
    private static Map<String, Class<?>> foundServices;
    private static final Map<Class<?>, Object> beans = new HashMap<>();
    private static final Map<String, List<Class<?>>> interfacesMap = new HashMap<>();

    /**
     * TODO idea рекомендует поработать над этими полями. Что она рекомендует и зачем?
     * пункт был над BeanFactory и мапой beansFromFactory.
     * ME: BeanFactory теперь будет создавать бины только по запросу,
     * поэтому, считаю, все же необходимо оставить данное поле, что бы не создавать
     * новую фабрику при каждом запросе на создание бина
     * TODO - ???
     */

    /**
     * TODO можно сделать типизированным, чтобы он возвращал то, что от него просят
     * TODO - DONE
     */
    public <T> T getBean(String beanName) {
        logger.debug("Старт метода [getBean]. Имя класса: [{}]", beanName);
        Class<?> beanClass = foundServices.get(beanName);
        if (beanClass == null) {
            List<Class<?>> interfaceClass = interfacesMap.get(beanName);
            if (interfaceClass == null) {
                logger.error("Сервис [{}] не найден. Неверное имя, либо не помечен аннотацией @Service", beanName);
                return null;
            } else {
                if (interfaceClass.size() > 1) {
                    logger.warn("Для запрашиваемого интерфейса [{}] имеется более одной реализации. Сделайте запрос по классу сервиса", beanName);
                    logger.debug("Завершение работы метода [getBean]");
                    return null;
                } else {
                    logger.debug("Завершение работы метода [getBean]");
                    return (T) getBean(interfaceClass.get(0));
                }
            }
        }
        logger.debug("Завершение работы метода [getBean]");
        return (T) getBean(beanClass);
    }

    /**
     * TODO можно сделать типизированным, чтобы он возвращал то, что от него просят
     * TODO - DONE
     */
    public <T> T getBean(Class<T> beanClass) {
        logger.debug("Старт метода [getBean]. Класс: [{}]", beanClass.getSimpleName());
        T bean = (T) beans.get(beanClass);
        if (bean == null) {
            if (foundServices.containsValue(beanClass)) {
                bean = (T) createBean(beanClass);
                if(hasTimedAnnotation(beanClass)){
                    replaceBeanWithTimedAnnotationToProxyBean(bean, beanClass);
                }
                logger.debug("Завершение работы метода [getBean]");
                return bean;
            } else {
                List<Class<?>> interfaceClass = interfacesMap.get(beanClass.getSimpleName());
                if (interfaceClass == null) {
                    logger.error("Сервис [{}] не найден. Неверное имя, либо не помечен аннотацией @Service", beanClass.getSimpleName());
                    logger.debug("Завершение работы метода [getBean]");
                    return null;
                } else {
                    if (interfaceClass.size() > 1) {
                        logger.warn("Для запрашиваемого интерфейса [{}] имеется более одной реализации. Сделайте запрос по классу сервиса", beanClass.getSimpleName());
                        logger.debug("Завершение работы метода [getBean]");
                        return null;
                    } else {
                        logger.debug("Завершение работы метода [getBean]");
                        return (T) getBean(interfaceClass.get(0));
                    }
                }
            }
        }
        logger.debug("Завершение работы метода [getBean]");
        return bean;
    }

    private Object createBean(Class<?> beanClass) {
        logger.debug("Старт метода [createBean]. Сервис: [{}]", beanClass.getSimpleName());
        Object bean = beanFactory.createBean(beanClass);
        if (bean != null) {
            beans.put(beanClass, bean);
            if(hasTimedAnnotation(beanClass)) {
                replaceBeanWithTimedAnnotationToProxyBean(bean, beanClass);
            }
            logger.info("Сервис [{}] успешно создан", beanClass.getSimpleName());
            logger.debug("Завершение работы метода [createBean]");
            return getBean(beanClass);
        }
        logger.error("Фабрике не удалось создать бин [{}]", beanClass.getSimpleName());
        logger.debug("Завершение работы метода [createBean]");
        return null;
    }

    /**
     * TODO данное приложение загружает все бины при старте. Что будет, когда бинов в
     * приложении будут тысячи и как оптимизировать запуск приложения?
     * ME: создавать бины по запросу из контекста, а не сразу все, помеченные аннотацией @Service
     * TODO- DONE
     */
    public void run(String packageName) {
        logger.debug("Старт метода [run]");
        ServiceFinder serviceFinder = new ServiceFinder();
        foundServices = serviceFinder.findServices(packageName);
        if (foundServices.isEmpty()) {
            logger.warn("Не найдено ни одного сервиса. Проверьте наличие аннотации @Service");
        } else {
            fillInterfacesMap(foundServices);
        }
        logger.debug("Завершение работы метода [run]");
    }

    private boolean hasTimedAnnotation(Class<?> beanClass) {
        logger.trace("Выполняется метод [hasTimedAnnotation]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredMethods())
                .anyMatch(e -> e.isAnnotationPresent(Timed.class));
    }

    private void replaceBeanWithTimedAnnotationToProxyBean(Object bean, Class<?> beanClass) {
        logger.debug("Старт метода [replaceBeanWithTimedAnnotationToProxyBean]");
        if(bean.getClass().getSuperclass().equals(Proxy.class)){
            return;
        }
        if (hasTimedAnnotation(beanClass)) {
            List<String> methodList = Arrays.stream(beanClass.getDeclaredMethods())
                    .filter(e -> e.isAnnotationPresent(Timed.class))
                    .map(Method::getName)
                    .collect(Collectors.toList());
            TimedProxy proxy = new TimedProxy(bean, methodList);
            Object proxyInstance = Proxy.newProxyInstance(beanClass.getClassLoader(), beanClass.getInterfaces(), proxy);
            logger.debug("Замена бина с методом, аннотированным @Timed на прокси-бин. Сервис [{}], прокси-бин [{}]", beanClass.getSimpleName(), proxyInstance);
            beans.put(beanClass, proxyInstance);
        } else {
            beans.put(beanClass, bean);
        }
        logger.debug("Завершение работы метода [replaceBeanWithTimedAnnotationToProxyBean]");
    }

    private void fillInterfacesMap(Map<String, Class<?>> foundServices) {
        logger.debug("Старт метода [fillInterfacesMap]");
        for (Map.Entry<String, Class<?>> entry : foundServices.entrySet()) {
            Class<?>[] interfaces = entry.getValue().getInterfaces();
            if (interfaces.length > 0) {
                for (Class<?> interfaceClass : interfaces) {
                    List<Class<?>> classList = interfacesMap.get(interfaceClass.getSimpleName());
                    if (classList == null) {
                        classList = new ArrayList<>();
                    }
                    classList.add(entry.getValue());
                    interfacesMap.put(interfaceClass.getSimpleName(), classList);
                }
            }
        }
        logger.debug("Завершение работы метода [fillInterfacesMap]");
    }
}
