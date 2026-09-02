package cn.oyzh.easyshell.tabs.dameng.view;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.record.DamengRecordFilter;
import cn.oyzh.easyshell.tabs.dameng.DamengTab;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.DamengViewTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import javafx.scene.Cursor;

import java.util.List;

/**
 * db表tab
 *
 * @author oyzh
 * @since 2023/12/24
 */
public class DamengViewRecordTab extends DamengTab {

    /**
     * 标签打开时间
     */
    private final long openedTime = System.currentTimeMillis();

    @Override
    protected String url() {
        return super.getBasePath() + "view/damengViewRecordTab.fxml";
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
        this.setText(this.item().viewName() + "@" + this.item().schema() + "(" + this.item().infoName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 树键
     */
    public boolean init(DamengViewTreeItem item) {
        this.controller().init(item);
        this.flush();
        return true;
    }

    @Override
    public DamengViewRecordTabController controller() {
        return (DamengViewRecordTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reload();
    }

    public DamengViewTreeItem item() {
        return this.controller().getItem();
    }

    public ShellDamengClient client() {
        return this.item().client();
    }

    public String viewName() {
        return this.item().viewName();
    }

    @Override
    public DamengSchemaTreeItem dbItem() {
        return this.item().dbItem();
    }

    public void setFilters(List<DamengRecordFilter> filters) {
        this.controller().setFilters(filters);
    }
}
