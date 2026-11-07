package cn.oyzh.easyshell.tabs.valkey.key;

import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.easyshell.valkey.ShellValkeyKeyUtil;
import cn.oyzh.easyshell.valkey.key.ShellValkeyKey;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyKeyTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * redis键信息组件
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyKeyInfoController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab infoRoot;

    /**
     * redis客户端
     */
    private ShellValkeyClient client;

    /**
     * redis键
     */
    private ShellValkeyKey redisKey;

    /**
     * redis树节点
     */
    private ShellValkeyKeyTreeItem treeItem;

    /**
     * 编码
     */
    @FXML
    private FXLabel objectEncoding;

    /**
     * 空闲时间
     */
    @FXML
    private FXLabel objectIdletime;

    /**
     * 引用数量
     */
    @FXML
    private FXLabel objectRefcount;

    /**
     * 复制信息
     */
    @FXML
    private void copy() {
        String builder = ShellI18nHelper.label(I18nHelper.keyName(), this.treeItem.key()) + System.lineSeparator() +
                ShellI18nHelper.label(I18nHelper.database(), this.treeItem.dbIndex()) + System.lineSeparator() +
                ShellI18nHelper.label(I18nHelper.encoding(), this.redisKey.getObjectedEncoding()) + System.lineSeparator() +
                ShellI18nHelper.label(I18nHelper.idleTime(), this.redisKey.getObjectIdletime()) + System.lineSeparator() +
                ShellI18nHelper.label(I18nHelper.refcount(), this.redisKey.getObjectRefcount()) + System.lineSeparator();
        ClipboardUtil.setStringAndTip(builder);
    }

    /**
     * 刷新信息
     */
    @FXML
    private void refresh() {
        this.initObject();
    }

    /**
     * 初始化组件
     *
     * @param treeItem redis树键
     */
    public void init(ShellValkeyKeyTreeItem treeItem) {
        if (treeItem == null || treeItem.getTreeView() == null) {
            return;
        }
        // 重置渲染标志位
        this.treeItem = treeItem;
        this.redisKey = treeItem.value();
        this.client = treeItem.client();
        // 选中时则更新
        if (this.infoRoot.isSelected()) {
            this.initObject();
        }
    }

    /**
     * 初始化对象
     */
    protected void initObject() {
        if (this.treeItem != null) {
            try {
                String key = this.treeItem.key();
                int dbIndex = this.treeItem.dbIndex();
                ShellValkeyKeyUtil.keyObject(this.redisKey, dbIndex, key, this.client);
                this.objectIdletime.text(ShellI18nHelper.label(I18nHelper.idleTime(), this.redisKey.objectIdletimeString()));
                this.objectRefcount.text(ShellI18nHelper.label(I18nHelper.refcount(), this.redisKey.objectRefcountString()));
                this.objectEncoding.text(ShellI18nHelper.label(I18nHelper.encoding(), this.redisKey.objectedEncodingString()));
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
        this.infoRoot.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.initObject();
            }
        });
    }
}
