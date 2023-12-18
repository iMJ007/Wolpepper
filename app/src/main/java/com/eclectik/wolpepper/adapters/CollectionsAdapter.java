package com.eclectik.wolpepper.adapters;

import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_COVER_PHOTO_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_DATE_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_DESCRIPTION_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_ID_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_IS_CURATED_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_TITLE_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_TOTAL_PHOTOS_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_NAME_OF_USER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USERNAME_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_PROFILE_PIC_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_DOWNLOAD_PATH_STRING;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.eclectik.wolpepper.activities.SingleCollectionsActivity;
import com.eclectik.wolpepper.dataStructures.PaperCollections;
import com.eclectik.wolpepper.databinding.CollectionsRowBinding;
import com.eclectik.wolpepper.listenerInterfaces.RecyclerViewDataUpdateRequester;

import java.util.ArrayList;

/**
 * Created by mj on 13/6/17.
 * <p>
 * Collections Adapter Class
 */

public class CollectionsAdapter extends RecyclerView.Adapter<CollectionsAdapter.CollectionsViewHolder> {
    private Context context;
    private ArrayList<PaperCollections> collectionsDataSet;
    private RecyclerViewDataUpdateRequester dataUpdateRequester;

    public CollectionsAdapter(RecyclerViewDataUpdateRequester dataUpdateRequester, Context context) {
        this.context = context;
        this.dataUpdateRequester = dataUpdateRequester;
    }

    @Override
    public CollectionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CollectionsRowBinding binding = CollectionsRowBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CollectionsViewHolder(binding.getRoot(), binding, false);
    }

    @Override
    public void onBindViewHolder(CollectionsViewHolder holder, int position) {
        final PaperCollections collections = collectionsDataSet.get(position);

        if (collections.getCoverImageColor() != null && !TextUtils.isEmpty(collections.getCoverImageColor())) {
            holder.binding.collectionsRootCard.setCardBackgroundColor(Color.parseColor(collections.getCoverImageColor()));
        }

        holder.binding.collectionPhotoCount.setText(collections.getTotalPhotos() + " Photos");
        holder.binding.collectionName.setText(collections.getCollectionTitle().toUpperCase());
        Glide.with(context).load(collections.getCoverPhotoDisplayUrl().replace("w=1080", "w=400")).format(DecodeFormat.PREFER_RGB_565).into(holder.binding.collectionCover);
        Glide.with(context).load(collections.getCollectionUserProfilePicUrl()).format(DecodeFormat.PREFER_RGB_565).into(holder.binding.profileImage);


        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SingleCollectionsActivity.class);
                intent.putExtra(COLLECTION_ID_KEY, collections.getCollectionId());
                intent.putExtra(COLLECTION_TITLE_KEY, collections.getCollectionTitle());
                intent.putExtra(COLLECTION_DATE_KEY, collections.getCollectionDate());
                intent.putExtra(COLLECTION_DESCRIPTION_KEY, collections.getCollectionDescrip());
                intent.putExtra(IMAGE_NAME_OF_USER_KEY, collections.getCollectionNameOfUser());
                intent.putExtra(IMAGE_USER_PROFILE_PIC_KEY, collections.getCollectionUserProfilePicUrl());
                intent.putExtra(IMAGE_USERNAME_KEY, collections.getCollectionUserName());
                intent.putExtra(COLLECTION_TOTAL_PHOTOS_KEY, collections.getTotalPhotos());
                intent.putExtra(COLLECTION_COVER_PHOTO_KEY, collections.getCoverPhotoDisplayUrl());
                intent.putExtra(COLLECTION_IS_CURATED_KEY, collections.isCurated());
                if (collections.isCurated()) {
                    intent.putExtra(UNSPLASH_DOWNLOAD_PATH_STRING, collections.getCollectionAllImageDownloadUrl());
                }
                context.startActivity(intent);
            }
        });

        if (position == getItemCount() - 1) {
            dataUpdateRequester.dataSetUpdateRequested();
        }
    }

    @Override
    public int getItemCount() {
        if (collectionsDataSet != null) {
            return collectionsDataSet.size();
        }
        return 0;
    }

    public void updateDataSet(ArrayList<PaperCollections> updatedDataSet) {
        collectionsDataSet = updatedDataSet;
    }

    /**
     * Collection View Holder Class
     */
    public static class CollectionsViewHolder extends RecyclerView.ViewHolder {
        private CollectionsRowBinding binding;

        public CollectionsViewHolder(View itemView, CollectionsRowBinding binding, boolean isAdView) {
            super(itemView);
            this.binding = binding;
//            profileImageIv = mView.findViewById(R.id.profile_image);
//            collectionCoverPhotoIv = mView.findViewById(R.id.collection_cover);
//            collectionNameTv = mView.findViewById(R.id.collection_name);
//            collectionPhotoCountTv = mView.findViewById(R.id.collection_photo_count);
//            rootCard = mView.findViewById(R.id.collections_root_card);

        }
    }
}
