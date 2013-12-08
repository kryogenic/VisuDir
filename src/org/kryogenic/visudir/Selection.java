package org.kryogenic.visudir;

/**
 * @author: Kale
 * @date: 03/10/13
 */
public class Selection {
    private final int length;
    private int current;

    public Selection(int length) {
        this.length = length;
    }

    public void inc() {
        if(current < length) {
            current++;
        } else {
            current = 0;
        }
    }
    public void dec() {
        if(current > 0) {
            current--;
        } else {
            current = length;
        }
    }

    public int get() {
        return current;
    }

    public int idx(int perPage) {
        return Selection.idx(perPage, current);
    }

    public void set(int current) {
        this.current = current;
    }

    public static int idx(int perPage, int current) {
        int page = perPage == 0 ? 0 : (current > 0 ? current - 1 : current) / perPage; // important: performs integer division
        return perPage * page;
    }
}
