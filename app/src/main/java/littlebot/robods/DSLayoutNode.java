package littlebot.robods;

import android.content.Context;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Represents a node in the layout of the driver station. This is used to save the layout to a
 * file.
 *
 * @author raystubbs
 * @author Ben Wolsieffer
 */
public class DSLayoutNode implements Serializable {

    private Class<ControlView> objectClass;
    private HashMap<String, Object> properties;

    public DSLayoutNode(Class objectClass, HashMap<String, Object> properties) {
        this.objectClass = objectClass;
        this.properties = properties;
    }

    public ControlView inflate(Context context, ControlDatabase controlDatabase) {
        try {
            ControlView controlView = objectClass.getConstructor(Context.class, ControlDatabase.class).newInstance(context, controlDatabase);
            controlView.readProperties(properties);
            return controlView;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }
}
