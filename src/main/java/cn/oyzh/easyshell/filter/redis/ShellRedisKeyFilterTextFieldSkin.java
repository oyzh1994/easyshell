package cn.oyzh.easyshell.filter.redis;

import cn.oyzh.easyshell.popups.redis.ShellRedisFilterSettingPopupController;
import cn.oyzh.fx.gui.skin.ClearableTextFieldSkin;
import cn.oyzh.fx.gui.svg.glyph.SettingSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;


/**
 * redis过滤文本输入框皮肤
 *
 * @author oyzh
 * @since 2025/01/23
 */
public class ShellRedisKeyFilterTextFieldSkin extends ClearableTextFieldSkin {

    /**
     * 过滤设置按钮
     */
    protected SVGGlyph setting;

    /**
     * 过滤设置弹窗
     */
    protected PopupAdapter popup;

    public PopupAdapter getPopup() {
        return popup;
    }

    /**
     * 过滤参数
     */
    private ShellRedisKeyFilterParam filterParam;

    public ShellRedisKeyFilterParam filterParam() {
        if (this.filterParam == null) {
            this.filterParam = new ShellRedisKeyFilterParam();
        }
        return this.filterParam;
    }

    /**
     * 显示弹窗
     */
    protected void showPopup() {
        if (this.popup != null) {
            this.closePopup();
        }
        this.popup = PopupManager.parsePopup(ShellRedisFilterSettingPopupController.class);
        this.popup.setProp("filterParam", this.filterParam());
        this.popup.setSubmitHandler(o -> {
            if (o instanceof ShellRedisKeyFilterParam param) {
                this.filterParam = param;
                this.onSearch(this.getText());
            }
        });
        this.popup.show(this.setting);
    }

    /**
     * 关闭历史弹窗组件
     */
    protected void closePopup() {
        if (this.popup != null && this.popup.isShowing()) {
            this.popup.hide();
        }
    }

    public ShellRedisKeyFilterTextFieldSkin(TextField textField) {
        super(textField);
//         // 初始化按钮
//         this.button = new SettingSVGGlyph();
//         this.button.setEnableWaiting(false);
// //        this.button.setFocusTraversable(false);
//         this.button.setOnMousePrimaryClicked(e -> this.showPopup());
//         this.button.setOnMouseMoved(mouseEvent -> this.button.setColor("#E36413"));
//         this.button.setOnMouseExited(mouseEvent -> this.button.setColor(this.getButtonColor()));
//         this.getChildren().add(this.button);
        // 鼠标监听
        this.getSkinnable().addEventHandler(MouseEvent.MOUSE_PRESSED, event -> this.closePopup());
        // 按键监听
        this.getSkinnable().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.onSearch(this.getText());
            }
        });
        // 文本变化监听
        this.getSkinnable().textProperty().addListener((observable, oldValue, newValue) -> this.onSearch(this.getText()));
    }

    @Override
    public ObjectProperty<Node> leftProperty() {
        if (this.leftProperty == null) {
            this.setting = new SettingSVGGlyph();
            this.setting.setPadding(Insets.EMPTY);
            this.setting.setFocusTraversable(false);
            this.setting.setOnMousePrimaryClicked(e -> this.showPopup());
            this.setting.setOnMouseEntered(mouseEvent -> this.setting.setColor("#E36413"));
            this.setting.setOnMouseExited(mouseEvent -> this.setting.setColor(this.getButtonColor()));
            super.leftProperty().set(this.setting);
        }
        return super.leftProperty();
    }

    /**
     * 搜索触发事件
     *
     * @param text 当前内容
     */
    public void onSearch(String text) {
        this.closePopup();
    }

//    @Override
//    protected Color getButtonColor() {
//        if (!ThemeManager.isDarkMode()) {
//            return Color.valueOf("#696969");
//        }
//        return super.getButtonColor();
//    }

    // @Override
    // protected void layoutChildren(double x, double y, double w, double h) {
    //     super.layoutChildren(x, y, w, h);
    //     // 组件大小
    //     double size = h * .8;
    //     // 计算组件大小
    //     double btnSize = this.snapSizeX(size);
    //     // 设置组件大小
    //     this.button.setSize(size);
    //     // 获取边距
    //     Insets padding = this.getSkinnable().getPadding();
    //     // 计算左边距
    //     double paddingLeft = btnSize + 8;
    //     // 设置左边距
    //     if (padding.getLeft() != paddingLeft) {
    //         padding = new Insets(padding.getTop(), padding.getRight(), padding.getBottom(), paddingLeft);
    //         this.getSkinnable().setPadding(padding);
    //     }
    //     // 设置组件位置
    //     // super.positionInArea(this.button, 3, y * 0.9, w, h, btnSize, HPos.LEFT, VPos.CENTER);
    //     super.positionInArea(this.button, 3, y * 0.9, 0, h, btnSize, HPos.LEFT, VPos.CENTER);
    // }

    @Override
    public void dispose() {
        NodeDestroyUtil.destroy(this.popup);
        this.popup = null;
        NodeDestroyUtil.destroy(this.setting);
        this.setting = null;
        super.dispose();
    }
}
