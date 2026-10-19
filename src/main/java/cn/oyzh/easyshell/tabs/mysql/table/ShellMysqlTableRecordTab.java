package cn.oyzh.easyshell.tabs.mysql.table;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
import cn.oyzh.easyshell.tabs.mysql.ShellMysqlBaseTab;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.table.ShellMysqlTableTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.List;

/**
 * MySQL 表记录标签页
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlTableRecordTab extends ShellMysqlBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/table/shellMysqlTableRecordTab.fxml";
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
        this.setText(this.item().tableName() + "@" + this.item().dbName() + "(" + this.item().infoName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 树键
     * @return 结果
     */
    public boolean init(ShellMysqlTableTreeItem item) {
        this.controller().init(item);
        // 刷新tab
        this.flush();
        // 加载耗时处理
        return true;
    }

    @Override
    public ShellMysqlTableRecordTabController controller() {
        return (ShellMysqlTableRecordTabController) super.controller();
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
    public ShellMysqlClient client() {
        return this.item().client();
    }

    /**
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<MysqlRecordFilter> filters) {
        this.controller().setFilters(filters);
    }

    /**
     * 获取树节点
     *
     * @return 树节点
     */
    public ShellMysqlTableTreeItem item(){
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
    public ShellMysqlDatabaseTreeItem dbItem() {
        return this.item().dbItem();
    }
}
