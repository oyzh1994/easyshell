package cn.oyzh.easyshell.tabs.dameng.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.tabs.dameng.ShellDamengBaseTab;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * db查询tab
 *
 * @author oyzh
 * @since 2024/02/18
 */
public class ShellDamengQueryMainTab extends ShellDamengBaseTab {

    // /**
    //  * 内容已变化
    //  */
    // private boolean contentChanged;
    //
    // public void setContentChanged(boolean contentChanged) {
    //     this.contentChanged = contentChanged;
    //     this.flush();
    // }

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "dameng/query/shellDamengQueryMainTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new QuerySVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String queryName = this.query().getName();
        if (queryName == null) {
            queryName = I18nHelper.newQuery();
        }
        // 设置提示文本
        if (this.controller().isUnsaved()) {
            this.setText("* " + queryName + "@" + this.schema() + "(" + this.connectName() + ")");
        } else {
            this.setText(queryName + "@" + this.schema() + "(" + this.connectName() + ")");
        }
    }

    public ShellQuery query() {
        return this.controller().getQuery();
    }

    public String queryId() {
        return this.query().getUid();
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
     * @param query 查询对象
     * @param item  db库树节点
     */
    public boolean init(ShellQuery query, ShellDamengSchemaTreeItem item) {
        this.controller().init(query, item);
        this.flush();
        return true;
    }

    @Override
    public ShellDamengQueryMainTabController controller() {
        return (ShellDamengQueryMainTabController) super.controller();
    }
}
