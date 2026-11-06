package cn.oyzh.easyshell.tabs.postgresql.view;

import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordFilter;
import cn.oyzh.easyshell.tabs.postgresql.ShellPostgresqlBaseTab;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.trees.postgresql.view.ShellPostgresqlViewTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import javafx.scene.Cursor;

import java.util.List;

/**
 * PostgreSQL视图记录标签页，用于查看与维护视图数据
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlViewRecordTab extends ShellPostgresqlBaseTab {

    /**
     * 标签打开时间
     */
    private final long openedTime = System.currentTimeMillis();

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "postgresql/view/shellPostgresqlViewRecordTab.fxml";
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
        this.setText(this.item().viewName() + "@" + this.item().schema() + "(" + this.item().infoName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 树键
     * @return 是否初始化成功
     */
    public boolean init(ShellPostgresqlViewTreeItem item) {
        this.controller().init(item);
        this.flush();
        return true;
    }

    @Override
    public ShellPostgresqlViewRecordTabController controller() {
        return (ShellPostgresqlViewRecordTabController) super.controller();
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
    public ShellPostgresqlViewTreeItem item() {
        return this.controller().getItem();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellPostgresqlClient client() {
        return this.item().client();
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
    public ShellPostgresqlSchemaTreeItem dbItem() {
        return this.item().dbItem();
    }

    /**
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<PostgresqlRecordFilter> filters) {
        this.controller().setFilters(filters);
    }
}
