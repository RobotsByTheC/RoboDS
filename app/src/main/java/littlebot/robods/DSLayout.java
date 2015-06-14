package littlebot.robods;

import java.io.Serializable;
import java.util.ArrayList;


public class DSLayout implements Serializable {

    public enum Orientation {LANDSCAPE, PORTRAIT}

    private String name;
    private Orientation orientation;
    private String rioIp;
    private int maxVideoFPS;

    private ArrayList<DSLayoutNode> nodes = new ArrayList<>();

    public DSLayout() {
        this("Default", Orientation.LANDSCAPE, "10.26.57.22", 30);
    }

    public DSLayout(String name, Orientation orientation, String rioIp, int maxVideoFPS) {
        this.name = name;
        this.orientation = orientation;
        this.rioIp = rioIp;
        this.maxVideoFPS = maxVideoFPS;
    }

    public void removeAllNodes() {
        nodes.clear();
    }

    public void addNode(DSLayoutNode node) {
        nodes.add(node);
    }

    public void addNode(ControlView controlView) {
        addNode(controlView.toLayoutNode());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public String getRioIP() {
        return rioIp;
    }

    public void setRioIP(String rioIp) {
        this.rioIp = rioIp;
    }

    public int getMaxVideoFPS() {
        return maxVideoFPS;
    }

    public void setMaxVideoFPS(int maxVideoFPS) {
        this.maxVideoFPS = maxVideoFPS;
    }

    public ArrayList<DSLayoutNode> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<DSLayoutNode> nodes) {
        this.nodes = nodes;
    }

    public DSLayoutNode getLayoutNode(int i) {
        return nodes.get(i);
    }

    public int getNodeCount() {
        return nodes.size();
    }
}
