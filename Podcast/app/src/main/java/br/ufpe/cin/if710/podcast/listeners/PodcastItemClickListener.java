package br.ufpe.cin.if710.podcast.listeners;

import br.ufpe.cin.if710.podcast.Extras.PodcastItemCurrentState;

/**
 * Created by acpr on 07/10/17.
 */

public interface PodcastItemClickListener {
    void userRequestedPodcastItemAction(PodcastItemCurrentState currentState, int position);
    void userRequestedEpisodeDetails(int position);
}
