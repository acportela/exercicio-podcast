package br.ufpe.cin.if710.podcast.domain;

public class ItemFeed {

    private final String id;

    private final String title;
    private final String link;
    private final String pubDate;
    private final String description;
    private final String downloadLink;
    private final String fileUri;

    public ItemFeed(String title, String link, String pubDate, String description, String downloadLink) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadLink = downloadLink;
        this.id = null;
        this.fileUri = null;
    }

    public ItemFeed(String title, String link, String pubDate, String description, String downloadLink, String fileUri, String id) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadLink = downloadLink;
        this.fileUri = fileUri;
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getDescription() {
        return description;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    @Override
    public String toString() {
        return title;
    }

    public String getFileUri() {
        return fileUri;
    }

    public String getId() {
        return id;
    }
}