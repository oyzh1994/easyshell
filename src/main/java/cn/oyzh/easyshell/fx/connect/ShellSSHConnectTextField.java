package cn.oyzh.easyshell.fx.connect;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 连接输入框，可搜索
 *
 * @author oyzh
 * @since 2025-06-06
 */
public class ShellSSHConnectTextField extends FXTextField {

    {
        // 覆盖默认菜单
        this.setContextMenu(FXContextMenu.EMPTY);
        this.setTipText(I18nHelper.pleaseSelectConnect());
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public ShellSSHConnectTextFieldSkin skin() {
        ShellSSHConnectTextFieldSkin skin = (ShellSSHConnectTextFieldSkin) this.getSkin();
        if (skin == null) {
            skin = this.createDefaultSkin();
            this.setSkin(skin);
        }
        return skin;
    }

    @Override
    protected ShellSSHConnectTextFieldSkin createDefaultSkin() {
        AtomicReference<ShellSSHConnectTextFieldSkin> ref = new AtomicReference<>();
        FXUtil.runWait(() -> {
            ShellSSHConnectTextFieldSkin skin = new ShellSSHConnectTextFieldSkin(this);
            ShellConnectStore store = ShellConnectStore.INSTANCE;
            // 仅ssh连接
            List<ShellConnect> connects = store.loadSSH();
            skin.setItemList(connects);
            skin.setConverter(new SimpleStringConverter<>() {
                @Override
                public String toString(ShellConnect o) {
                    if (o == null) {
                        return "";
                    }
                    return o.getName();
                }
            });
            this.addTextChangeListener((observable, oldValue, newValue) -> {
                if (this.skin().isTexting()) {
                    return;
                }
                // 移除选区
                this.skin().clearSelection();
                // 隐藏弹窗
                if (StringUtil.isBlank(newValue)) {
                    this.skin().setItemList(connects);
                    this.skin().hidePopup();
                    return;
                }
                // 过滤内容
                List<ShellConnect> newList = connects.stream()
                        .filter(t -> StringUtil.containsIgnoreCase(t.getName(), newValue))
                        .collect(Collectors.toList());
                // 设置内容
                this.skin().setItemList(newList);
                // 内容为空，隐藏弹窗
                if (newList.isEmpty()) {
                    this.skin().hidePopup();
                } else {
                    this.skin().showPopup();
                }
            });
            ref.set(skin);
        });
        return ref.get();
    }

    public void select(ShellConnect connect) {
        this.skin().selectItem(connect);
    }

    public ShellConnect getSelectedItem() {
        return this.skin().getSelectedItem();
    }

    public void selectedItemChanged(ChangeListener<ShellConnect> listener) {
        this.skin().selectItemChanged(listener);
    }

    @Override
    public boolean validate() {
        if (this.isRequire() && this.skin().getSelectedItem() == null) {
            return false;
        }
        return super.validate();
    }
}
