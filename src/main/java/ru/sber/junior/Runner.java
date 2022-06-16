package ru.sber.junior;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.junior.Services.Radio;
import ru.sber.junior.Services.SmartHouse;

import java.io.IOException;

public class Runner {
    private final static Logger logger = LogManager.getLogger(Runner.class);
    private final static String PACKAGE_NAME = Runner.class.getPackageName();

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("Программа запущена");
        ApplicationContext context = new ApplicationContext();
        logger.info("Создан ApplicationContext: [{}]", context);
        context.run(PACKAGE_NAME);

        /**
         * TODO Сейчас здесь добавлен каст к типу сервиса. Попробуйте реализовать getBean так, чтобы работали конструкции:
         * SmartHouseInterface smartHouse = context.getBean("SmartHouse");
         * SmartHouseInterface smartHouse = context.getBean(SmartHouseInterface.class);
         * TODO - DONE.
         */
        SmartHouse smartHouse = context.getBean("SmartHouseImpl");
        if(smartHouse != null){
            smartHouse.switchOn();
            smartHouse.switchOff();
        }

        Radio radio = context.getBean(Radio.class);
        if(radio != null){
            radio.radioOn();
            radio.radioOff();
        }

        logger.info("Программа выполняется...");

        System.in.read();
    }
}