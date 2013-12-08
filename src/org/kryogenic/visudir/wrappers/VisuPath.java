package org.kryogenic.visudir.wrappers;

import org.kryogenic.visudir.VisuDir;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author: Kale
 * @date: 02/09/13
 */
public final class VisuPath implements Iterable<String>, Comparable<VisuPath>, Serializable {

    private final String[] components;
    private final char drive;
    public final VisuPath relativeTo;

    public boolean isBad() {
        return components.length > 1;
    }

    // Constructors

    public VisuPath() { // system root
        this((char) 0, null);
    }

    public VisuPath(File f) {
        this(f.getAbsolutePath(), null);
    }

    public VisuPath(String path) {
        this(path, null);
    }

    public VisuPath(String path, VisuPath relativeTo) {
        if(path.charAt(0) != VisuDir.FS.charAt(0) && (path.length() == 1 || path.charAt(1) != ':')) {
            path = VisuDir.FS + path;
        }
        int count = getComponentCount(path);
        this.components = new String[count];
        if(path.contains(":")) {
            this.drive = path.toLowerCase().charAt(0);
        } else {
            this.drive = 0;
        }
        int last = -1;
        for(int i = 0; i < count; i++) {
            int idx = path.indexOf(VisuDir.FS, last + 1);
            int next = path.indexOf(VisuDir.FS, idx + 1);
            if(next < 0) {
                this.components[i] = path.substring(idx + 1);
                break;
            }
            last = idx;
            this.components[i] = path.substring(idx + 1, next);
        }
        this.relativeTo = relativeTo;
        /*System.out.println("Created a VisuPath..." + toString());
        if(relativeTo != null) {
            System.out.println("Components... " + Arrays.toString(components));
            System.out.println("Backed by..." + relativeTo.toString());
        }*/
    }

    public VisuPath(char drive, VisuPath relativeTo, Object... components) {
        int length = 0;
        for(Object o : components)
            length += o instanceof String ? 1 : o instanceof String[] ? ((String[]) o).length : 0;

        this.components = new String[length];

        int i = 0;
        for(final Object o : components){
            if(o instanceof String)
                this.components[i++] = (String) o;
            else if(o instanceof String[])
                for(final String s : (String[]) o)
                    this.components[i++] = s;
        }
        this.drive = Character.toLowerCase(drive);
        this.relativeTo = relativeTo;
    }

    // Static methods

    public static int getComponentCount(String path) {
        int count = 0;
        char[] charArray = path.toCharArray();
        for(int i = 0; i < charArray.length; i++) {
            if(charArray[i] == VisuDir.FS.charAt(0) && !path.substring(i + 1).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    // Private methods
    private String[] getComponents() {
        if(relativeTo == null) {
            return this.components;
        } else {
            String[] components = new String[this.getLength()];
            int i = 0;
            for(int j = 0; j < relativeTo.getLength(); i++, j++) {
                components[i] = relativeTo.getComponents()[j];
            }
            for(int j = 0; j < this.components.length; i++, j++) {
                components[i] = this.components[j];
            }
            return components;
        }
    }
    /**
     * Assumes that <tt>base</tt> actually does contain <tt>this</tt>
     * @param base the VisuPath to back this path with
     * @return a VisuPath that equal to this one, but is relative to <tt>base</tt> (internally it is backed by <tt>base</tt>
     */
    private VisuPath getRelativeTo(VisuPath base) {
        return new VisuPath(drive, base, Arrays.copyOfRange(getComponents(), base.getLength(), getLength() - 1));
    }

    // This weird thing...

    public boolean contains(VisuPath path) {
        if(isSystemRoot()) {
            return true;
        } else if(path.getLength() <= this.getLength()) {
            return false;
        }
        String[] thisComponents = this.getComponents();
        String[] pathComponents = path.getComponents();
        for(int i = 0; i < this.getLength(); i++) { // check all components in "this" against the first components in "path"
            if(!thisComponents[i].equals(pathComponents[i])) {
                return false;
            }
        }
        return true;
    }

    // "Getters" for various things

    public VisuPath getAt(int idx) {
        String[] list = getFile().list();
        if(isSystemRoot()) {
            return new VisuPath(File.listRoots()[idx].getAbsolutePath().charAt(0), null);
        } else if(list != null && list.length > 0) {
            if(list.length > idx) {
                return new VisuPath(drive, this, list[idx]);
            } else {
                VisuDir.display().refresh();
            }
        }
        return null;
    }
    public VisuPath getCommonBase(VisuPath path) {
        if(!path.isDrive() && this.getDrive() == path.getDrive()) {
            StringBuilder sb = new StringBuilder(getDriveName());
            String[] thisComponents = this.getComponents();
            String[] pathComponents = path.getComponents();
            for(int i = 0; i < Math.min(this.getLength() - 1, path.getLength() - 1); i++) {
                if(thisComponents[i].equals(pathComponents[i])) {
                    sb.append(thisComponents[i]).append(VisuDir.FS);
                } else {
                    break;
                }
            }
            System.out.println("New Common Base: " + sb.toString());
            VisuPath commonBase = new VisuPath(sb.toString());
            VisuPath relativeTo = null;
            if(this.relativeTo != null && commonBase.contains(this.relativeTo)) {
                relativeTo = this.relativeTo;
            }
            if(path.relativeTo != null && commonBase.contains(path.relativeTo)) {
                if(relativeTo == null || path.relativeTo.getLength() > relativeTo.getLength()) {
                    relativeTo = this.relativeTo;
                }
            }
            if(relativeTo == null) {
                return commonBase;
            } else {
                return commonBase.getRelativeTo(relativeTo);
            }
        } else {
            return new VisuPath();
        }
    }
    public char getDrive() {
        if(relativeTo != null) {
            return relativeTo.getDrive();
        } else {
            return drive;
        }
    }
    public String getDriveName() {
        return getDrive() + ":" + VisuDir.FS;
    }
    public File getFile() {
        return new File(toString());
    }
    public int getLength() {
        if(relativeTo == null) {
            return components.length;
        } else {
            return relativeTo.getLength() + components.length;
        }
    }
    public VisuPath getParent() {
        if(isSystemRoot()) {
            return null;
        } else if(isDrive()) {
            return new VisuPath();
        } else {
            if(relativeTo != null) {
                if(components.length == 1) {
                    return relativeTo;
                }
                return new VisuPath(drive, relativeTo, Arrays.copyOf(components, components.length - 1));
            } else {
                return new VisuPath(drive, null, Arrays.copyOf(components, components.length - 1));
            }
        }
    }
    public String getShortName() {
        if(isDrive()) {
            return (getDriveName()).toUpperCase();
        } else {
            VisuPath path = this;
            do {
                if(path.components.length > 0)
                    return components[components.length - 1];
            } while((path = path.relativeTo) != null);
            return "System root";
        }
    }
    public boolean hasDrive() {
        return drive != 0;
    }
    public boolean isDrive() {
        return hasDrive() && !isRelative() && components.length == 0;
    }
    public boolean isRelative() {
        return relativeTo != null && !relativeTo.isSystemRoot();
    }
    public boolean isSystemRoot() {
        return !hasDrive() && !isRelative() && components.length == 0;
    }

    // Superclass methods

    @Override
    public int compareTo(VisuPath path) {
        return path.toString().compareTo(this.toString());
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof String) {
            return this.toString().equals(o);
        } else if(o instanceof File) {
            return this.getFile().equals(o);
        } else if(o instanceof VisuPath) {
            return o.toString().equals(this.toString());
        }
        return false;
    }

    public Iterator<String> iterator() {
        return iterator(0);
    }
    public Iterator<String> iterator(final int start) {
        return new Iterator<String>() {
            String[] components = getComponents();
            int i = start < 0 ? 0 : start;

            @Override
            public boolean hasNext() {
                return i < getLength();
            }
            @Override
            public String next() {
                i++;
                return components[i - 1];
            }
            @Override
            public void remove() {
            }
        };
    }

    @Override
    public String toString() {
        return toString(getLength());
    }
    public String toString(int depth) {
        if(isSystemRoot()) {
            return "System Root";
        }
        StringBuilder sb = new StringBuilder(getDriveName());
        String[] thisComponents = getComponents();
        for(int i = 0; i < depth; i++) {
            sb.append(thisComponents[i]).append(VisuDir.FS);
        }
        return sb.toString();
    }

    public void printFull() {
        System.out.println("|--VisuPath--|");
        System.out.println(toString());
        System.out.println("Drive: " + getDriveName());
        System.out.println("Components: " + Arrays.toString(components));
        String oneSpaces = "    ";
        String spaces = oneSpaces;
        VisuPath relativeTo = this.relativeTo;
        while(relativeTo != null) {
            System.out.println("Relative to...");
            System.out.println(spaces + relativeTo.toString());
            System.out.println(spaces + "Drive: " + relativeTo.getDriveName());
            System.out.println(spaces + "Components: " + Arrays.toString(relativeTo.components));
            spaces += oneSpaces;
            relativeTo = relativeTo.relativeTo;
        }
        System.out.println("--------------");
    }
}
