package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.property.KeyValueProperty;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellZKQueryNodeTabController extends RichTabController {

    @FXML
    private FXTableView<KeyValueProperty<String, String>> nodeTable;

    public void init(String path, List<String> nodes) {
        List<KeyValueProperty<String, String>> data = new ArrayList<>();
        int index = 1;
        for (String node : nodes) {
            data.add(KeyValueProperty.of(index + "", ShellZKNodeUtil.concatPath(path, node)));
            index++;
        }
        this.nodeTable.setItem(data);
    }

}