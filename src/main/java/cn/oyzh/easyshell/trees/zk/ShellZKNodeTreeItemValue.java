package cn.oyzh.easyshell.trees.zk;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.svg.glyph.zk.NodeSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.zk.TempSVGGlyph;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.gui.svg.glyph.LockSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * zk树节点值
 *
 * @author oyzh
 * @since 2023/4/7
 */
public class ShellZKNodeTreeItemValue extends RichTreeItemValue {

    /**
     * 当前设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    public ShellZKNodeTreeItemValue(ShellZKNodeTreeItem item) {
        super(item);
        super.setRichMode(true);
    }

    @Override
    protected ShellZKNodeTreeItem item() {
        return (ShellZKNodeTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic != null && this.graphic.isWaiting()) {
//            this.graphic.enableTheme();
            return this.graphic;
        }
        boolean changed = false;
        if (this.graphic == null) {
            changed = true;
        } else if (this.item().isNeedAuth() && StringUtil.notContains(this.graphic.getUrl(), "lock")) {
            changed = true;
        } else if (this.item().isEphemeralNode() && StringUtil.notContains(this.graphic.getUrl(), "temp")) {
            changed = true;
        } else if (StringUtil.notContains(this.graphic.getUrl(), "file-text")) {
            changed = true;
        }
        if (changed) {
            if (this.item().isNeedAuth()) {
                this.graphic = new LockSVGGlyph("11");
            } else if (this.item().isEphemeralNode()) {
                this.graphic = new TempSVGGlyph("11");
            } else {
                this.graphic = new NodeSVGGlyph("11");
            }
            this.graphic.disableTheme();
            if (!this.item().isParentNode()) {
                this.graphic.disableWaiting();
            }
            this.graphic.setColor(this.graphicColor());
        }
        return super.graphic();
    }

    @Override
    public String extra() {
        String extra;
        int totalNum = this.item().getNumChildren();
        int showNum = this.item().itemChildrenSize();
        if (totalNum == 0) {
            extra = null;
        } else if (showNum == totalNum) {
            extra = "(" + totalNum + ")";
        } else {
            extra = "(" + showNum + "/" + totalNum + ")";
        }
        return extra;
    }

    @Override
    public Color extraColor() {
        return Color.FORESTGREEN;
    }

    @Override
    public Color graphicColor() {
        Color color;
        //// 节点已删除
        //if (this.item().isBeDeleted()) {
        //    color = Color.RED;
        //} 
        if (this.item().isDataUnsaved()) { // 节点数据未保存
            color = Color.ORANGE;
        //} else if (this.item().isBeChanged()) { // 节点已更新
        //    color = Color.PURPLE;
        //} else if (this.item().isBeChildChanged()) {// 子节点已更新
        //    color = Color.BROWN;
        } else {
            color = super.graphicColor();
        }
        return color;
    }

    @Override
    public String name() {
        if (this.setting.isShowNodePath()) {
            return this.item().decodeNodePath();
        }
        return this.item().decodeNodeName();
    }
}
