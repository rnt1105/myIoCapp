package ru.sber.junior.Services;

public interface Light {
    /**
     * Switch ON light
     * Writes result in console
     */
    void lightOn();

    /**
     * Switch OFF light
     * Writes result in console
     * @throws InterruptedException
     */
    void lightOff() throws InterruptedException;
}
