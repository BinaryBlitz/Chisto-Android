package com.chisto.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.chisto.Custom.RecyclerListView;

public class OrdersAdapter extends RecyclerListView.Adapter<RecyclerView.ViewHolder> {
    private Activity context;

    public OrdersAdapter(Activity context) {
        this.context = context;
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