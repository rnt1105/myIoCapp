package ru.sber.junior.Services;

import ru.sber.junior.MyAnnotations.Autowired;
import ru.sber.junior.MyAnnotations.Service;
import ru.sber.junior.MyAnnotations.Timed;

@Service
public class SmartHouseImpl implements SmartHouse {
    /**
     * TODO idea рекомендует поработать над этими полями. Что она рекомендует и зачем?
     */
    private RadioImpl radioImpl;
    private TvImpl tvImpl;

    @Autowired //Проверка внедрения зависимости через поле
    private LightImpl lightImpl;

    @Autowired //Проверка внедрения зависимости через конструктор
    public SmartHouseImpl(RadioImpl radioImpl) {
        this.radioImpl = radioImpl;
    }

    @Autowired //Проверка внедрения зависимости через метод
    public void setTv(TvImpl tvImpl){
        this.tvImpl = tvImpl;
    }

    @Timed
    public void switchOn() throws InterruptedException {
        System.out.println("###  Запускаю систему Умный Дом  ###");
        lightImpl.lightOn();
        tvImpl.switchOn();
        radioImpl.radioOn();
        System.out.println("###  Система Умный Дом включена  ###");
    }

    @Timed
    public void switchOff() throws InterruptedException {
        System.out.println("###  Отключаю систему Умный Дом!  ###");
        radioImpl.radioOff();
        tvImpl.switchOff();
        lightImpl.lightOff();
        System.out.println("###  Система Умный Дом отключена.  ###");
    }
}
