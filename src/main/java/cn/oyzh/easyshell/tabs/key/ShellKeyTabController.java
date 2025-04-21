package cn.oyzh.easyshell.tabs.key;

import cn.oyzh.easyshell.controller.key.ShellAddKeyController;
import cn.oyzh.easyshell.controller.key.ShellImportKeyController;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.event.key.ShellKeyAddedEvent;
import cn.oyzh.easyshell.event.key.ShellKeyUpdatedEvent;
import cn.oyzh.easyshell.fx.key.ShellKeyTableView;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;

/**
 * shell终端tab内容组件
 *
 * @author oyzh
 * @since 2025/03/20
 */
public class ShellKeyTabController extends RichTabController {

    /**
     * 密钥表单
     */
    @FXML
    private ShellKeyTableView keyTable;

    /**
     * 密钥存储管理
     */
    private final ShellKeyStore keyStore = ShellKeyStore.INSTANCE;

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        this.refresh();
    }

    /**
     * 添加密钥
     */
    @FXML
    private void addKey() {
        StageAdapter adapter = StageManager.parseStage(ShellAddKeyController.class);
        adapter.display();
    }

    /**
     * 导入密钥
     */
    @FXML
    private void importKey() {
        StageAdapter adapter = StageManager.parseStage(ShellImportKeyController.class);
        adapter.display();
    }

    /**
     * 编辑密钥
     */
    @FXML
    private void updateKey() {
        ShellKey key = this.keyTable.getSelectedItem();
        this.keyTable.updateKey(key);
    }

    /**
     * 刷新数据
     */
    private void refresh() {
        this.keyTable.setItem(this.keyStore.selectList());
    }

    /**
     * 密钥添加事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onKeyAdded(ShellKeyAddedEvent event) {
        this.refresh();
    }

    /**
     * 密钥修改事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onKeyUpdated(ShellKeyUpdatedEvent event) {
        this.keyTable.refresh();
    }
}
