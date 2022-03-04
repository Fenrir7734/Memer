package com.fenrir.Memer.api.entity;

public class ImageData {
    private final String site;
    private final String postURL;
    private final String imageURL;
    private final String author;
    private final String title;
    private final String source;
    private final boolean nfsw;

    public ImageData(
            String site,
            String postUrl,
            String imageURL,
            String author,
            String title,
            String source,
            boolean nfsw
    ) {
        this.site = site;
        this.imageURL = imageURL;
        this.postURL = postUrl;
        this.author = author;
        this.title = title;
        this.source = source;
        this.nfsw = nfsw;
    }

    public String getSite() {
        return site;
    }

    public String getPostURL() {
        return postURL;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public boolean isNFSW() {
        return nfsw;
    }

    @Override
    public String toString() {
        return "ImageData{" +
                "imageURL='" + imageURL + '\'' +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", source='" + source + '\'' +
                ", nfsw=" + nfsw +
                '}';
    }
}
