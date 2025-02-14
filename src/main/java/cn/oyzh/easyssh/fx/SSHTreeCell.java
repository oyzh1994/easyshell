package cn.oyzh.easyssh.fx;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyfx.controls.FXTreeCell;
import cn.oyzh.easyfx.drag.DrapEnhance;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

/**
 * ssh树节点工厂
 *
 * @author oyzh
 * @since 2023/06/31
 */
public class SSHTreeCell extends FXTreeCell<SSHTreeItemValue> {

    /**
     * 拖动增强
     */
    private DrapEnhance drapEnhance;

    @Override
    public Node initGraphic() {
        TreeItem<?> item = this.getTreeItem();
        // 初始化拖动
        if (this.drapEnhance == null) {
            this.initDragEvent();
        }
        // 基础节点
        if (item instanceof BaseTreeItem treeItem) {
            if (treeItem.flushGraphic()) {
                if (this.getCursor() != Cursor.HAND) {
                    this.setCursor(Cursor.HAND);
                }
            }
            return treeItem.itemValue().create();
        }
        return null;
    }

    /**
     * 初始化拖动事件
     */
    private void initDragEvent() {
        this.drapEnhance = new DrapEnhance();
        // 初始化特效生成器
        this.drapEnhance.targetDrapEffectCreator(() -> {
            DropShadow sourceEffect = new DropShadow();
            sourceEffect.setOffsetY(3);
            sourceEffect.setOffsetX(3);
            sourceEffect.setColor(Color.DARKGREEN);
            return sourceEffect;
        });
        this.drapEnhance.sourceDrapEffectCreator(() -> {
            DropShadow targetEffect = new DropShadow();
            targetEffect.setOffsetY(3);
            targetEffect.setOffsetX(3);
            targetEffect.setColor(Color.DARKRED);
            return targetEffect;
        });
        // 触发拖动
        this.setOnDragDetected(event -> {
            // 检查来源是否ssh连接
            Object source = event.getSource();
            if (source instanceof SSHTreeCell cell && cell.getTreeItem() instanceof SSHConnectTreeItem connectTreeItem) {
                // 触发开始事件
                Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("ssh_tree_drag");
                db.setContent(content);
                // 设置特效
                this.drapEnhance.initSourceDrapEffect(connectTreeItem.itemNode());
            }
            event.consume();
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("OnDragDetected");
            }
        });

        // 拖动完成
        this.setOnDragDone(event -> {
            // 清除特效
            this.drapEnhance.clear();
            // 清除数据
            event.getDragboard().clear();
            event.consume();
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("OnDragDone");
            }
        });

        // 拖到经过
        this.setOnDragOver(event -> {
            // 触发拖动事件
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("OnDragOver");
            }
        });

        // 拖动离开
        this.setOnDragExited(event -> {
            // 清除数据
            this.setOpacity(1);
            this.drapEnhance.source(null);
            this.drapEnhance.target(null);
            this.drapEnhance.accept(null);
            this.drapEnhance.clearTargetDrapEffect();
            event.consume();
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("OnDragExited");
            }
        });

        // 拖动进入
        this.setOnDragEntered(event -> {
            try {
                // 检查来源
                Object gestureSource = event.getGestureSource();
                if (gestureSource == this) {
                    if (JulLog.isDebugEnabled()) {
                        JulLog.debug("getGestureSource==this");
                    }
                    return;
                }
                if (!(gestureSource instanceof SSHTreeCell gestureSourceCell)) {
                    if (JulLog.isDebugEnabled()) {
                        JulLog.debug("gestureSource not SSHTreeCell");
                    }
                    return;
                }
                TreeItem<?> gestureSourceItem = gestureSourceCell.getTreeItem();
                if (!(gestureSourceItem instanceof SSHConnectTreeItem connectTreeItem)) {
                    if (JulLog.isDebugEnabled()) {
                        JulLog.debug("gestureSource.getTreeItem() not SSHConnectTreeItem");
                    }
                    return;
                }

                // 检查目标
                Object target = event.getTarget();
                if (!(target instanceof SSHTreeCell targetCell)) {
                    if (JulLog.isDebugEnabled()) {
                        JulLog.debug("target not SSHTreeCell");
                    }
                    return;
                }
                TreeItem<?> targetItem = targetCell.getTreeItem();
                if (!(targetItem instanceof ConnectManager manager)) {
                    if (JulLog.isDebugEnabled()) {
                        JulLog.debug("target.getTreeItem() not ConnectManager");
                    }
                    return;
                }

                // 检查是否同一个组
                if (connectTreeItem.getGroupItem() == manager) {
                    if (JulLog.isDebugEnabled()) {
                        JulLog.debug("connectTreeItem.getGroupItem()==targetItem");
                    }
                    return;
                }

                // 初始化特效
                this.setOpacity(0.3);
                if (targetItem instanceof BaseTreeItem item) {
                    this.drapEnhance.initTargetDrapEffect(item.itemNode());
                }
                // 设置数据
                this.drapEnhance.accept(true);
                this.drapEnhance.target(manager);
                this.drapEnhance.source(connectTreeItem);
            } finally {
                event.consume();
                if (JulLog.isDebugEnabled()) {
                    JulLog.debug("OnDragEntered");
                }
            }
        });

        // 拖动释放
        this.setOnDragDropped(event -> {
            try {
                // 清除拖动特效
                this.drapEnhance.clearDrapEffect();
                // 判断是否可以执行
                if (!this.drapEnhance.accept()) {
                    if (JulLog.isDebugEnabled()) {
                        JulLog.debug("OnDragDropped accept is false");
                    }
                    return;
                }
                // 来源
                SSHConnectTreeItem source = (SSHConnectTreeItem) this.drapEnhance.source();
                // 目标
                ConnectManager target = (ConnectManager) this.drapEnhance.target();
                // 移除来源
                source.remove();
                // 把来源对象添加到目标内
                target.addConnectItem(source);
            } finally {
                event.setDropCompleted(true);
                event.consume();
                if (JulLog.isDebugEnabled()) {
                    JulLog.debug("OnDragDropped accept={}", this.drapEnhance.accept());
                }
            }
        });
    }
}
