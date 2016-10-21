package com.p2ild.notetoeverything.other;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;

/**
 * Created by duypi on 8/26/2016.
 */
public class CustomStaggeredGridLayoutManager extends StaggeredGridLayoutManager {
    private boolean canScroll;
    /**
     * Creates a StaggeredGridLayoutManager with given parameters.
     *
     * @param spanCount   If orientation is vertical, spanCount is number of columns. If
     *                    orientation is horizontal, spanCount is number of rows.
     * @param orientation {@link #VERTICAL} or {@link #HORIZONTAL}
     */
    public CustomStaggeredGridLayoutManager(int spanCount, int orientation) {
        super(spanCount, orientation);
    }

    @Override
    public boolean canScrollVertically() {
        return canScroll;
    }

    public void setCanScroll(Boolean canScroll){
        this.canScroll=canScroll;
        canScrollVertically();
    }


}
