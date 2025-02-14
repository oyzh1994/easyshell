//package cn.oyzh.easyssh.fx;
//
//import cn.oyzh.common.log.JulLog;
//import cn.oyzh.fx.gui.tree.view.RichTreeItem;
//import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
//import cn.oyzh.fx.plus.util.FXUtil;
//import javafx.scene.Node;
//import javafx.scene.control.MenuItem;
//import javafx.scene.control.TreeItem;
//import javafx.scene.effect.Effect;
//import javafx.stage.Window;
//import lombok.Getter;
//import lombok.NonNull;
//import lombok.Setter;
//import lombok.experimental.Accessors;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.List;
//
///**
// * 基础的树节点
// *
// * @author oyzh
// * @since 2023/06/27
// */
//public abstract class BaseTreeItem extends RichTreeItem<? extends > {
//
//    /**
//     * ssh树
//     */
//    @Setter
//    @Getter
//    @Accessors(chain = true, fluent = true)
//    private SSHTreeView treeView;
//
//    /**
//     * 当前窗口对象
//     *
//     * @return 窗口对象
//     */
//    public Window window() {
//        return this.treeView().window();
//    }
//
//    /**
//     * 开始等待
//     *
//     * @param runnable 待执行业务
//     */
//    public void startWaiting(Runnable runnable) {
//        if (this.itemValue().graphic() instanceof SVGGlyph glyph) {
//            glyph.startWaiting(runnable);
//            
//        }
//    }
//
//    /**
//     * 取消等待
//     */
//    public void stopWaiting() {
//        if (this.itemValue().graphic() instanceof SVGGlyph glyph) {
//            glyph.stopWaiting();
//        }
//    }
//
//    /**
//     * 是否等待中
//     *
//     * @return 结果
//     */
//    public boolean isWaiting() {
//        if (this.itemValue() != null && this.itemValue().graphic() instanceof SVGGlyph glyph) {
//            return glyph.getWaiting() != null;
//        }
//        return false;
//    }
//
//    /**
//     * 自由处理
//     * 如果是展开状态，则收缩节点
//     * 如果是收缩状态，则展开节点
//     */
//    public void free() {
//        if (this.isExpanded()) {
//            this.collapse();
//        } else {
//            this.extend();
//        }
//    }
//
//    /**
//     * 重新展开
//     */
//    public void reExpanded() {
//        if (this.isExpanded()) {
//            FXUtil.runLater(() -> {
//                this.setExpanded(false);
//                this.setExpanded(true);
//            });
//        }
//    }
//
//    /**
//     * 展开节点
//     */
//    public void extend() {
//        if (!this.isExpanded()) {
//            FXUtil.runWait(() -> this.setExpanded(true));
//        }
//    }
//
//    /**
//     * 收缩节点
//     */
//    public void collapse() {
//        FXUtil.runWait(() -> this.setExpanded(false));
//    }
//
//    /**
//     * 删除节点
//     */
//    public void delete() {
//    }
//
//    /**
//     * 移除节点
//     */
//    public void remove() {
//        if (this.getParent() != null) {
//            this.getParent().getChildren().remove(this);
//        } else {
//            JulLog.warn("remove fail, this.getParent() is null.");
//        }
//    }
//
//    /**
//     * 节点更名
//     */
//    public void rename() {
//    }
//
//    /**
//     * 添加子节点
//     *
//     * @param item 节点
//     */
//    public void addChild(@NonNull TreeItem<?> item) {
//        this.getChildren().add(item);
//    }
//
//    /**
//     * 添加多个子节点
//     *
//     * @param items 节点列表
//     */
//    public void addChildes(@NonNull List<? extends TreeItem> items) {
//        this.getChildren().addAll(items);
//    }
//
//    /**
//     * 替换多个子节点
//     *
//     * @param items 节点列表
//     */
//    public void replaceChildes(@NonNull List<? extends TreeItem> items) {
//        this.getChildren().setAll(items);
//    }
//
//    /**
//     * 获取子节点数量
//     *
//     * @return 子节点数量
//     */
//    public int getChildrenSize() {
//        return this.getChildren().size();
//    }
//
//    /**
//     * 移除子节点
//     *
//     * @param item 节点
//     */
//    public void removeChild(@NonNull TreeItem<?> item) {
//        // 移除节点
//        this.getChildren().remove(item);
//    }
//
//    /**
//     * 移除多个子节点
//     *
//     * @param items 节点列表
//     */
//    public void removeChildes(@NonNull List<TreeItem<?>> items) {
//        // 移除节点
//        this.getChildren().removeAll(items);
//    }
//
//    /**
//     * 清空子节点
//     */
//    public void clearChild() {
//        this.getChildren().clear();
//    }
//
//    /**
//     * 子节点是否为空
//     *
//     * @return 结果
//     */
//    public boolean isChildEmpty() {
//        return CollectionUtil.isEmpty(this.getChildren());
//    }
//
//    /**
//     * 刷新图标
//     *
//     * @return 结果
//     */
//    public abstract boolean flushGraphic();
//
//    /**
//     * 获取右节点菜单按钮列表
//     *
//     * @return 右节点菜单按钮列表
//     */
//    public abstract List<MenuItem> getMenuItems();
//
//    /**
//     * 获取节点组件
//     *
//     * @return Node
//     */
//    public Node itemNode() {
//        SSHTreeItemValue itemValue = this.itemValue();
//        return itemValue == null ? null : itemValue.getRootNode();
//    }
//
//    /**
//     * 获取节点值
//     *
//     * @return SSHTreeItemValue
//     */
//    public SSHTreeItemValue itemValue() {
//        return (SSHTreeItemValue) super.getValue();
//    }
//
//    /**
//     * 设置节点值
//     *
//     * @param itemValue 节点值
//     */
//    public void itemValue(SSHTreeItemValue itemValue) {
//        super.setValue(itemValue);
//    }
//
//    /**
//     * 清除节点值
//     */
//    public void clearItemValue() {
//        super.setValue(null);
//    }
//
//    /**
//     * 设置节点值
//     *
//     * @param itemValue 节点值
//     */
//    public void itemValue(String itemValue) {
//        super.setValue(new SSHTreeItemValue(itemValue));
//    }
//
//    /**
//     * 设置特效
//     *
//     * @param effect 特效
//     */
//    public void setEffect(Effect effect) {
//        if (this.itemNode() != null) {
//            this.itemNode().setEffect(effect);
//        }
//    }
//
//    /**
//     * 获取特效
//     *
//     * @return Effect
//     */
//    public Effect getEffect() {
//        if (this.itemNode() != null) {
//            return this.itemNode().getEffect();
//        }
//        return null;
//    }
//}
