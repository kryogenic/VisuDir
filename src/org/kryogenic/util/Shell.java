package org.kryogenic.util;

/**
 * @author: Kale
 * @date: 27/09/12
 * @version: 0.0
 */
public class Shell<T> {
    private T value;
    public Shell(T t) {
        value = t;
    }
    public void set(T t) {
        value = t;
    }
    public T get() {
        return value;
    }
}
