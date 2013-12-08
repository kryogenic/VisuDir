package org.kryogenic.visudir;

import java.awt.*;
import java.util.HashMap;

/**
* @author: Kale
* @date: 12/10/13
*/
public abstract class ColorScheme {
    private static HashMap<String, ColorScheme> schemes = new HashMap<>();
    static {
        schemes.put("GreenBrown", new ColorScheme() {
            @Override
            public Color background() {
                return new Color(29, 41, 15);
            }
            @Override
            public Color foreground() {
                return new Color(26, 35, 10);
            }
            @Override
            public Color cursor() {
                return Color.WHITE;
            }
            @Override
            public Color text() {
                return new Color(200, 200, 200);
            }
            @Override
            public Color unknown() {
                return new Color(54, 57, 66);
            }
            @Override
            public Color dir() {
                return new Color(40, 73, 7);
            }
            @Override
            public Color file() {
                return new Color(56, 37, 19);
            }
        });
        schemes.put("Sandy", new ColorScheme() {
            @Override
            public Color background() {
                return new Color(42, 44, 43);
            }
            @Override
            public Color foreground() {
                return new Color(50, 65, 64);
            }
            @Override
            public Color cursor() {
                return new Color(220, 139, 17);
            }
            @Override
            public Color text() {
                return new Color(30, 30, 32);
            }
            @Override
            public Color unknown() {
                return new Color(54, 57, 66);
            }
            @Override
            public Color dir() {
                return new Color(217, 203, 158);
            }
            @Override
            public Color file() {
                return new Color(220, 53, 34);
            }
        });
        schemes.put("Classic", new ColorScheme() {
            @Override
            public Color background() {
                return new Color(30, 30, 30);
            }
            @Override
            public Color foreground() {
                return new Color(45, 45, 45);
            }
            @Override
            public Color cursor() {
                return new Color(255, 228, 86);
            }
            @Override
            public Color text() {
                return new Color(200, 200, 200);
            }
            @Override
            public Color unknown() {
                return new Color(70, 70, 70);
            }
            @Override
            public Color dir() {
                return new Color(53, 106, 53);
            }
            @Override
            public Color file() {
                return new Color(90, 18, 100);
            }
        });
    }

    public static ColorScheme getScheme(String name) {
        return schemes.get(name);
    }

    public abstract Color background();
    public abstract Color foreground();
    public abstract Color cursor();
    public abstract Color text();
    public abstract Color unknown();
    public abstract Color dir();
    public abstract Color file();
}