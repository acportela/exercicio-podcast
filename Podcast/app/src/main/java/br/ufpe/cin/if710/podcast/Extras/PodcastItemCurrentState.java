package br.ufpe.cin.if710.podcast.Extras;

/**
 * Created by acpr on 08/12/17.
 */

public enum PodcastItemCurrentState {

        INTHECLOUD(0),
        DOWNLOADING(1),
        DOWNLOADED(2),
        PLAYING(3);

        private final int state;


        PodcastItemCurrentState(int state) {
            this.state = state;
        }
        public int getState() {
            return state;
        }
}
