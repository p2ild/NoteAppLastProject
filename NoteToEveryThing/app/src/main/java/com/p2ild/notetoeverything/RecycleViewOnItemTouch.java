package com.p2ild.notetoeverything;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by duypi on 8/24/2016.
 */
public class RecycleViewOnItemTouch implements RecyclerView.OnItemTouchListener {

    private static final String TAG = RecycleViewOnItemTouch.class.getSimpleName();
    private GestureDetector gestureDetector;
    private onItemClick onItemClick;

    public RecycleViewOnItemTouch(Context context, final RecyclerView recyclerView, final onItemClick onItemClick) {
        this.onItemClick = onItemClick;


        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }


            @Override
            public void onLongPress(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && onItemClick != null) {
                    onItemClick.onLongClick(childView, recyclerView.getChildPosition(childView),e.getRawX(),e.getRawY());
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && onItemClick != null && gestureDetector.onTouchEvent(e)) {
            onItemClick.onClick(childView, rv.getChildPosition(childView));
        }

        if(onItemClick!=null && e.getAction()==MotionEvent.ACTION_UP){
            onItemClick.onActionUp(e.getRawX(),e.getRawY(),rv.getChildPosition(childView));
        }

        if(onItemClick!=null && e.getAction()==MotionEvent.ACTION_MOVE){
            onItemClick.onActionFocus(e.getRawX(),e.getRawY());
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    public interface onItemClick {
        void onClick(View view, int position);

        void onLongClick(View view, int position,float rawX,float rawY);

        void onActionFocus(float rawX, float rawY);

        void onActionUp(float rawX, float rawY,int position);
    }
}
