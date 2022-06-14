package ru.sber.junior.Services;

import ru.sber.junior.MyAnnotations.Service;
import ru.sber.junior.MyAnnotations.Timed;

@Service
public class Radio implements RadioInterface {
    @Timed
    public void radioOn() throws InterruptedException {
        System.out.println("###  Включаю музыку  ###");
        Thread.sleep(4000);
    }

    public void radioOff() {
        System.out.println("###  Выключаю музыку  ###");
    }
}