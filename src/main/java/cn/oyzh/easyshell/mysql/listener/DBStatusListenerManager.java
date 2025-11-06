package cn.oyzh.easyshell.mysql.listener;

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

    public static void bindListener(Object node, DBStatusListener listener) {
        // if (node instanceof GenericStyledArea<?, ?, ?> node1) {
        //     node1.textProperty().addListener((observable, oldValue, newValue) -> {
        //         if (listener != null) {
        //             listener.changed(observable, oldValue, newValue);
        //         }
        //     });
        // }

        if (node instanceof TextInputControl node1) {
            node1.textProperty().addListener((observable, oldValue, newValue) -> {
                if (listener != null) {
                    listener.changed(observable, oldValue, newValue);
                }
            });
        } else if (node instanceof ComboBox<?> node1) {
            node1.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (listener != null) {
                    listener.changed(observable, oldValue, newValue);
                }
            });
        } else if (node instanceof CheckBox checkBox) {
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (listener != null) {
                    listener.changed(observable, oldValue, newValue);
                }
            });
        } else if (node instanceof Property<?> property) {
            property.addListener((observable, oldValue, newValue) -> {
                if (listener != null) {
                    listener.changed(observable, oldValue, newValue);
                }
            });
        }
    }
}
