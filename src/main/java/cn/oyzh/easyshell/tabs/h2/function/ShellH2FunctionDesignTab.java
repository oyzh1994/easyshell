package cn.oyzh.easyshell.tabs.h2.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.routine.H2Routine;
import cn.oyzh.easyshell.tabs.h2.ShellH2BaseTab;
import cn.oyzh.easyshell.trees.h2.function.ShellH2FunctionsTreeItem;
import cn.oyzh.easyshell.trees.h2.function.ShellH2RoutinesTreeItem;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * H2 函数设计标签页。
 *
 * @since 2026-11-28
 */
public class ShellH2FunctionDesignTab extends ShellH2BaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "h2/function/shellH2FunctionDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
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
            name = this.controller().isProcedure()
                    ? I18nHelper.unnamedProcedure() : I18nHelper.unnamedFunction();
        }
        String title = name + "@" + this.schema() + "(" + this.connectName() + ")";
        this.text(this.isUnsaved() ? "* " + title : title);
    }

    public void init(H2Routine function, ShellH2RoutinesTreeItem group) {
        this.controller().init(function, group);
        this.flush();
    }

    public String functionName() {
        return this.controller().functionName();
    }

    public String originalName() {
        return this.controller().originalName();
    }

    public boolean isNewData() {
        return this.controller().isNewData();
    }

    public boolean isProcedure() {
        return this.controller().isProcedure();
    }

    public boolean isUnsaved() {
        return this.controller().isUnsaved();
    }

    public String schema() {
        return this.controller().schema();
    }

    @Override
    public ShellH2RootTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    @Override
    public ShellH2FunctionDesignTabController controller() {
        return (ShellH2FunctionDesignTabController) super.controller();
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
