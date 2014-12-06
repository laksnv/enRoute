package app.vlnvv.enRoute;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.List;

/**
 * Created by Vicky on 11/27/14.
 */
public class SwipeView extends ActionBarActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    private Toolbar toolbar;

    /**
     * The {@link android.support.v4.view.ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    ViewPager mViewPager;

    protected List<Venue> foursquareMarkers;
    protected Coordinates source = null;
    protected Coordinates destination = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_view);

        Bundle extras = getIntent().getExtras();
        foursquareMarkers = (List<Venue>) extras.getSerializable("fsqMarkers");
        source = (Coordinates) extras.get("source");
        destination = (Coordinates) extras.get("destination");

        // Create the adapter that will return a fragment for each of the three primary sections of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        // Enable back button
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }


    public Coordinates getSource() {
        return source;
    }

    public Coordinates getDestination() {
        return destination;
    }

    public List<Venue> getFoursquareMarkers() {
        return foursquareMarkers;
    }


    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            switch (i) {
                case 0: fragment = new MapViewFragment();
                    break;

                case 1: fragment = new RatingFragment();
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title;

            switch(position) {
                case 0: title = "Map View";
                    break;

                case 1: title = "Ratings";
                    break;

                default: title = "ERROR";
                    break;
            }

            return title;
        }
    }
}