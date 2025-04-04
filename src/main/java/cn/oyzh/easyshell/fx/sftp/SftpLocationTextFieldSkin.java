package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.fx.gui.skin.ActionTextFieldSkin;
import cn.oyzh.fx.gui.svg.glyph.CopySVGGlyph;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

/**
 * 搜索文本输入框皮肤
 *
 * @author oyzh
 * @since 2023/10/9
 */
public class SftpLocationTextFieldSkin extends ActionTextFieldSkin {

    /**
     * 跳转路径事件
     */
    private Consumer<String> onJumpLocation;

    public void setOnJumpLocation(Consumer<String> onJumpLocation) {
        this.onJumpLocation = onJumpLocation;
    }

    public Consumer<String> getOnJumpLocation() {
        return onJumpLocation;
    }

    /**
     * 跳转路径事件
     *
     * @param text 文本
     */
    protected void onJumpLocation(String text) {
        if (this.onJumpLocation != null) {
            this.onJumpLocation.accept(text);
        }
    }

    public SftpLocationTextFieldSkin(TextField textField) {
        super(textField, new CopySVGGlyph("13"));
        this.button.disappear();
        this.button.setTipText(I18nHelper.copyFilePath());
        // 按键监听
        this.getSkinnable().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.onJumpLocation(this.getText());
            }
        });
    }

    @Override
    protected void onButtonClicked(MouseEvent e) {
        ClipboardUtil.copy(this.getText());
    }

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean shouldBeVisible = !disable && visible && hasFocus;
        this.button.setVisible(shouldBeVisible);
    }
}
