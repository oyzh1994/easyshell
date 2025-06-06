package cn.oyzh.easyshell.fx.connect;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;

import java.util.List;
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
            this.setSkin(this.createDefaultSkin());
        }
        return skin;
    }

    @Override
    protected ShellSSHConnectTextFieldSkin createDefaultSkin() {
        ShellSSHConnectTextFieldSkin skin = new ShellSSHConnectTextFieldSkin(this);
        ShellConnectStore store = ShellConnectStore.INSTANCE;
        List<ShellConnect> connects = store.load();
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
            if (this.skin().isSetTexting()) {
                return;
            }
            if (StringUtil.isBlank(newValue)) {
                this.skin().setItemList(connects);
                this.skin().hidePopup();
                return;
            }
            List<ShellConnect> newList = connects.stream()
                    .filter(t -> StringUtil.containsIgnoreCase(t.getName(), newValue))
                    .collect(Collectors.toList());
            this.skin().setItemList(newList);
            if (newList.isEmpty()) {
                this.skin().hidePopup();
            } else {
                this.skin().showPopup();
            }
        });
        return skin;
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
}
