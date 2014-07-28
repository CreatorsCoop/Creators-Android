package org.creators.android.ui.nav;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.creators.android.R;

import java.util.List;

/**
 * Created by Damian Wieczorek <damianw@umich.edu> on 7/27/14.
 */
public class NavItemAdapter extends ArrayAdapter<NavItem> {

  public NavItemAdapter(Context context, List<NavItem> items) {
    super(context, R.layout.adapter_nav_item, items);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder = getHolder(getContext(), convertView);
    NavItem item = getItem(position);

    holder.title.setText(item.getTitle());
    holder.icon.setImageResource(item.getIconId());

    return holder.root;
  }

  private static ViewHolder getHolder(Context context, View convertView) {
    if (convertView == null) {
      LayoutInflater inflater = LayoutInflater.from(context);
      convertView = inflater.inflate(R.layout.adapter_nav_item, null);
      convertView.setTag(new ViewHolder(
        (LinearLayout) convertView,
        (ImageView) convertView.findViewById(R.id.nav_item_icon),
        (TextView) convertView.findViewById(R.id.nav_item_title)));
    }
    return (ViewHolder) convertView.getTag();
  }

  private static class ViewHolder {
    public final ImageView icon;
    public final TextView title;
    public final LinearLayout root;

    public ViewHolder(LinearLayout root, ImageView icon, TextView title) {
      this.root = root;
      this.icon = icon;
      this.title = title;
    }
  }

}