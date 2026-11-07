package cn.oyzh.easyshell.tabs.valkey.key;

import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyKeyTreeItem;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

/**
 * redis键tab内容组件
 *
 * @author oyzh
 * @since 2025-09-01
 */
public abstract class ShellValkeyKeyController<T extends ShellValkeyKeyTreeItem> extends SubTabController {

//    /**
//     * 根节点
//     */
//    @FXML
//    protected FXVBox root;

    /**
     * 树节点
     */
    protected T treeItem;

//    /**
//     * 键扩展信息
//     */
//    @FXML
//    private ShellValkeyKeyExtraController keyExtraController;

    /**
     * 初始化
     *
     * @param treeItem 树键
     * @return 结果
     */
    public boolean init(T treeItem) {
        this.treeItem = treeItem;
//        // 处理额外信息
//        this.keyExtraController.init(treeItem);
        // 键已过期
        if (this.treeItem.isExpire()) {
            return false;
        }
        // 初始化节点
        this.initKey();
        return true;
    }

    /**
     * 初始化键
     */
    protected void initKey() {
    }

    /**
     * 复制键
     */
    @FXML
    protected void copyKey() {
        String builder = ShellI18nHelper.label(I18nHelper.database(), this.treeItem.dbIndex()) + System.lineSeparator() +
                ShellI18nHelper.label(I18nHelper.keyType(), this.treeItem.value().getType()) + System.lineSeparator() +
                ShellI18nHelper.label(I18nHelper.keyName(), this.treeItem.key());
        ClipboardUtil.setStringAndTip(builder);
    }

    /**
     * 重命名键
     */
    @FXML
    protected void renameKey() {
        this.treeItem.rename();
    }

    /**
     * 数据组件键盘按下事件
     *
     * @param e 事件
     */
    @FXML
    protected void onKeyDataKeyPressed(KeyEvent e) {
        if (KeyboardUtil.isCtrlS(e)) {
            this.saveKeyValue();
            e.consume();
        }
    }

    /**
     * 保存键数据
     */
    @FXML
    protected void saveKeyValue() {
        if (this.treeItem.isDataUnsaved()) {
            StageManager.showMask(this.getWindow(), this.treeItem::saveKeyValue);
        }
    }

    /**
     * 重新加载键
     */
    public void reloadKey() {

    }

//    /**
//     * 刷新ttl
//     */
//    public void flushTTL() {
//        this.keyExtraController.flushTTL();
//    }

    /**
     * 首次显示数据
     */
    protected abstract void firstShowData();

    // /**
    //  * 显示数据
    //  *
    //  * @param formatType 格式化类型
    //  */
    // protected abstract void showData(EditorFormatType formatType);

//    @Override
//    public void onTabInit(DynamicTab tab) {
//        super.onTabInit(tab);
//        this.keyExtraController.onTabInit(tab);
//    }
//
//    @Override
//    public void onTabClose(DynamicTab tab, Event event) {
//        super.onTabClose(tab, event);
//        this.keyExtraController.onTabClose(tab, event);
//    }
//
//    @Override
//    public void initialize(URL location, ResourceBundle resourceBundle) {
//        super.initialize(location, resourceBundle);
//        this.keyExtraController.initialize(location, resourceBundle);
//    }
}
