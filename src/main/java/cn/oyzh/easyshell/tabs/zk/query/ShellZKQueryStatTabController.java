package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.property.KeyValueProperty;
import javafx.fxml.FXML;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellZKQueryStatTabController extends RichTabController {

    @FXML
    private FXTableView<KeyValueProperty<String, Object>> statTable;

    public void init(Stat stat) {
        List<KeyValueProperty<String, Object>> data = new ArrayList<>();
        data.add(KeyValueProperty.of("pzxid", stat.getPzxid()));

        data.add(KeyValueProperty.of("czxid", stat.getCzxid()));
        data.add(KeyValueProperty.of("ctime", stat.getCtime()));

        data.add(KeyValueProperty.of("mzxid", stat.getMzxid()));
        data.add(KeyValueProperty.of("mtime", stat.getMtime()));

        data.add(KeyValueProperty.of("version", stat.getVersion()));
        data.add(KeyValueProperty.of("cversion", stat.getCversion()));
        data.add(KeyValueProperty.of("aversion", stat.getAversion()));
        data.add(KeyValueProperty.of("aversion", stat.getEphemeralOwner()));

        data.add(KeyValueProperty.of("dataLength", stat.getDataLength()));
        data.add(KeyValueProperty.of("numChildren", stat.getNumChildren()));
        data.add(KeyValueProperty.of("ephemeralOwner", stat.getEphemeralOwner()));

        this.statTable.setItem(data);
    }

}