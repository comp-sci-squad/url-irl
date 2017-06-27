package comp_sci_squad.com.github.url_irl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class ListURLsActivity extends AppCompatActivity implements UriAdapter.ListItemClickListener {

    // TODO: 6/23/17 add loading bar, spinning shit, to be able to be uesd 
    // TODO: 6/23/17 listview -> clickable, list adapter view
    // TODO: 6/23/17 how to access uri thats in listview to open
    // TODO: 6/23/17 to do title bar, menus
    // TODO: 6/23/17 do polish

    private static final String URI_ARRAY_LIST = "comp_sci_squad.com.github.url_irl.uri_array_true";

    private UriAdapter mAdapter;
    private RecyclerView mList;
    private ArrayList<String> mUriList;
    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_urls);

        mUriList = getIntent().getStringArrayListExtra(URI_ARRAY_LIST);

        mList = (RecyclerView) findViewById(R.id.rv_id);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mList.setLayoutManager(layoutManager);
        mList.setHasFixedSize(true);

        mAdapter = new UriAdapter(this);
        mList.setAdapter(mAdapter);
    }

    public static Intent newIntent(Context packageContext, ArrayList<String> stringList) {
        Intent i = new Intent(packageContext, ListURLsActivity.class);
        i.putExtra(URI_ARRAY_LIST, stringList);

        return i;
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Intent i = new Intent(Intent.ACTION_VIEW, mAdapter.getUri(clickedItemIndex));
        startActivity(i);
    }
}
