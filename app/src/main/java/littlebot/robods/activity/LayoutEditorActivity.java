package littlebot.robods.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import littlebot.robods.BasicButton;
import littlebot.robods.BasicJoystick;
import littlebot.robods.ControlLayout;
import littlebot.robods.ControlView;
import littlebot.robods.DSLayout;
import littlebot.robods.LayoutManager;
import littlebot.robods.LayoutSettingsDialog;
import littlebot.robods.R;


public class LayoutEditorActivity extends AppCompatActivity {

    private static final String TAG = LayoutEditorActivity.class.getName();

    private ControlLayout controlLayout;
    private CoordinatorLayout footerLayout;
    private FloatingActionMenu addButtonMenu;
    private FloatingActionButton joystickAddButton,
            buttonAddButton;
    private MenuItem menuEditItem;
    private MenuItem menuDeleteItem;
    private DSLayout loadedLayout;

    private ControlView selectedView;

    private Animation controlCreateAnimation;
    private Animation controlDeleteAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutManager.initialize(this);

        setContentView(R.layout.activity_editor);

        controlCreateAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_scale_up);
        controlDeleteAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_scale_down);

        footerLayout = (CoordinatorLayout) findViewById(R.id.editor_layout_footer);
        controlLayout = new ControlLayout(this);
        controlLayout.setControlListener(new ControlLayout.ControlListener() {
            @Override
            public void controlAdded(ControlView control) {
                control.setSelectedListener(selectionListener);
                control.setEditing(true);
            }

            @Override
            public void controlRemoved(ControlView control) {
            }
        });
        addButtonMenu = (FloatingActionMenu) findViewById(R.id.editor_add_menu);
        joystickAddButton = (FloatingActionButton) findViewById(R.id.editor_joystick_add_button);
        buttonAddButton = (FloatingActionButton) findViewById(R.id.editor_button_add_button);

        controlLayout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        final ControlView control = (ControlView) event.getLocalState();
                        control.setPosition((int) event.getX(), (int) event.getY());
                }
                return true;
            }
        });
        controlLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedControl(null);
            }
        });

        loadLayout();
        ((ViewGroup) findViewById(R.id.editor_layout)).addView(controlLayout);
        footerLayout.bringToFront();
    }

    private void addControl(ControlView control) {
        controlLayout.addControl(control);
        setSelectedControl(control);
        control.startAnimation(controlCreateAnimation);
    }

    private void setupControl(final ControlView control) {
        addButtonMenu.close(true);
        final Dialog dialog = control.getEditDialog();
        control.setEditListener(new ControlView.EditListener() {
            @Override
            public void onEdit(ViewGroup editDialogLayout) {
                addControl(control);
                control.setEditListener(null);
            }
        });
        dialog.show();
    }

    public void createJoystick(View v) {
        BasicJoystick joystick = new BasicJoystick(this, controlLayout.getControlDatabase());
        setupControl(joystick);
    }

    public void createButton(View v) {
        BasicButton button = new BasicButton(this, controlLayout.getControlDatabase());
        setupControl(button);
    }

    public void editControl(MenuItem item) {
        if (selectedView != null) {
            selectedView.getEditDialog().show();
        }
    }

    public void deleteControl(MenuItem item) {
        if (selectedView != null) {
            final ControlView deletedControl = selectedView;
            deletedControl.startAnimation(controlDeleteAnimation);
            controlLayout.removeControl(deletedControl);
            setSelectedControl(null);
            Snackbar.make(footerLayout, R.string.control_delete_snackbar, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addControl(deletedControl);
                        }
                    }).show();
        }
    }

    public void showLayoutSettings(MenuItem item) {
        final LayoutSettingsDialog lsd = new LayoutSettingsDialog(this, false);
        lsd.setOkListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String oldName = loadedLayout.getName();
                lsd.toLayout(loadedLayout);
                updateOrientation();

                if (!oldName.equals(loadedLayout.getName())) {
                    final LayoutManager lm = LayoutManager.getInstance();
                    lm.saveLayout(loadedLayout, new LayoutManager.OperationCallback<Void>() {
                        @Override
                        public void finished(Void r) {
                            lm.setCurrentLayout(loadedLayout);
                            lm.removeLayout(oldName);
                        }
                    });
                }
            }
        });
        lsd.show();
        lsd.fromLayout(loadedLayout);
    }

    public void clearLayout(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.editor_clear_confirmation))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        controlLayout.removeAllViews();
                        setSelectedControl(null);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_editor, menu);
        menuEditItem = menu.findItem(R.id.layout_editor_menu_edit);
        menuDeleteItem = menu.findItem(R.id.layout_editor_menu_delete);

        updateOptionsMenu();
        return true;
    }

    private void updateOptionsMenu() {
        if (selectedView != null) {
            menuEditItem.setVisible(true);
            menuEditItem.setEnabled(true);
            menuDeleteItem.setVisible(true);
            menuDeleteItem.setEnabled(true);
        } else {
            menuEditItem.setVisible(false);
            menuEditItem.setEnabled(false);
            menuDeleteItem.setVisible(false);
            menuDeleteItem.setEnabled(false);
        }
    }

    private void loadLayout() {
        final LayoutManager lm = LayoutManager.getInstance();
        lm.getCurrentLayout(new LayoutManager.OperationCallback<DSLayout>() {
            @Override
            public void finished(final DSLayout l) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DSLayout layout = l;
                        if (layout == null) {
                            layout = new DSLayout();
                            final DSLayout cLayout = layout;
                            lm.saveLayout(layout, new LayoutManager.OperationCallback<Void>() {
                                @Override
                                public void finished(Void r) {
                                    lm.setCurrentLayout(cLayout);
                                }
                            });
                        }
                        loadedLayout = layout;

                        controlLayout.load(layout);
                        updateOrientation();
                    }
                });
            }
        });
    }

    private void updateOrientation() {
        int requestedOrientation;
        switch (loadedLayout.getOrientation()) {
            default:
            case LANDSCAPE:
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
                break;
            case PORTRAIT:
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
                break;
        }
        if (requestedOrientation != getRequestedOrientation()) {
            setRequestedOrientation(requestedOrientation);
        }
    }

    private void saveLayout() {
        if (loadedLayout != null) {
            controlLayout.save(loadedLayout);
            LayoutManager.getInstance().saveLayout(loadedLayout, null);
        }
    }

    //Save the layout what the user navigates back to the control activity
    @Override
    public boolean onNavigateUp() {
        saveLayout();
        return super.onNavigateUp();
    }

    //Also save the layout if the activity exits without navigating up
    @Override
    public void onPause() {
        super.onPause();
        saveLayout();
    }

    private void setSelectedControl(@Nullable ControlView view) {
        if (selectedView != view) {
            if (view != null) {
                view.setSelected(true);
            } else {
                selectedView.setSelected(false);
            }
        }
    }

    ////////////////////////////Selection Listener///////////////////////////////
    private final ControlView.SelectedListener selectionListener = new ControlView.SelectedListener() {
        @Override
        public void selected(ControlView v) {
            ControlView oldSelectedView = selectedView;
            selectedView = v;
            if (oldSelectedView != null && v != oldSelectedView) {
                oldSelectedView.setSelected(false);
            }
            updateOptionsMenu();
        }

        @Override
        public void deselected(ControlView v) {
            if (selectedView == v) {
                selectedView = null;
                updateOptionsMenu();
            }
        }
    };

}
