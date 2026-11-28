package cn.oyzh.easyshell.tabs.h2.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.tabs.h2.ShellH2BaseTab;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.fx.gui.svg.glyph.EditSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * H2 表设计标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2TableDesignTab extends ShellH2BaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "h2/table/shellH2TableDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new EditSVGGlyph();
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
        if (this.isUnsaved()) {
            this.text("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.text(name + "@" + this.dbName() + "(" + this.connectName() + ")");
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
     * 获取表所属模式。
     *
     * @return 模式名称
     */
    public String schema() {
        return this.controller().schema();
    }

    /**
     * 初始化
     *
     * @param table  表对象
     * @param dbItem 数据库树节点
     */
    public void init(H2Table table, ShellH2RootTreeItem dbItem) {
        StageManager.showMask(this.getWindow(), () -> {
            try {
                this.controller().init(table, dbItem);
                this.flush();
            } catch (Exception ex) {
                MessageBox.exception(ex, this.getWindow());
            }
        });
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
    public ShellH2TableDesignTabController controller() {
        return (ShellH2TableDesignTabController) super.controller();
    }

    @Override
    public ShellH2RootTreeItem dbItem() {
        return this.controller().getDbItem();
    }
}
