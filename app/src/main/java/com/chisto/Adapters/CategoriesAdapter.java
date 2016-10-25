package com.chisto.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chisto.Custom.RecyclerListView;
import com.chisto.Model.Category;
import com.chisto.R;

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

    @SuppressWarnings("unused")
    public void clear() {
        categories.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_category, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final ViewHolder holder = (ViewHolder) viewHolder;

        holder.name.setText(categories.get(position).getName());
        holder.description.setText(categories.get(position).getDesc());
        //holder.name.setText(categories.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView description;
        ImageView icon;

        ViewHolder(final View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            description = (TextView) itemView.findViewById(R.id.description);
            icon = (ImageView) itemView.findViewById(R.id.category_icon);
        }
    }
}