package comp_sci_squad.com.github.url_irl;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


class TextAdapter extends RecyclerView.Adapter<TextAdapter.TextAdapterViewHolder>{
    private String[] mTextList;
    private final TextAdapterOnClickHandler mClickHandler;

    interface TextAdapterOnClickHandler {
        void onClick(String text);
    }

    TextAdapter(TextAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    class TextAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView;

        private TextAdapterViewHolder(View view) {
            super(view);
            mTextView  = (TextView) view.findViewById(R.id.tv_text);
            view.setOnClickListener(this);
        }
        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String weatherForDay = mTextList[adapterPosition];
            mClickHandler.onClick(weatherForDay);
        }
    }

    @Override
    public TextAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.retrieve_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        return new TextAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TextAdapterViewHolder holder, int position) {
        String textItem = mTextList[position].trim();
        holder.mTextView.setText(textItem);
    }

    @Override
    public int getItemCount() {
        if (mTextList == null) return 0;
        return  mTextList.length;
    }

    void setmTextList(String[] textList) {
        mTextList = textList;
        notifyDataSetChanged();
    }
}
