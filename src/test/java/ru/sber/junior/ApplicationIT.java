package ru.sber.junior;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.sber.junior.Services.LightInterface;
import ru.sber.junior.Services.RadioInterface;
import ru.sber.junior.Services.SmartHouseInterface;
import ru.sber.junior.Services.TvInterface;

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
        SmartHouseInterface smartHouse = (SmartHouseInterface) context.getBean("SmartHouse");
        Assertions.assertNotNull(smartHouse);
        Assertions.assertDoesNotThrow(() -> smartHouse.switchOn());
    }

    @Test
    public void radioTest() {
        RadioInterface radio = (RadioInterface) context.getBean("Radio");
        Assertions.assertNotNull(radio);
        Assertions.assertDoesNotThrow(() -> radio.radioOff());
    }

    @Test
    public void tvTest(){
        TvInterface tv = (TvInterface) context.getBean("Tv");
        Assertions.assertNotNull(tv);
        Assertions.assertDoesNotThrow(() -> tv.switchOn());
    }

    @Test
    public void lightTest(){
        LightInterface light = (LightInterface) context.getBean("Light");
        Assertions.assertNotNull(light);
        Assertions.assertDoesNotThrow(() -> light.lightOn());
    }
}