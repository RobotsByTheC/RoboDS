package littlebot.robods;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.net.InetAddress;
import java.util.ArrayList;

import littlebot.robods.communication.RobotResolver;

/**
 * @author Ben Wolsieffer
 */
public class RobotChooserDialogFragment extends DialogFragment {

    private static final String TAG = RobotChooserDialogFragment.class.getSimpleName();

    public interface RobotChooseListener {
        void robotChosen(String name);
    }

    private ArrayAdapter<String> robots;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final RobotResolver resolver = RobotResolver.getInstance(getActivity());
        resolver.start();
        resolver.setRobotListener(new RobotResolver.RobotListener() {
            @Override
            public void robotFound(final String name, InetAddress address) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        robots.add(name);
                    }
                });
            }

            @Override
            public void robotLost(final String name) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        robots.remove(name);
                    }
                });
            }
        });

        View content = getActivity().getLayoutInflater().inflate(R.layout.dialog_robot_chooser, null);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Choose Robot")
                .setView(content)
                .create();

        final ListView robotList = (ListView) content.findViewById(R.id.robot_chooser_list);
        robots = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<>(resolver.getRobots().keySet()));
        robotList.setAdapter(robots);
        robotList.setEmptyView(content.findViewById(R.id.robot_chooser_empty));
        robotList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Item selected");
                if (getTargetFragment() instanceof RobotChooseListener) {
                    ((RobotChooseListener) getTargetFragment()).robotChosen(robots.getItem(position));
                }
                dialog.dismiss();
            }
        });

        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RobotResolver.getInstance(getActivity()).setRobotListener(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        RobotResolver.getInstance(getActivity()).stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        RobotResolver.getInstance(getActivity()).start();
    }
}
