package littlebot.robods.control;

import java.util.HashMap;

/**
 * @author Ben Wolsieffer
 */
public abstract class Property {

    private final String name;

    public Property(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void read(HashMap<String, Object> properties);

    public abstract void write(HashMap<String, Object> properties);
}
