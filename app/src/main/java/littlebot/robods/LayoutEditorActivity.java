package littlebot.robods;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;


public class LayoutEditorActivity extends AppCompatActivity {

    private static final String TAG = LayoutEditorActivity.class.getName();

    private ControlLayout controlLayout;
    private FloatingActionsMenu addButtonMenu;
    private FloatingActionButton joystickAddButton,
            buttonAddButton;
    private MenuItem menuEditItem;
    private MenuItem menuDeleteItem;
    private DSLayout loadedLayout;

    private ControlView selectedView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutManager.initialize(this);

        setContentView(R.layout.activity_layout_editor);

        controlLayout = new ControlLayout(this);
        controlLayout.setControlListener(new ControlLayout.ControlListener() {
            @Override
            public void controlAdded(ControlView control) {
                control.setSelectedListener(selectionListener);
                control.setEditing(true);
            }
        });
        addButtonMenu = (FloatingActionsMenu) findViewById(R.id.editor_add_menu);
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
        addButtonMenu.bringToFront();
    }

    private void setupControl(final ControlView control) {
        addButtonMenu.collapse();
        final Dialog dialog = control.getEditDialog();
        control.setEditListener(new ControlView.EditListener() {
            @Override
            public void onEdit(ViewGroup editDialogLayout) {
                controlLayout.addControl(control);
                control.setEditListener(null);
            }
        });
        dialog.show();
    }

    public void createJoystick(View v) {
        BasicJoystick joystick = new BasicJoystick(this);
        setupControl(joystick);
    }

    public void createButton(View v) {
        BasicButton button = new BasicButton(this);
        setupControl(button);
    }

    public void editControl(MenuItem item) {
        if (selectedView != null) {
            selectedView.getEditDialog().show();
        }
    }

    public void deleteControl(MenuItem item) {
        if (selectedView != null) {
            controlLayout.removeControl(selectedView);
            setSelectedControl(null);
        }
    }

    public void showLayoutSettings(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LayoutEditorActivity.this);
        builder.setTitle("Layout Settings");

        final View dialogView = getLayoutInflater().inflate(R.layout.layout_settings_dialog, null);
        final TextView name = (TextView) dialogView.findViewById(R.id.layout_settings_name);
        final TextView roboRIOIP = (TextView) dialogView.findViewById(R.id.layout_settings_roborio_ip);
        final RadioButton orientLandRB = (RadioButton) dialogView.findViewById(R.id.layout_settings_orientation_landscape);
        final RadioButton orientPortRB = (RadioButton) dialogView.findViewById(R.id.layout_settings_orientation_portrait);

        if (loadedLayout.getOrientation() == DSLayout.Orientation.LANDSCAPE) {
            orientLandRB.setChecked(true);
        } else {
            orientPortRB.setChecked(true);
        }

        name.setText(loadedLayout.getName());
        roboRIOIP.setText(loadedLayout.getRioIP());

        builder.setView(dialogView);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String oldName = loadedLayout.getName();
                String newName = name.getText().toString();

                if (!oldName.equals(newName)) {
                    loadedLayout.setName(newName);
                    LayoutManager lm = LayoutManager.getInstance();
                    lm.removeLayout(oldName);
                }
                loadedLayout.setRioIP(roboRIOIP.getText().toString());

                if (orientLandRB.isChecked()) {
                    loadedLayout.setOrientation(DSLayout.Orientation.LANDSCAPE);
                } else {
                    loadedLayout.setOrientation(DSLayout.Orientation.PORTRAIT);
                }
                updateOrientation();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public void clearLayout(MenuItem item) {
        controlLayout.removeAllViews();
        setSelectedControl(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.layout_editor, menu);
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
            if(selectedView == v) {
                selectedView = null;
                updateOptionsMenu();
            }
        }
    };

}
