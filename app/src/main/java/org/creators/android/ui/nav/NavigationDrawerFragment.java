package org.creators.android.ui.nav;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.creators.android.R;
import org.creators.android.ui.announcements.AnnouncementsFragment;
import org.creators.android.ui.calendar.CalendarFragment;
import org.creators.android.ui.members.MembersFragment;

import java.util.Arrays;
import java.util.List;

public class NavigationDrawerFragment extends Fragment {

  private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
  private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
  private NavigationDrawerCallbacks mCallbacks;

  private ActionBarDrawerToggle mDrawerToggle;
  private NavItemAdapter mAdapter;

  private DrawerLayout mDrawerLayout;
  private ListView mDrawerListView;
  private View mFragmentContainerView;

  private int mCurrentSelectedPosition = 0;
  private boolean mFromSavedInstanceState;
  private boolean mUserLearnedDrawer;

  public NavigationDrawerFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
    mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

    if (savedInstanceState != null) {
      mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
      mFromSavedInstanceState = true;
    }

    List<NavItem> items = Arrays.asList(
      new NavItem(getFragmentManager(), AnnouncementsFragment.class, getString(R.string.announcements), R.drawable.ic_newspaper, AnnouncementsFragment.TAG),
      new NavItem(getFragmentManager(), CalendarFragment.class, getString(R.string.events), R.drawable.ic_calendar, CalendarFragment.TAG),
      new NavItem(getFragmentManager(), MembersFragment.class, getString(R.string.members), R.drawable.ic_member, MembersFragment.TAG)
    );

    mAdapter = new NavItemAdapter(getActivity(), items);

    selectItem(mCurrentSelectedPosition);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    mDrawerListView = (ListView) inflater.inflate(
      R.layout.fragment_navigation_drawer, container, false);
    mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
      }
    });

    mDrawerListView.setAdapter(mAdapter);
    mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
    return mDrawerListView;
  }

  public boolean isDrawerOpen() {
    return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
  }

  public void setUp(int fragmentId, DrawerLayout drawerLayout) {
    mFragmentContainerView = getActivity().findViewById(fragmentId);
    mDrawerLayout = drawerLayout;

    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

    ActionBar actionBar = getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);

    mDrawerToggle = new ActionBarDrawerToggle(
      getActivity(),                    /* host Activity */
      mDrawerLayout,                    /* DrawerLayout object */
      R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
      R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
      R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
    ) {
      @Override
      public void onDrawerClosed(View drawerView) {
        super.onDrawerClosed(drawerView);
        if (!isAdded()) {
          return;
        }

        getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
      }

      @Override
      public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        if (!isAdded()) {
          return;
        }

        if (!mUserLearnedDrawer) {
          // The user manually opened the drawer; store this flag to prevent auto-showing
          // the navigation drawer automatically in the future.
          mUserLearnedDrawer = true;
          SharedPreferences sp = PreferenceManager
            .getDefaultSharedPreferences(getActivity());
          sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
        }

        getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
      }
    };

    if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
      mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    // Defer code dependent on restoration of previous instance state.
    mDrawerLayout.post(new Runnable() {
      @Override
      public void run() {
        mDrawerToggle.syncState();
      }
    });

    mDrawerLayout.setDrawerListener(mDrawerToggle);
  }

  private void selectItem(int position) {
    mCurrentSelectedPosition = position;
    if (mDrawerListView != null) {
      mDrawerListView.setItemChecked(position, true);
    }
    if (mDrawerLayout != null) {
      mDrawerLayout.closeDrawer(mFragmentContainerView);
    }
    if (mCallbacks != null) {
      mCallbacks.onNavigationDrawerItemSelected(getItem(position));
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mCallbacks = (NavigationDrawerCallbacks) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mCallbacks = null;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Forward the new configuration the drawer toggle component.
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    if (mDrawerLayout != null && isDrawerOpen()) {
      inflater.inflate(R.menu.global, menu);
      showGlobalContextActionBar();
    }
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

  }

  public NavItem getItem(int position) {
    return mAdapter.getItem(position);
  }

  private void showGlobalContextActionBar() {
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setTitle(R.string.app_name);
  }

  private ActionBar getActionBar() {
    return getActivity().getActionBar();
  }

  public static interface NavigationDrawerCallbacks {
    void onNavigationDrawerItemSelected(NavItem item);
  }
}
