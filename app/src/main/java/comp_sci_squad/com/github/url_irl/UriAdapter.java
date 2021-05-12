package comp_sci_squad.com.github.url_irl;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

  final private ListItemClickListener mOnClickListener;
  private ArrayList<Uri> mURIArray;

  /**
   * Constructs a URIAdapter.
   *
   * @param listener - The class implementing the ListItemClickListener interface listening for
   * clicks
   */
  public UriAdapter(ListItemClickListener listener) {
    mOnClickListener = listener;
  }

  /**
   * Sets the array of the adapter to a list of URLs and notifies it.
   *
   * @param newUriArray - The parsed arraylist of URLs.
   */
  public void setArray(ArrayList<Uri> newUriArray) {
    mURIArray = newUriArray;
    notifyDataSetChanged();
  }

  /**
   * Gets the URL at a given position in the adapter/recycler view.
   *
   * @param position - The position in the adapter.
   * @return - The requested URL.
   */
  public Uri getUri(int position) {
    return mURIArray.get(position);
  }

  /**
   * Creates a view holder for an instance of the layout content_list_urls. Caches the view inside
   * of it. Allows for the recycler view to reuse view holders saving memory.
   *
   * @param viewGroup - The ViewGroup into which the new View will be added after it is bound to an
   * adapter position.
   * @param viewType - The view type of the new View.
   * @return - A new ViewHolder that holds a View of the given view type.
   */
  @NonNull
  @Override
  public UriViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
    Context context = viewGroup.getContext();
    int layoutIdForListItem = R.layout.content_list_urls;
    LayoutInflater inflater = LayoutInflater.from(context);
    return new UriViewHolder(inflater.inflate(layoutIdForListItem, viewGroup, false));
  }

  /**
   * Updates the contents of the current UriViewHolder to hold the URL at the given position.
   *
   * @param holder - The ViewHolder which is to be updated to represent the contents of the URL at
   * the given position in the mURIArray.
   * @param position - The position of the item in the array list.
   */
  public void onBindViewHolder(UriViewHolder holder, int position) {
    Uri u = mURIArray.get(position);
    String text = u.getHost() + u.getPath();
    holder.listItemView.setText(text);
  }

  /**
   * Gets the number of items in the adapter.
   *
   * @return - The size of mUriArray.
   */
  public int getItemCount() {
    if (mURIArray != null) {
      return mURIArray.size();
    } else {
      return 0;
    }
  }

  /**
   * Implementing this interface allows a class to receive events from an instance of this class.
   * Events occur when clicking on the view's elements.
   */
  public interface ListItemClickListener {

    /**
     * Should handle click on the URL.
     *
     * @param clickedItemIndex - The index of the Item in the Adapter that was clicked on.
     */
    void onListItemClick(int clickedItemIndex);

    /**
     * Should handle click on the share button under its url.
     *
     * @param clickedItemIndex - The index of the Item in the Adapter that was clicked on.
     */
    void onShareButtonClick(int clickedItemIndex);

    /**
     * Should handle click on the search button under its url.
     *
     * @param clickedItemIndex - The index of the Item in the Adapter that was clicked on.
     */
    void onSearchButtonClick(int clickedItemIndex);

    /**
     * Should handle long press on the URL. (Copy it to clipboard)
     *
     * @param clickedItemIndex - The index of the Item in the Adapter that was clicked on.
     */
    void onListItemLongClick(int clickedItemIndex);
  }

  class UriViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
      View.OnLongClickListener {

    final TextView listItemView;
    final TextView shareButton;
    final TextView searchButton;

    public UriViewHolder(View itemView) {
      super(itemView);
      listItemView = itemView.findViewById(R.id.textViewUrl);
      listItemView.setOnClickListener(this);
      listItemView.setOnLongClickListener(this);

      shareButton = itemView.findViewById(R.id.text_view_share_button);
      shareButton.setOnClickListener(this);

      searchButton = itemView.findViewById(R.id.text_view_search_button);
      searchButton.setOnClickListener(this);
    }

    /**
     * Calls the respective onListItemClick() listening activity for the view that was clicked on.
     *
     * @param v - The view that was clicked on.
     */
    @Override
    public void onClick(View v) {
      int clickedPosition = getAdapterPosition();
      if (v.getId() == R.id.textViewUrl) {
        mOnClickListener.onListItemClick(clickedPosition);
      } else if (v.getId() == R.id.text_view_share_button) {
        mOnClickListener.onShareButtonClick(clickedPosition);
      } else if (v.getId() == R.id.text_view_search_button) {
        mOnClickListener.onSearchButtonClick(clickedPosition);
      }
    }

    /**
     * Calls onListItemLongClick() on the listening activity if the view was the URL.
     *
     * @param v - The view that was long clicked on.
     * @return - Returns whether or not the event was handled.
     */
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
