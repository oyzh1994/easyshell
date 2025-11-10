package cn.oyzh.easyshell.tabs.mysql.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.view.MysqlView;
import cn.oyzh.easyshell.tabs.mysql.ShellMysqlBaseTab;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * db视图设计tab
 *
 * @author oyzh
 * @since 2023/12/24
 */
public class ShellMysqlViewDesignTab extends ShellMysqlBaseTab {

    {
        this.setClosable(true);
    }

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/view/shellMysqlViewDesignTab.fxml";
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
        String name = this.viewName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedView();
        }
        // 设置提示文本
        if (this.isUnsaved()) {
            this.setText("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.setText(name + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    // public String dbName() {
    //     return this.controller().dbName();
    // }

    public String viewName() {
        return this.controller().viewName();
    }

    @Override
    public MysqlDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param item 树键
     */
    public void init(MysqlView view, MysqlDatabaseTreeItem item) {
        this.controller().init(view, item);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellMysqlViewDesignTabController controller() {
        return (ShellMysqlViewDesignTabController) super.controller();
    }

    public boolean isUnsaved() {
        return this.controller().isUnsaved();
    }

    @Override
    protected void onTabCloseRequest(Event event) {
        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }
}
