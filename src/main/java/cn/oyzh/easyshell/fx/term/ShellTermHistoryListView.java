package cn.oyzh.easyshell.fx.term;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * shell终端历史列表
 *
 * @author oyzh
 * @since 2025/05/31
 */
public class ShellTermHistoryListView extends FXListView<FXHBox> {

    {
        this.setPadding(Insets.EMPTY);
    }

    public Runnable getOnItemPicked() {
        return onItemPicked;
    }

    public void setOnItemPicked(Runnable onItemPicked) {
        this.onItemPicked = onItemPicked;
    }

    private void onItemPicked() {
        if (this.onItemPicked != null) {
            this.onItemPicked.run();
        }
    }

    /**
     * 节点选中事件
     */
    private Runnable onItemPicked;

    @Override
    public void select(int index) {
        if (index < 0) {
            index = 0;
        }
        if (index >= this.getItems().size()) {
            index = this.getItems().size() - 1;
        }
        super.select(index);
    }

    /**
     * 获取选中项
     *
     * @return 结果
     */
    public String getPickedItem() {
        FXHBox hBox = this.getSelectedItem();
        if (hBox != null) {
            FXLabel label = (FXLabel) hBox.getFirstChild();
            return label.getText();
        }
        return null;
    }

    /**
     * 执行初始化
     *
     * @param items 提示
     */
    public void init(List<String> items) {
        // 初始化数据
        List<FXHBox> boxList = new ArrayList<>();
        // 初始化节点内容
        if (CollectionUtil.isNotEmpty(items)) {
            for (String item : items) {
                FXHBox box = this.initBox(item);
                boxList.add(box);
            }
        }
        this.setItem(boxList);
    }

    /**
     * 初始化组件
     *
     * @return FXHBox 组件
     */
    private FXHBox initBox(String item) {
        FXHBox hBox = new FXHBox();
        // 设置高度
        hBox.setRealHeight(20);
        // 设置鼠标样式
        hBox.setCursor(Cursor.HAND);
        // 设置内边距
        hBox.setPadding(Insets.EMPTY);
        // 鼠标点击事件
        hBox.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (MouseUtil.isDubboClick(event)) {
                this.onItemPicked();
            }
        });
        // 组件
        FXLabel label = new FXLabel(item);
        hBox.addChild(label);
        return hBox;
    }

    @Override
    public void initNode() {
        super.initNode();
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<? extends MenuItem> items = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(items)) {
                this.showContextMenu(items, e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
    }

    private void onCopy() {
        String item = this.getPickedItem();
        ClipboardUtil.copy(item);
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        FXMenuItem run = MenuItemHelper.run( this::onItemPicked);
        FXMenuItem copy = MenuItemHelper.copy( this::onCopy);
        return List.of(run, copy);
    }
}
