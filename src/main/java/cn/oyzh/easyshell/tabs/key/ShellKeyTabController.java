package cn.oyzh.easyshell.tabs.key;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.key.ShellAddKeyController;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.event.key.ShellKeyAddedEvent;
import cn.oyzh.easyshell.fx.key.ShellKeyTableView;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

/**
 * shell终端tab内容组件
 *
 * @author oyzh
 * @since 2025/03/20
 */
public class ShellKeyTabController extends RichTabController {

    /**
     * 软件信息
     */
    @FXML
    private FXVBox root;

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
     * 编辑密钥
     */
    @FXML
    private void editKey() {
        ShellKey key = this.keyTable.getSelectedItem();
        if (key == null) {
            return;
        }
        String oldName = key.getName();
        String newName = MessageBox.prompt(I18nHelper.pleaseInputName(), oldName);
        if (StringUtil.isNotBlank(newName)) {
            return;
        }
        key.setName(newName);
        if (this.keyStore.update(key)) {
            this.keyTable.refresh();
        } else {
            key.setName(oldName);
        }
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


}
