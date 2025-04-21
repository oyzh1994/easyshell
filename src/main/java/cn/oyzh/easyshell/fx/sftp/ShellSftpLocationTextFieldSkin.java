package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.fx.gui.skin.SelectTextFiledSkin;
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
public class ShellSftpLocationTextFieldSkin extends SelectTextFiledSkin {

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

    public ShellSftpLocationTextFieldSkin(TextField textField) {
        super(textField);
        // 设置选中事件
        super.setSelectIndexChanged((observable, oldValue, newValue) -> {
            this.onJumpLocation(this.getText());
        });
        // 按键监听
        this.getSkinnable().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.onJumpLocation(this.getText());
                event.consume();
            }
        });
    }

    @Override
    protected void onButtonClicked(MouseEvent e) {
        super.onButtonClicked(e);
    }

//    @Override
//    protected void updateButtonVisibility() {
//        boolean visible = this.getSkinnable().isVisible();
//        boolean disable = this.getSkinnable().isDisable();
//        boolean hasFocus = this.getSkinnable().isFocused();
//        boolean shouldBeVisible = !disable && visible && hasFocus;
//        this.button.setVisible(shouldBeVisible);
//    }
}
