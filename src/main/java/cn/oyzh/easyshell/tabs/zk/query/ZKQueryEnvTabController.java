package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dto.zk.ZKEnvNode;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.property.KeyValueProperty;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/21
 */
public class ZKQueryEnvTabController extends RichTabController {

    @FXML
    private FXTableView<KeyValueProperty<String, Object>> envTable;

    public void init(List<ZKEnvNode> envNodes) {
        List<KeyValueProperty<String, Object>> data = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(envNodes)) {
            for (ZKEnvNode envNode : envNodes) {
                data.add(KeyValueProperty.of(envNode.getName(), envNode.getValue()));
            }
        }
        this.envTable.setItem(data);
    }

}