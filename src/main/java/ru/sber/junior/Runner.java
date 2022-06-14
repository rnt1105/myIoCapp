package ru.sber.junior;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.junior.Proxy.TimedProxy;
import ru.sber.junior.Services.LightInterface;
import ru.sber.junior.Services.RadioInterface;
import ru.sber.junior.Services.SmartHouseInterface;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Runner {
    private final static Logger logger = LogManager.getRootLogger();
    private final static String PACKAGE_NAME = Runner.class.getPackageName();

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("Программа запущена");
        ApplicationContext context = new ApplicationContext();
        logger.info("Создан ApplicationContext: [{}]", context);
        context.run(PACKAGE_NAME);

        SmartHouseInterface smartHouse = (SmartHouseInterface) context.getBean("SmartHouse");
        if(smartHouse != null){
            smartHouse.switchOn();
            smartHouse.switchOff();
        }

        RadioInterface radio = (RadioInterface) context.getBean("Radio");
        if(radio != null){
            radio.radioOn();
            radio.radioOff();
        }

        logger.info("Программа выполняется...");

        System.in.read();
    }
}