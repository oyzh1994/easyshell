package cn.oyzh.easyshell.server;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ServerInfo {

    private final StringProperty totalMemoryProperty = new SimpleStringProperty();

    public StringProperty totalMemoryProperty() {
        return totalMemoryProperty;
    }

    public void setTotalMemory(double totalMemory) {
        this.totalMemoryProperty.set(totalMemory + "Mb");
    }
}
