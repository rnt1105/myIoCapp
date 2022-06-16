package ru.sber.junior;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.sber.junior.Services.*;

class ApplicationIT {
    private static final String PACKAGE = Runner.class.getPackageName();
    private static ApplicationContext context = new ApplicationContext();

    @BeforeAll
    @Test
    public static void runContextTest(){
        context.run(PACKAGE);
    }

    @Test
    public void smartHouseTest() {
        SmartHouse smartHouse = context.getBean(SmartHouseImpl.class);
        //SmartHouse smartHouse = context.getBean("SmartHouse");
        Assertions.assertNotNull(smartHouse);
        Assertions.assertDoesNotThrow(() -> smartHouse.switchOn());
    }

    @Test
    public void radioTest() {
        Radio radio = context.getBean("Radio");
        Assertions.assertNotNull(radio);
        Assertions.assertDoesNotThrow(() -> radio.radioOff());
    }

    @Test
    public void tvTest(){
        Tv tv = context.getBean("Tv");
        Assertions.assertNotNull(tv);
        Assertions.assertDoesNotThrow(() -> tv.switchOn());
    }

    @Test
    public void lightTest(){
        Light light = (Light) context.getBean("Light");
        Assertions.assertNotNull(light);
        Assertions.assertDoesNotThrow(() -> light.lightOn());
    }
}