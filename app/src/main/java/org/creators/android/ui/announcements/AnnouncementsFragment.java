package org.creators.android.ui.announcements;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import org.creators.android.R;
import org.creators.android.data.model.Announcement;
import org.creators.android.ui.MainActivity;
import org.creators.android.ui.common.ParseAdapter;

/**
 * Created by Damian Wieczorek <damianw@umich.edu> on 7/27/14.
 */
public class AnnouncementsFragment extends Fragment implements ParseAdapter.ListCallbacks<Announcement>, AdapterView.OnItemClickListener {
  public static final String TAG = "AnnouncementsFragment";

  private ListView mListView;
  private SwipeRefreshLayout mLayout;
  private ParseAdapter<Announcement> mAdapter;

  public AnnouncementsFragment() {
    super();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ParseQueryAdapter.QueryFactory<Announcement> factory = new ParseQueryAdapter.QueryFactory<Announcement>() {
      @Override
      public ParseQuery<Announcement> create() {
        return Announcement.query().orderByDescending(Announcement.PINNED).orderByDescending(Announcement.CREATED_AT);
      }
    };
    mAdapter = new ParseAdapter<>(getActivity(), R.layout.adapter_announcement, this, factory);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_announcements, null);

    mListView = (ListView) mLayout.findViewById(R.id.announcements_list);
    mListView.setAdapter(mAdapter);
    mListView.setOnItemClickListener(this);

    mAdapter.bindSync(mLayout);
    if (getArguments().getBoolean(MainActivity.SHOULD_SYNC, false)) mAdapter.onRefresh();

    return mLayout;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putParcelableArrayList(ParseAdapter.ITEMS, mAdapter.getItems());
  }

  @Override
  public void fillView(ParseAdapter.ViewHolder holder, Announcement announcement) {
    TextView title = holder.get(R.id.announcement_title);
    TextView details = holder.get(R.id.announcement_details);
    ImageView pinned = holder.get(R.id.announcement_pinned_icon);

    title.setText(announcement.getTitle());
    details.setText(announcement.getDetails());
    pinned.setVisibility(announcement.isPinned() ? View.VISIBLE : View.GONE);
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    Intent intent = new Intent(getActivity(), AnnouncementDetailActivity.class);
    intent.putExtra(Announcement.OBJECT_ID, mAdapter.getItem(i).getObjectId());
    startActivity(intent);
  }
}
