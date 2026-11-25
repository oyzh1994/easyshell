package cn.oyzh.easyshell.tabs.mongo.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mongo.view.MongoView;
import cn.oyzh.easyshell.tabs.mongo.ShellMongoBaseTab;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * MongoDB 视图设计标签页。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoViewDesignTab extends ShellMongoBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/view/shellMongoViewDesignTab.fxml";
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
        String name = this.viewName();
        if (StringUtil.isBlank(name)) {
            name = ShellI18nHelper.mongoUnnamedView();
        }
        if (this.isUnsaved()) {
            this.text("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.text(name + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    public void init(MongoView view, ShellMongoDatabaseTreeItem dbItem) {
        this.controller().init(view, dbItem);
        this.flush();
    }

    public String viewName() {
        return this.controller().viewName();
    }

    public String dbName() {
        return this.dbItem().dbName();
    }

    public String connectName() {
        return this.dbItem().connectName();
    }

    @Override
    public ShellMongoViewDesignTabController controller() {
        return (ShellMongoViewDesignTabController) super.controller();
    }

    @Override
    public ShellMongoDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    public boolean isUnsaved() {
        return this.controller().isUnsaved();
    }

    @Override
    protected void onTabCloseRequest(Event event) {
        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue(), this.getWindow())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }
}
