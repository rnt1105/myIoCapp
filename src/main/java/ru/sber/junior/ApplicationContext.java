package ru.sber.junior;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.junior.BeanFactory.BeanFactory;
import ru.sber.junior.MyAnnotations.Timed;
import ru.sber.junior.Proxy.TimedProxy;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ApplicationContext {
    private static final Logger logger = LogManager.getRootLogger();
    private static BeanFactory beanFactory;
    private static Map<String, Object> beansFromFactory;
    private static Map<String, Object> beans = new HashMap<>();

    public Object getBean(String beanName){
        logger.debug("Старт метода [ApplicationContext.getBean]. Класс: [{}]", beanName);
        return beans.get(beanName);
    }

    public void run(String packageName) {
        logger.debug("Старт метода [ApplicationContext.run]");
        beanFactory = new BeanFactory();
        logger.info("Создана BeanFactory: [{}]", beanFactory);
        beansFromFactory = beanFactory.run(packageName);
        if(!beansFromFactory.isEmpty()){
            replaceBeanWithTimedAnnotationToProxyBean(beansFromFactory);
        }
        logger.debug("Завершение работы метода [ApplicationContext.run]");
    }

    private boolean hasTimedAnnotation(Class<?> beanClass) {
        logger.trace("Выполняется метод [BeanFactory.hasTimedAnnotation]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredMethods())
                .anyMatch(e -> e.isAnnotationPresent(Timed.class));
    }

    private void replaceBeanWithTimedAnnotationToProxyBean(Map<String, Object> beansFromFactory) {
        logger.debug("Старт метода [ApplicationContext.replaceBeanWithTimedAnnotationToProxyBean]");
        for (Map.Entry<String, Object> entry : beansFromFactory.entrySet()) {
            Object bean = entry.getValue();
            Class<?> beanClass = bean.getClass();
            if (hasTimedAnnotation(beanClass)) {
                TimedProxy proxy = new TimedProxy(bean);
                Object proxyInstance = Proxy.newProxyInstance(beanClass.getClassLoader(), beanClass.getInterfaces(), proxy);
                logger.debug("Замена бина с методом, аннотированным @Timed на прокси-бин. Сервис [{}], прокси-бин [{}]", beanClass.getSimpleName(),proxyInstance);
                beans.put(beanClass.getSimpleName(), proxyInstance);
            } else {
                beans.put(beanClass.getSimpleName(), bean);
            }
        }
    }
}
