package com.andrus.easy3;

import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;




public class ViewPager2ScrollHelper {
    private final ViewPager2 viewPager;
    private final SparseArray<View> scrollableViews = new SparseArray<>();
    private boolean originalUserInputEnabled;
    private static final String TAG = "ViewPager2ScrollHelper";

    // Flag to control listener attachment strategy
    private final boolean attachToScrollableViews;

    // RecyclerView detection threshold for horizontal swipes
    private static final int HORIZONTAL_SWIPE_THRESHOLD = 15;



    /**
     * Creates a ViewPager2ScrollHelper with the default behavior (attach listener to parent)
     * @param viewPager The ViewPager2 to manage
     */
    public ViewPager2ScrollHelper(ViewPager2 viewPager) {
        this(viewPager, false);
    }

    /**
     * Creates a ViewPager2ScrollHelper with configurable behavior
     * @param viewPager The ViewPager2 to manage
     * @param attachToScrollableViews If true, attaches listeners directly to scrollable views
     *                              instead of to the parent view
     */
    public ViewPager2ScrollHelper(ViewPager2 viewPager, boolean attachToScrollableViews) {
        this.viewPager = viewPager;
        this.originalUserInputEnabled = viewPager.isUserInputEnabled();
        this.attachToScrollableViews = attachToScrollableViews;

        // Keep ViewPager2's default behavior, we'll use a different approach
        viewPager.setUserInputEnabled(true);

        Log.d(TAG, "Helper initialized, pager user input enabled: " + viewPager.isUserInputEnabled());

        // If using parent-based approach, set up the listener now
        if (!attachToScrollableViews) {
            setupParentTouchListener();
        }
    }

    private void setupParentTouchListener() {
        // Add touch interceptor to the ViewPager2's parent
        View viewPagerParent = (View) viewPager.getParent();
        if (viewPagerParent != null) {
            Log.d(TAG, "Setting touch listener on parent: " + viewPagerParent.getClass().getSimpleName());
            viewPagerParent.setOnTouchListener(new ParentTouchListener());
        } else {
            Log.e(TAG, "ViewPager2 parent is null!");
        }
    }

    /**
     * Register a scrollable view for a specific ViewPager2 position
     */
    public void registerScrollableView(int position, View view) {
        if (view != null) {
            // Remove any previous listener if we're using direct attachment
            View previousView = scrollableViews.get(position);
            if (attachToScrollableViews && previousView != null) {
                if (previousView instanceof RecyclerView) {
                    ((RecyclerView) previousView).removeOnItemTouchListener(
                            (RecyclerView.OnItemTouchListener) previousView.getTag(X.id.scroll_helper_listener_tag));
                } else {
                    previousView.setOnTouchListener(null);
                }
            }

            scrollableViews.put(position, view);
            Log.d(TAG, "Helper registered view for position " + position + ": " + view.getClass().getSimpleName());

            // If using direct attachment, set up the listener now
            if (attachToScrollableViews) {
                if (view instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) view;
                    RecyclerViewTouchInterceptor interceptor = new RecyclerViewTouchInterceptor();
                    recyclerView.addOnItemTouchListener(interceptor);
                    // Save the listener for later removal
                    recyclerView.setTag(X.id.scroll_helper_listener_tag, interceptor);
                    Log.d(TAG, "RecyclerViewTouchInterceptor attached to RecyclerView for position " + position);
                } else {
                    // For non-RecyclerView scrollable views
                    NonRecyclerViewTouchListener listener = new NonRecyclerViewTouchListener();
                    view.setOnTouchListener(listener);
                    Log.d(TAG, "NonRecyclerViewTouchListener attached to view for position " + position);
                }
            }

            // Debug - print all registered views
            for (int i = 0; i < scrollableViews.size(); i++) {
                int key = scrollableViews.keyAt(i);
                View v = scrollableViews.get(key);
                Log.d(TAG, "Helper registry contains position " + key + ": " + v.getClass().getSimpleName());
            }
        } else {
            Log.e(TAG, "Attempted to register null view for position " + position);
        }
    }

    public View getScrollableViewForPosition(int position) {
        View view = scrollableViews.get(position);
        Log.d(TAG, "Getting view for position " + position + ": " + (view != null ? "found" : "null"));
        return view;
    }

    public void restore() {
        viewPager.setUserInputEnabled(originalUserInputEnabled);

        // Clean up listeners
        if (!attachToScrollableViews) {
            View parent = (View) viewPager.getParent();
            if (parent != null) {
                parent.setOnTouchListener(null);

            }
        } else {
            // Remove listeners from all scrollable views
            for (int i = 0; i < scrollableViews.size(); i++) {
                View view = scrollableViews.valueAt(i);
                if (view instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) view;
                    RecyclerView.OnItemTouchListener listener =
                            (RecyclerView.OnItemTouchListener) recyclerView.getTag(X.id.scroll_helper_listener_tag);
                    if (listener != null) {
                        recyclerView.removeOnItemTouchListener(listener);
                    }
                } else {
                    view.setOnTouchListener(null);
                }
            }
        }

        Log.d(TAG, "ViewPager2ScrollHelper restored");
    }

    /**
     * Parent Touch Listener for when using the parent-based approach
     */
    private class ParentTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d(TAG, "parent touch listener");
            int currentItem = viewPager.getCurrentItem();
            View scrollableView = getScrollableViewForPosition(currentItem);

            // If no view is registered, just let normal touch handling occur
            if (scrollableView == null) {
                return false;
            }

            // Check if touch is in the scrollable view
            if (isTouchInView(event, scrollableView)) {
                // Disable the ViewPager2 temporarily
                viewPager.setUserInputEnabled(false);

                // Re-dispatch the event to the scrollable view
                MotionEvent clonedEvent = MotionEvent.obtain(event);
                // Convert coordinates to the scrollable view's coordinate system
                int[] location = new int[2];
                scrollableView.getLocationOnScreen(location);
                clonedEvent.offsetLocation(-location[0], -location[1]);

                // Dispatch to the scrollable view directly
                boolean handled = scrollableView.dispatchTouchEvent(clonedEvent);
                clonedEvent.recycle();

                // On ACTION_UP or ACTION_CANCEL, re-enable ViewPager2
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    viewPager.setUserInputEnabled(true);
                }

                return handled;
            } else {
                // Touch is outside scrollable view, enable ViewPager2 swiping
                viewPager.setUserInputEnabled(true);
                return false; // Let normal touch handling occur
            }
        }

        private boolean isTouchInView(MotionEvent event, View view) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];
            int right = left + view.getWidth();
            int bottom = top + view.getHeight();

            float x = event.getRawX();
            float y = event.getRawY();

            boolean inView = x >= left && x <= right && y >= top && y <= bottom;
            Log.d(TAG, "Touch at (" + x + "," + y + "), view at (" + left + "," + top + "),(" + right + "," + bottom + "), inView=" + inView);
            return inView;
        }
    }

    /**
     * Special touch interceptor for RecyclerViews to avoid the pointer index errors
     */
    private class RecyclerViewTouchInterceptor implements RecyclerView.OnItemTouchListener {
        private float startX;
        private boolean isHandlingViewPager = false;

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startX = e.getX();
                    isHandlingViewPager = false;
                    viewPager.setUserInputEnabled(false);
                    return false; // Don't intercept yet, let the RecyclerView handle it

                case MotionEvent.ACTION_MOVE:
                    float dx = e.getX() - startX;

                    if (!isHandlingViewPager && Math.abs(dx) > HORIZONTAL_SWIPE_THRESHOLD) {
                        // Determine if we should handle this in the ViewPager
                        boolean canScrollLeft = rv.canScrollHorizontally(-1);
                        boolean canScrollRight = rv.canScrollHorizontally(1);

                        // If trying to scroll left but can't, or right but can't, use ViewPager
                        if ((dx > 0 && !canScrollLeft) || (dx < 0 && !canScrollRight)) {
                            isHandlingViewPager = true;
                            viewPager.setUserInputEnabled(true);

                            // Cancel current gesture in RecyclerView
                            MotionEvent cancel = MotionEvent.obtain(e);
                            cancel.setAction(MotionEvent.ACTION_CANCEL);
                            rv.onTouchEvent(cancel);
                            cancel.recycle();

                            // Start gesture in ViewPager
                            return true; // Intercept further touch events
                        }
                    }
                    return false;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (isHandlingViewPager) {
                        isHandlingViewPager = false;
                    } else {
                        // Re-enable ViewPager at the end of the touch sequence
                        viewPager.setUserInputEnabled(true);
                    }
                    return false;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            // Forward touch to ViewPager's parent to handle the swipe
            if (isHandlingViewPager) {
                if (e.getActionMasked() == MotionEvent.ACTION_UP ||
                        e.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    isHandlingViewPager = false;
                }
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            // We don't use this
        }
    }

    /**
     * Special touch listener for non-RecyclerView scrollable views
     */
    private class NonRecyclerViewTouchListener implements View.OnTouchListener {
        private float startX;
        private boolean isHandlingViewPager = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    isHandlingViewPager = false;
                    viewPager.setUserInputEnabled(false);
                    return false; // Let the view handle the touch

                case MotionEvent.ACTION_MOVE:
                    float dx = event.getX() - startX;

                    if (!isHandlingViewPager && Math.abs(dx) > HORIZONTAL_SWIPE_THRESHOLD) {
                        // Determine if we should handle this in the ViewPager
                        boolean canScrollLeft = v.canScrollHorizontally(-1);
                        boolean canScrollRight = v.canScrollHorizontally(1);

                        // If trying to scroll left but can't, or right but can't, use ViewPager
                        if ((dx > 0 && !canScrollLeft) || (dx < 0 && !canScrollRight)) {
                            isHandlingViewPager = true;
                            viewPager.setUserInputEnabled(true);

                            // Start gesture in ViewPager
                            MotionEvent downEvent = MotionEvent.obtain(event);
                            downEvent.setAction(MotionEvent.ACTION_DOWN);
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            return true; // Take over the event
                        }
                    }
                    return false;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (isHandlingViewPager) {
                        isHandlingViewPager = false;
                    }
                    // Re-enable ViewPager at the end of the touch sequence
                    viewPager.setUserInputEnabled(true);
                    return false;
            }
            return false;
        }
    }

    /**
     * Create View tag to store listener references
     */
    public static final class X {
        public static final class id {
            public static final int scroll_helper_listener_tag = 0x7f090001; // Use a unique ID
        }
    }
}
