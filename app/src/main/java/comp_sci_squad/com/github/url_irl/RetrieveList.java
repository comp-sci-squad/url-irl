package comp_sci_squad.com.github.url_irl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

/**
 * This class is the activity that will displays the result of reading the image
 */
public class RetrieveList extends Activity implements TextAdapter.TextAdapterOnClickHandler {
    private static final String EXTRA_LIST = "listTester";

    RecyclerView mRecyclerView;
    TextAdapter mTextAdapter;

    public String[] stringList;
    public RetrieveList() {}
    @Override
    public void onCreate(Bundle onInstanceState)
    {
        super.onCreate(onInstanceState);
        setContentView(R.layout.retrieve_list_view);

        // Initialize the RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_retrieve_list);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        // Create a TextAdapter to handle the String array
        mTextAdapter = new TextAdapter(this);
        stringList =  getIntent().getStringArrayExtra(EXTRA_LIST);
        mRecyclerView.setAdapter(mTextAdapter);

        // TextAdapter will now handle the String Array
        mTextAdapter.setmTextList(stringList);
    }

    @Override
    public void onClick(String text) {
    }

    /**
     The newIntent function allows for a different activity to send an arraylist of strings
     to the retrieve_list_view activity. Hence, when this file opens, the information will be sent to
     this class.
     */
    public Intent newIntent(Context packageContext, String[] list)
    {
        Intent i = new Intent(packageContext, RetrieveList.class);
        i.putExtra(EXTRA_LIST, list);
        return i;
    }

}
