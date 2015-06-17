package littlebot.robods.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.github.clans.fab.FloatingActionButton;

import java.text.Collator;
import java.util.List;

import littlebot.robods.DSLayout;
import littlebot.robods.LayoutManager;
import littlebot.robods.LayoutSettingsDialog;
import littlebot.robods.R;
import littlebot.robods.SwitcherListAdapter;


public class LayoutSwitcherActivity extends AppCompatActivity {

    private LayoutSettingsDialog createLayoutDialog;
    private ListView list;
    private FloatingActionButton fab;
    private SwitcherListAdapter switcherListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switcher);

        createLayoutDialog = new LayoutSettingsDialog(this, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createLayoutDialog.create();
        }
        fab = (FloatingActionButton) findViewById(R.id.activity_switcher_fab);
        list = (ListView) findViewById(R.id.switcher_list);
        LayoutManager.getInstance().getLayoutNames(new LayoutManager.OperationCallback<List<String>>() {
            @Override
            public void finished(final List<String> r) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.setAdapter(switcherListAdapter = new SwitcherListAdapter(LayoutSwitcherActivity.this, r));
                    }
                });
            }
        });
        list.setOnScrollListener(new AbsListViewScrollDetector() {
            @Override
            void onScrollUp() {
                fab.hide(true);
            }

            @Override
            void onScrollDown() {
                fab.show(true);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_switcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    public void createLayout(View view) {
        final DSLayout newLayout = new DSLayout();
        createLayoutDialog.show();
        createLayoutDialog.fromLayout(newLayout);
        createLayoutDialog.setOkListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createLayoutDialog.toLayout(newLayout);

                final LayoutManager lm = LayoutManager.getInstance();
                lm.saveLayout(newLayout, new LayoutManager.OperationCallback<Void>() {
                    @Override
                    public void finished(Void r) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (switcherListAdapter != null) {
                                    switcherListAdapter.add(newLayout.getName());
                                    switcherListAdapter.sort(Collator.getInstance());
                                }
                            }
                        });
                        lm.setCurrentLayout(newLayout);
                    }
                });
            }
        });
    }

    private abstract class AbsListViewScrollDetector implements AbsListView.OnScrollListener {
        private int mLastScrollY;
        private int mPreviousFirstVisibleItem;
        private AbsListView mListView;
        private int mScrollThreshold;

        abstract void onScrollUp();

        abstract void onScrollDown();

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if(totalItemCount == 0) return;
            if (isSameRow(firstVisibleItem)) {
                int newScrollY = getTopItemScrollY();
                boolean isSignificantDelta = Math.abs(mLastScrollY - newScrollY) > mScrollThreshold;
                if (isSignificantDelta) {
                    if (mLastScrollY > newScrollY) {
                        onScrollUp();
                    } else {
                        onScrollDown();
                    }
                }
                mLastScrollY = newScrollY;
            } else {
                if (firstVisibleItem > mPreviousFirstVisibleItem) {
                    onScrollUp();
                } else {
                    onScrollDown();
                }

                mLastScrollY = getTopItemScrollY();
                mPreviousFirstVisibleItem = firstVisibleItem;
            }
        }

        public void setScrollThreshold(int scrollThreshold) {
            mScrollThreshold = scrollThreshold;
        }

        public void setListView(@NonNull AbsListView listView) {
            mListView = listView;
        }

        private boolean isSameRow(int firstVisibleItem) {
            return firstVisibleItem == mPreviousFirstVisibleItem;
        }

        private int getTopItemScrollY() {
            if (mListView == null || mListView.getChildAt(0) == null) return 0;
            View topChild = mListView.getChildAt(0);
            return topChild.getTop();
        }
    }
}
