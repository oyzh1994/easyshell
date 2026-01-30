package cn.oyzh.easyshell.db.listener;

import javafx.beans.property.Property;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/7/23
 */
public class DBStatusListenerManager {

    private static final Map<String, DBStatusListener> LISTENERS = new HashMap<>();

    public static void addListener(DBStatusListener listener) {
        if (listener != null) {
            LISTENERS.put(listener.getKey(), listener);
        }
    }

    public static void removeListener(DBStatusListener listener) {
        if (listener != null) {
            LISTENERS.remove(listener.getKey());
        }
    }

    public static DBStatusListener getListener(String key) {
        return LISTENERS.get(key);
    }

    /**
     * 绑定监听器
     *
     * @param node     节点
     * @param listener 监听器
     */
    public static void bindListener(Object node, DBStatusListener listener) {
        // if (node instanceof GenericStyledArea<?, ?, ?> node1) {
        //     node1.textProperty().addListener((observable, oldValue, newValue) -> {
        //         if (listener != null) {
        //             listener.changed(observable, oldValue, newValue);
        //         }
        //     });
        // }
        if (listener == null) {
            return;
        }
        if (node instanceof TextInputControl node1) {
            node1.textProperty().addListener(listener);
        } else if (node instanceof ComboBox<?> node1) {
            node1.getSelectionModel().selectedItemProperty().addListener(listener);
        } else if (node instanceof CheckBox checkBox) {
            checkBox.selectedProperty().addListener(listener);
        } else if (node instanceof Property<?> property) {
            property.addListener(listener);
        }
    }

    /**
     * 解绑监听器
     *
     * @param node     节点
     * @param listener 监听器
     */
    public static void unbindListener(Object node, DBStatusListener listener) {
        if (listener == null) {
            return;
        }
        if (node instanceof TextInputControl node1) {
            node1.textProperty().removeListener(listener);
        } else if (node instanceof ComboBox<?> node1) {
            node1.getSelectionModel().selectedItemProperty().removeListener(listener);
        } else if (node instanceof CheckBox checkBox) {
            checkBox.selectedProperty().removeListener(listener);
        } else if (node instanceof Property<?> property) {
            property.removeListener(listener);
        }
    }
}
