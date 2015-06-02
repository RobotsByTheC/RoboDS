package littlebot.robods;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;


public class DSLayout implements Serializable {

    public enum Orientation {ORIENTATION_LANDSCAPE, ORIENTATION_PORTRAIT, ORIENTATION_SENSOR}
    private String name;
    private Orientation orientation;
    private String rioIp;
    private int maxVideoFPS;

    private ArrayList<DSLayoutNode> nodes = new ArrayList<DSLayoutNode>();

    public DSLayout(String name, Orientation orientation, String rioIp, int maxVideoFPS){
        this.name = name;
        this.orientation = orientation;
        this.rioIp = rioIp;
        this.maxVideoFPS = maxVideoFPS;
    }

    public void addNode(DSLayoutNode node){
        nodes.add(node);
    }

    public void addNode(ControlView controlView){
        nodes.add(controlView.toLayoutNode());
    }

    public void addAllNodes(ViewGroup viewGroup){
        for(int i = 0 ; i < viewGroup.getChildCount() ; i++){
            View view = viewGroup.getChildAt(i);

            if(view instanceof ControlView){
                addNode((ControlView)view);
            }
        }
    }

    public ViewGroup inflate(Context context){
        ViewGroup layout = new AbsoluteLayout(context);

        for(DSLayoutNode node : nodes){
            layout.addView(node.inflate(context));
        }

        return  layout;
    }

    public void writeToStream(OutputStream outputStream) throws IOException {

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.close();
    }

    public void writeToFile(File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        writeToStream(fileOutputStream);
    }

    public static DSLayout fromStream(InputStream stream){

        ObjectInputStream objectInputStream = null;
        DSLayout layout = null;

        try {

            objectInputStream = new ObjectInputStream(stream);
            layout = (DSLayout)objectInputStream.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                objectInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return layout;
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

    public DSLayoutNode getLayoutNode(int i){
        return nodes.get(i);
    }

    public int getNodeCount(){
        return nodes.size();
    }
}
