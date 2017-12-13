package br.ufpe.cin.if710.podcast.ui.adapter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import br.ufpe.cin.if710.podcast.Extras.PodcastItemCurrentState;
import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.listeners.PodcastItemClickListener;

public class XmlFeedAdapter extends ArrayAdapter<ItemFeed> {

    int linkResource;
    PodcastItemClickListener listener;
    List<ItemFeed> feed;
    private String labelEscutar;
    private String labelBaixar;
    private String labelBaixando;
    private String labelEscutando;
    private File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);

    public XmlFeedAdapter(Context context, int resource, List<ItemFeed> objects, PodcastItemClickListener l) {
        super(context, resource, objects);
        linkResource = resource;
        feed = objects;
        listener = l;
        labelEscutar = context.getString(R.string.action_listen);
        labelBaixar = context.getString(R.string.action_download);
        labelBaixando = context.getString(R.string.action_downloading);
        labelEscutando = context.getString(R.string.action_playing);
    }

    static class ViewHolder {
        TextView item_title;
        TextView item_date;
        Button button;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(getContext(), linkResource, null);
            holder = new ViewHolder();
            holder.item_title = (TextView) convertView.findViewById(R.id.item_title);
            holder.item_date = (TextView) convertView.findViewById(R.id.item_date);
            holder.button = (Button) convertView.findViewById(R.id.item_action);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.item_title.setText(getItem(position).getTitle());
        holder.item_date.setText(getItem(position).getPubDate());

        //Todos os itens iniciam com estado INTHECLOUD
        //Se já existe um arquivo então o estado é alterado para DOWNLOADED
        ItemFeed currentItem = feed.get(position);
        PodcastItemCurrentState itemCurrentState = currentItem.getCurrentState();

        if(itemCurrentState == PodcastItemCurrentState.INTHECLOUD){
            String episodeFileUri = currentItem.getDownloadLink();
            String[] temp = episodeFileUri.split("/");
            String fileName = temp[temp.length-1];

            File output = new File(root,fileName);
            if (output.exists()) {
                itemCurrentState = PodcastItemCurrentState.DOWNLOADED;
                currentItem.setCurrentState(itemCurrentState);
            }
        }

        //Setando o texto do botão do item de acordo com o estado
        switch (itemCurrentState){
            case INTHECLOUD:
                holder.button.setText(labelBaixar);
                break;
            case DOWNLOADING:
                holder.button.setText(labelBaixando);
                break;
            case DOWNLOADED:
                holder.button.setText(labelEscutar);
                break;
            case PLAYING:
                holder.button.setText(labelEscutando);
                break;
        }

        //holder.button.setText( ( episodeFileUri == null || episodeFileUri.isEmpty() ) ? labelBaixar : labelEscutar);

        holder.item_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.userRequestedEpisodeDetails(position);
            }
        });
        holder.item_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.userRequestedEpisodeDetails(position);
            }
        });
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.userRequestedPodcastItemAction(feed.get(position).getCurrentState(),position);
            }
        });

        return convertView;
    }
}