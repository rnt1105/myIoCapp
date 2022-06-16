package ru.sber.junior.Services;

import ru.sber.junior.MyAnnotations.Service;

@Service
public class LightImpl implements Light {
    public void lightOn() {
        System.out.println("###  Включаю свет в доме  ###");
    }

    public void lightOff() throws InterruptedException {
        System.out.println("###  Выключаю свет в доме  ###");
        Thread.sleep(2000);
    }
}
