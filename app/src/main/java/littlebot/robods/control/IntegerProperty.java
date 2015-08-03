package littlebot.robods.control;

import java.util.HashMap;

/**
 * @author Ben Wolsieffer
 */
public class IntegerProperty extends Property {

    private int value;

    public IntegerProperty(String name, int value) {
        super(name);
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public void read(HashMap<String, Object> properties) {
        Object valueObj = properties.get(getName());
        value = valueObj != null ? (Integer) valueObj : value;
    }

    @Override
    public void write(HashMap<String, Object> properties) {
        properties.put(getName(), value);
    }
}
