package com.eclectik.wolpepper.activities.settingsActivities;

import static com.eclectik.wolpepper.utils.ConstantValues.IntentKeys.ADAPTER_POSITION;
import static com.eclectik.wolpepper.utils.ConstantValues.IntentKeys.LIST_NAME;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.adapters.MuzeiListItemsAdapter;
import com.eclectik.wolpepper.dataStructures.MuzeiList;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.tapadoo.alerter.Alerter;

public class EditMuzeiListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MuzeiListItemsAdapter adapter;
    private MuzeiList muzeiList;
    private String listName;
    private int previousActivtyAdapterPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_muzei_list);

        Alerter.create(this)
                .setTextTypeface(ResourcesCompat.getFont(this, R.font.spacemono_regular))
                .setTitleTypeface(ResourcesCompat.getFont(this, R.font.spacemono_bold))
                .setTextAppearance(R.style.alertBody)
                .setTitleAppearance(R.style.alertTitle)
                .setText(getString(R.string.muzei_swipe_to_delete_notification))
                .setBackgroundColorRes(R.color.colorAccent)
                .setDuration(3000)
                .show();

        recyclerView = findViewById(R.id.muzei_list_content);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(layoutManager);

        listName = getIntent().getStringExtra(LIST_NAME);
        previousActivtyAdapterPosition = getIntent().getIntExtra(ADAPTER_POSITION, -1);

        adapter = new MuzeiListItemsAdapter(listName);

        recyclerView.setAdapter(adapter);

        setUpSwipeAction();

        muzeiList = UtilityMethods.getMuzeiList(this, listName);

        if (muzeiList != null) {
            adapter.updateDataSet(muzeiList.getListOfPapersInList());
        }

    }


    /**
     * Method to implement swipe to delete gesture on recycler view
     */
    private void setUpSwipeAction() {

        ItemTouchHelper.SimpleCallback simpleCallbackTouchHelper = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int index = viewHolder.getAdapterPosition();
                adapter.deleteItem(index);
                adapter.notifyItemRemoved(index);
                if (adapter.getItemCount() == 0) {
                    UtilityMethods.deleteMuzeiList(EditMuzeiListActivity.this, listName);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", previousActivtyAdapterPosition);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }

        };

        ItemTouchHelper swipeHelper = new ItemTouchHelper(simpleCallbackTouchHelper);

        swipeHelper.attachToRecyclerView(recyclerView);
    }

}
