package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.easyshell.trees.redis.ShellRedisKeyTreeItem;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * redis键额外信息组件
 *
 * @author oyzh
 * @since 2023/08/03
 */
public class ShellRedisKeyExtraController extends SubTabController {

    /**
     * ttl组件
     */
    @FXML
    private FXLabel ttl;

    /**
     * 加载耗时
     */
    @FXML
    private FXText loadTime;

    /**
     * 内存占用
     */
    @FXML
    private FXText memoryUsage;

    /**
     * redis键节点
     */
    private ShellRedisKeyTreeItem treeItem;

    /**
     * 初始化组件
     *
     * @param treeItem redis树键
     */
    public void init(ShellRedisKeyTreeItem treeItem) {
        this.treeItem = treeItem;
        this.memoryUsage.textProperty().bind(this.treeItem.memoryUsageInfoProperty());
        this.flushTTL();
        this.treeItem.flushMemoryUsage();
//        this.flushMemoryUsage();
        this.loadTime.setText(I18nHelper.cost() + " : " + this.treeItem.loadTime() + "ms");
    }

    /**
     * 刷新ttl
     */
    public void flushTTL() {
        this.ttl.text("TTL : " + this.treeItem.ttl());
    }

//    /**
//     * 刷新内存占用信息
//     */
//    public void flushMemoryUsage() {
//        this.memoryUsage.text(I18nHelper.size() + " : " + this.treeItem.memoryUsageInfo());
//    }

    /**
     * ttl设置
     */
    @FXML
    protected void ttlUpdate() {
        ShellViewFactory.redisTtlKey(this.treeItem);
    }
}
