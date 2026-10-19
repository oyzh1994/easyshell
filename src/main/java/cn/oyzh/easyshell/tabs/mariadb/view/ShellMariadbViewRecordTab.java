package cn.oyzh.easyshell.tabs.mariadb.view;

import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordFilter;
import cn.oyzh.easyshell.tabs.mariadb.ShellMariadbBaseTab;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mariadb.view.ShellMariadbViewTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import javafx.scene.Cursor;

import java.util.List;

/**
 * MariaDB 视图记录标签页
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbViewRecordTab extends ShellMariadbBaseTab {

    /**
     * 标签打开时间
     */
    private final long openedTime = System.currentTimeMillis();

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mariadb/view/shellMariadbViewRecordTab.fxml";
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
    public boolean init(ShellMariadbViewTreeItem item) {
        this.controller().init(item);
        this.flush();
        return true;
    }

    @Override
    public ShellMariadbViewRecordTabController controller() {
        return (ShellMariadbViewRecordTabController) super.controller();
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
    public ShellMariadbViewTreeItem item() {
        return this.controller().getItem();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellMariadbClient client() {
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
    public ShellMariadbDatabaseTreeItem dbItem() {
        return this.item().dbItem();
    }

    /**
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<MariadbRecordFilter> filters) {
        this.controller().setFilters(filters);
    }
}
