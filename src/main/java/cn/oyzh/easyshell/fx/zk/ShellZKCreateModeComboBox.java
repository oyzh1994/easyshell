package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;
import cn.oyzh.fx.plus.node.NodeManager;

import java.util.List;
import java.util.Locale;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ShellZKCreateModeComboBox extends FXComboBox<String> implements I18nSelectAdapter<String> {

    {
        NodeManager.init(this);
    }

    @Override
    public List<String> values(Locale locale) {
        this.clearItems();
        if (locale == Locale.TRADITIONAL_CHINESE || locale == Locale.TAIWAN) {
            this.addItem("持久節點(PERSISTENT)");
            this.addItem("臨時節點(EPHEMERAL)");
            this.addItem("持久順序節點(PERSISTENT_SEQUENTIAL)");
            this.addItem("臨時順序節點(EPHEMERAL_SEQUENTIAL)");
            this.addItem("容器節點(CONTAINER)");
        } else if (locale == Locale.SIMPLIFIED_CHINESE || locale == Locale.PRC) {
            this.addItem("持久节点(PERSISTENT)");
            this.addItem("临时节点(EPHEMERAL)");
            this.addItem("持久顺序节点(PERSISTENT_SEQUENTIAL)");
            this.addItem("临时顺序节点(EPHEMERAL_SEQUENTIAL)");
            this.addItem("容器节点(CONTAINER)");
        } else {
            this.addItem("PERSISTENT");
            this.addItem("EPHEMERAL");
            this.addItem("PERSISTENT_SEQUENTIAL");
            this.addItem("EPHEMERAL_SEQUENTIAL");
            this.addItem("CONTAINER");
        }
        return this.getItems();
    }
}
