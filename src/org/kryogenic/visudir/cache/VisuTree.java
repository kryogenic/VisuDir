package org.kryogenic.visudir.cache;

import org.kryogenic.visudir.VisuDir;
import org.kryogenic.visudir.wrappers.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author: Kale
 * @date: 18/09/13
 */
public class VisuTree {

    private FolderSet cache;
    private ExecutorService es = Executors.newFixedThreadPool(1);
    private Map<VisuPath, Future> futures = new HashMap<>();

    public VisuTree() {
        cache = new FolderSet();
    }

    public FolderSet getCache() {
        return cache;
    }

    public void cache(VisuPath toCache) {
        cache(toCache, false);
    }

    public void cache(final VisuPath toCache, final boolean refresh) {
        for(VisuPath path : futures.keySet()) {
            if(path.contains(toCache)) {
                return;
            }
        }
        if(getCache().containsKey(toCache.getFile())) {
            return;
        }
        Future cacheFuture = es.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    getCache().put(new VisuFolder(toCache));
                    if(refresh && VisuDir.display().focus().equals(toCache)) {
                        VisuDir.display().update();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
        futures.put(toCache, cacheFuture);
    }

    public boolean updating() {
        Iterator<Future> iter = futures.values().iterator();
        while(iter.hasNext()) {
            if(iter.next().isDone()) {
                iter.remove();
            } else {
                return true;
            }
        }
        return false;
    }

    public void refresh(VisuPath focus) {
        getCache().remove(focus.getFile());
        cache(focus, true);
    }
}
