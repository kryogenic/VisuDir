package org.kryogenic.visudir.wrappers;

import java.io.File;

/**
 * @author: Kale
 * @date: 01/10/12
 */
public final class VisuFile extends VisuThing {
    private final long size;

    public VisuFile(File f) {
        super(f);
        this.size = f.length();
    }

    @Override
    public long bytesSize() {
        return size;
    }
}
