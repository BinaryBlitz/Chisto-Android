package ru.binaryblitz.Chisto.Custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class RecyclerListView extends RecyclerView {

    private View emptyView;

    private AdapterDataObserver observer = new MyAdapterDataObserver();

    public RecyclerListView(Context context) {
        super(context);
    }

    public RecyclerListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setEmptyView(View view) {
        if (emptyView == view) {
            return;
        }
        emptyView = view;
        checkIfEmpty();
    }

    @SuppressWarnings("unused")
    public View getEmptyView() {
        return emptyView;
    }

    @SuppressWarnings("unused")
    public void invalidateViews() {
        int count = getChildCount();
        for (int a = 0; a < count; a++) {
            getChildAt(a).invalidate();
        }
    }

    private void checkIfEmpty() {
        if (emptyView == null || getAdapter() == null) {
            return;
        }
        boolean emptyViewVisible = getAdapter().getItemCount() == 0;
        emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
        setVisibility(emptyViewVisible ? INVISIBLE : VISIBLE);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
        checkIfEmpty();
    }

    @Override
    public void stopScroll() {
        try {
            super.stopScroll();
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    private class MyAdapterDataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    }
}