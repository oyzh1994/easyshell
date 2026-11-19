package cn.oyzh.easyshell.tabs.oracle.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.function.OracleFunction;
import cn.oyzh.easyshell.tabs.oracle.ShellOracleBaseTab;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * Oracle函数设计标签页，负责函数信息的展示与设计维护
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleFunctionDesignTab extends ShellOracleBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "oracle/function/shellOracleFunctionDesignTab.fxml";
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
            this.text("* " + name + "@" + this.schema() + "(" + this.connectName() + ")");
        } else {
            this.text(name + "@" + this.schema() + "(" + this.connectName() + ")");
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
    public ShellOracleSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param function 查询对象
     * @param item     db库树节点
     */
    public void init(OracleFunction function, ShellOracleSchemaTreeItem item) {
        this.controller().init(function, item);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellOracleFunctionDesignTabController controller() {
        return (ShellOracleFunctionDesignTabController) super.controller();
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
}
