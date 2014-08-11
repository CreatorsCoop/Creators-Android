package org.creators.android.ui.common;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import org.creators.android.R;
import org.creators.android.data.sync.Synchronization;
import org.creators.android.data.sync.Synchronize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Damian Wieczorek <damianw@umich.edu> on 7/27/14.
 */
public class ParseAdapter<T extends ParseObject> extends BaseAdapter implements
  Synchronization.SyncCallbacks,
  SwipeRefreshLayout.OnRefreshListener {
  public static final String TAG = "ParseAdapter";
  public static final String ITEMS = "items";

  private final int mResId;
  private final Context mContext;
  private final ArrayFilter mFilter = new ArrayFilter();
  private final Object mLock = new Object();
  private final ListCallbacks<T> mCallbacks;
  private final ArrayList<T> mItems = new ArrayList<>();
  private final ArrayList<T> mOriginalItems = new ArrayList<>();
  private Optional<FilterHandler> mFilterHandler = Optional.absent();
  private ParseQueryAdapter.QueryFactory<T> mQueryFactory;
  private SwipeRefreshLayout mLayout;

  public ParseAdapter(Context context, int resource, ListCallbacks<T> callbacks) {
    mContext = context;
    mResId = resource;
    mCallbacks = callbacks;
    mQueryFactory = null;
  }

  public ParseAdapter(Context context, int resource, ListCallbacks<T> callbacks, List<T> items) {
    this(context, resource, callbacks);
    mItems.addAll(items);
  }

  public ParseAdapter(Context context, int resource, ListCallbacks<T> callbacks, ParseQueryAdapter.QueryFactory<T> queryFactory) {
    this(context, resource, callbacks);
    mQueryFactory = queryFactory;
    load();
  }

  public ParseAdapter(Context context, int resource, ListCallbacks<T> callbacks, List<T> items, ParseQueryAdapter.QueryFactory<T> queryFactory) {
    this(context, resource, callbacks, items);
    mQueryFactory = queryFactory;
    load();
  }

  public ParseQueryAdapter.QueryFactory<T> load() {
    if (mQueryFactory == null) {
      clear();
      return null;
    }
    mQueryFactory.create().findInBackground(new FindCallback<T>() {
      @Override
      public void done(List<T> ts, ParseException e) {
        if (e != null) {
          e.printStackTrace();
          return;
        }
        clear();
        mItems.addAll(ts);
        mOriginalItems.clear();
        mOriginalItems.addAll(mItems);
        notifyDataSetChanged();
      }
    });
    return mQueryFactory;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = convertView;
    if (view == null) {
      LayoutInflater inflater = LayoutInflater.from(mContext);
      view = inflater.inflate(mResId, null);
    }

    if (mCallbacks != null) mCallbacks.fillView(ViewHolder.from(view), getItem(position));

    return view;
  }

  private class ArrayFilter extends Filter {
    private Optional<Function<T, String>> mmGetter = Optional.absent();
    private Optional<Predicate<T>> mmPredicate = Optional.absent();

    public ArrayFilter() {}

    public ArrayFilter withGetter(Function<T, String> getter) {
      mmGetter = Optional.fromNullable(getter);
      mmPredicate = Optional.absent();
      return this;
    }

    public ArrayFilter withPredicate(Predicate<T> predicate) {
      mmGetter = Optional.absent();
      mmPredicate = Optional.fromNullable(predicate);
      return this;
    }

    @Override
    protected FilterResults performFiltering(final CharSequence prefix) {
      FilterResults results = new FilterResults();
      ArrayList<T> list;

      if (prefix == null || prefix.length() == 0) synchronized (mLock) {
        list = new ArrayList<>(mOriginalItems);
        results.values = list;
        results.count =  list.size();
      } else if (mmPredicate.isPresent()) {
        list = Lists.newArrayList(Iterables.filter(mOriginalItems, mmPredicate.get()));
      } else {
        list = Lists.newArrayList(Iterables.filter(mOriginalItems, new Predicate<T>() {
          @Override
          public boolean apply(T input) {
            String value = mmGetter.isPresent() ? mmGetter.get().apply(input) : input.toString();
            return value != null && value.contains(prefix);
          }
        }));
      }
      results.values = list;
      results.count = list.size();

      return results;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
      mItems.clear();
      mItems.addAll((List<T>) results.values);
      if (results.count > 0) notifyDataSetChanged();
      else notifyDataSetInvalidated();
    }
  }

  public FilterHandler prepareFilter(Menu menu, int searchId, Function<T, String> getter) {
    unloadFilter();
    mFilterHandler = Optional.of(new FilterHandler(getter, menu, searchId));
    return mFilterHandler.get();
  }

  public ArrayFilter getFilter() {
    return mFilter;
  }

  public void unloadFilter() {
    if (mFilterHandler.isPresent()) mFilterHandler.get().unload();
    mFilterHandler = Optional.absent();
  }

  private class FilterHandler implements TextWatcher, MenuItem.OnActionExpandListener {

    private Function<T, String> mmGetter;
    private EditText mmEditText;
    private MenuItem mmMenuItem;

    public FilterHandler(Function<T, String> getter, Menu menu, int searchId) {
      mmGetter = getter;

      mmEditText = (EditText) menu.findItem(searchId).getActionView();
      mmEditText.addTextChangedListener(this);

      mmMenuItem = menu.findItem(R.id.menu_search);
      mmMenuItem.setOnActionExpandListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
      // nothing to do here
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
      mFilter.withGetter(mmGetter).filter(charSequence);
    }

    @Override
    public void afterTextChanged(Editable editable) {
      // ditto
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem menuItem) {
      mmEditText.post(new Runnable() {
        @Override
        public void run() {
          mmEditText.requestFocusFromTouch();
          InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.showSoftInput(mmEditText, InputMethodManager.SHOW_IMPLICIT);
          mLayout.setEnabled(false);
        }
      });
      return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
      mmEditText.setText(null);
      mmEditText.clearFocus();
      mLayout.setEnabled(true);
      mItems.clear();
      mItems.addAll(mOriginalItems);
      notifyDataSetChanged();
      return true;
    }

    public void unload() {
      mmEditText.removeTextChangedListener(this);
      mmMenuItem.setOnActionExpandListener(null);
      mmGetter = null;
      mmEditText = null;
      mmMenuItem = null;
    }
  }

  @Override
  public void onSyncError(Synchronize.SyncException e) {
    load();
  }

  public void bindSync(SwipeRefreshLayout layout) {
    mLayout = layout;
    layout.setOnRefreshListener(this);
    layout.setColorSchemeResources(android.R.color.holo_blue_bright,
      android.R.color.holo_green_light,
      android.R.color.holo_orange_light,
      android.R.color.holo_red_light);
  }

  @Override
  public void onRefresh() {
    new Synchronization(this, mLayout).execute();
  }

  private void clear() {
    mItems.clear();
  }

  public void load(ParseQueryAdapter.QueryFactory<T> queryFactory) {
    mQueryFactory = queryFactory;
    load();
  }

  public ParseQueryAdapter.QueryFactory<T> getQueryFactory() {
    return mQueryFactory;
  }

  public int getResId() {
    return mResId;
  }

  @Override
  public int getCount() {
    return mItems.size();
  }

  @Override
  public T getItem(int i) {
    return mItems.get(i);
  }

  public ArrayList<T> getItems() {
    return mItems;
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  @Override
  public void onSyncStarted() {
    load();
  }

  @Override
  public void onSyncCompleted() {
    load();
  }

  public static class ViewHolder {
    public final View root;
    private final Map<Integer, View> mMap = new ConcurrentHashMap<>();

    private ViewHolder(View root) {
      this.root = root;
      this.root.setTag(this);
    }

    public static ViewHolder from(View view) {
      ViewHolder holder = (ViewHolder) view.getTag();
      if (holder == null) {
        view.setTag(holder = new ViewHolder(view));
      }
      return holder;
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T get(int id) {
      if (!mMap.containsKey(id)) {
        mMap.put(id, root.findViewById(id));
      }
      return (T) mMap.get(id);
    }
  }

  public static interface ListCallbacks<T extends ParseObject> {
    public void fillView(ViewHolder holder, T t);
  }
}
