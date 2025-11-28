package cn.oyzh.easyshell.fx.db;

import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.gui.skin.ActionTextFieldSkin;
import cn.oyzh.fx.gui.svg.glyph.CancelSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.EnlargeSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SubmitSVGGlyph;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.window.PopupExt;
import cn.oyzh.i18n.I18nHelper;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

/**
 * json文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/21
 */
public class DBJsonTextFiledSkin extends ActionTextFieldSkin {

    /**
     * 展开宽
     */
    protected double enlargeWidth = 350;

    /**
     * 展开高
     */
    protected double enlargeHeight = 280;

    /**
     * 弹窗组件
     */
    protected PopupExt popup;

    @Override
    protected void onButtonClicked(MouseEvent e) {
        if (this.popup == null) {
            this.popup = new PopupExt();
            this.popup.setWidth(this.enlargeWidth);
            this.popup.setHeight(this.enlargeHeight);
            this.popup.setOnHiding(windowEvent -> this.handleHide());
        }
        // 输入框
        TextField textField = this.getSkinnable();
        textField.setDisable(true);
        // 文本节点
        Editor textArea = new Editor();
        textArea.setFormatType(EditorFormatType.JSON);
        textArea.setRealWidth(this.enlargeWidth);
        textArea.realHeight(this.enlargeHeight - 30);
        // textArea.setPromptText(I18nHelper.pleaseInputContent());
        textArea.showData(this.getText());
        // 按钮
        SubmitSVGGlyph ok = new SubmitSVGGlyph("13");
        ok.setOnMousePrimaryClicked(event -> this.onSubmit(textArea.getTextTrim()));
        CancelSVGGlyph cancel = new CancelSVGGlyph("12");
        cancel.setOnMousePrimaryClicked(event -> this.handleHide());
        HBox.setMargin(ok, new Insets(5, 0, 0, 5));
        HBox.setMargin(cancel, new Insets(5, 0, 0, 15));
        FXHBox hBox = new FXHBox(ok, cancel);
        // 组装阶段
        FXVBox vBox = new FXVBox(textArea, hBox);
        this.popup.content(vBox);
        this.popup.setOnHiding(event -> this.onSubmit(textArea.getTextTrim()));
        this.popup.showPopup(textField);
    }

    protected void handleHide() {
        this.popup.hide();
        this.getSkinnable().setDisable(false);
        this.resetButtonColor();
    }

    /**
     * 内容提交事件
     *
     * @param text 内容
     */
    protected void onSubmit(String text) {
        if (text != null) {
            this.setText(text);
        }
        this.handleHide();
    }

    public DBJsonTextFiledSkin(TextField textField) {
        super(textField);
        // super(textField, new EnlargeSVGGlyph("13"));
        // this.button.disappear();
        // this.button.setTipText(I18nHelper.enlarge());
    }

    @Override
    protected SVGGlyph getButton() {
        if (super.button == null) {
            super.button = new EnlargeSVGGlyph("13");
            super.initButton(super.button);
        }
        return super.button;
    }

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean shouldBeVisible = !disable && visible && hasFocus;
        this.button.setVisible(shouldBeVisible);
    }

    public double getEnlargeWidth() {
        return enlargeWidth;
    }

    public void setEnlargeWidth(double enlargeWidth) {
        this.enlargeWidth = enlargeWidth;
    }

    public double getEnlargeHeight() {
        return enlargeHeight;
    }

    public void setEnlargeHeight(double enlargeHeight) {
        this.enlargeHeight = enlargeHeight;
    }

    public PopupExt getPopup() {
        return popup;
    }

    public void setPopup(PopupExt popup) {
        this.popup = popup;
    }

    @Override
    public void dispose() {
        NodeDestroyUtil.destroy(this.popup);
        this.popup = null;
        super.dispose();
    }
}
