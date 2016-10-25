package com.chisto.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chisto.Activities.CategoryInfoActivity;
import com.chisto.Custom.RecyclerListView;
import com.chisto.Model.Category;
import com.chisto.R;
import com.chisto.Utils.Image;

import java.util.ArrayList;

public class CategoriesAdapter extends RecyclerListView.Adapter<RecyclerView.ViewHolder> {
    private Activity context;

    private ArrayList<Category> categories;

    public CategoriesAdapter(Activity context) {
        this.context = context;
        Image.init(context);
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

        Category category = categories.get(position);

        holder.name.setText(category.getName());
        holder.description.setText(category.getDesc());

        Image.loadPhoto(category.getIcon(), holder.icon);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CategoryInfoActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
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