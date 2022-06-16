package ru.sber.junior.Services;

import ru.sber.junior.MyAnnotations.Service;

@Service
public class TvImpl implements Tv {
    public void switchOn() throws InterruptedException {
        System.out.println("###  Включаю телевизор  ###");
        Thread.sleep(1000);
    }

    public void switchOff() {
        System.out.println("###  Выключаю телевизор  ###");
    }
}
