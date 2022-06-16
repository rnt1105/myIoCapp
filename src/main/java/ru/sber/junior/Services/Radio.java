package ru.sber.junior.Services;

/**
 * TODO предлагаю использовать общепринятый нейминг для интерфейсов и сервисов
 * Убрал постфикс Interface у интерфейсов,
 * добавил постфикс Impl у классов, реализующих интерфейс
 * TODO - DONE
 */
public interface Radio {
    /**
     * TODO idea подсвечивает модификатор public серым. Почему?
     * методы, объявленные в интерфейсе, по умолчанию с модификатором доступа public
     * TODO - DONE
     */

    /**
     * Switch ON radio
     * Writes result in console
     * @throws InterruptedException
     */
    void radioOn() throws InterruptedException;

    /**
     * TODO всегда нужно описывать методы интерфейса (javaDoc)
     * TODO - DONE
     */

    /**
     * Switch OFF radio
     * Writes result in console
     */
    void radioOff();
}
