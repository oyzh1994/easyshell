package cn.oyzh.easyssh.fx;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.easyfx.information.FXAlertUtil;
import cn.oyzh.easyfx.information.FXDialogUtil;
import cn.oyzh.easyfx.menu.FXMenuItem;
import cn.oyzh.easyfx.svg.SVGGlyph;
import cn.oyzh.easyfx.view.FXView;
import cn.oyzh.easyfx.view.FXViewUtil;
import cn.oyzh.easyssh.controller.info.SSHInfoAddController;
import cn.oyzh.easyssh.domain.SSHGroup;
import cn.oyzh.easyssh.domain.SSHInfo;
import cn.oyzh.easyssh.store.SSHGroupStore;
import cn.oyzh.easyssh.store.SSHInfoStore;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ssh分组键
 *
 * @author oyzh
 * @since 2023/05/12
 */
@Slf4j
public class SSHGroupTreeItem extends BaseTreeItem implements ConnectManager {

    /**
     * 分组对象
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private final SSHGroup value;

    /**
     * ssh信息储存
     */
    private final SSHInfoStore infoStore = SSHInfoStore.INSTANCE;

    /**
     * ssh分组储存
     */
    private final SSHGroupStore groupStore = SSHGroupStore.INSTANCE;

    public SSHGroupTreeItem(@NonNull SSHGroup group, @NonNull SSHTreeView treeView) {
        this.value = group;
        this.treeView(treeView);
        this.itemValue(group.getName());

        // 监听键变化
        this.getChildren().addListener((ListChangeListener<? super SSHConnectTreeItem>) c -> {
            this.treeView().fireChildChanged();
            this.treeView().flushLocal();
        });

        // 监听展开变化
        this.expandedProperty().addListener((observable, oldValue, newValue) -> {
            this.value.setExpand(newValue);
            this.groupStore.update(this.value);
        });

        // 判断是否展开
        this.setExpanded(this.value.isExpand());
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        MenuItem addConnect = FXMenuItem.newItem("添加连接", new SVGGlyph("/font/add.svg", "12"), "添加ssh连接", this::addConnect);
        MenuItem renameGroup = FXMenuItem.newItem("分组更名", new SVGGlyph("/font/edit-square.svg", "12"), "更改分组名称(快捷键f2)", this::rename);
        MenuItem delGroup = FXMenuItem.newItem("删除分组", new SVGGlyph("/font/delete.svg", "12"), "删除此分组", this::delete);

        items.add(addConnect);
        items.add(renameGroup);
        items.add(delGroup);
        return items;
    }

    @Override
    public void rename() {
        String groupName = FXDialogUtil.prompt("请输入新的分组名称", this.value.getName());

        // 名称为null或者跟当前名称相同，则忽略
        if (groupName == null || Objects.equals(groupName, this.value.getName())) {
            return;
        }

        // 检查名称
        if (StrUtil.isBlank(groupName)) {
            // FXAlertUtil.warn("分组名称不能为空！");
            return;
        }

        // 检查是否存在
        String name = this.value.getName();
        this.value.setName(groupName);
        if (this.groupStore.exist(this.value)) {
            this.value.setName(name);
            FXAlertUtil.warn("此分组已存在！");
            return;
        }

        // 修改名称
        if (this.groupStore.update(this.value)) {
            this.itemValue(groupName);
        } else {
            FXAlertUtil.warn("修改分组名称失败！");
        }
    }

    @Override
    public void delete() {
        if (this.isChildEmpty() && !FXAlertUtil.confirm("确定删除此分组？")) {
            return;
        }
        if (!this.isChildEmpty() && !FXAlertUtil.confirm("确定删除此分组？(连接将移动到根键)")) {
            return;
        }

        // 删除失败
        if (!this.groupStore.delete(this.value)) {
            FXAlertUtil.warn("删除分组失败！");
            return;
        }

        // 处理连接
        if (!this.isChildEmpty()) {
            // 清除分组id
            List<SSHConnectTreeItem> childes = this.getChildren();
            childes.forEach(c -> c.value().setGroupId(null));
            // 连接转移到父键
            this.parent().addConnectItems(childes);
        }
        // 移除键
        this.remove();
    }

    /**
     * 添加连接
     */
    private void addConnect() {
        FXView fxView = FXViewUtil.parseView(SSHInfoAddController.class, this.parent().window());
        fxView.setProp("group", this.value);
        fxView.show();
    }

    /**
     * 父键
     *
     * @return ssh根键
     */
    public SSHRootTreeItem parent() {
        return (SSHRootTreeItem) this.getParent();
    }

    @Override
    public ObservableList<SSHConnectTreeItem> getChildren() {
        return super.getChildren();
    }

    @Override
    public boolean flushGraphic() {
        boolean result = false;
        SVGGlyph glyph = (SVGGlyph) this.itemValue().graphic();
        if (glyph == null) {
            glyph = new SVGGlyph("/font/group.svg", "12");
            this.itemValue().graphic(glyph);
            // this.treeView().fireGraphicChanged(this);
            result = true;
        }
        if (this.isChildEmpty() && glyph.getColor() != Color.BLACK) {
            glyph.setColor(Color.BLACK);
        } else if (!this.isChildEmpty() && glyph.getColor() != Color.DEEPSKYBLUE) {
            glyph.setColor(Color.DARKBLUE);
        }
        return result;
    }

    @Override
    public void addConnect(@NonNull SSHInfo sshInfo) {
        super.addChild(new SSHConnectTreeItem(sshInfo, this.treeView()));
    }

    @Override
    public void addConnectItem(@NonNull SSHConnectTreeItem item) {
        if (this.getChildren().contains(item)) {
            return;
        }
        if (!Objects.equals(item.value().getGroupId(), this.value.getGid())) {
            item.value().setGroupId(this.value.getGid());
            this.infoStore.update(item.value());
        }
        super.addChild(item);
        if (!this.isExpanded()) {
            this.extend();
        }
    }

    @Override
    public void addConnectItems(@NonNull List<SSHConnectTreeItem> items) {
        if (CollUtil.isNotEmpty(items)) {
            this.getChildren().addAll(items);
        }
    }

    @Override
    public boolean delConnectItem(@NonNull SSHConnectTreeItem item) {
        // 删除连接
        if (this.infoStore.delete(item.value())) {
            this.removeChild(item);
            return true;
        }
        return false;
    }

    @Override
    public List<SSHConnectTreeItem> getConnectItems() {
        return this.getChildren();
    }

    @Override
    public List<SSHConnectTreeItem> getConnectedItems() {
        List<SSHConnectTreeItem> items = new ArrayList<>(this.getChildren().size());
        for (SSHConnectTreeItem item : this.getChildren()) {
            if (item.isConnected()) {
                items.add(item);
            }
        }
        return items;
    }
}
