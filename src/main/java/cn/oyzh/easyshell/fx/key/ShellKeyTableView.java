package cn.oyzh.easyshell.fx.key;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.chooser.DirChooserHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.tableview.TableViewMouseSelectHelper;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-04-03
 */
public class ShellKeyTableView extends FXTableView<ShellKey> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }

    /**
     * 密钥存储器
     */
    private final ShellKeyStore keyStore = ShellKeyStore.INSTANCE;

    @Override
    public void initNode() {
        super.initNode();
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<? extends MenuItem> menuItems = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(menuItems)) {
                this.showContextMenu(menuItems, e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
        // 初始化鼠标多选辅助类
        TableViewMouseSelectHelper.install(this);
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<ShellKey> keys = this.getSelectedItems();
        if (CollectionUtil.isEmpty(keys)) {
            return Collections.emptyList();
        }
        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem copyKeys1ToHost = MenuItemHelper.copyToHost("12", () -> ShellViewFactory.copyKeysToHost(keys));
        menuItems.add(copyKeys1ToHost);
        MenuItem deleteKey = MenuItemHelper.deleteKey1("12", () -> this.deleteKey(keys));
        menuItems.add(deleteKey);
        if (keys.size() == 1) {
            ShellKey key = keys.getFirst();
            MenuItem updateKey = MenuItemHelper.updateKey1("12", () -> ShellViewFactory.updateKey(key));
            menuItems.add(updateKey);
            MenuItem renameKey = MenuItemHelper.renameKey1("12", () -> this.renameKey(key));
            menuItems.add(renameKey);
            MenuItem exportKey = MenuItemHelper.exportKey1("12", () -> this.exportKey(key));
            menuItems.add(exportKey);
        }
        return menuItems;
    }

//    /**
//     * 复制密钥到主机
//     *
//     * @param keys 密钥
//     */
//    public void copyKeysToHost(List<ShellKey> keys) {
//        try {
//            StageAdapter adapter= StageManager.parseStage(ShellCopyIdKeyController.class);
//            adapter.setProp("keys", keys);
//            adapter.display();
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        }
//    }

    /**
     * 删除密钥
     *
     * @param keys 密钥
     */
    public void deleteKey(List<ShellKey> keys) {
        try {
            if (!MessageBox.confirm(I18nHelper.areYouSure())) {
                return;
            }
            for (ShellKey key : new ArrayList<>(keys)) {
                this.keyStore.delete(key);
            }
            this.removeItem(keys);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

//    /**
//     * 修改密钥
//     *
//     * @param key 密钥
//     */
//    public void updateKey(ShellKey key) {
//        try {
//            if (key == null) {
//                return;
//            }
//            StageAdapter adapter=StageManager.parseStage(ShellUpdateKeyController.class);
//            adapter.setProp("key", key);
//            adapter.display();
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        }
//    }

    /**
     * 重命名密钥
     *
     * @param key 密钥
     */
    public void renameKey(ShellKey key) {
        try {
            if (key == null) {
                return;
            }
            String oldName = key.getName();
            String newName = MessageBox.prompt(I18nHelper.pleaseInputName(), oldName);
            if (StringUtil.isBlank(newName)) {
                return;
            }
            key.setName(newName);
            if (this.keyStore.update(key)) {
                this.refresh();
            } else {
                key.setName(oldName);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 导出密钥
     *
     * @param key 密钥
     */
    public void exportKey(ShellKey key) {
        try {
            if (key == null) {
                return;
            }
            File dir = DirChooserHelper.chooseDownload(I18nHelper.pleaseSelectDirectory());
            if (dir == null) {
                return;
            }
            File priFile = new File(dir, key.getName());
            File pubFile = new File(dir, key.getName() + ".pub");
            if ((pubFile.exists() || priFile.exists()) && !MessageBox.confirm(I18nHelper.fileExistsAndContinue())) {
                return;
            }
            FileUtil.writeUtf8String(key.getPrivateKey(), priFile);
            FileUtil.writeUtf8String(key.getPublicKey(), pubFile);
            // 提示信息
            MessageBox.info(I18nHelper.operationSuccess() + " [" + priFile.getName() + "," + pubFile.getName() + "]");
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }
}
