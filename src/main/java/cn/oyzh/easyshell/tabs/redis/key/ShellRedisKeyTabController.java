package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.redis.ShellRedisKeyTTLUpdatedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisZSetReverseViewEvent;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisKeyTreeItem;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.fxml.FXML;

import java.util.List;

/**
 * redis键标签页控制器
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellRedisKeyTabController extends ParentTabController {

    /**
     * 键数据
     */
    @FXML
    private ShellRedisKeyDataController keyDataController;

    /**
     * 键hex
     */
    @FXML
    private ShellRedisKeyHexController keyHexController;

    /**
     * 键信息
     */
    @FXML
    private ShellRedisKeyInfoController keyInfoController;

    /**
     * 当前键节点
     */
    private ShellRedisKeyTreeItem item;

    /**
     * 初始化
     *
     * @param item 键节点
     */
    public void init(ShellRedisKeyTreeItem item) {
        this.item = item;
        this.keyDataController.init(item);
        this.keyInfoController.init(item);
        this.keyHexController.init(item);
    }

    /**
     * 获取键节点
     *
     * @return 键节点
     */
    public ShellRedisKeyTreeItem item() {
        return item;
    }

    /**
     * 重载键
     */
    public void reloadKey() {
        this.keyDataController.reloadKey();
    }

    /**
     * 刷新ttl
     */
    public void flushTTL() {
        this.keyDataController.flushTTL();
    }

    /**
     * ttl改变事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTTLUpdate(ShellRedisKeyTTLUpdatedEvent event) {
        if (this.item == null) {
            return;
        }
        if (event.data() != this.item.shellConnect()) {
            return;
        }
        if (event.getDbIndex() != this.item.dbIndex()) {
            return;
        }
        if (StringUtil.notEquals(this.item.key(), event.getKey())) {
            return;
        }
        this.flushTTL();
    }

    /**
     * 视图改变事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onReverseView(ShellRedisZSetReverseViewEvent event) {
        if (this.item == event.data()) {
            this.init(this.item);
        }
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(
                this.keyDataController,
                this.keyHexController,
                this.keyInfoController
        );
    }
}
