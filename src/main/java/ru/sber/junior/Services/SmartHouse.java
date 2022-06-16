package ru.sber.junior.Services;

public interface SmartHouse {
    /**
     * Switch ON Smart house and devices (Radio, Tv, Light ...)
     * Writes result in console
     * @throws InterruptedException
     */
    public void switchOn() throws InterruptedException;

    /**
     * Switch OFF Smart house and devices (Radio, Tv, Light ...)
     * Writes result in console
     * @throws InterruptedException
     */
    public void switchOff() throws InterruptedException;
}
