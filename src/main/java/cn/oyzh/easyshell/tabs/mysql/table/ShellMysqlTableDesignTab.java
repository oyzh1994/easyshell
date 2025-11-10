package cn.oyzh.easyshell.tabs.mysql.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.tabs.mysql.ShellMysqlBaseTab;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * db表设计tab
 *
 * @author oyzh
 * @since 2024/08/07
 */
public class ShellMysqlTableDesignTab extends ShellMysqlBaseTab {

    {
        this.setClosable(true);
    }

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/table/shellMysqlTableDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new TableSVGGlyph("13");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.tableName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedTable();
        }
        // 设置提示文本
        if (this.isUnsaved()) {
            this.setText("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.setText(name + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    public String tableName() {
        return this.controller().tableName();
    }

    // public String dbName() {
    //     return this.controller().dbName();
    // }

    /**
     * 初始化
     *
     * @param table  表
     * @param dbItem db数据库树节点
     */
    public void init(MysqlTable table, MysqlDatabaseTreeItem dbItem) throws Exception {
        StageManager.showMask(() -> {
            try {
                this.controller().init(table, dbItem);
                this.flush();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    @Override
    public ShellMysqlTableDesignTabController controller() {
        return (ShellMysqlTableDesignTabController) super.controller();
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

    @Override
    public MysqlDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }
}
