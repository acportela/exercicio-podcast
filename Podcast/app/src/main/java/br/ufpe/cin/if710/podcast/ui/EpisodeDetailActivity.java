package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import br.ufpe.cin.if710.podcast.R;

public class EpisodeDetailActivity extends Activity {

    public final String INTENT_DETAILS_TITLE_KEY = "detailTitle";
    public final String INTENT_DETAILS_DATE_KEY  = "detailDate";
    public final String INTENT_DETAILS_DESC_KEY  = "detailDesc";
    public final String INTENT_DETAILS_LINK_KEY  = "detailLink";
    public final String DEFAULT_VALUE = "";

    private TextView title;
    private TextView date;
    private TextView desc;
    private TextView link;
    private LinearLayout layoutLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);

        title = (TextView) findViewById(R.id.detailTitle);
        date = (TextView) findViewById(R.id.detailPubDate);
        desc = (TextView) findViewById(R.id.detailDesc);
        link = (TextView) findViewById(R.id.detailEpisodeLink);
        layoutLink = (LinearLayout) findViewById(R.id.layoutEpisodeLink);


        Bundle extras = getIntent().getExtras();
        if(extras != null){
            title.setText(extras.getString(INTENT_DETAILS_TITLE_KEY,DEFAULT_VALUE));
            date.setText(extras.getString(INTENT_DETAILS_DATE_KEY,DEFAULT_VALUE));
            desc.setText(extras.getString(INTENT_DETAILS_DESC_KEY,DEFAULT_VALUE));
            link.setText(extras.getString(INTENT_DETAILS_LINK_KEY,DEFAULT_VALUE));
        }

        layoutLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String l = link.getText().toString();
                if(!l.isEmpty()){
                    //Chamar browser
                }
            }
        });

    }
}
