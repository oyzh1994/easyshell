package cn.oyzh.easyshell.tabs.mysql.view;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
import cn.oyzh.easyshell.tabs.mysql.ShellMysqlBaseTab;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import javafx.scene.Cursor;

import java.util.List;

/**
 * MySQL 视图记录标签页
 *
 * @author oyzh
 * @since 2023/12/24
 */
public class ShellMysqlViewRecordTab extends ShellMysqlBaseTab {

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
            graphic = new ViewSVGGlyph();
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
     * @param item 视图树节点
     * @return 结果
     */
    public boolean init(ShellMysqlViewTreeItem item) {
        this.controller().init(item);
        this.flush();
        return true;
    }

    @Override
    public ShellMysqlViewRecordTabController controller() {
        return (ShellMysqlViewRecordTabController) super.controller();
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
    public ShellMysqlViewTreeItem item() {
        return this.controller().getItem();
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
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.item().viewName();
    }

    @Override
    public ShellMysqlDatabaseTreeItem dbItem() {
        return this.item().dbItem();
    }

    /**
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<MysqlRecordFilter> filters) {
        this.controller().setFilters(filters);
    }
}
