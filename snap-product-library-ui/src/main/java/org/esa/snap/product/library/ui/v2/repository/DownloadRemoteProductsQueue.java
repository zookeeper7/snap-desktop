package org.esa.snap.product.library.ui.v2.repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcoravu on 13/9/2019.
 */
public class DownloadRemoteProductsQueue {

    private List<RemoteProductDownloader> queue;
    private int totalPushed;

    public DownloadRemoteProductsQueue() {
        clear();
    }

    public RemoteProductDownloader peek() {
        if (this.queue.size() > 0) {
            return this.queue.get(0);
        }
        return null;
    }

    public RemoteProductDownloader pop() {
        if (this.queue.size() > 0) {
            return this.queue.remove(0);
        }
        return null;
    }

    public void push(RemoteProductDownloader remoteProductDownloader) {
        this.queue.add(remoteProductDownloader);
        this.totalPushed++;
    }

    public int getSize() {
        return this.queue.size();
    }

    public void clear() {
        this.queue = new ArrayList<>();
        this.totalPushed = 0;
    }

    public int getDownloadedProductCount() {
        return this.totalPushed - this.queue.size();
    }

    public int getTotalPushed() {
        return totalPushed;
    }
}
