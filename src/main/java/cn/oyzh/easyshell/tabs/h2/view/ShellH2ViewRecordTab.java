package cn.oyzh.easyshell.tabs.h2.view;

import cn.oyzh.easyshell.h2.record.H2RecordFilter;
import cn.oyzh.easyshell.tabs.h2.ShellH2BaseTab;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import javafx.scene.Cursor;

import java.util.List;

/**
 * H2 视图记录标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2ViewRecordTab extends ShellH2BaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "h2/view/shellH2ViewRecordTab.fxml";
    }

    @Override
    public void flushGraphic() {
        ViewSVGGlyph graphic = (ViewSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new ViewSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        this.text(this.item().viewName() + "@" + this.item().dbName() + "(" + this.item().infoName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 视图树节点
     * @return 结果
     */
    public boolean init(ShellH2ViewTreeItem item) {
        this.controller().init(item);
        this.flush();
        return true;
    }

    @Override
    public ShellH2ViewRecordTabController controller() {
        return (ShellH2ViewRecordTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reload();
    }

    /**
     * 获取树节点
     *
     * @return 树节点
     */
    public ShellH2ViewTreeItem item() {
        return this.controller().getItem();
    }

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.item().viewName();
    }

    @Override
    public ShellH2RootTreeItem dbItem() {
        return this.item().dbItem();
    }

    /**
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<H2RecordFilter> filters) {
        this.controller().setFilters(filters);
    }
}
