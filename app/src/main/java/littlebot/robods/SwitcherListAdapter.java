package littlebot.robods;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import littlebot.robods.activity.DriverStationActivity;

/**
 * Created by ben on 6/14/15.
 */
public class SwitcherListAdapter extends ArrayAdapter<String> {

    public SwitcherListAdapter(Context context, List<String> layouts) {
        super(context, -1, new ArrayList<String>(layouts));
    }

    @Override
    public View getView(final int position, View rowView, ViewGroup parent) {
        final String layout = getItem(position);
        if(rowView == null) {
            rowView = (ViewGroup) (LayoutInflater.from(getContext()).inflate(R.layout.row_switcher_list, parent, false));
        }
        ImageButton menuButton = (ImageButton) rowView.findViewById(R.id.row_switcher_list_menu);
        final PopupMenu menu = new PopupMenu(getContext(), menuButton);
        menu.inflate(R.menu.menu_row_switcher_list);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.switcher_list_menu_delete:
                        LayoutManager.getInstance().removeLayout(layout);
                        remove(layout);
                        break;
                }
                return true;
            }
        });
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.show();
            }
        });
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutManager lm = LayoutManager.getInstance();
                lm.getLayout(layout, new LayoutManager.OperationCallback<DSLayout>() {
                    @Override
                    public void finished(final DSLayout layout) {
                        LayoutManager.getInstance().setCurrentLayout(layout);
                    }
                });
                getContext().startActivity(new Intent(getContext(), DriverStationActivity.class));
            }
        });
        TextView name = (TextView) rowView.findViewById(R.id.row_switcher_list_name);
        name.setText(layout);
        return rowView;
    }
}
