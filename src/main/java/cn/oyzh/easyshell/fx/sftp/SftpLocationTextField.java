package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import javafx.scene.control.Skin;

import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-27
 */
public class SftpLocationTextField extends FXTextField {

    {
        // 覆盖默认菜单
        this.setContextMenu(FXContextMenu.EMPTY);
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public SftpLocationTextFieldSkin skin() {
        SftpLocationTextFieldSkin skin = (SftpLocationTextFieldSkin) this.getSkin();
        if (skin == null) {
            this.setSkin(this.createDefaultSkin());
            skin = (SftpLocationTextFieldSkin) this.getSkin();
        }
        return skin;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SftpLocationTextFieldSkin(this);
    }

    public void setOnJumpLocation(Consumer<String> onJumpLocation) {
        this.skin().setOnJumpLocation(onJumpLocation);
    }

    public Consumer<String> getOnJumpLocation() {
        return this.skin().getOnJumpLocation();
    }

}
