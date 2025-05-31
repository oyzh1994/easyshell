package cn.oyzh.easyshell.fx;

import cn.oyzh.easyshell.domain.ShellTermHistory;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

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

    /**
     * 选中项坐标
     */
    private volatile int currentPickIndex = -1;

    public int getCurrentPickIndex() {
        return currentPickIndex;
    }

    public void setCurrentPickIndex(int currentPickIndex) {
        this.currentPickIndex = currentPickIndex;
    }

    public Runnable getOnItemPicked() {
        return onItemPicked;
    }

    public void setOnItemPicked(Runnable onItemPicked) {
        this.onItemPicked = onItemPicked;
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
        // 应用背景色
        this.applyBackground(index);
    }

    /**
     * 选中下一个
     */
    public synchronized void pickNext() {
        this.select(this.currentPickIndex + 1);
    }

    /**
     * 选中上一个
     */
    public synchronized void pickPrev() {
        this.select(this.currentPickIndex - 1);
    }

    /**
     * 是否有选中项
     *
     * @return 结果
     */
    public synchronized boolean hasPicked() {
        FXHBox box = this.getSelectedItem();
        return box != null && this.currentPickIndex != -1;
    }

    /**
     * 获取选中项
     *
     * @return 结果
     */
    public ShellTermHistory getPickedItem() {
        FXHBox hBox = this.getSelectedItem();
        if (hBox != null) {
            ShellTermHistory item = hBox.getProp("item");
            if (item != null) {
                this.applyBackground(-1);
                return item;
            }
        }
        return null;
    }

    /**
     * 应用背景色
     *
     * @param pickedIndex 选择位置的索引
     */
    private void applyBackground(int pickedIndex) {
        if (this.currentPickIndex >= 0) {
            try {
                FXHBox hBox1 = (FXHBox) this.getItem(this.currentPickIndex);
                if (hBox1 != null) {
                    hBox1.setBackground(null);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (pickedIndex >= 0) {
            try {
                FXHBox hBox1 = (FXHBox) this.getItem(pickedIndex);
                if (hBox1 != null) {
                    hBox1.setBackground(ControlUtil.background(Color.DEEPSKYBLUE));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        this.currentPickIndex = pickedIndex;
    }

    /**
     * 执行初始化
     *
     * @param items 提示
     */
    public void init(List<ShellTermHistory> items) {
        // 应用背景色
        this.applyBackground(-1);
        // 初始化数据
        List<FXHBox> boxList = new ArrayList<>();
        // 初始化节点内容
        for (ShellTermHistory item : items) {
            FXHBox box = this.initBox(item);
            box.setProp("item", item);
            boxList.add(box);
        }
        this.setItem(boxList);
    }

    /**
     * 初始化组件
     *
     * @return FXHBox 组件
     */
    private FXHBox initBox(ShellTermHistory item) {
        FXHBox hBox = new FXHBox();
        FXLabel box = new FXLabel(item.getContent());
        // 设置高度
        box.setRealHeight(20);
        // 设置鼠标样式
        box.setCursor(Cursor.HAND);
        // 设置内边距
        box.setPadding(Insets.EMPTY);
        // 鼠标点击事件
        hBox.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (MouseUtil.isSingleClick(event)) {
                this.applyBackground(this.getItems().indexOf(hBox));
            } else if (this.onItemPicked != null) {
                this.onItemPicked.run();
            }
        });
        hBox.addChild(box);
        return hBox;
    }
}
