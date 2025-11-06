package cn.oyzh.easyshell.tabs.mysql.table;

import cn.oyzh.easyshell.mysql.MysqlClient;
import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
import cn.oyzh.easyshell.tabs.mysql.MysqlTab;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.table.MysqlTableTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.List;

/**
 * db表tab
 *
 * @author oyzh
 * @since 2023/12/24
 */
public class MysqlTableRecordTab extends MysqlTab {

    {
        this.setClosable(true);
    }

    // /**
    //  * 标签打开时间
    //  */
    // private final long openedTime = System.currentTimeMillis();

    // private MysqlTableTreeItem item;

    @Override
    protected String url() {
        return FXConst.FXML_PATH + "table/mysqlTableRecordTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new TableSVGGlyph("13");
            // graphic = new SVGGlyph("/font/table.svg", "13");
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
     */
    public boolean init(MysqlTableTreeItem item) {
        // this.item = item;
        this.controller().init(item);
        // 刷新tab
        this.flush();
        // 加载耗时处理
        return true;
    }

    @Override
    public MysqlTableRecordTabController controller() {
        return (MysqlTableRecordTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reload();
    }

    public MysqlClient client() {
        return this.item().client();
    }

    public void setFilters(List<MysqlRecordFilter> filters) {
        this.controller().setFilters(filters);
    }

    public MysqlTableTreeItem item(){
        return this.controller().getItem();
    }
    
    public String tableName() {
        return this.item().tableName();
    }

    @Override
    public MysqlDatabaseTreeItem dbItem() {
        return this.item().dbItem();
    }

    public String dbName() {
        return this.item().dbName();
    }

    // @Override
    // protected void onTabClosed(Event event) {
    //     super.onTabClosed(event);
    //     System.out.println("------1");
    // }
}
