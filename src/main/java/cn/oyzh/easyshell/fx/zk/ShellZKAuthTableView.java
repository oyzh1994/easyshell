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

    //    /**
    //     * 当前过滤列表
    //     */
    //    private List<ShellZKAuth> list;

    /**
     * 关键字
     */
    private String kw;

    /**
     * 连接id
     */
    private String iid;

    //    public boolean hasData() {
    //        return list != null;
    //    }
    //
    //    public void setAuths(List<ShellZKAuth> auths) {
    //        this.list = auths;
    //        this.initDataList();
    //    }

    /**
     * 初始化
     *
     * @param iid 连接id
     * @param kw  关键字
     */
    public void init(String iid, String kw) {
        this.kw = kw;
        this.iid = iid;
        this.refreshAuths();
    }

    /**
     * 刷新认证
     */
    public void refreshAuths() {
        List<ShellZKAuth> dataList = ShellZKAuthStore.INSTANCE.loadByIid(this.iid);
        if (dataList != null && !StringUtil.isBlank(this.kw)) {
            List<ShellZKAuth> list = new ArrayList<>();
            for (ShellZKAuth authVO : dataList) {
                if (StringUtil.isBlank(this.kw) || StringUtil.containsIgnoreCase(authVO.getUser(), this.kw)
                        || StringUtil.containsIgnoreCase(authVO.getPassword(), this.kw)) {
                    list.add(authVO);
                }
            }
            dataList = list;
        }
        super.setItem(dataList);
    }

//    public void addAuth(ShellZKAuth authVO) {
//        if (this.list == null) {
//            this.list = new ArrayList<>(12);
//        }
//        this.list.add(authVO);
//        this.initDataList();
//    }

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
            //            this.list.remove(data);
            this.refreshAuths();
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

    //    @Override
    //    public void destroy() {
    //        if (this.list != null) {
    //            this.list.clear();
    //            this.list = null;
    //        }
    //        super.destroy();
    //    }

}
