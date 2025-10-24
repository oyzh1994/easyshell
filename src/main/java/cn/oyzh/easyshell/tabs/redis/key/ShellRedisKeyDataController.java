package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.easyshell.trees.redis.ShellRedisHashKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisJsonKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisListKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisSetKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisStreamKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisStringKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisZSetKeyTreeItem;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.Node;

import java.util.List;

/**
 * redis键信息组件
 *
 * @author oyzh
 * @since 2023/08/03
 */
public class ShellRedisKeyDataController extends ParentTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab dataRoot;

    /**
     * set键
     */
    @FXML
    private ShellRedisSetKeyController setKeyController;

    /**
     * zset键
     */
    @FXML
    private ShellRedisZSetKeyController zsetKeyController;

    /**
     * list键
     */
    @FXML
    private ShellRedisListKeyController listKeyController;

    /**
     * hylog键
     */
    @FXML
    private ShellRedisHylogKeyController hylogKeyController;

    /**
     * hash键
     */
    @FXML
    private ShellRedisHashKeyController hashKeyController;

    /**
     * json键
     */
    @FXML
    private ShellRedisJsonKeyController jsonKeyController;

    /**
     * string键
     */
    @FXML
    private ShellRedisStringKeyController stringKeyController;

    /**
     * stream键
     */
    @FXML
    private ShellRedisStreamKeyController streamKeyController;

    /**
     * 坐标键
     */
    @FXML
    private ShellRedisCoordinateKeyController coordinateKeyController;

    /**
     * 当前item
     */
    private ShellRedisKeyTreeItem treeItem;

    /**
     * 键扩展信息
     */
    @FXML
    private ShellRedisKeyExtraController keyExtraController;

    /**
     * 执行初始化
     *
     * @param treeItem 节点
     */
    public void init(ShellRedisKeyTreeItem treeItem) {
        this.treeItem = treeItem;
        // 隐藏旧内容
        NodeGroupUtil.disappear(this.dataRoot, "key-data");
        // 处理具体业务
        if (treeItem instanceof ShellRedisStringKeyTreeItem item1) {
            if (item1.isHyLog()) {
                this.hylogKeyController.init(item1);
                Node node = this.dataRoot.getContent().lookup("#hylogKey");
                NodeUtil.display(node);
            } else {
                this.stringKeyController.init(item1);
                Node node = this.dataRoot.getContent().lookup("#stringKey");
                NodeUtil.display(node);
            }
        } else if (treeItem instanceof ShellRedisJsonKeyTreeItem item1) {
            this.jsonKeyController.init(item1);
            Node node = this.dataRoot.getContent().lookup("#jsonKey");
            NodeUtil.display(node);
        } else if (treeItem instanceof ShellRedisZSetKeyTreeItem item1) {
            if (item1.isCoordinateView()) {
                this.coordinateKeyController.init(item1);
                Node node = this.dataRoot.getContent().lookup("#coordinateKey");
                NodeUtil.display(node);
            } else {
                this.zsetKeyController.init(item1);
                Node node = this.dataRoot.getContent().lookup("#zsetKey");
                NodeUtil.display(node);
            }
        } else if (treeItem instanceof ShellRedisHashKeyTreeItem item1) {
            this.hashKeyController.init(item1);
            Node node = this.dataRoot.getContent().lookup("#hashKey");
            NodeUtil.display(node);
        } else if (treeItem instanceof ShellRedisListKeyTreeItem item1) {
            this.listKeyController.init(item1);
            Node node = this.dataRoot.getContent().lookup("#listKey");
            NodeUtil.display(node);
        } else if (treeItem instanceof ShellRedisSetKeyTreeItem item1) {
            this.setKeyController.init(item1);
            Node node = this.dataRoot.getContent().lookup("#setKey");
            NodeUtil.display(node);
        } else if (treeItem instanceof ShellRedisStreamKeyTreeItem item1) {
            this.streamKeyController.init(item1);
            Node node = this.dataRoot.getContent().lookup("#streamKey");
            NodeUtil.display(node);
        }
        // 处理额外信息
        this.keyExtraController.init(treeItem);
        // 刷新tab
        this.flushTab();
        // 判断这个key是否到期
        if (treeItem.isExpire()) {
            // BackgroundService.submitFXLater(() -> {
            String tips = I18nHelper.key() + " [" + treeItem.key() + "] " + I18nHelper.expired() + ", " + I18nHelper.deleteKey() + "?";
            if (MessageBox.confirm(tips)) {
                this.treeItem.deleteByExpired();
            }
            // });
        }
    }

    /**
     * 获取键Controller
     *
     * @return 键Controller
     */
    private ShellRedisKeyController<?> getKeyController() {
        if (this.treeItem instanceof ShellRedisStringKeyTreeItem item1) {
            if (item1.isHyLog()) {
                return this.stringKeyController;
            }
            return this.hylogKeyController;
        }
        if (this.treeItem instanceof ShellRedisJsonKeyTreeItem) {
            return this.jsonKeyController;
        }
        if (this.treeItem instanceof ShellRedisZSetKeyTreeItem item1) {
            if (item1.isCoordinateView()) {
                return this.zsetKeyController;
            }
            return this.coordinateKeyController;
        }
        if (this.treeItem instanceof ShellRedisHashKeyTreeItem) {
            return this.hashKeyController;
        }
        if (this.treeItem instanceof ShellRedisListKeyTreeItem) {
            return this.listKeyController;
        }
        if (this.treeItem instanceof ShellRedisSetKeyTreeItem) {
            return this.setKeyController;
        }
        if (this.treeItem instanceof ShellRedisStreamKeyTreeItem) {
            return this.streamKeyController;
        }
        return null;
    }

    /**
     * 重载键
     */
    public void reloadKey() {
        ShellRedisKeyController<?> controller = this.getKeyController();
        if (controller != null) {
            controller.reloadKey();
        }
    }

    /**
     * 刷新ttl
     */
    public void flushTTL() {
//        ShellRedisKeyController<?> controller = this.getKeyController();
//        if (controller != null) {
//            controller.flushTTL();
//        }
        this.keyExtraController.flushTTL();
    }

    @Override
    public List<? extends SubTabController> getSubControllers() {
        return List.of(
                this.setKeyController,
                this.zsetKeyController,
                this.hashKeyController,
                this.listKeyController,
                this.jsonKeyController,
                this.keyExtraController,
                this.hylogKeyController,
                this.stringKeyController,
                this.streamKeyController,
                this.coordinateKeyController
        );
    }


}
