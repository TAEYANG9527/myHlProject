package com.itcalf.renhe.context.imageselector;

import java.io.Serializable;

/**
 * Image bean
 * Created by Yancy on 2015/12/2.
 */
public class Image implements Serializable{

    public String path;
    public String name;
    public long time;

    public Image(String path, String name, long time) {
        this.path = path;
        this.name = name;
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        try {
            Image other = (Image) o;
            return this.path.equalsIgnoreCase(other.path);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }
}