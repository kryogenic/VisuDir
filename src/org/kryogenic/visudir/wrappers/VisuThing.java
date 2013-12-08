package org.kryogenic.visudir.wrappers;

import org.kryogenic.visudir.Config;

import java.io.File;

/**
 * @author: Kale
 * @date: 01/10/12
 */
public abstract class VisuThing extends PathBacked {

    public abstract long bytesSize();

    public VisuThing(File f) {
        super(new VisuPath(f));
    }
    public VisuThing(VisuPath p) {
        super(p);
    }

    public String size() {
        return formatSize(bytesSize());
    }

    public static String formatSize(long size) {
        boolean si = Config.get("SI") instanceof Boolean ? Config.get("SI", Boolean.class) : Boolean.valueOf(Config.get("SI", String.class));
        int unit = si ? 1000 : 1024;
        if(size < unit) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", size / Math.pow(unit, exp), pre);
    }
}