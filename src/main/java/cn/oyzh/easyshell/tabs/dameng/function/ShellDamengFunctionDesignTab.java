package cn.oyzh.easyshell.tabs.dameng.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.tabs.dameng.ShellDamengBaseTab;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * db查询tab
 *
 * @author oyzh
 * @since 2024/02/18
 */
public class ShellDamengFunctionDesignTab extends ShellDamengBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "function/damengFunctionDesignTab.fxml";
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
            this.setText("* " + name + "@" + this.schema() + "(" + this.connectName() + ")");
        } else {
            this.setText(name + "@" + this.schema() + "(" + this.connectName() + ")");
        }
    }

    public String functionName() {
        return this.controller().getFunction().getName();
    }

    @Override
    public ShellDamengSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    public String schema() {
        return this.dbItem().schema();
    }

    public String connectName() {
        return this.dbItem().connectName();
    }

    /**
     * 初始化
     *
     * @param function 查询对象
     * @param item     db库树节点
     */
    public void init(DamengFunction function, ShellDamengSchemaTreeItem item) {
        this.controller().init(function, item);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellDamengFunctionDesignTabController controller() {
        return (ShellDamengFunctionDesignTabController) super.controller();
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
