package cn.oyzh.easyshell.tabs.mariadb.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
import cn.oyzh.easyshell.tabs.mariadb.ShellMariadbBaseTab;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * MariaDB 函数设计标签页
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbFunctionDesignTab extends ShellMariadbBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mariadb/function/shellMariadbFunctionDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        FunctionSVGGlyph graphic = (FunctionSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new FunctionSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.functionName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedFunction();
        }
        // 设置提示文本
        if (this.isUnsaved()) {
            this.setText("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.setText(name + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.controller().getFunction().getName();
    }

    @Override
    public ShellMariadbDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    // public String dbName() {
    //     return this.dbItem().dbName();
    // }
    //
    // public String connectName() {
    //     return this.dbItem().connectName();
    // }

    /**
     * 初始化
     *
     * @param function 函数对象
     * @param item     数据库树节点
     */
    public void init(MariadbFunction function, ShellMariadbDatabaseTreeItem item) {
        this.controller().init(function, item);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellMariadbFunctionDesignTabController controller() {
        return (ShellMariadbFunctionDesignTabController) super.controller();
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
        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }
}
