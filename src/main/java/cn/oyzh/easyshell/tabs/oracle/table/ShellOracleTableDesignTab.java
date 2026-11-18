package cn.oyzh.easyshell.tabs.oracle.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.tabs.oracle.ShellOracleBaseTab;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
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
 * @since 2025-11-06
 */
public class ShellOracleTableDesignTab extends ShellOracleBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "oracle/table/shellOracleTableDesignTab.fxml";
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
        String name = this.tableName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedTable();
        }
        // 设置提示文本
        if (this.isUnsaved()) {
            this.setText("* " + this.schema() + "-" + name);
        } else {
            this.setText(this.schema() + "-" + name);
        }
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.controller().tableName();
    }

    /**
     * 初始化
     *
     * @param table  表
     * @param dbItem db数据库树节点
     * @throws Exception 异常
     */
    public void init(OracleTable table, ShellOracleSchemaTreeItem dbItem) throws Exception {
        StageManager.showMask(this.getWindow(), () -> {
            try {
                this.controller().init(table, dbItem);
                this.flush();
            } catch (Exception ex) {
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    @Override
    public ShellOracleTableDesignTabController controller() {
        return (ShellOracleTableDesignTabController) super.controller();
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
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

    @Override
    public ShellOracleSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }

}
