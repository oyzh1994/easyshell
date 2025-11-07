package cn.oyzh.easyshell.tabs.mysql.view;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
import cn.oyzh.easyshell.tabs.mysql.ShellMysqlBaseTab;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.MysqlViewTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import javafx.scene.Cursor;

import java.util.List;

/**
 * db表tab
 *
 * @author oyzh
 * @since 2023/12/24
 */
public class MysqlViewRecordTab extends ShellMysqlBaseTab {

    {
        this.setClosable(true);
    }

    /**
     * 标签打开时间
     */
    private final long openedTime = System.currentTimeMillis();

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/view/shellMysqlViewRecordTab.fxml";
    }

    @Override
    public void flushGraphic() {
        ViewSVGGlyph graphic = (ViewSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new ViewSVGGlyph("13");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        this.setText(this.item().viewName() + "@" + this.item().dbName() + "(" + this.item().infoName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 树键
     */
    public boolean init(MysqlViewTreeItem item) {
        this.controller().init(item);
        this.flush();
        return true;
    }

    @Override
    public MysqlViewRecordTabController controller() {
        return (MysqlViewRecordTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reload();
    }

    public MysqlViewTreeItem item() {
        return this.controller().getItem();
    }

    public ShellMysqlClient client() {
        return this.item().client();
    }

    public String viewName() {
        return this.item().viewName();
    }

    @Override
    public MysqlDatabaseTreeItem dbItem() {
        return this.item().dbItem();
    }

    public void setFilters(List<MysqlRecordFilter> filters) {
        this.controller().setFilters(filters);
    }
}
