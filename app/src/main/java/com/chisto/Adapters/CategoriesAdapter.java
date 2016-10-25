package com.chisto.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.chisto.Custom.RecyclerListView;
import com.chisto.Model.Category;

import java.util.ArrayList;

public class CategoriesAdapter extends RecyclerListView.Adapter<RecyclerView.ViewHolder> {
    private Activity context;

    private ArrayList<Category> categories;

    public CategoriesAdapter(Activity context) {
        this.context = context;
        categories = new ArrayList<>();
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(final View itemView) {
            super(itemView);
        }
    }
}