package cn.oyzh.easyssh.fx;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.common.thread.RunnableWrapper;
import cn.oyzh.common.thread.TimerUtil;
import cn.oyzh.common.util.SystemUtil;
import cn.oyzh.easyfx.event.EventUtil;
import cn.oyzh.easyfx.information.FXAlertUtil;
import cn.oyzh.easyfx.information.FXDialogUtil;
import cn.oyzh.easyfx.menu.FXMenuItem;
import cn.oyzh.easyfx.svg.SVGGlyph;
import cn.oyzh.easyfx.view.FXView;
import cn.oyzh.easyfx.view.FXViewUtil;
import cn.oyzh.easyssh.controller.info.SSHInfoUpdateController;
import cn.oyzh.easyssh.domain.SSHInfo;
import cn.oyzh.easyssh.parser.SSHExceptionParser;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.ssh.SSHEvents;
import cn.oyzh.easyssh.store.SSHInfoStore;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ssh连接键
 *
 * @author oyzh
 * @since 2023/06/22
 */
public class SSHConnectTreeItem extends BaseTreeItem {

    /**
     * ssh信息
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private SSHInfo value;

    /**
     * ssh客户端
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private SSHClient client;

    /**
     * 已取消操作标志位
     */
    private boolean canceled;

    /**
     * ssh信息储存
     */
    private final SSHInfoStore infoStore = SSHInfoStore.INSTANCE;

    public SSHConnectTreeItem(@NonNull SSHInfo value, @NonNull SSHTreeView treeView) {
        this.treeView(treeView);
        this.value(value);
    }

    /**
     * 初始化连接
     *
     * @return 结果
     */
    private boolean initConnect() {
        try {
            EventUtil.fire(SSHEvents.SSH_OPEN_TERMINAL, this.value);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            FXAlertUtil.warn(ex, SSHExceptionParser.INSTANCE);
        }
        return false;
    }

    @Override
    public SSHConnectTreeItemValue itemValue() {
        return (SSHConnectTreeItemValue) super.itemValue();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        if (this.isWaiting()) {
            MenuItem cancel = FXMenuItem.newItem("取消连接", new SVGGlyph("/font/close.svg", "11"), "取消ssh连接", this::cancelConnect);
            items.add(cancel);
        } else if (this.isConnected()) {
            FXMenuItem disConnect = FXMenuItem.newItem("断开连接", new SVGGlyph("/font/poweroff.svg", "12"), "断开ssh连接(快捷键pause)", this::disConnect);
            FXMenuItem editConnect = FXMenuItem.newItem("编辑连接", new SVGGlyph("/font/edit.svg", "12"), "编辑连接", this::editConnect);
            FXMenuItem serverInfo = FXMenuItem.newItem("服务信息", new SVGGlyph("/font/server.svg", "12"), "查看服务信息", this::serverInfo);

            items.add(disConnect);
            items.add(editConnect);
            items.add(serverInfo);
        } else {
            FXMenuItem connect = FXMenuItem.newItem("开始连接", new SVGGlyph("/font/play-circle.svg", "12"), "开始连接(鼠标左键双击)", this::connect);
            FXMenuItem editConnect = FXMenuItem.newItem("编辑连接", new SVGGlyph("/font/edit.svg", "12"), "编辑连接", this::editConnect);
            FXMenuItem renameConnect = FXMenuItem.newItem("连接更名", new SVGGlyph("/font/edit-square.svg", "12"), "更改连接名称(快捷键f2)", this::rename);
            FXMenuItem deleteConnect = FXMenuItem.newItem("删除连接", new SVGGlyph("/font/delete.svg", "12"), "删除连接(快捷键delete)", this::delete);

            items.add(connect);
            items.add(editConnect);
            items.add(renameConnect);
            items.add(deleteConnect);
        }

        MenuItem terminal = FXMenuItem.newItem("打开终端", new SVGGlyph("/font/code library.svg", "11"), "打开ssh终端", this::openTerminal);
        items.add(terminal);
        return items;
    }

    /**
     * 服务信息
     */
    @FXML
    private void serverInfo() {
        EventUtil.fire(SSHEvents.SSH_SERVER_INFO, this.client);
    }

    /**
     * 打开终端
     */
    @FXML
    private void openTerminal() {
        EventUtil.fire(SSHEvents.SSH_OPEN_TERMINAL, this.value);
    }

    /**
     * 取消连接
     */
    public void cancelConnect() {
        this.canceled = true;
        TimerUtil.start(() -> {
            this.client.close();
            this.stopWaiting();
        });
    }

    /**
     * 连接
     */
    public void connect() {
        if (!this.isWaiting() && !this.isConnected() && !this.isConnecting()) {
            // 执行业务
            RunnableWrapper wrapper = new RunnableWrapper(() -> {
                try {
                    this.client.start();
                    if (!this.isConnected()) {
                        if (!this.canceled) {
                            FXAlertUtil.warn(this.value.getName() + "连接失败");
                        }
                        this.canceled = false;
                    } else if (this.initConnect()) {
                        this.flushGraphic();
                    } else {
                        this._disConnect();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    FXAlertUtil.warn(ex, SSHExceptionParser.INSTANCE);
                }
            }, this::stopWaiting);
            // 执行连接
            this.startWaiting(wrapper);
        }
    }

    /**
     * 编辑连接
     */
    private void editConnect() {
        if (this.isConnected()) {
            this._disConnect();
        }
        FXView fxView = FXViewUtil.parseView(SSHInfoUpdateController.class, this.treeView().window());
        fxView.setProp("sshInfo", this.value());
        fxView.show();
    }

    /**
     * 断开连接
     */
    public void disConnect() {
        if (!this.isWaiting() && this.isConnected()) {
            this.startWaiting(this::_disConnect);
        }
    }

    /**
     * 断开连接实际业务
     */
    private void _disConnect() {
        this.itemValue().clearRole();
        this.client.close();
        this.clearChildren();
        this.flushGraphic();
        SystemUtil.gcLater();
    }

    @Override
    public void free() {
        if (!this.isConnected()) {
            this.connect();
        } else {
            super.free();
        }
    }

    @Override
    public void delete() {
        if (FXAlertUtil.confirm("删除" + this.value.getName(), "确定删除连接？")) {
            this._disConnect();
            if (this.getParent() instanceof ConnectManager connectManager) {
                if (!connectManager.delConnectItem(this)) {
                    FXAlertUtil.warn("删除连接失败！");
                }
            }
        }
    }

    @Override
    public void rename() {
        String connectName = FXDialogUtil.prompt("请输入新的连接名称", this.value.getName());

        // 名称为null或者跟当前名称相同，则忽略
        if (connectName == null || Objects.equals(connectName, this.value.getName())) {
            return;
        }

        // 检查名称
        if (StrUtil.isBlank(connectName)) {
            // FXAlertUtil.warn("连接名称不能为空！");
            return;
        }

        // 检查是否存在
        String name = this.value.getName();
        this.value.setName(connectName);
        if (this.infoStore.exist(this.value)) {
            this.value.setName(name);
            FXAlertUtil.warn("此连接名称已存在！");
            return;
        }

        // 修改名称
        if (this.infoStore.update(this.value)) {
            this.itemValue(connectName);
        } else {
            FXAlertUtil.warn("修改连接名称失败！");
        }
    }

    /**
     * 设置值
     *
     * @param value ssh信息
     */
    public void value(@NonNull SSHInfo value) {
        this.value = value;
        this.disConnect();
        this.client = new SSHClient(value);
        this.itemValue(new SSHConnectTreeItemValue(value.getName()));
    }

    /**
     * 是否已连接
     *
     * @return 结果
     */
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    /**
     * 是否连接中
     *
     * @return 结果
     */
    public boolean isConnecting() {
        return this.client != null && this.client.isConnecting();
    }

    /**
     * 是否以广播
     *
     * @return 结果
     */
    public boolean isClosed() {
        return this.client != null && this.client.isClosed();
    }

    /**
     * 清理子节点
     */
    public void clearChildren() {
        try {
            this.setExpanded(false);
            this.getChildren().clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public boolean flushGraphic() {
        SVGGlyph glyph = (SVGGlyph) this.itemValue().graphic();
        if (glyph == null) {
            glyph = new SVGGlyph("/font/linux.svg", "12");
            this.itemValue().graphic(glyph);
        }
        if (this.isConnected() && glyph.getColor() != Color.GREEN) {
            glyph.setColor(Color.GREEN);
            return true;
        }
        if (!this.isConnected() && glyph.getColor() != Color.BLACK) {
            glyph.setColor(Color.BLACK);
            return true;
        }
        return false;
    }

    /**
     * 获分组键
     *
     * @return 分组键
     */
    public SSHGroupTreeItem getGroupItem() {
        if (this.getParent() instanceof SSHGroupTreeItem groupItem) {
            return groupItem;
        }
        return null;
    }
}
