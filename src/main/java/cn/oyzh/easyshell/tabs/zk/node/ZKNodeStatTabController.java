package cn.oyzh.easyshell.tabs.zk.node;

import cn.oyzh.common.dto.FriendlyInfo;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.trees.zk.ShellZKNodeTreeItem;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.Set;

/**
 * zk节点stat组件
 *
 * @author oyzh
 * @since 2025/04/11
 */
public class ZKNodeStatTabController extends SubTabController {


    /**
     * 右侧zk属性组件
     */
    @FXML
    private FXVBox statBox;

    /**
     * zk属性视图切换按钮
     */
    @FXML
    private FXToggleSwitch statViewSwitch;

    /**
     * 复制zk状态
     */
    @FXML
    private void copyStat() {
        List<FriendlyInfo<Stat>> statInfos = this.activeItem().statInfos();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < statInfos.size(); i++) {
            FriendlyInfo<Stat> statInfo = statInfos.get(i);
            builder.append(statInfo.getName(this.statViewSwitch.isSelected())).append(" : ").append(statInfo.getValue(this.statViewSwitch.isSelected()));
            if (statInfo != CollectionUtil.getLast(statInfos)) {
                builder.append(System.lineSeparator());
            }
        }
        ClipboardUtil.setStringAndTip(builder.toString());
    }

    /**
     * 刷新zk状态
     */
    @FXML
    private void reloadStat() {
        try {
            this.activeItem().refreshStat();
            this.initStat();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 初始化状态
     */
    public void initStat() {
        if (this.activeItem() == null) {
            return;
        }
        List<FriendlyInfo<Stat>> statInfos = this.activeItem().statInfos();
        // 有可能为空
        if (CollectionUtil.isNotEmpty(statInfos)) {
            Set<Node> statItems = this.statBox.lookupAll(".statItem");
            // 遍历节点
            int index = 0;
            for (Node statItem : statItems) {
                FXHBox box = (FXHBox) statItem;
                FriendlyInfo<Stat> statInfo = statInfos.get(index++);
                Label label = (Label) box.getChildren().get(0);
                Label data = (Label) box.getChildren().get(1);
                data.setFocusTraversable(true);
                // 设置属性值及属性值
                FXUtil.runLater(() -> {
                    label.setText(statInfo.getName(this.statViewSwitch.isSelected()));
                    data.setText(statInfo.getValue(this.statViewSwitch.isSelected()).toString());
                });
            }
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 切换显示监听
        this.statViewSwitch.selectedChanged((t3, t2, t1) -> this.initStat());
    }

    private ShellZKNodeTreeItem activeItem() {
        return this.parent().getActiveItem();
    }

    @Override
    public ZKNodeTabController parent() {
        return (ZKNodeTabController) super.parent();
    }
}