package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * shell树节点值
 *
 * @author oyzh
 * @since 2025/4/7
 */
public class ShellConnectTreeItemValue extends RichTreeItemValue {

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    public ShellConnectTreeItemValue(ShellConnectTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    protected ShellConnectTreeItem item() {
        return (ShellConnectTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().value().getName();
    }

    @Override
    public String extra() {
        ShellConnect connect = this.item().value();
        String type = connect.getType();
        StringBuilder sb = new StringBuilder("(");
        if (this.setting.isConnectShowMoreInfo()) {
            if (StringUtil.isNotBlank(connect.getUser())) {
                sb.append(connect.getUser()).append("@");
            }
            sb.append(connect.getHost()).append("/").append(type.toUpperCase());
        } else {
            sb.append(type.toUpperCase());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = ShellOsTypeComboBox.getGlyph(this.item().value().getOsType());
            this.graphic.setSizeStr("12");
        }
        return super.graphic();
    }

    @Override
    public Color extraColor() {
        return Color.DARKGREY;
    }
}
