package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.property.KeyValueProperty;
import javafx.fxml.FXML;
import org.apache.zookeeper.StatsTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/21
 */
public class ZKQueryQuotaTabController extends RichTabController {

    @FXML
    private FXTableView<KeyValueProperty<String, Object>> quotaTable;

    public void init(StatsTrack track) {
        List<KeyValueProperty<String, Object>> data = new ArrayList<>();
        if (track == null) {
            data.add(KeyValueProperty.of("bytes", -1));
            data.add(KeyValueProperty.of("count", -1));
        } else {
            data.add(KeyValueProperty.of("bytes", track.getBytes()));
            data.add(KeyValueProperty.of("count", track.getCount()));
        }
        this.quotaTable.setItem(data);
    }

}