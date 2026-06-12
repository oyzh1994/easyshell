package cn.oyzh.easyshell.query;

import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询提示列表
 *
 * @author oyzh
 * @since 2024/02/21
 */
public abstract class ShellQueryPromptListView<E extends ShellQueryPromptItem> extends FXListView<FXHBox> {

    /**
     * 选中项坐标
     */
    protected volatile int currentPickIndex = -1;

    /**
     * 节点选中事件
     */
    protected Runnable onItemPicked;

    @Override
    public void select(int index) {
        if (index < 0) {
            index = 0;
        }
        if (index >= this.getItemSize()) {
            index = this.getItemSize() - 1;
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
    public E getPickedItem() {
        FXHBox hBox = this.getSelectedItem();
        if (hBox != null) {
            E item = hBox.getProp("item");
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
    protected void applyBackground(int pickedIndex) {
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
    public void init(List<E> items) {
        // 应用背景色
        this.applyBackground(-1);
        // 初始化数据
        List<FXHBox> boxList = new ArrayList<>();
        // 初始化节点内容
        for (E item : items) {
            // 初始化组件
            FXHBox box = new FXHBox();
            this.initBox(box);
            // 提示组件
            SVGLabel promptLabel = this.initPromptLabel(item);
            box.addChild(promptLabel);
            // 额外组件
            FXLabel extLabel = this.initExtLabel(item);
            if (extLabel != null) {
                box.addChild(extLabel);
            }
            box.setProp("item", item);
            boxList.add(box);
        }
        this.setItem(boxList);
    }

    /**
     * 初始化提示组件
     *
     * @param item 提示词
     * @return 组件
     */
    protected abstract SVGLabel initPromptLabel(E item);

    /**
     * 初始化额外信息组件
     *
     * @param item 提示词
     * @return 组件
     */
    protected abstract FXLabel initExtLabel(E item);

    /**
     * 初始化提示词组件
     *
     * @param box 提示词组件
     */
    protected void initBox(FXHBox box) {
        // 设置高度
        box.setRealHeight(20);
        // 设置内边距
        box.setPadding(new Insets(0));
        // 设置鼠标样式
        box.setCursor(Cursor.HAND);
        // 鼠标点击事件
        box.setOnMouseClicked(event -> {
            if (MouseUtil.isSingleClick(event)) {
                this.applyBackground(this.getItems().indexOf(box));
            } else if (this.onItemPicked != null) {
                this.onItemPicked.run();
            }
        });
    }

    public Runnable getOnItemPicked() {
        return onItemPicked;
    }

    public void setOnItemPicked(Runnable onItemPicked) {
        this.onItemPicked = onItemPicked;
    }

    @Override
    public void initNode() {
        this.setRealWidth(360);
        this.setRealHeight(240);
        this.setPadding(Insets.EMPTY);
        super.initNode();
    }
}
