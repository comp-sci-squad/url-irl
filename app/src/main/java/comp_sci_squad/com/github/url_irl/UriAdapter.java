package comp_sci_squad.com.github.url_irl;

import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by matt on 6/25/17.
 */

public class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder>{

    final private ListItemClickListener mOnClickListener;
    private ArrayList<Uri> uriArray;
    public UriAdapter(ListItemClickListener listener) {
        mOnClickListener = listener;
    }

    public void setArray(ArrayList<Uri> newUriArray) {
        uriArray = newUriArray;
        notifyDataSetChanged();
    }

    public Uri getUri(int position) {
        return uriArray.get(position);
    }

    @Override
    public UriViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.content_list_urls;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        return new UriViewHolder(view);
    }

    public void onBindViewHolder(UriViewHolder holder, int position) {
        Uri u = uriArray.get(position);
        String text = u.getHost() + u.getPath();
        holder.listItemView.setText(text);
    }

    public int getItemCount() {
        if (uriArray != null)
            return uriArray.size();
        else
            return 0;
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);

        void onShareButtonClick(int clickedItemIndex);

        void onSearchButtonClick(int clickedItemIndex);

        void onListItemLongClick(int clickedItemIndex);
    }

     class UriViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
         TextView listItemView;
         TextView shareButton;
         TextView searchButton;

         public UriViewHolder(View itemView) {
             super(itemView);
             listItemView = (TextView) itemView.findViewById(R.id.textViewUrl);
             listItemView.setOnClickListener(this);
             listItemView.setOnLongClickListener(this);

             shareButton = (TextView) itemView.findViewById(R.id.text_view_share_button);
             shareButton.setOnClickListener(this);

             searchButton = (TextView) itemView.findViewById(R.id.text_view_search_button);
             searchButton.setOnClickListener(this);
         }

         @Override
          public void onClick(View v) {
             int clickedPosition = getAdapterPosition();
             if (v.getId() == R.id.textViewUrl)
                mOnClickListener.onListItemClick(clickedPosition);
             else if (v.getId() == R.id.text_view_share_button)
                 mOnClickListener.onShareButtonClick(clickedPosition);
             else if (v.getId() == R.id.text_view_search_button)
                 mOnClickListener.onSearchButtonClick(clickedPosition);
         }

         @Override
         public boolean onLongClick(View v) {
             int clickedPosition = getAdapterPosition();
             if (v.getId() == R.id.textViewUrl) {
                 mOnClickListener.onListItemLongClick(clickedPosition);
                 return true;
             }
             return false;
         }
     }
}
