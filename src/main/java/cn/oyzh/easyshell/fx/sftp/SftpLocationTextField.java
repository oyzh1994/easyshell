package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import javafx.scene.control.Skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-27
 */
public class SftpLocationTextField extends FXTextField {

    {
        // 覆盖默认菜单
        this.setContextMenu(FXContextMenu.EMPTY);
    }

    @Override
    public void text(String text) {
        if (text == null) {
            super.clear();
            this.skin().setItemList(Collections.emptyList());
        } else {
            if (!text.equals("/") && text.endsWith("/")) {
                text = text.substring(0, text.length() - 1);
            }
            super.text(text);
            List<String> list = new ArrayList<>(this.skin().getItemList());
            list.remove(text);
            list.add(text);
            // 仅保留20条记录
            List<String> list1 = list.stream()
                    .skip(Math.max(0, list.size() - 20))
                    .collect(Collectors.toList());
            // 更新数据、异步，避免并发问题
            ThreadUtil.startVirtual(() -> this.skin().setItemList(list1));
        }
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
