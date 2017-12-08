package br.ufpe.cin.if710.podcast.ui.adapter;

import java.util.List;
import android.content.Context;
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

    /**
     * public abstract View getView (int position, View convertView, ViewGroup parent)
     * <p>
     * Added in API level 1
     * Get a View that displays the data at the specified position in the data set. You can either create a View manually or inflate it from an XML layout file. When the View is inflated, the parent View (GridView, ListView...) will apply default layout parameters unless you use inflate(int, android.view.ViewGroup, boolean) to specify a root view and to prevent attachment to the root.
     * <p>
     * Parameters
     * position	The position of the item within the adapter's data set of the item whose view we want.
     * convertView	The old view to reuse, if possible. Note: You should check that this view is non-null and of an appropriate type before using. If it is not possible to convert this view to display the correct data, this method can create a new view. Heterogeneous lists can specify their number of view types, so that this View is always of the right type (see getViewTypeCount() and getItemViewType(int)).
     * parent	The parent that this view will eventually be attached to
     * Returns
     * A View corresponding to the data at the specified position.
     */


	/*
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.itemlista, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.item_title);
		textView.setText(items.get(position).getTitle());
	    return rowView;
	}
	/**/

    //http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
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

        //String episodeFileUri = feed.get(position).getFileUri();
        PodcastItemCurrentState state = feed.get(position).getCurrentState();

        switch (state){
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