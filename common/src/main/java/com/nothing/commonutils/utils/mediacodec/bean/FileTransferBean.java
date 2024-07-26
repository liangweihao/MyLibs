package com.nothing.commonutils.utils.mediacodec.bean;

public class FileTransferBean {
    private String filePath;
    private boolean isCanceled;
    private boolean isDone;
    private boolean isTransfering;
    private long totalSize;
    private long transferedSize;

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String str) {
        this.filePath = str;
    }

    public long getTotalSize() {
        return this.totalSize;
    }

    public void setTotalSize(long j) {
        this.totalSize = j;
    }

    public long getTransferedSize() {
        return this.transferedSize;
    }

    public void setTransferedSize(long j) {
        this.transferedSize = j;
    }

    public boolean isTransfering() {
        return this.isTransfering;
    }

    public void setTransfering(boolean z) {
        this.isTransfering = z;
    }

    public boolean isDone() {
        return this.isDone;
    }

    public void setDone(boolean z) {
        this.isDone = z;
    }

    public boolean isCanceled() {
        return this.isCanceled;
    }

    public void setCanceled(boolean z) {
        this.isCanceled = z;
    }

    public String toString() {
        return "FileTransferBean{filePath='" + this.filePath + "', totalSize=" + this.totalSize + ", transferedSize=" + this.transferedSize + ", isTransfering=" + this.isTransfering + ", isDone=" + this.isDone + ", isCanceled=" + this.isCanceled + '}';
    }
}
