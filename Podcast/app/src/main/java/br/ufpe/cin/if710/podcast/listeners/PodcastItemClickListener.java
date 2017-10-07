package br.ufpe.cin.if710.podcast.listeners;

/**
 * Created by acpr on 07/10/17.
 */

public interface PodcastItemClickListener {
    void userRequestedPodcastItemAction(String currentTitle, int position);
    void userRequestedEpisodeDetails(int position);
}
