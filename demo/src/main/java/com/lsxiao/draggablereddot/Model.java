package com.lsxiao.draggablereddot;

/**
 * author:lsxiao
 * date:2016/01/04 18:14
 */
public class Model {
    private boolean read;

    public Model(boolean read) {
        this.read = read;
    }

    public Model() {
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
