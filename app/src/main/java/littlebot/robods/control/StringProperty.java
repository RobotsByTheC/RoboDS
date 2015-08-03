package littlebot.robods.control;

import java.util.HashMap;

/**
 * @author Ben Wolsieffer
 */
public class StringProperty extends Property {

    private String value;

    public StringProperty(String name) {
        this(name, "");
    }

    public StringProperty(String name, String value) {
        super(name);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void read(HashMap<String, Object> properties) {
        value = (String)properties.get(getName());
    }

    @Override
    public void write(HashMap<String, Object> properties) {
        properties.put(getName(), value);
    }
}
