package ru.sber.junior.Proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.junior.MyAnnotations.Timed;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TimedProxy implements InvocationHandler {
    /**
     * TODO для создания логгера необходимо использовать org.apache.logging.log4j.LogManager#getLogger(java.lang.Class)
     * TODO - DONE
     */
    private final static Logger logger = LogManager.getLogger(TimedProxy.class);
    /**
     * TODO idea рекомендует поработать над этими полями. Что она рекомендует и зачем?
     * Сделал поле Service final. Что бы присвоенную ссылку на объект нельзя было изменить
     * TODO - DONE
     */
    private final Object service;

    public TimedProxy(Object service) {
        this.service = service;
    }

    /**
     * TODO сейчас при вызове каждого метода сканируются все методы сервиса. Стоит ли это делать каждый раз?
     * Можно ли сгенерировать готовый прокси класс, который будет знать, какие методы троебуют дополнительной логики?
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method[] declaredMethods = service.getClass().getDeclaredMethods();
        for (Method myMethod : declaredMethods) {
            if (myMethod.isAnnotationPresent(Timed.class)) {
                if (myMethod.getName().equals(method.getName())) {
                    long startMethod = System.currentTimeMillis();
                    Object o = method.invoke(service, args);
                    long finishMethod = System.currentTimeMillis();
                    long runTimeSec = (finishMethod-startMethod)/1000;
                    /**
                     * TODO здесь стоит использовать перегрузку org.apache.logging.log4j.Logger#info(java.lang.String, java.lang.Object...)
                     * Как думаете, почему?
                     * Думаю, сообщение создавать с помощью конкатенации не безопасно. Передача в качестве параметров безопаснее
                     * TODO - DONE
                     */
                    logger.info("Метод [{}.{}] выполнялся {} секунд", service.getClass().getSimpleName(),method.getName(),runTimeSec);
                    // TODO потерял возвращаемое значение
                    //TODO - DONE
                    return o;
                }
            }
        }
        return method.invoke(service, args);
    }
}