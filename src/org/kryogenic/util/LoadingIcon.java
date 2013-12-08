package org.kryogenic.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferStrategy;

/**
 * @author: Kale
 * @date: 04/09/13
 */
public class LoadingIcon {
    private double arcLength = 0;
    private double ratio = 1;
    private double location = 90;
    boolean b = false;
    public void paint(Graphics g, Point p, double size) {
        g.setColor(new Color(0.7f, 0.7f, 0.7f, 0.6f));
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke((float)(size / 8)));
        g2.draw(new Arc2D.Double(p.getX(), p.getY(), size, size, location, arcLength, Arc2D.OPEN));
        //System.out.println(arcLength);
        //System.out.println(ratio);
        arcLength += ratio;
        location += Math.abs(ratio - 1);
        //System.out.println(arcLength);
        //System.out.println(ratio);
        if(location > 225) {
            ratio += 1 / 39d;
        } else if(arcLength > 70 && ratio >= -0.7d) {
            ratio -= 1 / 9d;
        }
        if(Math.abs(location) >= 360) {
            location = 0;
        }
        if(arcLength > 330) {
            ratio -= 1 / 4d;
        }
        //g2.drawString(String.valueOf(ratio), 5f, (float)(size + 10));
        //g2.drawString(String.valueOf(location), 5f, (float)(size + 25));
        //g2.drawString(String.valueOf(arcLength), 5f, (float)(size + 40));
    }
    public static void main(String... args) throws InterruptedException {
        JFrame j = new JFrame();
        j.setSize(1920, 1080);
        LoadingIcon l = new LoadingIcon();
        j.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        j.setVisible(true);
        j.createBufferStrategy(2);
        BufferStrategy buff = j.getBufferStrategy();
        while(true) {
            Graphics g = buff.getDrawGraphics();
            g.fillRect(0, 0, 1920, 1080);
            l.paint(g, new Point(100, 100), 900d);
            g.dispose();
            buff.show();
            Thread.sleep(3);
        }
    }
}
