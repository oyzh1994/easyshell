package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.easyshell.trees.redis.key.RedisHashKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisListKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisSetKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisStreamKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisStringKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisZSetKeyTreeItem;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.thread.BackgroundService;
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
public class RedisKeyDataController extends ParentTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab dataRoot;

    /**
     * set键
     */
    @FXML
    private RedisSetKeyController setKeyController;

    /**
     * zset键
     */
    @FXML
    private RedisZSetKeyController zsetKeyController;

    /**
     * list键
     */
    @FXML
    private RedisListKeyController listKeyController;

    /**
     * hylog键
     */
    @FXML
    private RedisHylogKeyController hylogKeyController;

    /**
     * hash键
     */
    @FXML
    private RedisHashKeyController hashKeyController;

    /**
     * string键
     */
    @FXML
    private RedisStringKeyController stringKeyController;

    /**
     * stream键
     */
    @FXML
    private RedisStreamKeyController streamKeyController;

    /**
     * 坐标键
     */
    @FXML
    private RedisCoordinateKeyController coordinateKeyController;

    /**
     * 当前item
     */
    private RedisKeyTreeItem treeItem;

    /**
     * 键扩展信息
     */
    @FXML
    private RedisKeyExtraController keyExtraController;

    /**
     * 执行初始化
     *
     * @param treeItem 节点
     */
    public void init(RedisKeyTreeItem treeItem) {
        this.treeItem = treeItem;
        // 隐藏旧内容
        NodeGroupUtil.disappear(this.dataRoot, "key-data");
        // 处理具体业务
        if (treeItem instanceof RedisStringKeyTreeItem item1) {
            if (item1.isHyLog()) {
                this.hylogKeyController.init(item1);
                Node node = this.dataRoot.getContent().lookup("#hylogKey");
                NodeUtil.display(node);
            } else {
                this.stringKeyController.init(item1);
                Node node = this.dataRoot.getContent().lookup("#stringKey");
                NodeUtil.display(node);
            }
        } else if (treeItem instanceof RedisZSetKeyTreeItem item1) {
            if (item1.isCoordinateView()) {
                this.coordinateKeyController.init(item1);
                Node node = this.dataRoot.getContent().lookup("#coordinateKey");
                NodeUtil.display(node);
            } else {
                this.zsetKeyController.init(item1);
                Node node = this.dataRoot.getContent().lookup("#zsetKey");
                NodeUtil.display(node);
            }
        } else if (treeItem instanceof RedisHashKeyTreeItem item1) {
            this.hashKeyController.init(item1);
            Node node = this.dataRoot.getContent().lookup("#hashKey");
            NodeUtil.display(node);
        } else if (treeItem instanceof RedisListKeyTreeItem item1) {
            this.listKeyController.init(item1);
            Node node = this.dataRoot.getContent().lookup("#listKey");
            NodeUtil.display(node);
        } else if (treeItem instanceof RedisSetKeyTreeItem item1) {
            this.setKeyController.init(item1);
            Node node = this.dataRoot.getContent().lookup("#setKey");
            NodeUtil.display(node);
        } else if (treeItem instanceof RedisStreamKeyTreeItem item1) {
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
    private RedisKeyController<?> getKeyController() {
        if (this.treeItem instanceof RedisStringKeyTreeItem item1) {
            if (item1.isHyLog()) {
                return this.stringKeyController;
            }
            return this.hylogKeyController;
        }
        if (this.treeItem instanceof RedisZSetKeyTreeItem item1) {
            if (item1.isCoordinateView()) {
                return this.zsetKeyController;
            }
            return this.coordinateKeyController;
        }
        if (this.treeItem instanceof RedisHashKeyTreeItem) {
            return this.hashKeyController;
        }
        if (this.treeItem instanceof RedisListKeyTreeItem) {
            return this.listKeyController;
        }
        if (this.treeItem instanceof RedisSetKeyTreeItem) {
            return this.setKeyController;
        }
        if (this.treeItem instanceof RedisStreamKeyTreeItem) {
            return this.streamKeyController;
        }
        return null;
    }

    /**
     * 重载键
     */
    public void reloadKey() {
        RedisKeyController<?> controller = this.getKeyController();
        if (controller != null) {
            controller.reloadKey();
        }
    }

    /**
     * 刷新ttl
     */
    public void flushTTL() {
//        RedisKeyController<?> controller = this.getKeyController();
//        if (controller != null) {
//            controller.flushTTL();
//        }
        this.keyExtraController.flushTTL();
    }

    @Override
    public List<? extends SubTabController> getSubControllers() {
        return List.of(
                this.streamKeyController, this.setKeyController, this.coordinateKeyController, this.zsetKeyController,
                this.hashKeyController, this.listKeyController, this.stringKeyController, this.hylogKeyController,
                this.keyExtraController
        );
    }
}
