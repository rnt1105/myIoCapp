package ru.sber.junior;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.junior.BeanFactory.BeanFactory;
import ru.sber.junior.MyAnnotations.Timed;
import ru.sber.junior.Proxy.TimedProxy;
import ru.sber.junior.ServiceFinder.ServiceFinder;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ApplicationContext {
    private static final Logger logger = LogManager.getLogger(ApplicationContext.class);
    private final BeanFactory beanFactory = new BeanFactory(this);
    private static Map<String, Class<?>> foundServices;
    private static final Map<Class<?>, Object> beans = new HashMap<>();

    /**
     * TODO idea рекомендует поработать над этими полями. Что она рекомендует и зачем?
     * пункт был над BeanFactory и мапой beansFromFactory.
     * ME: BeanFactory теперь будет создавать бины только по запросу,
     * поэтому, считаю, все же необходимо оставить данное поле, что бы не создавать
     * новую фабрику при каждом запросе на создание бина
     * TODO - DONE?
     */

    /**
     * TODO можно сделать типизированным, чтобы он возвращал то, что от него просят
     * TODO - DONE
     */
    public <T> T getBean(String beanName) {
        logger.debug("Старт метода [getBean]. Имя класса: [{}]", beanName);
        Class<?> beanClass = foundServices.get(beanName);
        if(beanClass == null){
            logger.error("Сервис [{}] не найден. Неверное имя, либо не помечен аннотацией @Service", beanName);
            return null;
        }
        return (T)getBean(beanClass);
    }

    /**
     * TODO можно сделать типизированным, чтобы он возвращал то, что от него просят
     * TODO - DONE
     */
    public <T> T getBean(Class<T> beanClass) {
        logger.debug("Старт метода [getBean]. Класс: [{}]", beanClass);
        T bean = (T)beans.get(beanClass.toString());
        if(bean == null){
            if(foundServices.containsValue(beanClass)){
                createBean(beanClass);
            } else {
                logger.error("Сервис [{}] не найден. Неверное имя, либо не помечен аннотацией @Service", beanClass.getSimpleName());
                return null;
            }
        }
        return bean;
    }

    private Object createBean(Class<?> beanClass) {
        logger.debug("Старт метода createBean. Сервис: [{}]", beanClass.getSimpleName());
        Object bean = beanFactory.createBean(beanClass);
        if(bean != null){
            beans.put(beanClass, bean);
            logger.debug("Завершение работы метода [createBean]");
            return bean;
        }
        logger.error("Фабрике не удалось создать бин [{}]", beanClass.getSimpleName());
        return null;
    }

    /**
     * TODO данное приложение загружает все бины при старте. Что будет, когда бинов в
     * приложении будут тысячи и как оптимизировать запуск приложения?
     * ME: создавать бины по запросу из контекста, а не сразу все, помеченные аннотацией
     * TODO- DONE
     */
    public void run(String packageName) {
        logger.debug("Старт метода [run]");
        ServiceFinder serviceFinder = new ServiceFinder();
        foundServices = serviceFinder.findServices(packageName);
        if(foundServices.isEmpty()){
            logger.warn("Не найдено ни одного сервиса. Проверьте наличие аннотации @Service");
        }
        logger.debug("Завершение работы метода [run]");
    }

    private boolean hasTimedAnnotation(Class<?> beanClass) {
        logger.trace("Выполняется метод [hasTimedAnnotation]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredMethods())
                .anyMatch(e -> e.isAnnotationPresent(Timed.class));
    }

    private void replaceBeanWithTimedAnnotationToProxyBean(Map<String, Object> beansFromFactory) {
        logger.debug("Старт метода [replaceBeanWithTimedAnnotationToProxyBean]");
        for (Map.Entry<String, Object> entry : beansFromFactory.entrySet()) {
            Object bean = entry.getValue();
            Class<?> beanClass = bean.getClass();
            if (hasTimedAnnotation(beanClass)) {
                TimedProxy proxy = new TimedProxy(bean);
                Object proxyInstance = Proxy.newProxyInstance(beanClass.getClassLoader(), beanClass.getInterfaces(), proxy);
                logger.debug("Замена бина с методом, аннотированным @Timed на прокси-бин. Сервис [{}], прокси-бин [{}]", beanClass.getSimpleName(),proxyInstance);
                beans.put(beanClass, proxyInstance);
            } else {
                beans.put(beanClass, bean);
            }
        }
        logger.debug("Завершение работы метода [replaceBeanWithTimedAnnotationToProxyBean]");
    }
}
