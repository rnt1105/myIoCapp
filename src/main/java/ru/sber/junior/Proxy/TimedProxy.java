package ru.sber.junior.Proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.junior.MyAnnotations.Timed;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TimedProxy implements InvocationHandler {
    private final static Logger logger = LogManager.getRootLogger();
    private Object service;

    public TimedProxy(Object service) {
        this.service = service;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method[] declaredMethods = service.getClass().getDeclaredMethods();
        for (Method myMethod : declaredMethods) {
            if (myMethod.isAnnotationPresent(Timed.class)) {
                if (myMethod.getName().equals(method.getName())) {
                    long startMethod = System.currentTimeMillis();
                    method.invoke(service, args);
                    long finishMethod = System.currentTimeMillis();
                    logger.info("Метод " + service.getClass().getSimpleName() + "." + method.getName() + " выполнялся: " + ((finishMethod - startMethod) / 1000) + " сек.");
                    return null;
                }
            }
        }
        return method.invoke(service, args);
    }
}