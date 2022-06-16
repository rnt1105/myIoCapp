package ru.sber.junior.Proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class TimedProxy implements InvocationHandler {
    /**
     * TODO для создания логгера необходимо использовать org.apache.logging.log4j.LogManager#getLogger(java.lang.Class)
     * TODO - DONE
     */
    private final static Logger logger = LogManager.getLogger(TimedProxy.class);
    /**
     * TODO idea рекомендует поработать над этими полями. Что она рекомендует и зачем?
     * ME: Сделал поле Service final. Что бы присвоенную ссылку на объект нельзя было изменить
     * TODO - DONE
     */
    private final Object service;

    private final List<String> methodsWithTimedAnnotation;

    public TimedProxy(Object service, List<String> methodList) {
        this.service = service;
        this.methodsWithTimedAnnotation = methodList;
    }

    /**
     * TODO сейчас при вызове каждого метода сканируются все методы сервиса. Стоит ли это делать каждый раз?
     * Можно ли сгенерировать готовый прокси класс, который будет знать, какие методы требуют дополнительной логики?
     * ME: добавил в качестве поля список методов с аннотацией @Timed
     * TODO - DONE
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (methodsWithTimedAnnotation.contains(method.getName())) {
            long startMethod = System.currentTimeMillis();
            Object returnedValue = method.invoke(service, args);
            long finishMethod = System.currentTimeMillis();
            long runTimeSec = (finishMethod - startMethod) / 1000;
            /**
             * TODO здесь стоит использовать перегрузку org.apache.logging.log4j.Logger#info(java.lang.String, java.lang.Object...)
             * Как думаете, почему?
             * ME: Думаю, сообщение создавать с помощью конкатенации не безопасно. Передача в качестве параметров безопаснее
             * TODO - DONE
             */
            logger.info("Метод [{}.{}] выполнялся {} сек.", service.getClass().getSimpleName(), method.getName(), runTimeSec);
            // TODO потерял возвращаемое значение
            //TODO - DONE
            return returnedValue;
        }
        return method.invoke(service, args);
    }
}

