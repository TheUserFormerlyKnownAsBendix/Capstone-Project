package at.dingbat.type.widget;

/**
 * Created by Max on 11/24/2015.
 */
public interface Editable {
    public void onEnterEditMode();
    public void onExitEditMode();
    public void setEditable(boolean editable);
}
