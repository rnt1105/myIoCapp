package ru.sber.junior;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sber.junior.Services.*;

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
         * SmartHouseInterface smartHouse = context.getBean("SmartHouse"); по имени интерфейса
         * SmartHouseInterface smartHouse = context.getBean(SmartHouseInterface.class); по интерфейсу
         * МЕ: Сделал реализацию 4 вариантов. Как указаны выше и
         * SmartHouseInterface smartHouse = context.getBean("SmartHouseImpl"); по имени класса
         * SmartHouseInterface smartHouse = context.getBean(SmartHouseImpl.class); по классу
         * TODO - DONE.
         */
//        SmartHouse smartHouse = context.getBean("SmartHouseImpl"); //По имени класса
//        SmartHouse smartHouse = context.getBean(SmartHouseImpl.class); //По классу
//        SmartHouse smartHouse = context.getBean("SmartHouse"); //По имени интерфейса
        SmartHouse smartHouse = context.getBean(SmartHouse.class); //По интерфейсу
        if(smartHouse != null){
            smartHouse.switchOn();
            smartHouse.switchOff();
        }
        logger.info("Программа выполняется...");
        System.in.read();
    }
}