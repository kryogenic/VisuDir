package org.kryogenic.visudir.wrappers;

import java.io.File;

/**
 * @author: Kale
 * @date: 27/09/13
 */
public abstract class PathBacked {
    private final VisuPath path;
    protected PathBacked(VisuPath p) {
        this.path = p;
    }
    public VisuPath getPath() {
        return path;
    }
    public File getFile() {
        return path.getFile();
    }
    public String getName() {
        return path.getShortName();
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof PathBacked && ((PathBacked) o).getPath().equals(this.getPath());
    }
    @Override
    public String toString() {
        return getPath().toString();
    }
}