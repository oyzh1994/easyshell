package cn.oyzh.easyshell.tabs.mongo.view;

import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.record.MongoRecordFilter;
import cn.oyzh.easyshell.tabs.mongo.ShellMongoBaseTab;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.view.ShellMongoViewTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import javafx.scene.Cursor;

import java.util.List;

/**
 * MongoDB 视图记录标签页。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoViewRecordTab extends ShellMongoBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/view/shellMongoViewRecordTab.fxml";
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
        this.text(this.item().viewName() + "@" + this.item().dbName()
                + "(" + this.item().connectName() + ")");
    }

    public void init(ShellMongoViewTreeItem item) {
        this.controller().init(item);
        this.flush();
    }

    @Override
    public ShellMongoViewRecordTabController controller() {
        return (ShellMongoViewRecordTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reload();
    }

    public ShellMongoClient client() {
        return this.item().client();
    }

    public void setFilters(List<MongoRecordFilter> filters) {
        this.controller().setFilters(filters);
    }

    public ShellMongoViewTreeItem item() {
        return this.controller().getItem();
    }

    public String viewName() {
        return this.item().viewName();
    }

    @Override
    public ShellMongoDatabaseTreeItem dbItem() {
        return this.item().dbItem();
    }
}
