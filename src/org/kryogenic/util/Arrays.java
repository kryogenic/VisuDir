package org.kryogenic.util;

/**
 * @author: Kale
 * @date: 20/09/12
 * @version: 0.0
 */
public class Arrays {
    /**
     * Converts an array to a string array, by calling toString()
     * @param array the object array to convert
     * @return the array of strings
     */
    public static String[] toString(Object[] array) {
        String[] sarray = new String[array.length];
        for(int i = 0; i < array.length; i++) {
            sarray[i] = array[i].toString();
        }
        return sarray;
    }
}
