package ru.sber.junior.Services;

import ru.sber.junior.MyAnnotations.Autowired;
import ru.sber.junior.MyAnnotations.Service;
import ru.sber.junior.MyAnnotations.Timed;

@Service
public class SmartHouseImpl implements SmartHouse {
    /**
     * TODO idea рекомендует поработать над этими полями. Что она рекомендует и зачем?
     * Пометка над полем Radio.
     * МЕ: установил модификатор final. Ссылка присваивается в конструкторе и остается неизменяемой
     * т.к она privat и не имеется сеттеров для её изменения
     * TODO - DONE
     */
    private final Radio radio;
    private Tv tv;

    @Autowired //Проверка внедрения зависимости через поле
    private Light light;

    @Autowired //Проверка внедрения зависимости через конструктор
    public SmartHouseImpl(Radio radio) {
        this.radio = radio;
    }

    @Autowired //Проверка внедрения зависимости через метод
    public void setTv(Tv tv){
        this.tv = tv;
    }

    @Timed
    public void switchOn() throws InterruptedException {
        System.out.println("###  Запускаю систему Умный Дом  ###");
        light.lightOn();
        tv.switchOn();
        radio.radioOn();
        System.out.println("###  Система Умный Дом включена  ###");
    }

    @Timed
    public void switchOff() throws InterruptedException {
        System.out.println("###  Отключаю систему Умный Дом!  ###");
        radio.radioOff();
        tv.switchOff();
        light.lightOff();
        System.out.println("###  Система Умный Дом отключена.  ###");
    }
}
