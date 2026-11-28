package cn.oyzh.easyshell.tabs.h2.table;

import cn.oyzh.easyshell.h2.record.H2RecordFilter;
import cn.oyzh.easyshell.tabs.h2.ShellH2BaseTab;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TableTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.List;

/**
 * H2 表记录标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2TableRecordTab extends ShellH2BaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "h2/table/shellH2TableRecordTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new TableSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        this.text(this.item().tableName() + "@" + this.item().dbName() + "(" + this.item().infoName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 表树节点
     * @return 结果
     */
    public boolean init(ShellH2TableTreeItem item) {
        this.controller().init(item);
        this.flush();
        return true;
    }

    @Override
    public ShellH2TableRecordTabController controller() {
        return (ShellH2TableRecordTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reload();
    }

    /**
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<H2RecordFilter> filters) {
        this.controller().setFilters(filters);
    }

    /**
     * 获取树节点
     *
     * @return 树节点
     */
    public ShellH2TableTreeItem item() {
        return this.controller().getItem();
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.item().tableName();
    }

    @Override
    public ShellH2RootTreeItem dbItem() {
        return this.item().dbItem();
    }
}
