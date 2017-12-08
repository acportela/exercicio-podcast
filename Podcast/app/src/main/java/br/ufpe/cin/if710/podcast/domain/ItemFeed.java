package br.ufpe.cin.if710.podcast.domain;

import br.ufpe.cin.if710.podcast.Extras.PodcastItemCurrentState;

public class ItemFeed {

    private final String id;

    private String title;
    private String link;
    private String pubDate;
    private String description;
    private String downloadLink;
    private String fileUri;

    private PodcastItemCurrentState currentState;

    public ItemFeed(String title, String link, String pubDate, String description, String downloadLink) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadLink = downloadLink;
        this.id = null;
        this.fileUri = null;
        this.currentState = PodcastItemCurrentState.INTHECLOUD;
    }

    public ItemFeed(String title, String link, String pubDate, String description, String downloadLink, String fileUri, String id) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadLink = downloadLink;
        this.fileUri = fileUri;
        this.id = id;
        this.currentState = PodcastItemCurrentState.INTHECLOUD;
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

    public PodcastItemCurrentState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(PodcastItemCurrentState currentState) {
        this.currentState = currentState;
    }
}