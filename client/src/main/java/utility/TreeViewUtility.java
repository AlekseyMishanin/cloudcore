package utility;

import javafx.scene.control.TreeItem;

import java.io.File;

public class TreeViewUtility {

    public static <T> String proba(TreeItem<T> item){
        if(item.getParent() == null) return "";
        return  (proba(item.getParent()).equals("") ? "" : proba(item.getParent()) + File.separator) + item.getValue().toString();
    }
}
