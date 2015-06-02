package littlebot.robods;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import littlebot.views.TabDrawer;


public class LayoutEditorActivity extends ActionBarActivity {

    private ViewGroup mContentLayout;
    private TabDrawer mJoystickDrawer,
                      mButtonDrawer,
                      mDigitalDisplayDrawer,
                      mAnalogDisplayDrawer;

    //Layout properties
    private String name = "Default";
    private DSLayout.Orientation orientation = DSLayout.Orientation.ORIENTATION_LANDSCAPE;
    private String rioIp = "10.26.57.22";
    private int maxViewFPS = 30;

    private ControlView selectedView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContentLayout = (ViewGroup)this.getLayoutInflater().inflate(R.layout.activity_layout_editor, null);
        setContentView(mContentLayout);
        mJoystickDrawer = (TabDrawer)this.findViewById(R.id.drawer_joysticks);
        mButtonDrawer = (TabDrawer)this.findViewById(R.id.drawer_buttons);
        mDigitalDisplayDrawer = (TabDrawer)this.findViewById(R.id.drawer_display_digital);
        mAnalogDisplayDrawer = (TabDrawer)this.findViewById(R.id.drawer_display_analog);



        mContentLayout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()){
                    case DragEvent.ACTION_DROP:
                        ControlView view = (ControlView)event.getLocalState();
                        ViewGroup.LayoutParams lp = view.getLayoutParams();
                        ViewGroup parent = ((ViewGroup)view.getParent());
                        if(parent instanceof TabDrawer){

                            view = view.clone();
                            view.setX(event.getX() - lp.width/2);
                            view.setY(event.getY() - lp.height/2);
                            view.showEditDialog();
                            mContentLayout.addView(view, mContentLayout.getChildCount() - 4);
                        }else if(parent.equals(mContentLayout)){
                            view.setX(event.getX() - lp.width/2);
                            view.setY(event.getY() - lp.height/2);
                            view.setSelectedListener(selectionListener);
                        }



                }
                return true;
            }
        });


        View.OnDragListener drawerDragListener = new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return true;
            }
        };

        mJoystickDrawer.setOnDragListener(drawerDragListener);
        mButtonDrawer.setOnDragListener(drawerDragListener);
        mDigitalDisplayDrawer.setOnDragListener(drawerDragListener);
        mAnalogDisplayDrawer.setOnDragListener(drawerDragListener);

        //Get rid of any ControlViews already in mContentLayout
        for(int i = 0 ; i < mContentLayout.getChildCount() ; i++){
            View child = mContentLayout.getChildAt(i);
            if(child instanceof ControlView){
                mContentLayout.removeView(child);
            }
        }
        //Setup the layout
        File layoutFile = new File(this.getFilesDir() + "/layouts/Default");

        if(layoutFile.exists()) {
            try {
                DSLayout layout = DSLayout.fromStream(new FileInputStream(layoutFile));
                orientation = layout.getOrientation();
                rioIp = layout.getRioIP();

                //Add all the ControlViews from the layout to mContentLayout
                for(int i = 0 ; i < layout.getNodeCount() ; i++){
                    ControlView newChild = layout.getLayoutNode(i).inflate(this);
                    newChild.setEditing(true);
                    newChild.setSelectedListener(selectionListener);
                    mContentLayout.addView(newChild, mContentLayout.getChildCount() - 4);
                }

                //Set the orientation, always landscape for now; switch is for future use
                switch(layout.getOrientation()){
                    case ORIENTATION_LANDSCAPE:
                        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        orientation = DSLayout.Orientation.ORIENTATION_LANDSCAPE;
                        break;
                    case ORIENTATION_PORTRAIT:
                        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        orientation = DSLayout.Orientation.ORIENTATION_PORTRAIT;
                        break;
                    case ORIENTATION_SENSOR:
                        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                        orientation = DSLayout.Orientation.ORIENTATION_SENSOR;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.layout_editor, menu);
        MenuItem editItem = menu.getItem(0);
        MenuItem deleteItem = menu.getItem(1);
        MenuItem settingsItem = menu.getItem(2);

        editItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                selectedView.showEditDialog();
                selectedView.invalidate();
                return true;
            }
        });

        deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                mContentLayout.removeView(selectedView);
                selectedView = null;
                return true;
            }
        });

        settingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                AlertDialog.Builder builder = new AlertDialog.Builder(LayoutEditorActivity.this);
                builder.setTitle("Layout Settings");

                View dialogView = getLayoutInflater().inflate(R.layout.layout_settings_dialog, null);
                final TextView rioIpTV = (TextView)dialogView.findViewById(R.id.rio_ip);
                final RadioButton orientLandRB = (RadioButton)dialogView.findViewById(R.id.orientation_landscape);
                RadioButton orientPortRB = (RadioButton)dialogView.findViewById(R.id.orientation_portrait);

                if(orientation == DSLayout.Orientation.ORIENTATION_LANDSCAPE)
                    orientLandRB.setChecked(true);
                else
                    orientPortRB.setChecked(true);

                rioIpTV.setText(rioIp);

                builder.setView(dialogView);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rioIp = rioIpTV.getText().toString();

                        if(orientLandRB.isChecked()){
                            orientation = DSLayout.Orientation.ORIENTATION_LANDSCAPE;
                        }else{
                            orientation = DSLayout.Orientation.ORIENTATION_PORTRAIT;
                        }

                        saveLayout();
                        recreate();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();

                return true;
            }
        });


        if(selectedView != null){
            editItem.setVisible(true);
            editItem.setEnabled(true);
            deleteItem.setVisible(true);
            deleteItem.setEnabled(true);
        }else{
            editItem.setVisible(false);
            editItem.setEnabled(false);
            deleteItem.setVisible(false);
            deleteItem.setEnabled(false);
        }
        return true;
    }

    private void saveLayout(){

        DSLayout layout = new DSLayout(name, orientation, rioIp, maxViewFPS);
        layout.setRioIP(rioIp);
        layout.setOrientation(orientation);
        layout.addAllNodes(mContentLayout);

        File layoutsDir = new File(this.getFilesDir().getAbsolutePath() + "/layouts");
        if(!layoutsDir.exists())
            layoutsDir.mkdir();

        File outFile = new File(layoutsDir.getAbsolutePath() + "/" + layout.getName());


            try {
                if(!outFile.exists())
                    outFile.createNewFile();

                layout.writeToFile(outFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    //Save the layout what the user navigates back to the control activity
    @Override
    public boolean onNavigateUp(){
        saveLayout();
        return super.onNavigateUp();
    }

    //Also save the layout if the activity exits without navigating up
    @Override
    public void onStop(){
        super.onStop();

    }

    ////////////////////////////Selection Listener///////////////////////////////
    final ControlView.SelectedListener selectionListener = new ControlView.SelectedListener() {
        @Override
        public void selected(ControlView v) {

            selectedView = v;

            for(int i = 0 ; i < mContentLayout.getChildCount() ; i++){
                View view = mContentLayout.getChildAt(i);

                if(view instanceof ControlView && !view.equals(selectedView))
                    ((ControlView)view).setSelected(false);
            }

            LayoutEditorActivity.this.invalidateOptionsMenu();
        }

        @Override
        public void deselected(ControlView v) {

            if(v.equals(selectedView))
                selectedView = null;

            invalidateOptionsMenu();
        }
    };

}
