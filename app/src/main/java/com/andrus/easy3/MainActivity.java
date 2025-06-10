package com.andrus.easy3;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.andrus.easy3.ViewPager2ScrollHelper.X;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private ViewPager2ScrollHelper scrollHelper;
    private TabLayout tabLayout;


    // Shared variables accessible to all fragments
    private String sharedData = "This data is accessible across all layouts";
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getWindow().setDecorFitsSystemWindows(false);
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            // Hide navigation bar and status bar
            controller.hide(WindowInsets.Type.systemBars());
            // Optional: Change system bars behavior when user swipes
            controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }

        // Initialize ViewPager2
        viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(5);
        tabLayout = findViewById(R.id.tabLayout);

        // Create adapter and set to ViewPager2
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1, false);

        // Initialize the scroll helper AFTER setting up ViewPager2 and adapter
        scrollHelper = new ViewPager2ScrollHelper(viewPager, true);
        Log.d("MainActivity", "ScrollHelper Initialized post-layout");

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(heading(position))
        ).attach();

        C.synth = new Synth(MainActivity.this);
        C.presets = new Presets(MainActivity.this, 24);
        C.sequencer = new Sequencer();
    }

    // Method for fragments to register their scrollable views
    public void registerScrollableView(int position, View view) {
        if (scrollHelper != null) {
            scrollHelper.registerScrollableView(position, view);
            Log.d("MainActivity", "Registered view for position " + position + ": " + view);
        } else {
            Log.e("MainActivity", "Cannot register view: scrollHelper is null");
        }
    }

    // Delegate to the scroll helper to get views
    public View getScrollableViewForPosition(int position) {
        if (scrollHelper != null) {
            View view = scrollHelper.getScrollableViewForPosition(position);
            Log.d("MainActivity", "Getting view for position " + position + ": " + (view != null ? view : "null"));
            return view;
        }
        Log.e("MainActivity", "Cannot get view: scrollHelper is null");
        return null;
    }
    private String heading(int position) {
        switch (position) {
            case 0:
                return "Left";
            case 1:
                return "Main";
            case 2:
                return "Right";
            case 3:
                return "Extras";
            case 4:
                return "Pre";
            case 5:
                return "Seq";
            default:
                return "tktktk";
        }
    }

    @Override
    protected void onDestroy() {
        // Clean up resources
        if (scrollHelper != null) {
            scrollHelper.restore();
        }
        super.onDestroy();
    }

    // ViewPagerAdapter implementation
    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Return the appropriate fragment for each position
            switch (position) {
                case 0:
                    return FirstLayoutFragment.newInstance(position);
                case 1:
                    return SecondLayoutFragment.newInstance(position);
                case 2:
                    return ThirdLayoutFragment.newInstance(position);
                case 3:
                    return ExtrasFragment.newInstance(position);
                case 4:
                    return FourthLayoutFragment.newInstance(position);
                case 5:
                    return SequencerLayoutFragment.newInstance(position);
                default:
                    return SecondLayoutFragment.newInstance(position);
            }
        }

        @Override
        public int getItemCount() {
            return 6; // Number of pages
        }
    }
}
