/**
 * TODO крайне не рекомендуется использовать прописные буквы в именах интерфейсов. Как думаете, почему?
 */
package ru.sber.junior.MyAnnotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface Service {

}
