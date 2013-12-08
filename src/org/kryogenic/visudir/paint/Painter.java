package org.kryogenic.visudir.paint;

import org.kryogenic.visudir.Config;
import org.kryogenic.visudir.Selection;
import org.kryogenic.visudir.VisuDir;
import org.kryogenic.visudir.wrappers.*;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * @author: Kale
 * @date: 18/09/13
 */
public class Painter {
    private final Graphics2D g;
    private final Selection cursor;

    private Rectangle2D rect;

    private Painter(PainterBuilder b) {
        this.g = b.g;
        this.rect = b.rect;
        this.cursor = b.cursor;
    }

    public Painter setRect(Rectangle2D rect) {
        this.rect = rect;
        return this;
    }

    public void paint(VisuPath path) {
        paint(path, true);
    }

    public void paint(VisuPath path, boolean cursor) {
        paintForeground();
        if(path.isSystemRoot()) {
            paintSystemRoot();
            g.drawString("System Root", (int) rect.getX(), 10);
        } else if(path.getFile().isFile()) {
            paintFile(path.getFile());
            g.drawString("File", (int) rect.getX(), 10);
        } else if(VisuDir.tree().getCache().containsKey(path.getFile())) {
            paintVisuFolder(VisuDir.tree().getCache().get(path.getFile()), cursor);
            g.drawString("VisuFolder", (int) rect.getX(), 10);
        } else if(path.getFile().isDirectory()) {
            paintFileFolder(path.getFile());
            g.drawString("Directory", (int) rect.getX(), 10);
        } else {
            VisuDir.display().showMessage("Cannot paint 'Other': " + path);
        }
        g.setColor(Color.BLACK);
        g.fillRect(8, (int) rect.getHeight() - 15, 300, 20);
        g.setColor(Color.WHITE);
        g.drawString((String)Config.get("cur"), 10, (int)rect.getHeight());
    }

    private void paintForeground() {
        g.setColor(VisuDir.display().getColors().foreground());
        g.fill(rect);
        double innerPad = 2;
        rect.setRect(rect.getX() + innerPad, rect.getY() + innerPad, rect.getWidth() -  innerPad * 2, rect.getHeight() - innerPad * 2);
    }

    private void paintFile(File file) {
        paintFile(file, false);
    }
    private void paintFile(File file, boolean extraInfo) {
        int fontSize = g.getFont().getSize();

        int lines = 10;
        if(extraInfo) {
            lines += 5;
        }
        if(file.isHidden()) {
            lines += 2;
        }

        float x = (float) rect.getX() + 5;
        float y = (float) (rect.getY() + rect.getHeight() / 2 - (lines / 2d) * (double) fontSize);

        g.setColor(VisuDir.display().getColors().text()); // paint the text
        g.drawString("Name: " + file.getName(), x, y);
        g.drawString("Size: " + VisuThing.formatSize(file.length()), x,  y += fontSize);
        if(file.isHidden())
            g.drawString("Hidden File", x,  y += 2 * fontSize);
        g.drawString("Modified on", x,  y += 2 * fontSize);
        g.drawString(DateFormat.getDateTimeInstance().format(new Date(file.lastModified())), x,  y += fontSize);
        g.drawString("Permissions:", x,  y += 2 * fontSize);
        g.drawString("  Read: " + file.canRead(), x,  y += fontSize);
        g.drawString("  Write: " + file.canWrite(), x,  y += fontSize);
        g.drawString("  Execute: " + file.canExecute(), x, y += fontSize);
        if(extraInfo) {
            g.drawString("Extra: ", x, y += 2 * fontSize);
            g.drawString("Is Directory: " + file.isDirectory(), x, y += fontSize);
            g.drawString("Is File: " + file.isFile(), x, y += fontSize);
            g.drawString("List: " + file.list(), x, y += fontSize);
        }
    }

    private void paintSystemRoot() {
        File[] roots = File.listRoots();
        if(roots != null) {
            int largestIdx = 0;
            long largestSize = roots[0].getTotalSpace() - roots[0].getFreeSpace();
            for(int i = 1; i < roots.length; i++) {
                if(roots[i].getTotalSpace() - roots[i].getFreeSpace() > largestSize) {
                    largestIdx = i;
                }
            }

            UnitProperties properties = new UnitProperties(rect.getHeight(), roots.length);
            double y = rect.getY();

            int i = cursor == null ? 0 : cursor.idx(properties.PER_PAGE);
            for(; i < roots.length && y < rect.getHeight(); i++) {
                File root = roots[i];
                double thingWidth = rect.getWidth() * (roots[i].getTotalSpace() - roots[i].getFreeSpace()) / (double) (roots[largestIdx].getTotalSpace() - roots[largestIdx].getFreeSpace());
                paintUnit(root, rect.getX(), y, properties, thingWidth, root.getAbsolutePath(), root.getTotalSpace() - root.getFreeSpace(), cursor != null && i == cursor.get());
                y += properties.UNIT_HEIGHT + UnitProperties.UNIT_PAD;
            }
        }
    }

    private void paintFileFolder(File folder) {
        File[] files = folder.listFiles();
        if(files == null) {
            VisuDir.display().showMessage("Cannot display the contents of " + folder);
            paintFile(folder, true);
        } else {
            UnitProperties properties = new UnitProperties(rect.getHeight(), files.length);
            double y = rect.getY();

            int i = cursor == null ? 0 : cursor.idx(properties.PER_PAGE);
            for(; i < files.length && y < rect.getHeight(); i++) {
                File file = files[i];
                paintUnit(file, rect.getX(), y, properties, rect.getWidth(), file.getName(), file.isDirectory() ? -1 : file.length(), cursor != null && i == cursor.get());
                y += properties.UNIT_HEIGHT + UnitProperties.UNIT_PAD;
            }
        }
    }

    private void paintVisuFolder(VisuFolder folder, boolean cursor) {
        int l = folder.contents().length;
        if(l == 0) {
            g.drawString("(empty)", (float) rect.getX() + 5, (float) (rect.getY() + rect.getHeight() / 2 - g.getFont().getSize()));
        } else {
            UnitProperties properties = new UnitProperties(rect.getHeight(), l);
            double y = rect.getY();

            for(int i = Selection.idx(properties.PER_PAGE, folder.getCursor()); i < l && y < rect.getHeight(); i++) {
                VisuThing thing = folder.contents()[i];
                double thingWidth = thing.bytesSize() < 0 ? rect.getWidth() : rect.getWidth() * thing.bytesSize() / (double) folder.getLargest().bytesSize();
                paintUnit(thing, rect.getX(), y, properties, thingWidth, thing.getName(), thing.bytesSize(), cursor && i == folder.getCursor());
                y += properties.UNIT_HEIGHT + UnitProperties.UNIT_PAD;
            }
        }
    }

    private void paintUnit(Object unit, double x, double y, UnitProperties properties, double unitWidth, String name, long size, boolean cursor) {
        g.setColor(UnitType.getType(unit).getColor());
        g.fill(new Rectangle2D.Double(x, y, unitWidth, properties.UNIT_HEIGHT));

        if(cursor) {
            g.setColor(VisuDir.display().getColors().cursor());
            Rectangle2D cursorRect = new Rectangle2D.Double(x - 1d, y - 1d, rect.getWidth() + 1d, properties.UNIT_HEIGHT + 1d);
            g.draw(new Line2D.Double(cursorRect.getX(), cursorRect.getY(), cursorRect.getX(), cursorRect.getY() + cursorRect.getHeight()));
            g.draw(new Line2D.Double(cursorRect.getX() + cursorRect.getWidth(), cursorRect.getY(), cursorRect.getX() + cursorRect.getWidth(), cursorRect.getY() + cursorRect.getHeight()));
        }

        g.setColor(VisuDir.display().getColors().text());
        x += 5;
        if(size < 0) {
            y += properties.UNIT_HEIGHT / 2 - (g.getFontMetrics().getAscent() + g.getFontMetrics().getDescent()) / 2 + g.getFontMetrics().getAscent(); /// middle
        } else {
            y += properties.UNIT_HEIGHT / 2 - 2;
        }
        g.drawString(name, (float) x, (float) y); // todo scroll this text if the cursor is on this unit
        if(size >= 0) {
            g.drawString(VisuThing.formatSize(size), (float) x, (float) (y + g.getFontMetrics().getAscent()));
        }
    }

    private static enum UnitType {
        FILE, DIR, UNKNOWN;
        public Color getColor() {
            switch (this) {
                case FILE:
                    return VisuDir.display().getColors().file();
                case DIR:
                    return VisuDir.display().getColors().dir();
                default:
                    return VisuDir.display().getColors().unknown();
            }
        }
        public static UnitType getType(Object o) {
            if(o instanceof File) {
                File f = (File) o;
                if(f.isFile()) {
                    return FILE;
                } else if(f.isDirectory()) {
                    return DIR;
                }
            } else {
                if(o instanceof VisuFile) {
                    return FILE;
                } else if(o instanceof VisuFolder) {
                    return DIR;
                }
            }
            return UNKNOWN;
        }
    }

    private static class UnitProperties {
        public static final double MIN_HEIGHT = 30;
        public static final double UNIT_PAD = 1;
        public final double UNIT_HEIGHT;
        public final int PER_PAGE;

        public UnitProperties(double containerHeight, int numUnits) {
            UNIT_HEIGHT = Math.max((containerHeight - UNIT_PAD * (numUnits - 1)) / (double) numUnits, MIN_HEIGHT);
            PER_PAGE = (int) (containerHeight / (UNIT_HEIGHT + UNIT_PAD));
        }
    }

    public static class PainterBuilder {

        private final Graphics2D g;
        private final Rectangle2D rect;

        private Selection cursor;

        public PainterBuilder(Graphics2D g, Rectangle2D rect) {
            this.g = g;
            this.rect = rect;
        }

        public PainterBuilder(Painter p) {
            this.g = p.g;
            this.rect = p.rect;
            this.cursor = p.cursor;
        }

        public PainterBuilder setCursor(Selection cursor) {
            this.cursor = cursor;
            return this;
        }

        public Painter build() {
            return new Painter(this);
        }
    }
}
