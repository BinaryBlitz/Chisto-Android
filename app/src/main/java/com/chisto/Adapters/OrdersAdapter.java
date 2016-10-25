package com.chisto.Adapters;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chisto.Activities.SelectCityActivity;
import com.chisto.Custom.RecyclerListView;
import com.chisto.R;

import java.util.ArrayList;

public class OrdersAdapter extends RecyclerListView.Adapter<RecyclerView.ViewHolder> {

    public static class City {
        private String name;
        private boolean selected;

        public City(String name, boolean selected) {
            this.name = name;
            this.selected = selected;
        }
    }

    private ArrayList<City> collection = new ArrayList<>();
    private Activity context;

    public OrdersAdapter(Activity context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_city, parent, false);

        return new FriendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final FriendViewHolder holder = (FriendViewHolder) viewHolder;

        holder.name.setText(collection.get(position).name);

        if(collection.get(position).selected) {
            holder.name.setTextColor(Color.parseColor("#4bc2f7"));
            holder.marker.setVisibility(View.VISIBLE);
        } else {
            holder.name.setTextColor(Color.parseColor("#212121"));
            holder.marker.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collection.get(holder.getAdapterPosition()).selected = true;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return collection.size();
    }

    public void setCollection(ArrayList<City> collection) {
        this.collection = collection;
    }

    public void selectCity(String cityName) {
        int position = 0;
        for (int i = 0; i < collection.size(); i++) {
            if (collection.get(i).name.equals(cityName)) {
                position = i;
            }

            if (i == collection.size() - 1) {
                ((SelectCityActivity) context).cityError();
                return;
            }
        }
        collection.get(position).selected = true;
        City city =  collection.get(0);
        collection.set(0, collection.get(position));
        collection.set(position, city);

        notifyDataSetChanged();
    }

    private class FriendViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ImageView marker;

        FriendViewHolder(final View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.textView4);
            marker = (ImageView) itemView.findViewById(R.id.marker);
        }
    }
}