package comp_sci_squad.com.github.url_irl;

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
    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

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
        UriViewHolder viewHolder = new UriViewHolder(view);

        return viewHolder;
    }

    public void onBindViewHolder(UriViewHolder holder, int position) {
        holder.listItemView.setText(uriArray.get(position).toString());
    }

    public int getItemCount() {
        if (uriArray != null)
            return uriArray.size();
        else
            return 0;
    }


     class UriViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView listItemView;

        public UriViewHolder(View itemView) {
            super(itemView);
            listItemView = (TextView) itemView.findViewById(R.id.textViewUrl);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}
