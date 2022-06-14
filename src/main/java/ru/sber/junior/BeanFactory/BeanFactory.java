package ru.sber.junior.BeanFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.junior.MyAnnotations.Autowired;
import ru.sber.junior.MyAnnotations.Timed;
import ru.sber.junior.Proxy.TimedProxy;
import ru.sber.junior.ServiceFinder.ServiceFinder;
import ru.sber.junior.Services.RadioInterface;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeanFactory {
    private static final Logger logger = LogManager.getRootLogger();
    private static ServiceFinder serviceFinder;

    private static Map<String, Class<?>> foundServices = new HashMap<>();
    private static final Map<String, Object> preparedBeans = new HashMap<>();
    private static final Map<String, Object> beans = new HashMap<>();

    public Object getBean(String beanName) {
        logger.trace("Старт метода [BeanFactory.getBean], бин: [{}]", beanName);
        return beans.get(beanName);
    }

    public Map<String, Object> run(String packageName) {
        logger.debug("Старт метода [BeanFactory.run]");
        servicesSearch(packageName);
        while (!foundServices.isEmpty() || !preparedBeans.isEmpty()){
            createBeans(foundServices);
            createBeansFromPreparedBeans(preparedBeans);
            cleanMaps();
        }
        logger.debug("Завершение работы метода [BeanFactory.run]");
        return beans;
    }

    private void servicesSearch(String packageName) {
        logger.debug("Старт метода [BeanFactory.servicesSearch]");
        serviceFinder = new ServiceFinder();
        logger.debug("Создан ServiceFinder: [{}]", serviceFinder);
        foundServices = serviceFinder.findServices(packageName);
        logger.debug("Завершение работы метода [BeanFactory.servicesSearch]");
    }

    private void cleanMaps(){
        for(Map.Entry<String, Object> entry: beans.entrySet()){
            preparedBeans.remove(entry.getKey());
            foundServices.remove(entry.getKey());
        }
        for(Map.Entry<String, Object> entry: preparedBeans.entrySet()){
            foundServices.remove(entry.getKey());
        }
    }

    private void createBeans(Map<String, Class<?>> foundServices) {
        logger.debug("Старт метода [BeanFactory.createBeans]. Сервисы [{}]", foundServices);
        for (Map.Entry<String, Class<?>> entry : foundServices.entrySet()) {
            Class<?> beanClass = entry.getValue();
            createBean(beanClass);
        }
        logger.debug("Завершена работа метода [BeanFactory.createBeans]");
    }

    private void createBean(Class<?> beanClass) {
        logger.debug("Старт метода [BeanFactory.createBean]. Класс: [{}]", beanClass);
        Object bean = getBean(beanClass.getSimpleName());
        if (bean != null) {
            logger.debug("Сервис [{}] был создан ранее", beanClass.getSimpleName());
            return;
        }
        try {
            if (!hasAutowiredAnnotationInConstructor(beanClass)) {
                bean = beanClass.getDeclaredConstructor().newInstance();
                boolean successInjectDepends = true;
                if (hasAutowiredAnnotationInFields(beanClass)) {
                    successInjectDepends = injectDependsInField(bean, beanClass);
                }
                if(hasAutowiredAnnotationInMethods(beanClass)){
                    successInjectDepends = injectDependsInMethod(bean, beanClass);
                }
                if(successInjectDepends){
                    logger.info("Сервис [{}] успешно создан", beanClass.getSimpleName());
                    beans.put(beanClass.getSimpleName(), bean);
                } else {
                    logger.debug("Отсутствуют необходимые бины для внедрения зависимостей и создания сервиса [{}]", beanClass.getSimpleName());
                    preparedBeans.put(beanClass.getSimpleName(), bean);
                }
            } else {
                Constructor constructor = getConstructorWithAutowiredAnnotation(beanClass);
                constructor.setAccessible(true);
                Class[] parameterTypes = constructor.getParameterTypes();
                Object[] injectParameters = new Object[parameterTypes.length];
                int i = 0;
                for (Class parameter : parameterTypes) {
                    Object beanForInject = getBean(parameter.getSimpleName());
                    if (beanForInject == null) {
                        logger.debug("Невозможно создать сервис [{}], отсутствует бин для внедрения",parameter.getSimpleName());
                        return;
                    }
                    injectParameters[i] = parameter.cast(beanForInject);
                    i++;
                }
                try {
                    Object instance = beanClass.getDeclaredConstructor(parameterTypes).newInstance(injectParameters);
                    boolean successInjectDepends = true;
                    if(hasAutowiredAnnotationInFields(beanClass)){
                        successInjectDepends = injectDependsInField(instance, beanClass);
                    }
                    if(hasAutowiredAnnotationInMethods(beanClass)){
                        successInjectDepends = injectDependsInMethod(instance, beanClass);
                    }
                    if(successInjectDepends){
                        logger.info("Сервис [{}] успешно создан", beanClass.getSimpleName());
                        beans.put(beanClass.getSimpleName(), instance);
                    }
                    logger.debug("Не удалось создать сервис. Нет бина для внедрения");
                    preparedBeans.put(beanClass.getSimpleName(), instance);
                } catch (Exception e) {
                    logger.error("Ошибка при создании сервиса [{}]", beanClass.getSimpleName());
                    e.printStackTrace();
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Ошибка при создании сервиса. Класс: [{}]", beanClass);
            e.printStackTrace();
        }
        logger.debug("Завершение работы метода [BeanFactory.createBean]");
    }

    private void createBeansFromPreparedBeans(Map<String, Object> preparedBeans){
        logger.debug("Старт метода [BeanFactory.createBeansFromPreparedBeans]");
        for(Map.Entry<String, Object> entry: preparedBeans.entrySet()){
            Object bean = entry.getValue();
            Class beanClass = bean.getClass();
            boolean successInjectDepends = true;
            if(hasAutowiredAnnotationInFields(beanClass)){
                successInjectDepends = injectDependsInField(bean, beanClass);
            }
            if(hasAutowiredAnnotationInMethods(beanClass)){
                successInjectDepends = injectDependsInMethod(bean, beanClass);
            }
            if(successInjectDepends){
                logger.info("Сервис [{}] успешно создан", beanClass.getSimpleName());
                beans.put(beanClass.getSimpleName(), bean);
            }
            logger.debug("Завершение работы метода [BeanFactory.createBeansFromPreparedBeans]");
        }
    }

    private boolean hasAutowiredAnnotationInConstructor(Class<?> beanClass) {
        logger.trace("Выполняется метод [BeanFactory.hasAutowiredAnnotationInConstructor]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredConstructors())
                .anyMatch(e -> e.isAnnotationPresent(Autowired.class));
    }

    private Constructor getConstructorWithAutowiredAnnotation(Class<?> beanClass) {
        logger.trace("Выполняется метод [BeanFactory.getConstructorWithAutowiredAnnotation]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredConstructors())
                .filter(e -> e.isAnnotationPresent(Autowired.class))
                .findAny().get();
    }

    private boolean hasAutowiredAnnotationInFields(Class<?> beanClass) {
        logger.trace("Выполняется метод [BeanFactory.hasAutowiredAnnotationInFields]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredFields())
                .anyMatch(e -> e.isAnnotationPresent(Autowired.class));
    }

    private List<Field> getFieldsWithAutowiredAnnotation(Class<?> beanClass) {
        logger.trace("Выполняется метод [BeanFactory.getFieldsWithAutowiredAnnotation]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(Autowired.class))
                .collect(Collectors.toList());
    }

    private boolean injectDependsInField(Object bean, Class<?> beanClass) {
        List<Field> fields = getFieldsWithAutowiredAnnotation(beanClass);
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> beanForInjectClass = field.getType();
            Object beanForInject = getBean(beanForInjectClass.getSimpleName());
            while (beanForInject == null) {
                createBean(beanForInjectClass);
                beanForInject = getBean(beanForInjectClass.getSimpleName());
            }
            try {
                field.set(bean, beanForInject);
            } catch (IllegalAccessException e) {
                logger.error("Ошибка при внедрении зависимости. Сервис: [{}], поле: [{}]", beanClass.getSimpleName(), field);
                e.printStackTrace();
            }
            logger.debug("Внедрены зависимости в полях. Сервис: [{}], поля: [{}]", beanClass.getSimpleName(), fields);
            return true;
        }
        return false;
    }

    private boolean hasAutowiredAnnotationInMethods(Class<?> beanClass) {
        logger.trace("Выполняется метод [BeanFactory.hasAutowiredAnnotationInMethods]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredMethods())
                .anyMatch(e -> e.isAnnotationPresent(Autowired.class));
    }

    private List<Method> getMethodsWithAutowiredAnnotation(Class<?> beanClass) {
        logger.trace("Выполняется метод [BeanFactory.getMethodsWithAutowiredAnnotation]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredMethods())
                .filter(e -> e.isAnnotationPresent(Autowired.class))
                .collect(Collectors.toList());
    }

    private boolean injectDependsInMethod(Object bean, Class<?> beanClass) {
        List<Method> methods = getMethodsWithAutowiredAnnotation(beanClass);
        for (Method method : methods) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] beansForInject = new Object[parameterTypes.length];
            int i = 0;
            for (Class parameterClass : parameterTypes) {
                Object beanForInject = getBean(parameterClass.getSimpleName());
                if (beanForInject != null) {
                    beansForInject[i] = beanForInject;
                    i++;
                } else {
                    return false;
                }
            }
            if (i == parameterTypes.length) {
                try {
                    method.invoke(bean, beansForInject);
                    logger.debug("Успешное внедрение зависимости в сервис: [{}], внедрены объекты: [{}]", bean, beansForInject);
                    return true;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.debug("Ошибка при внедрении зависимости. Сервис [ {} ], метод [ {} ], внедряемые объекты [ {} ]", bean, method, beansForInject);
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
