package org.kryogenic.util;

/**
 * @author: Kale
 * @date: 19/09/12
 * @version: 0.0
 */
public class BoundedInteger {
    private int integer;
    private final int lowBound, highBound;
    
    public BoundedInteger(int i, int low, int high) {
        this.integer = i;
        this.lowBound = low;
        this.highBound = high;
    }

    public int inc() {
        return inc(1);
    }
    public int inc(int a) {
        if(integer + a > highBound) {
            return integer = lowBound;
        } else {
            return integer += a;
        }
    }
    
    public int dec() {
        return dec(1);
    }
    public int dec(int s) {
        if(integer - s < lowBound) {
            return integer = highBound;
        } else {
            return integer -= s;
        }
    }
    
    public int get() {
        return integer;
    }

    public int max() {
        return highBound;
    }

    public int min() {
        return lowBound;
    }
}
