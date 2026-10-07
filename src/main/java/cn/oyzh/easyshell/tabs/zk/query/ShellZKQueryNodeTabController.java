package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.property.KeyValueProperty;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

/**
 * zk查询节点内容组件
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKQueryNodeTabController extends RichTabController {

    /**
     * 节点表格
     */
    @FXML
    private FXTableView<KeyValueProperty<String, String>> nodeTable;

    /**
     * 初始化节点数据
     *
     * @param path  父节点路径
     * @param nodes 子节点列表
     */
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