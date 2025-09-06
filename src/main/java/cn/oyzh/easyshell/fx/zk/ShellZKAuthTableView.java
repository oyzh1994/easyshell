package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.easyshell.store.zk.ShellZKAuthStore;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-12-19
 */
public class ShellZKAuthTableView extends FXTableView<ShellZKAuth> {

    /**
     * 当前过滤列表
     */
    private List<ShellZKAuth> list;

    /**
     * 关键字
     */
    private String kw;

    public boolean hasData() {
        return list != null;
    }

    public void setAuths(List<ShellZKAuth> auths) {
        this.list = auths;
        this.initDataList();
    }

    public void setKw(String kw) {
        this.kw = kw;
        this.initDataList();
    }

    //public List<ShellZKAuth> getAuths() {
    //    List<ShellZKAuth> list = new ArrayList<>(this.list.size());
    //    for (ShellZKAuth authVO : this.list) {
    //        if (authVO != null && StringUtil.isNotBlank(authVO.getUser()) && StringUtil.isNotBlank(authVO.getPassword())) {
    //            list.add(authVO);
    //        }
    //    }
    //    return list;
    //}

    private void initDataList() {
        List<ShellZKAuth> list = new ArrayList<>(12);
        if (this.list != null) {
            for (ShellZKAuth authVO : this.list) {
                if (StringUtil.isBlank(this.kw) || StringUtil.containsIgnoreCase(authVO.getUser(), this.kw)
                        || StringUtil.containsIgnoreCase(authVO.getPassword(), this.kw)) {
                    list.add(authVO);
                }
            }
        }
        super.setItem(list);
    }

    public void addAuth(ShellZKAuth authVO) {
        if (this.list == null) {
            this.list = new ArrayList<>(12);
        }
        this.list.add(authVO);
        this.initDataList();
    }

    //@Override
    //public void removeItem(Object item) {
    //    if (this.list != null) {
    //        this.list.remove(item);
    //    }
    //    super.removeItem(item);
    //    this.initDataList();
    //}
    //
    //@Override
    //public void removeItem(List<?> item) {
    //    if (this.list != null) {
    //        this.list.removeAll(item);
    //    }
    //    super.removeItem(item);
    //    this.initDataList();
    //}

    @Override
    public void initNode() {
        super.initNode();
        //this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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

        ShellZKAuth data = this.getSelectedItem();

        FXMenuItem deleteAuth = MenuItemHelper.deleteAuth("12", () -> this.deleteData(data));
        deleteAuth.setDisable(data == null);
        items.add(deleteAuth);

        FXMenuItem copyAuth = MenuItemHelper.copyAuth("12", () -> this.copyData(data));
        copyAuth.setDisable(data == null);
        items.add(copyAuth);

        return items;
    }

    /**
     * 删除认证
     *
     * @param data 数据
     */
    public void deleteData(ShellZKAuth data) {
        if (MessageBox.confirm(I18nHelper.deleteAuth())) {
            ShellZKAuthStore.INSTANCE.delete(data);
            this.list.remove(data);
            this.initDataList();
            //this.removeItem(data);
        }
    }

    /**
     * 复制认证
     *
     * @param data 数据
     */
    public void copyData(ShellZKAuth data) {
        String dataStr = I18nHelper.userName() + " " + data.getUser() + System.lineSeparator()
                + I18nHelper.password() + " " + data.getPassword();
        ClipboardUtil.setStringAndTip(dataStr);
    }
}
