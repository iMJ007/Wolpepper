package com.eclectik.wolpepper.adapters;

import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_CATEGORY_KEY;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eclectik.wolpepper.GlideApp;
import com.eclectik.wolpepper.activities.CategoryResultActivity;
import com.eclectik.wolpepper.dataStructures.PaperCategory;
import com.eclectik.wolpepper.databinding.CategoriesRowBinding;

import java.util.ArrayList;

/**
 * Created by mj on 1/7/17.
 */

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>{
    private ArrayList<PaperCategory> categoryDataSet;
    private Context context;

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        CategoriesRowBinding binding = CategoriesRowBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CategoryViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {

        final PaperCategory category = categoryDataSet.get(position);

        holder.getBinding().heading.setText(category.getCategoryTitle());

        GlideApp.with(context).load(category.getCategoryImageUrl()).format(DecodeFormat.PREFER_RGB_565).transition(new DrawableTransitionOptions().crossFade()).into(holder.getBinding().image);

        holder.getBinding().image.setContentDescription(category.getCategoryTitle());

        holder.getBinding().getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CategoryResultActivity.class);
                intent.putExtra(IMAGE_CATEGORY_KEY, category.getCategoryTitle());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        if (categoryDataSet!=null){
            return categoryDataSet.size();
        }
        return 0;
    }

    public void updateDataSet(ArrayList<PaperCategory> dataSet){
        categoryDataSet = dataSet;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        private CategoriesRowBinding binding;


        public CategoryViewHolder(View itemView, CategoriesRowBinding binding) {
            super(itemView);
            itemView.getLayoutParams().height = 500;
            this.binding = binding;
        }

        public CategoriesRowBinding getBinding() {
            return binding;
        }
    }
}
