package com.eclectik.wolpepper.adapters;

import static android.content.Context.MODE_PRIVATE;
import static com.eclectik.wolpepper.utils.ConstantValues.IntentKeys.ACTIVITY_RESULT;
import static com.eclectik.wolpepper.utils.ConstantValues.IntentKeys.ADAPTER_POSITION;
import static com.eclectik.wolpepper.utils.ConstantValues.IntentKeys.LIST_NAME;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.activities.settingsActivities.EditMuzeiListActivity;
import com.eclectik.wolpepper.dataStructures.MuzeiList;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by mj on 4/7/17.
 **/

public class MuzeiAvailableListAdapter extends RecyclerView.Adapter<MuzeiAvailableListAdapter.MuzeiListViewHolder>{

    private ArrayList<MuzeiList> muzeiListDataSet = new ArrayList<>();

    private Context context;

    private int activeListIndex = -1;

    private boolean isInitializationLoad = true;

    @Override
    public MuzeiListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.muzei_list_row, parent, false);
        return new MuzeiListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MuzeiListViewHolder holder, int position) {
        final MuzeiList muzeiList = muzeiListDataSet.get(position);

        holder.listTitle.setText(muzeiList.getListName());

        int sizeOfList = muzeiList.getListOfPapersInList().size();
        String imageUrl = "";
        if (sizeOfList > 0) {
            imageUrl = muzeiList.getListOfPapersInList().get(new Random().nextInt(sizeOfList)).getFullImageUrl().concat("&w=400");
        }

        if (isInitializationLoad) {
            Glide.with(context).load(imageUrl).format(DecodeFormat.PREFER_RGB_565).into(holder.listCoverImage);
        }

        if (muzeiList.isActive()){
            activeListIndex = holder.getAdapterPosition();
            holder.isActiveIndicatorView.setVisibility(View.VISIBLE);
            holder.setListAsActive.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        } else {
            holder.isActiveIndicatorView.setVisibility(View.GONE);
            holder.setListAsActive.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        }

        deleteClickListener(holder);

        holder.listCoverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditMuzeiListActivity.class);
                intent.putExtra(LIST_NAME, muzeiList.getListName());
                intent.putExtra(ADAPTER_POSITION, holder.getAdapterPosition());
                ((Activity)context).startActivityForResult(intent, ACTIVITY_RESULT);
            }
        });

        holder.setListAsActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.getSharedPreferences(context.getString(R.string.muzei_active_list_base_prefs_key), MODE_PRIVATE).edit().putBoolean(context.getString(R.string.is_active_list_changed_pref), true).apply();
                UtilityMethods.setListAsActiveMuzeiList(context, muzeiList.getListName());
                muzeiListDataSet.get(activeListIndex).setActive(false);
                muzeiList.setActive(true);

                activeListIndex = holder.getAdapterPosition();

                isInitializationLoad = false;

                notifyDataSetChanged();
                Alerter.create((Activity)context)
                        .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setTitle(context.getString(R.string.muzei_this_list_will_used_now_title))
                        .setText(context.getString(R.string.muzei_this_list_will_used_now))
                        .setBackgroundColorRes(R.color.colorAccent)
                        .show();
            }
        });
    }

    public void removeItem(int position){
        muzeiListDataSet.remove(position);
        notifyDataSetChanged();
    }


    private void deleteClickListener(final MuzeiListViewHolder holder){
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilityMethods.deleteMuzeiList(context, muzeiListDataSet.get(holder.getAdapterPosition()).getListName());

                Alerter.create((Activity)context)
                        .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setBackgroundColorRes(R.color.colorAccent)
                        .setText(muzeiListDataSet.get(holder.getAdapterPosition()).getListName() + context.getString(R.string.muzei_list_deleted))
                        .show();

                muzeiListDataSet.remove(holder.getAdapterPosition());
                notifyDataSetChanged();

            }
        });
    }

    /**
     * Method to update data set of this adapter
     * @param dataSet - Arraylist of updated data set
     */
    public void updateDataSet(ArrayList<MuzeiList> dataSet){
        muzeiListDataSet = dataSet;
    }

    @Override
    public int getItemCount() {
        if (muzeiListDataSet != null){
            return muzeiListDataSet.size();
        }
        return 0;
    }

    /**
     * Muzei List View Holder Class
     */
    public class MuzeiListViewHolder extends RecyclerView.ViewHolder{

        View mView;
        View isActiveIndicatorView;
        View setListAsActive;
        ImageView listCoverImage;
        ImageView deleteButton;
        TextView listTitle;

        public MuzeiListViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            listCoverImage = mView.findViewById(R.id.list_cover_image_view);
            deleteButton = mView.findViewById(R.id.delete_list_button);
            listTitle = mView.findViewById(R.id.list_name_tv);
            isActiveIndicatorView = mView.findViewById(R.id.active_list_indicator);
            setListAsActive = mView.findViewById(R.id.set_list_as_active_button);
        }
    }
}
