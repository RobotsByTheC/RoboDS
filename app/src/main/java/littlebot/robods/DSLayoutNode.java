package littlebot.robods;

import android.content.Context;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by raystubbs on 22/03/15.
 */
public class DSLayoutNode implements Serializable{

    private Class objectClass;
    private Object[] properties;

    public DSLayoutNode(Class objectClass, Object[] properties){
        this.objectClass = objectClass;
        this.properties = properties;
    }

    public ControlView inflate(Context context){
        try {
            ControlView controlView = (ControlView)objectClass.getConstructor(Context.class).newInstance(context);
            controlView.setProperties(properties);
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
