package org.kryogenic.visudir.wrappers;

import org.kryogenic.visudir.Config;
import org.kryogenic.visudir.VisuDir;
import org.kryogenic.visudir.cache.FolderSet;

import java.io.File;

/**
 * @author: Kale
 * @date: 01/10/12
 */
public final class VisuFolder extends VisuThing {
    private final VisuThing[] contents;
    private final VisuThing largest;
    private final long size;

    private int cursor;

    public VisuFolder(File file) {
        this(new VisuPath(file));
    }

    public VisuFolder(VisuPath path) {
        this(path, null, null);
    }

    private VisuFolder(VisuPath path, File[] files, FolderSet cache) {
        super(path);
        int largestIdx = -1;
        long largestSize = -1;
        long size = 0;

        if(files == null) {
            File f = path.getFile();
            Config.put("cur", f.toString());
            files = f.listFiles();
        }
        if(files == null) {
            throw new IllegalArgumentException("Tried to get me to create a VisuFolder out of a file. Not likely, bub.\n" + "Path: '" + path.toString() + "'");
        } else {
            if(cache == null) {
                cache = VisuDir.tree().getCache().getAllIn(getPath());
            }
            contents = new VisuThing[files.length];
            boolean maybeInCache = !cache.isEmpty() && cache.mayContainFilesIn(getPath());
            File[] filesIn;
            for(int i = 0; i < files.length; i++) {
                if(maybeInCache && cache.containsKey(files[i])) {
                    contents[i] = cache.get(files[i]);
                } else {
                    if(files[i].isFile()) {
                        contents[i] = new VisuFile(files[i]);
                    } else if(files[i].isDirectory() && (filesIn = files[i].listFiles()) != null) {
                        contents[i] = new VisuFolder(new VisuPath(files[i].getName(), this.getPath()), filesIn, cache);
                        Config.put("cur", this.getPath() + VisuDir.FS + files[i].getName());
                    } else {
                        //System.out.println("Cannot fromFile '" + files[i] + "'");
                        contents[i] = new VisuThing(files[i]) {
                            @Override
                            public long bytesSize() {
                                return 0;
                            }
                        };
                    }
                }
                size += contents[i].bytesSize();
                if(contents[i].bytesSize() > largestSize) {
                    largestIdx = i;
                    largestSize = contents[i].bytesSize();
                }
            }
        }
        if(largestIdx < 0) {
            this.largest = null;
        } else {
            this.largest = contents[largestIdx];
        }
        this.size = size;
    }

    @Override
    public long bytesSize() {
        return size;
    }

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public VisuThing[] contents() {
        return contents;
    }

    public VisuThing get(int index) {
        return contents()[index];
    }

    public int indexOf(String shortName) {
        VisuThing[] contents = contents();
        for(int i = 0; i < contents.length; i++) {
            if(contents[i].getName().equals(shortName)) {
                return i;
            }
        }
        return -1;
    }

    public VisuThing getLargest() {
        return largest;
    }
}
