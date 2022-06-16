package ru.sber.junior.BeanFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.junior.ApplicationContext;
import ru.sber.junior.MyAnnotations.Autowired;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeanFactory {
    private static final Logger logger = LogManager.getLogger(BeanFactory.class);
    private final ApplicationContext context;

    public BeanFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * TODO нужно ли это выносить в отдельное поле?
     * вопрос над полем private static ServiceFinder serviceFinder;
     * ME: Убрал, сканер вызываю из контекста
     * TODO - DONE
     */

    /**
     * TODO нужно ли это выносить в отдельное поле? Чем это плохо?
     * Вопрос над мапами foundServices, preparedBeans, beans.
     * ME: не храним созданные бины в фабрике, сразу после создания передаём бин в контекст, храним там.
     * TODO - DONE
     */

    /**
     * TODO непонятен проход в 2 этапа, почему сразу не вызывать createBean в месте обращения к бину, см. строки 101/189
     *  ME: Вопрос был над методом run. Метод убрал, создаю бины только по запросу
     *  TODO - DONE
     */

    public Object createBean(Class<?> beanClass) {
        logger.debug("Старт метода [createBean]. Класс: [{}]", beanClass);
        try {
            boolean successInjectDependsInFields = true;
            boolean successInjectDependsInMethods = true;
            if (!hasAutowiredAnnotationInConstructor(beanClass)) {
                Object bean = beanClass.getDeclaredConstructor().newInstance();
                if (hasAutowiredAnnotationInFields(beanClass)) {
                    successInjectDependsInFields = injectDependsInField(bean, beanClass);
                }
                if (hasAutowiredAnnotationInMethods(beanClass)) {
                    successInjectDependsInMethods = injectDependsInMethod(bean, beanClass);
                }
                // TODO может быть неуспешное внедрение через поля и успешное через сеттер, тогда всё считается успешным
                // TODO - DONE. Согласен. Писать всю ночь перед сдачей было плохой идеей. исправил
                if (successInjectDependsInFields && successInjectDependsInMethods) {
                    logger.info("Сервис [{}] успешно создан", beanClass.getSimpleName());
                    logger.debug("Завершение работы метода [createBean]");
                    return bean;
                } else {
                    logger.debug("Отсутствуют необходимые бины для внедрения зависимостей и создания сервиса [{}]", beanClass.getSimpleName());
                    logger.debug("Завершение работы метода [createBean]");
                }
            } else {
                Constructor constructor = getConstructorWithAutowiredAnnotation(beanClass);
                if(constructor == null){
                    logger.error("Ошибка при получении конструктора, помеченного аннотацией @Autowired. Сервис [{}]",beanClass.getSimpleName());
                    logger.debug("Завершение работы метода [createBean]");
                    return null;
                }
                constructor.setAccessible(true);
                Class<?>[] parameterTypes = constructor.getParameterTypes();

                Object[] injectParameters = new Object[parameterTypes.length];
                int i = 0;
                for (Class<?> parameter : parameterTypes) {
                    // TODO почему только из кэша?
                    //TODO - DONE. беру бин из контекста
                    Object beanForInject = context.getBean(parameter);
                    if (beanForInject == null) {
                        logger.debug("Невозможно создать сервис [{}], отсутствует бин для внедрения",parameter.getSimpleName());
                        logger.debug("Завершение работы метода [createBean]");
                        return null;
                    }
                    injectParameters[i] = parameter.cast(beanForInject);
                    i++;
                }
                try {
                    Object bean = beanClass.getDeclaredConstructor(parameterTypes).newInstance(injectParameters);
                    if (hasAutowiredAnnotationInFields(beanClass)) {
                        successInjectDependsInFields = injectDependsInField(bean, beanClass);
                    }
                    if (hasAutowiredAnnotationInMethods(beanClass)) {
                        successInjectDependsInMethods = injectDependsInMethod(bean, beanClass);
                    }
                    if (successInjectDependsInFields && successInjectDependsInMethods) {
                        logger.info("Сервис [{}] успешно создан", beanClass.getSimpleName());
                        logger.debug("Завершение работы метода [createBean]");
                        return bean;
                    } else {
                        // TODO что-то не то, сюда всегда доходит исполнение
                        //TODO - DONE. как и выше, успешное внедрение в метод перезаписывало результат внедрения в поле
                        logger.debug("Не удалось создать сервис [{}]. Нет бина для внедрения", beanClass.getSimpleName());
                        logger.debug("Завершение работы метода [createBean]");
                        return null;
                    }
                } catch (Exception e) {
                    logger.error("Ошибка при создании сервиса [{}]. Ошибка: [{}]", beanClass.getSimpleName(), e.toString());
                    logger.debug("Завершение работы метода [createBean]");
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Ошибка при создании сервиса [{}]. Ошибка: [{}]", beanClass.getSimpleName(), e.toString());
            logger.debug("Завершение работы метода [createBean]");
        }
        logger.debug("Завершение работы метода [createBean]");
        return null;
    }

    private boolean hasAutowiredAnnotationInConstructor(Class<?> beanClass) {
        logger.trace("Выполняется метод [hasAutowiredAnnotationInConstructor]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredConstructors())
                .anyMatch(e -> e.isAnnotationPresent(Autowired.class));
    }

    private Constructor getConstructorWithAutowiredAnnotation(Class<?> beanClass) {
        logger.trace("Выполняется метод [getConstructorWithAutowiredAnnotation]. Класс: [{}]", beanClass);

        /**
         * TODO Здесь возможен NPE. Необходимо исправить
         * TODO-DONE. Обрабатываю возможный null в месте вызова метода
         */
        return Stream.of(beanClass.getDeclaredConstructors())
                .filter(e -> e.isAnnotationPresent(Autowired.class))
                .findAny().orElse(null);
    }

    private boolean hasAutowiredAnnotationInFields(Class<?> beanClass) {
        logger.trace("Выполняется метод [hasAutowiredAnnotationInFields]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredFields())
                .anyMatch(e -> e.isAnnotationPresent(Autowired.class));
    }

    private List<Field> getFieldsWithAutowiredAnnotation(Class<?> beanClass) {
        logger.trace("Выполняется метод [getFieldsWithAutowiredAnnotation]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(Autowired.class))
                .collect(Collectors.toList());
    }

    private boolean injectDependsInField(Object bean, Class<?> beanClass) {
        logger.debug("Старт метода [injectDependsInField]. Сервис: [{}]",beanClass.getSimpleName());
        List<Field> fields = getFieldsWithAutowiredAnnotation(beanClass);
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> beanForInjectClass = field.getType();
            // TODO лучше сразу createBean, он внутри уже проверяет наличие в кэше. Надо просто возвращать объект из этого метода
            //TODO - DONE. Беру бин из контекста
            Object beanForInject = context.getBean(beanForInjectClass);
            if(beanForInject == null){
                logger.error("Невозможно внедрить зависимость в [{}]. Отсутствует бин для внедрения", beanClass.getSimpleName());
                logger.debug("Завершение работы метода [injectDependsInField]");
                return false;
            }
            try {
                field.set(bean, beanForInject);
            } catch (IllegalAccessException e) {
                logger.error("Ошибка при внедрении зависимости. Сервис: [{}], поле: [{}]. Ошибка[{}]", beanClass.getSimpleName(), field, e.toString());
                logger.debug("Завершение работы метода [injectDependsInField]");
            }
            logger.debug("Внедрены зависимости в полях. Сервис: [{}], поля: [{}]", beanClass.getSimpleName(), fields);
            logger.debug("Завершение работы метода [injectDependsInField]");
            return true;
        }
        logger.debug("Завершение работы метода [injectDependsInField]");
        return false;
    }

    private boolean hasAutowiredAnnotationInMethods(Class<?> beanClass) {
        logger.trace("Выполняется метод [hasAutowiredAnnotationInMethods]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredMethods())
                .anyMatch(e -> e.isAnnotationPresent(Autowired.class));
    }

    private List<Method> getMethodsWithAutowiredAnnotation(Class<?> beanClass) {
        logger.trace("Выполняется метод [getMethodsWithAutowiredAnnotation]. Класс: [{}]", beanClass);
        return Stream.of(beanClass.getDeclaredMethods())
                .filter(e -> e.isAnnotationPresent(Autowired.class))
                .collect(Collectors.toList());
    }

    private boolean injectDependsInMethod(Object bean, Class<?> beanClass) {
        logger.debug("Старт метода [injectDependsInMethod]. Сервис: [{}]", beanClass.getSimpleName());
        List<Method> methods = getMethodsWithAutowiredAnnotation(beanClass);
        for (Method method : methods) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] beansForInject = new Object[parameterTypes.length];
            int i = 0;
            /**
             * TODO IDEA подсвечивает Class - почему? Исправьте пожалуйста все такие предупреждения в данном классеr
             * TODO - DONE
             */
            for (Class<?> parameterClass : parameterTypes) {
                //TODO почему через метод только из кэша?
                //TODO - DONE берем бин из контекста
                Object beanForInject = context.getBean(parameterClass);
                if (beanForInject != null) {
                    beansForInject[i] = beanForInject;
                    i++;
                } else {
                    logger.warn("Не удалось внедрить зависимость в методе. Сервис: [{}], параметры метода: [{}]", beanClass.getSimpleName(), parameterTypes);
                    logger.debug("Завершение работы метода [injectDependsInMethod]");
                    return false;
                }
            }
            /**
             * TODO может ли не выполниться это условие?
             * Было над if(i==parameterTypes.lenght)
             * МЕ: убрал, если контекст не вернет бин, мы сюда даже не попадем
             * TODO - DONE
             */
            try {
                method.invoke(bean, beansForInject);
                logger.debug("Успешное внедрение зависимости в сервис: [{}], внедрены объекты: [{}]", bean, beansForInject);
                logger.debug("Завершение работы метода [injectDependsInMethod]");
                return true;
            } catch (IllegalAccessException | InvocationTargetException e) {
                /**
                 * TODO для вывода ошибок используется logger#error. Также он должен выводить ошибку, вместо прямого вызова e.printStackTrace()
                 * TODO - DONE
                 */
                logger.error("Ошибка при внедрении зависимости. Сервис [ {} ], метод [ {} ], внедряемые объекты [ {} ]. Ошибка: [{}]", bean, method, beansForInject, e.toString());
                logger.debug("Завершение работы метода [injectDependsInMethod]");
            }
        }
        logger.warn("Не удалось внедрить зависимость. Сервис [{}], методы: [{}]", beanClass.getSimpleName(), methods);
        logger.debug("Завершение работы метода [injectDependsInMethod]");
        return false;
    }
}
