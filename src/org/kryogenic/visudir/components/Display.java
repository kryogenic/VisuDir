package org.kryogenic.visudir.components;

import org.kryogenic.util.LoadingIcon;
import org.kryogenic.visudir.ColorScheme;
import org.kryogenic.visudir.Config;
import org.kryogenic.visudir.Selection;
import org.kryogenic.visudir.VisuDir;
import org.kryogenic.visudir.paint.Painter;
import org.kryogenic.visudir.wrappers.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author: Kale
 * @date: 01/10/12
 */
public class Display {
    private ColorScheme colorScheme;
    private Selection cursor;

    private final int numStates;
    private final LoadingIcon loader = new LoadingIcon();
    private Message message = null;

    /**
     * Constructs a new Display
     * @param numViews The number of directories to show. Always shows the working directory, and the directory
     *                  selected by the cursor. Shows the directories above the working directory up to numStates - 2.
     */
    public Display(int numViews) {
        setColorScheme(Config.get("ColorScheme", String.class));
        cursor = new Selection(0);
        this.numStates = numViews;
    }

    public void initialize() {
        update();
    }

    public void moveUp() {
        if(cursor != null) {
            cursor.dec();
            if(VisuDir.tree().getCache().containsKey(focus().getFile())) {
                VisuDir.tree().getCache().get(focus().getFile()).setCursor(cursor.get());
            }
        }
    }
    public void moveDown() {
        if(cursor != null) {
            cursor.inc();
            if(VisuDir.tree().getCache().containsKey(focus().getFile())) {
                VisuDir.tree().getCache().get(focus().getFile()).setCursor(cursor.get());
            }
        }
    }
    public void moveLeft() {
        Config.put("Focus", focus().getParent());
        update();
    }
    public void moveRight() {
        if(cursor != null) {
            Config.put("Focus", focus().getAt(cursor.get()));
            update();
        }
    }

    public void delete() {
        File toDelete = focus().getAt(cursor.get()).getFile();
        int result = JOptionPane.showConfirmDialog(VisuDir.frame(), "Really delete '" + toDelete.getName() + "'?\nThis action cannot be recovered.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        switch(result) {
            case JOptionPane.YES_OPTION:
                try {
                    Files.walkFileTree(toDelete.toPath(), new FileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            file.toFile().delete();
                            return FileVisitResult.CONTINUE;
                        }
                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            dir.toFile().delete();
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        refresh();
    }

    public VisuPath focus() {
        return Config.get("Focus", VisuPath.class);
    }

    public ColorScheme getColors() {
        return colorScheme;
    }

    public void paint(Graphics2D g, int width, int height) {
        double unitPad = 3;

        double unitWidth = (width - unitPad * (1 + numStates)) / (numStates);
        double unitHeight = height - unitPad * 2;

        double x, y = x = unitPad;

        Rectangle2D.Double rect = new Rectangle2D.Double(x, y, unitWidth, unitHeight);

        Painter generalPainter = new Painter.PainterBuilder(g, rect).build();
        VisuPath toPaint = focus().getParent();
        for(int i = 0; i < numStates - 2 && toPaint != null; i++) {
            generalPainter.paint(toPaint);
            toPaint = toPaint.getParent();
            x += unitWidth + unitPad;
            rect.setRect(x, y, unitWidth, unitHeight);
        }

        new Painter.PainterBuilder(generalPainter).setCursor(cursor).build().setRect(rect).paint(focus());
        x += unitWidth + unitPad;
        rect.setRect(x, y, unitWidth, unitHeight);

        toPaint = cursor == null ? null : focus().getAt(cursor.get());
        if(toPaint != null) {
            generalPainter.setRect(rect).paint(toPaint, false);
        }

        if(message != null) {
            message.paint(g, new Rectangle(0, 0, width, height));
        }
        if(VisuDir.tree().updating()) {
            int size = 15;
            loader.paint(g, new Point(width - size * 3 / 2, height - size * 3 / 2), size);
        }
    }

    public void setColorScheme(String name) {
        colorScheme = ColorScheme.getScheme(name);
    }

    public void showMessage(String message) {
        this.message = Message.get(message);
    }

    public void refresh() {
        VisuDir.tree().refresh(focus());
        update();
    }

    public void update() {
        File focusFile = focus().getFile();
        String[] focusList = focusFile.list();
        if(focus().isSystemRoot()) {
            cursor = new Selection(File.listRoots().length - 1);
        } else if(VisuDir.tree().getCache().containsKey(focusFile)) {
            focus().printFull();
            cursor = new Selection(focusList.length - 1);
            cursor.set(VisuDir.tree().getCache().get(focusFile).getCursor());
        } else if(focusFile.isDirectory() && focusList != null) {
            VisuDir.tree().cache(focus());
            cursor = new Selection(focusList.length - 1);
        } else {
            cursor = null;
        }
    }
}
