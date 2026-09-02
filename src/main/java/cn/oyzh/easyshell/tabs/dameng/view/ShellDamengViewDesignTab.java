package cn.oyzh.easyshell.tabs.dameng.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.tabs.dameng.ShellDamengBaseTab;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
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
public class ShellDamengViewDesignTab extends ShellDamengBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "view/damengViewDesignTab.fxml";
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
            name = I18nHelper.unnamedView();
        }
        // 设置提示文本
        if (this.isUnsaved()) {
            this.setText("* " + this.schema() + "-" + name);
        } else {
            this.setText(this.schema() + "-" + name);
        }
    }

    public String schema() {
        return this.controller().schema();
    }

    public String viewName() {
        return this.controller().viewName();
    }

    @Override
    public ShellDamengSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param item 树键
     */
    public void init(DamengView view, ShellDamengSchemaTreeItem item) {
        this.controller().init(view, item);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellDamengViewDesignTabController controller() {
        return (ShellDamengViewDesignTabController) super.controller();
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
