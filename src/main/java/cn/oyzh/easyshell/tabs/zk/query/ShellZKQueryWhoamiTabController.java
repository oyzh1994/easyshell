package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.property.KeyValueProperty;
import javafx.fxml.FXML;
import org.apache.zookeeper.data.ClientInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellZKQueryWhoamiTabController extends RichTabController {

    @FXML
    private FXTableView<KeyValueProperty<String, Object>> whoamiTable;

    public void init(List<ClientInfo> clientInfos) {
        List<KeyValueProperty<String, Object>> data = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(clientInfos)) {
            for (ClientInfo clientInfo : clientInfos) {
                data.add(KeyValueProperty.of(clientInfo.getAuthScheme(), clientInfo.getUser()));
            }
        }
        this.whoamiTable.setItem(data);
    }

}