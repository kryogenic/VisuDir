package org.kryogenic.visudir.components;

import org.kryogenic.visudir.VisuDir;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author: Kale
 * @date: 12/09/13
 */
public class Message {
    private static final float color = 0.8f;
    private static final HashMap<String, Message> cache = new HashMap<>();
    private static long timeout = 12500;
    private final String message;
    private float alpha = 1.0f;
    private long time;

    private Message(String message) {
        this.message = message;
        this.time = System.currentTimeMillis();
    }

    public void paint(Graphics2D g, RectangularShape canvas) {
        if(alpha > 0) {
            FontMetrics fm = g.getFontMetrics();
            int strWidth = fm.stringWidth(message);
            int strHeight = fm.getHeight();
            int boxWidth = strWidth + 100;
            int boxHeight = strHeight + 50;
            g.setColor(new Color(color, color, color, alpha));
            Rectangle2D.Double r = new Rectangle2D.Double(canvas.getWidth() / 2 - boxWidth / 2, canvas.getHeight() / 2 - boxHeight / 2, boxWidth, boxHeight);
            g.fill(r);
            g.setColor(new Color(0, 0, 0, alpha));
            g.drawString(message, (int) (canvas.getWidth() / 2 - strWidth / 2), (int) (canvas.getHeight() / 2 + g.getFont().getSize() / 2));
            alpha -= 0.001f;
        }
    }

    public Message reset() {
        if(System.currentTimeMillis() - time > timeout) {
            alpha = 1.0f;
            time = System.currentTimeMillis();
        }
        return this;
    }

    public static Message get(String message) {
        Message cached = cache.get(message);
        if(cached == null) {
            Message m = new Message(message);
            cache.put(message, m);
            return m.reset();
        } else {
            return cached.reset();
        }
    }
}
