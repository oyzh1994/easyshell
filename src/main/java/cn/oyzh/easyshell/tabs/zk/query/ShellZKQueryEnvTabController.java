package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dto.zk.ShellZKEnvNode;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.property.KeyValueProperty;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

/**
 * zk查询环境变量内容组件
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellZKQueryEnvTabController extends RichTabController {

    /**
     * 环境变量表格
     */
    @FXML
    private FXTableView<KeyValueProperty<String, Object>> envTable;

    /**
     * 初始化环境变量数据
     *
     * @param envNodes 环境变量节点
     */
    public void init(List<ShellZKEnvNode> envNodes) {
        List<KeyValueProperty<String, Object>> data = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(envNodes)) {
            for (ShellZKEnvNode envNode : envNodes) {
                data.add(KeyValueProperty.of(envNode.getName(), envNode.getValue()));
            }
        }
        this.envTable.setItem(data);
    }

}