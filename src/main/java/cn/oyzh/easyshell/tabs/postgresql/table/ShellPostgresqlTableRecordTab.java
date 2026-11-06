package cn.oyzh.easyshell.tabs.postgresql.table;

import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordFilter;
import cn.oyzh.easyshell.tabs.postgresql.ShellPostgresqlBaseTab;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.trees.postgresql.table.ShellPostgresqlTableTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.List;

/**
 * db表tab
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlTableRecordTab extends ShellPostgresqlBaseTab {

    // /**
    //  * 标签打开时间
    //  */
    // private final long openedTime = System.currentTimeMillis();

    // private PostgresqlTableTreeItem item;

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "postgresql/table/shellPostgresqlTableRecordTab.fxml";
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
        // 设置提示文本
        this.setText(this.item().tableName() + "@" + this.item().schema() + "(" + this.item().infoName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 树键
     * @return 是否初始化成功
     */
    public boolean init(ShellPostgresqlTableTreeItem item) {
        // this.item = item;
        this.controller().init(item);
        // 刷新tab
        this.flush();
        // 加载耗时处理
        return true;
    }

    @Override
    public ShellPostgresqlTableRecordTabController controller() {
        return (ShellPostgresqlTableRecordTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reload();
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
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<PostgresqlRecordFilter> filters) {
        this.controller().setFilters(filters);
    }

    /**
     * 获取树节点
     *
     * @return 树节点
     */
    public ShellPostgresqlTableTreeItem item(){
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
    public ShellPostgresqlSchemaTreeItem dbItem() {
        return this.item().dbItem();
    }
}
