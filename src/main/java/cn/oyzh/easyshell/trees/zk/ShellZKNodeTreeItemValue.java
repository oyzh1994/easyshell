package cn.oyzh.easyshell.trees.zk;

import cn.oyzh.common.util.StringUtil;
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

    //    /**
    //     * 当前设置
    //     */
    //    private final ShellSetting setting = ShellSettingStore.SETTING;

    public ShellZKNodeTreeItemValue(ShellZKNodeTreeItem item) {
        super(item);
        super.setRichMode(true);
    }

    @Override
    public ShellZKNodeTreeItem item() {
        return (ShellZKNodeTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() != null && super.graphic().isWaiting()) {
            //            this.graphic.enableTheme();
            return super.graphic();
        }
        boolean changed = false;
        if (super.graphic() == null) {
            changed = true;
        } else if (this.item().isNeedAuth() && StringUtil.notContains(super.graphic().getUrl(), "lock")) {
            changed = true;
        } else if (this.item().isEphemeralNode() && StringUtil.notContains(super.graphic().getUrl(), "temp")) {
            changed = true;
        } else if (StringUtil.notContains(super.graphic().getUrl(), "file-text")) {
            changed = true;
        }
        if (changed) {
            if (this.item().isNeedAuth()) {
                super.graphic(new LockSVGGlyph("12"));
            } else if (this.item().isEphemeralNode()) {
                super.graphic(new TempSVGGlyph("12"));
            } else {
                super.graphic(new NodeSVGGlyph("12"));
            }
            super.graphic().disableTheme();
            if (!this.item().isParentNode()) {
                super.graphic().disableWaiting();
            }
            super.graphic().setColor(this.graphicColor());
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

    /**
     * 是否显示节点路径
     */
    private final boolean showNodePath = ShellSettingStore.SETTING.isShowNodePath();

    @Override
    public String name() {
        if (this.showNodePath) {
            return this.item().decodeNodePath();
        }
        return this.item().decodeNodeName();
    }
}
