package cn.oyzh.easyshell.tabs.dameng.table;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.record.DamengRecordFilter;
import cn.oyzh.easyshell.tabs.dameng.ShellDamengBaseTab;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.ShellDamengTableTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.List;

/**
 * db表tab
 *
 * @author oyzh
 * @since 2023/12/24
 */
public class ShellDamengTableRecordTab extends ShellDamengBaseTab {

    // /**
    //  * 标签打开时间
    //  */
    // private final long openedTime = System.currentTimeMillis();

    // private DamengTableTreeItem item;

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "dameng/table/shellDamengTableRecordTab.fxml";
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
        // 设置提示文本
        this.setText(this.item().tableName() + "@" + this.item().schema() + "(" + this.item().infoName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 树键
     * @return 是否初始化成功
     */
    public boolean init(ShellDamengTableTreeItem item) {
        // this.item = item;
        this.controller().init(item);
        // 刷新tab
        this.flush();
        // 加载耗时处理
        return true;
    }

    @Override
    public ShellDamengTableRecordTabController controller() {
        return (ShellDamengTableRecordTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reload();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellDamengClient client() {
        return this.item().client();
    }

    /**
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<DamengRecordFilter> filters) {
        this.controller().setFilters(filters);
    }

    /**
     * 获取树节点
     *
     * @return 树节点
     */
    public ShellDamengTableTreeItem item(){
        return this.controller().getItem();
    }
    
    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.item().tableName();
    }

    @Override
    public ShellDamengSchemaTreeItem dbItem() {
        return this.item().dbItem();
    }
}
