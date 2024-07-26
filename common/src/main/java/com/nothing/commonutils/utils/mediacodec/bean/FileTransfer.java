package com.nothing.commonutils.utils.mediacodec.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class FileTransfer implements Parcelable {
    public static final Creator<FileTransfer> CREATOR = new Creator<FileTransfer>() { // from class: com.nothing.commonutils.utils.mediacodec.bean.FileTransfer.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public FileTransfer createFromParcel(Parcel parcel) {
            return new FileTransfer(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public FileTransfer[] newArray(int i) {
            return new FileTransfer[i];
        }
    };
    private int fileId;
    private String path;
    private long progress;
    private long size;
    private int state;
    private int type;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public FileTransfer(String str, int i, int i2, int i3, long j, long j2) {
        this.path = str;
        this.fileId = i;
        this.state = i2;
        this.type = i3;
        this.progress = j;
        this.size = j2;
    }

    public FileTransfer(int i) {
        this.type = i;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String str) {
        this.path = str;
    }

    public int getFileId() {
        return this.fileId;
    }

    public void setFileId(int i) {
        this.fileId = i;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int i) {
        this.state = i;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int i) {
        this.type = i;
    }

    public long getProgress() {
        return this.progress;
    }

    public void setProgress(long j) {
        this.progress = j;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long j) {
        this.size = j;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.path);
        parcel.writeInt(this.fileId);
        parcel.writeInt(this.state);
        parcel.writeInt(this.type);
        parcel.writeLong(this.progress);
        parcel.writeLong(this.size);
    }

    protected FileTransfer(Parcel parcel) {
        this.path = parcel.readString();
        this.fileId = parcel.readInt();
        this.state = parcel.readInt();
        this.type = parcel.readInt();
        this.progress = parcel.readLong();
        this.size = parcel.readLong();
    }
}
