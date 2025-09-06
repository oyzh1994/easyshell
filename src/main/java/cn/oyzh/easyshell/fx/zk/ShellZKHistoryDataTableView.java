package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dto.zk.ShellZKHistoryData;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.easyshell.util.zk.ShellZKDataUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-12-19
 */
public class ShellZKHistoryDataTableView extends FXTableView<ShellZKHistoryData> {

    /**
     * 节点路径
     */
    private String nodePath;

    /**
     * 客户端
     */
    private ShellZKClient client;

    public void init(ShellZKClient client, String nodePath) {
        this.client = client;
        this.nodePath = nodePath;
        this.refreshData();
    }

    @Override
    public void initNode() {
        super.initNode();
        this.setOnContextMenuRequested(e -> {
            List<? extends MenuItem> menuItems = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(menuItems)) {
                this.showContextMenu(menuItems, e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {

        List<MenuItem> items = new ArrayList<>();

        ShellZKHistoryData data = this.getSelectedItem();
        FXMenuItem refreshHistory = MenuItemHelper.refreshHistory("12", this::refreshData);
        items.add(refreshHistory);

        FXMenuItem deleteHistory = MenuItemHelper.deleteHistory("12", () -> this.deleteData(data));
        deleteHistory.setDisable(data == null);
        items.add(deleteHistory);

        FXMenuItem viewHistory = MenuItemHelper.view1History("12", () -> this.viewData(data));
        viewHistory.setDisable(data == null);
        items.add(viewHistory);

        FXMenuItem restoreHistory = MenuItemHelper.restoreHistory("12", () -> this.restoreData(data));
        restoreHistory.setDisable(data == null);
        items.add(restoreHistory);

        return items;
    }

    /**
     * 刷新数据
     */
    public void refreshData() {
        this.setItem(ShellZKDataUtil.listHistory(this.nodePath, this.client));
    }

    /**
     * 删除历史
     *
     * @param data 数据
     */
    public void deleteData(ShellZKHistoryData data) {
        if (MessageBox.confirm(I18nHelper.deleteHistory())) {
            ShellZKDataUtil.deleteHistory(this.nodePath, data.getSaveTime(), this.client);
            this.removeItem(data);
        }
    }

    /**
     * 查看历史
     *
     * @param data 树
     */
    public void viewData(ShellZKHistoryData data) {
        StageManager.showMask(() -> {
            try {
                byte[] bytes = ShellZKDataUtil.getHistory(this.nodePath, data.getSaveTime(), this.client);
                ShellViewFactory.zkHistoryView(bytes);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 还原历史
     *
     * @param data 数据
     */
    public void restoreData(ShellZKHistoryData data) {
        StageManager.showMask(() -> {
            try {
                byte[] bytes = ShellZKDataUtil.getHistory(this.nodePath, data.getSaveTime(), this.client);
                this.client.setData(this.nodePath, bytes);
                MessageBox.info(I18nHelper.operationSuccess());
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

}
