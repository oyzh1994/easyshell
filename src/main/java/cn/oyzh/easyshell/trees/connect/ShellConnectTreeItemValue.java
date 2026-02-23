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
        StringBuilder sb = new StringBuilder();
        if (this.setting.isConnectShowMoreInfo()) {
            if (StringUtil.isNotBlank(connect.getUser())) {
                sb.append(connect.getUser()).append("@");
            }
            if (StringUtil.isNotBlank(connect.getHost())) {
                sb.append(connect.getHost());
            }
        }
        if (this.setting.isConnectShowType()) {
            sb.append("@").append(type.toUpperCase());
        }
        if (sb.toString().startsWith("@")) {
            return sb.substring(1);
        }
        if (!sb.isEmpty()) {
            return sb.toString();
        }
        return null;
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
        if (this.item().isSSHType()) {
            return Color.valueOf("#2563EB");
        }
        if (this.item().isSFTPType()) {
            return Color.valueOf("#059669");
        }
        if (this.item().isFTPType()) {
            return Color.valueOf("#F97316");
        }
        if (this.item().isRedisType()) {
            return Color.valueOf("#7C3AED");
        }
        if (this.item().isZKType()) {
            return Color.valueOf("#DB2777");
        }
        if (this.item().isSerialType()) {
            return Color.valueOf("#0891B2");
        }
        if (this.item().isRloginType()) {
            return Color.valueOf("#D97706");
        }
        if (this.item().isLocalType()) {
            return Color.valueOf("#166534");
        }
        if (this.item().isTelnetType()) {
            return Color.valueOf("#5B21B6");
        }
        if (this.item().isS3Type()) {
            return Color.valueOf("#EC4899");
        }
        if (this.item().isSMBType()) {
            return Color.valueOf("#10B981");
        }
        if (this.item().isWebdavType()) {
            return Color.valueOf("#3B82F6");
        }
        if (this.item().isVNCType()) {
            return Color.valueOf("#C2410C");
        }
        if (this.item().isRDPType()) {
            return Color.valueOf("#4F46E5");
        }
        if (this.item().isMysqlType()) {
            return Color.valueOf("#CA8A04");
        }
        // 9F7AEA
        // 65A30D
        // B91C1C
        // 0E7490
        // 8B5CF6
        return Color.DARKGREY;
    }
}
