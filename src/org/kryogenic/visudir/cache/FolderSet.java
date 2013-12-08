package org.kryogenic.visudir.cache;

import org.kryogenic.util.Arrays;
import org.kryogenic.visudir.VisuDir;
import org.kryogenic.visudir.wrappers.VisuFolder;
import org.kryogenic.visudir.wrappers.VisuPath;
import org.kryogenic.visudir.wrappers.VisuThing;

import java.io.File;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author: Kale
 * @date: 20/09/13
 */
public class FolderSet extends TreeMap<File, VisuFolder> {
    public VisuPath base = null;
    private static FolderSet emptySet = new FolderSet(new TreeMap<File, VisuFolder>(), null);

    public FolderSet() {
        super();
    }

    public FolderSet(SortedMap<File, VisuFolder> map, VisuPath newBase) {
        super(map);
        this.base = newBase;
    }

    public VisuFolder get(File key) {
        if(super.containsKey(key)) {
            return super.get(key);
        } else if(key.isDirectory()) {
            VisuFolder found = null;
            int start = 0;
            for(File f : keySet()) {
                if(key.toString().contains(f.toString())) { // if key is a subset of f
                    found = super.get(f);
                    start = VisuPath.getComponentCount(f.getAbsolutePath());
                }
            }
            if(found != null) {
                Iterator<String> iter = new VisuPath(key).iterator(start);
                while(iter.hasNext()) {
                    String s = iter.next();
                    found = (VisuFolder) found.get(found.indexOf(s));
                }
            }
            return found;
        } else {
            return null;
        }
    }

    public VisuFolder put(VisuFolder folder) {
        if(containsKey(folder.getFile())) {
            return get(folder.getFile());
        } else {
            base = updateBase(base, folder.getPath());
            removeAllIn(folder.getPath());
            return super.put(folder.getFile(), folder);
        }
    }

    @Override
    public boolean containsKey(Object o) {
        if(((File) o).isDirectory() && mayContain((File) o) && ((File) o).list() != null) {
            if(super.containsKey(o)) {
                return true;
            } else {
                for(File f : keySet()) {
                    if(o.toString().toUpperCase().contains((f.toString() + VisuDir.FS).toUpperCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public FolderSet getAllIn(VisuPath scope) {
        if(!mayContainFilesIn(scope))
            return emptySet;
        NavigableMap<File, VisuFolder> tailMap = tailMap(scope.getFile(), false);
        VisuPath lastKey = null;
        VisuPath newBase = null;
        for(VisuFolder folder : tailMap.values()) {
            newBase = updateBase(newBase, folder.getPath());
            if(!scope.contains(folder.getPath())) {
                lastKey = folder.getPath();
                break;
            }
        }
        return new FolderSet(lastKey == null ? tailMap : tailMap.headMap(lastKey.getFile()), newBase);
    }

    public boolean mayContain(File f) {
        return base != null && (base.isSystemRoot() || f.getAbsolutePath().contains(base.toString()));
    }

    public boolean mayContain(VisuPath p) {
        return base != null && base.contains(p);
    }

    public boolean mayContainFilesIn(File f) {
        return base != null && (base.isSystemRoot() || f.getAbsolutePath().contains(base.toString()) || base.toString().equals(f.getAbsolutePath()));
    }

    public boolean mayContainFilesIn(VisuPath p) {
        return base != null && (base.contains(p) || base.equals(p));
    }

    @Override
    public VisuFolder remove(Object key) {
        File file;
        if(!(key instanceof File)) {
            file = new File(key.toString());
        } else {
            file = (File) key;
        }
        if(!super.containsKey(file)) {
            File top = file;
            TreeMap<File, VisuFolder> keep = new TreeMap<>();
            while(!super.containsKey(top = top.getParentFile())) {
                for(VisuThing thing : get(top).contents()) {
                    if(thing instanceof VisuFolder) {
                        VisuFolder folder = (VisuFolder) thing;
                        if(!file.toString().contains(folder.getFile().toString())) {
                            keep.put(folder.getFile(), folder);
                        }
                    }
                }
            }
            putAll(keep);
            return super.remove(top);
        } else {
            return super.remove(file);
        }
    }

    public void removeAllIn(VisuPath scope) {
        Iterator<VisuFolder> iterator = this.tailMap(scope.getFile(), false).values().iterator();
        while(iterator.hasNext() && scope.contains(iterator.next().getPath())) {
            iterator.remove();
        }
    }

    private static VisuPath updateBase(VisuPath currentBase, VisuPath addition) {
        if(currentBase == null) {
            return addition.getParent();
        } else if(!currentBase.contains(addition)) {
            return currentBase.getCommonBase(addition);
        } else {
            return currentBase;
        }
    }
}
