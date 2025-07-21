package cn.oyzh.easyshell.fx.snippet;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellSnippet;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * shell片段列表
 *
 * @author oyzh
 * @since 2025/07/21
 */
public class ShellSnippetListView extends FXListView<FXHBox> {

    {
        this.setPadding(Insets.EMPTY);
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
    }

    /**
     * 获取选中项
     *
     * @return 结果
     */
    public ShellSnippet getPickedItem() {
        FXHBox hBox = this.getSelectedItem();
        if (hBox != null) {
            return hBox.getProp("item");
        }
        return null;
    }

    /**
     * 执行初始化
     *
     * @param items 提示
     */
    public void init(List<ShellSnippet> items) {
        // 初始化数据
        List<FXHBox> boxList = new ArrayList<>();
        // 初始化节点内容
        if (CollectionUtil.isNotEmpty(items)) {
            for (ShellSnippet item : items) {
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
    private FXHBox initBox(ShellSnippet item) {
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
                this.onItemPicked.run();
            }
        });
        // 组件
        FXLabel label = new FXLabel(item.getName());
        hBox.setProp("item", item);
        hBox.addChild(label);
        return hBox;
    }
}
