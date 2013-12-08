package org.kryogenic.visudir;

import org.kryogenic.util.BoundedInteger;
import org.kryogenic.visudir.cache.VisuTree;
import org.kryogenic.visudir.components.Display;
import org.kryogenic.visudir.wrappers.VisuFolder;
import org.kryogenic.visudir.wrappers.VisuPath;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * @author: Kale
 * @date: 19/09/12
 */
public class VisuDir extends JComponent implements Runnable, KeyListener, MouseWheelListener {

    private static VisuDir VISU_DIR;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                Config.saveConfig();
            }
        }));
    }

    private static class ButtonState {
        public boolean up, down, left, right;
        public int scroll;
    }

    private final ButtonState buttons;
    private final Display display;
    private final JFrame frame;
    private final VisuTree visuTree = new VisuTree();

    public VisuDir() {
        buttons = new ButtonState();
        display = new Display(Config.get("NumViews", Integer.class));
        frame = new JFrame();
    }

    public void update() {
        if(buttons.up) {
            display.moveUp();
            buttons.up = false;
        }
        if(buttons.down) {
            display.moveDown();
            buttons.down = false;
        }
        if(buttons.left) {
            display.moveLeft();
            buttons.left = false;
        }
        if(buttons.right) {
            display.moveRight();
            buttons.right = false;
        }
        if(buttons.scroll != 0) {
            if(buttons.scroll < 0) {
                display.moveUp();
            } else {
                display.moveDown();
            }
            buttons.scroll = 0;
        }
    }

    public static Display display() {
        return VISU_DIR.display;
    }

    public static JFrame frame() {
        return VISU_DIR.frame;
    }

    public static VisuTree tree() {
        return VISU_DIR.visuTree;
    }


    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(display().getColors().background());
        g2.fillRect(0, 0, getWidth(), getHeight());

        display.paint(g2, getWidth(), getHeight());
    }

    @Override
    public void run() {
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addKeyListener(this);
        frame.addMouseWheelListener(this);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                frame.requestFocusInWindow();
            }
        });
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_UP) {
            buttons.up = true;
        } else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
            buttons.down = true;
        } else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
            buttons.left = true;
        } else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
            buttons.right = true;
        } else if(e.getKeyCode() == KeyEvent.VK_F5) {
            display().refresh();
        } else if(e.getKeyCode() == KeyEvent.VK_DELETE) {
            display().delete();
        } else if(e.getKeyCode() == KeyEvent.VK_C) {
            display().setColorScheme(COLORS.values()[cs.inc()].colName());
        }
    }

    BoundedInteger cs = new BoundedInteger(0, 0, COLORS.values().length - 1);
    private enum COLORS {
        ONE("Classic"), TWO("Sandy"), THREE("GreenBrown");
        final String name;
        COLORS(String name) {
            this.name = name;
        }
        public String colName() {
            return name;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        buttons.scroll = e.getWheelRotation();
    }

    public static final String FS = System.getProperty("file.separator");
    public static final String HOME = System.getProperty("user.home");
    public static final String VISU_HOME = HOME + FS + "VisuDir";

    public static void main(final String... args) {
        if(!Config.canWrite()) {
            int result = JOptionPane.showConfirmDialog(null, "Allow VisuDir to store files on your hard drive?", "Permission Check", JOptionPane.YES_NO_CANCEL_OPTION);
            switch(result) {
                case JOptionPane.YES_OPTION:
                    Config.touchConfig();
                    break;
                case JOptionPane.CLOSED_OPTION:
                case JOptionPane.CANCEL_OPTION:
                    return;
            }
        }
        if(args.length > 0) {
            File wd;
            int i = 0;
            do {
                String arg = args[i];
                if((wd = new File(arg)).isDirectory() // argument itself is a directory
                        || (wd = new File(System.getProperty("user.dir") + FS + arg)).isDirectory() // argument relative to working directory is a directory
                        || (wd = new File(HOME + FS + arg)).isDirectory() // argument relative to user's home is a directory
                        ) {
                    Config.put("Focus", new VisuPath(wd));
                    break;
                }
                i++;
            } while(i < args.length);
        }
        VISU_DIR = new VisuDir();
        display().initialize();

        EventQueue.invokeLater(VISU_DIR);
        while(true) {
            VISU_DIR.update();
            VISU_DIR.repaint();
        }
    }
}
