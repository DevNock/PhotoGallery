package com.google.photogallery;

/**
 * Created by Sergey on 06.02.2016.
 */
public class GalleryItem {

    private String caption;
    private String id;
    private String url;

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String toString(){
        return caption;
    }
}
