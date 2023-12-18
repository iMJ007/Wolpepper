package com.eclectik.wolpepper.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eclectik.wolpepper.GlideApp;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.dataStructures.Papers;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;

/**
 * Created by mj on 5/7/17.
 */

public class MuzeiListItemsAdapter  extends  RecyclerView.Adapter<MuzeiListItemsAdapter.MuzeiListItemsViewHolder>{

    private Context context;
    private ArrayList<Papers> papersDataSet = new ArrayList<>();
    private String listName = "";

    public MuzeiListItemsAdapter(String listName) {
        this.listName = listName;
    }

    @Override
    public MuzeiListItemsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.muzei_list_item_row, parent, false);
        return new MuzeiListItemsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MuzeiListItemsViewHolder holder, int position) {
        Papers currentPaper = papersDataSet.get(position);

        GlideApp.with(context).load(currentPaper.getFullImageUrl().concat("&w=500")).format(DecodeFormat.PREFER_RGB_565).transition(new DrawableTransitionOptions().crossFade(200)).into(holder.wallpaper);

    }

    @Override
    public int getItemCount() {
        if (papersDataSet!=null){
            return papersDataSet.size();
        }
        return 0;
    }

    /**
     * Method to update the dataSet of this adapter
     * @param dataSet - ArrayList of the data set
     */
    public void updateDataSet(ArrayList<Papers> dataSet){
        papersDataSet = dataSet;
    }

    /**
     * Method to delete wallpaper from this list
     * @param index - The position of wallpaper in list (ArrayList)
     */
    public void deleteItem(int index){
        UtilityMethods.removeImageFromSelectedMuzeiList(context, listName, papersDataSet.get(index).getImageId());
        Alerter.create((Activity)context)
                .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                .setTextAppearance(R.style.alertBody)
                .setTitleAppearance(R.style.alertTitle)
                .setText(context.getString(R.string.image_deleted_from_list)).setBackgroundColorRes(R.color.colorAccent).show();
        papersDataSet.remove(index);
        context.getSharedPreferences(context.getString(R.string.muzei_active_list_base_prefs_key), MODE_PRIVATE).edit().putBoolean(context.getString(R.string.is_active_list_changed_pref), true).apply();
    }

    /**
     * View holder class for this adapter
     */
    public class MuzeiListItemsViewHolder extends RecyclerView.ViewHolder{

        ImageView wallpaper;
        View mView;

        public MuzeiListItemsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            wallpaper = mView.findViewById(R.id.wallpaper);
        }
    }
}
