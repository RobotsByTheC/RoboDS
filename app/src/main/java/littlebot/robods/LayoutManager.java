package littlebot.robods;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by ben on 6/4/15.
 */
public class LayoutManager {

    private static final String TAG = LayoutManager.class.getName();

    public static final String PREFERENCE_FILE = "layout";
    public static final String CURRENT_LAYOUT_KEY = "currentLayout";

    public static final String LAYOUT_PATH = "/layouts";
    private final Context context;
    private DSLayout currentLayout;
    private final File layoutDirectory;
    private final Thread operationThread;
    private Handler operationHandler;

    private static abstract class Operation<Parameter, Result> {
        private static int maxId = 0;
        public final int id = maxId++;

        public abstract Result run(Parameter param);
    }

    public interface OperationCallback<Result> {
        void finished(Result r);
    }

    private LayoutManager(Context context) {
        this.context = context;

        operationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                synchronized (operationThread) {
                    operationHandler = new Handler() {
                        public void handleMessage(Message msg) {
                            Object[] obj = (Object[]) msg.obj;
                            Operation operation = (Operation) obj[0];
                            Object param = obj[1];
                            OperationCallback callback = (OperationCallback) obj[2];

                            Object result = operation.run(param);
                            if (callback != null) {
                                callback.finished(result);
                            }
                        }
                    };
                    operationThread.notifyAll();
                }
                Looper.loop();
            }
        });
        operationThread.setDaemon(true);
        operationThread.start();

        layoutDirectory = new File(context.getFilesDir().getAbsolutePath() + LAYOUT_PATH);
        startOperation(initOperation, null, null);
    }

    private final Operation<Void, Void> initOperation = new Operation<Void, Void>() {
        @Override
        public Void run(Void param) {
            layoutDirectory.mkdirs();

            SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
            DSLayout prefsCurrentLayout = getLayout(prefs.getString(CURRENT_LAYOUT_KEY, "Default"));
            if (prefsCurrentLayout != null) {
                setCurrentLayoutImpl(prefsCurrentLayout);
            }
            return null;
        }
    };

    private <Parameter, Result> void startOperation(Operation<Parameter, Result> operation, Parameter param, OperationCallback<Result> callback) {
        synchronized (operationThread) {
            while (operationHandler == null) {
                try {
                    operationThread.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        operationHandler.sendMessage(operationHandler.obtainMessage(operation.id, 0, 0, new Object[]{operation, param, callback}));
    }

    public String[] getLayoutNames() {
        return layoutDirectory.list();
    }

    private Operation<Void, DSLayout> getCurrentLayoutOperation = new Operation<Void, DSLayout>() {
        @Override
        public DSLayout run(Void param) {
            return currentLayout;
        }
    };

    /**
     * Gets the currently selected layout. This method does not normally block very long, but it
     * will in the case that the current layout is still being loaded, so the callback is necessary.
     *
     * @param callback the callback that is called when the layout is ready
     */
    public void getCurrentLayout(OperationCallback<DSLayout> callback) {
        startOperation(getCurrentLayoutOperation, null, callback);
    }

    private final Operation<DSLayout, Void> setCurrentLayoutOperation = new Operation<DSLayout, Void>() {

        @Override
        public Void run(DSLayout layout) {
            setCurrentLayoutImpl(layout);
            return null;
        }
    };

    private void setCurrentLayoutImpl(DSLayout layout) {
        currentLayout = layout;
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE).edit();
        if (layout == null) {
            prefs.remove(CURRENT_LAYOUT_KEY);
        } else {
            prefs.putString(CURRENT_LAYOUT_KEY, currentLayout.getName());
        }
        prefs.commit();
    }

    public void setCurrentLayout(DSLayout layout) {
        startOperation(setCurrentLayoutOperation, layout, null);
    }

    private final Operation<String, DSLayout> getLayoutOperation = new Operation<String, DSLayout>() {

        @Override
        public DSLayout run(String name) {
            return getLayout(name);
        }
    };

    /**
     * Necessary to get the current layout during initialization, but it private to avoid another
     * thread interacting with the layouts.
     *
     * @param name the name of the layout to get
     * @return the layout, or null if the layout does not exist
     */
    private DSLayout getLayout(String name) {
        File layoutFile = getLayoutFile(name);
        DSLayout layout = null;

        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(layoutFile));
            layout = (DSLayout) input.readObject();
        } catch (ClassNotFoundException e) {
            Log.wtf(TAG, "DSLayout class not found or layout file contains another class.");
        } catch (Exception e) {
            Log.w(TAG, "Unable to read layout file: " + e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
            }
        }

        return layout;
    }

    public void getLayout(String name, OperationCallback<DSLayout> callback) throws IOException {
        startOperation(getLayoutOperation, name, callback);
    }

    private Operation<DSLayout, Void> saveLayoutOperation = new Operation<DSLayout, Void>() {
        @Override
        public Void run(DSLayout layout) {
            ObjectOutputStream output = null;
            try {
                output = new ObjectOutputStream(new FileOutputStream(getLayoutFile(layout.getName())));
                output.writeObject(layout);
            } catch (IOException e) {
                Log.w(TAG, "Unable to save layout file: " + e);
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                }
            }
            return null;
        }
    };

    public void saveLayout(DSLayout layout, OperationCallback<Void> callback) {
        startOperation(saveLayoutOperation, layout, callback);
    }

    public void removeLayout(String name) {
        getLayoutFile(name).delete();
    }

    private File getLayoutFile(String name) {
        return new File(layoutDirectory.getAbsolutePath() + "/" + name);
    }

    private static LayoutManager instance;

    public static void initialize(Context context) {
        if (instance == null) {
            instance = new LayoutManager(context);
        }
    }

    public static LayoutManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LayoutManager must be initialized before use.");
        }
        return instance;
    }
}
