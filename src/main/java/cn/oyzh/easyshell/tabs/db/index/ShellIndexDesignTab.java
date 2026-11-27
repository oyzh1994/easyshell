package cn.oyzh.easyshell.tabs.db.index;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.fx.db.DBIndex;
import cn.oyzh.fx.gui.svg.glyph.database.IndexSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * 索引设计标签页
 * <p>
 * 各数据库共用此标签页，索引的取定义、执行SQL、删除等操作由 {@link ShellIndexTarget} 提供。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellIndexDesignTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "db/index/shellIndexDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        IndexSVGGlyph graphic = (IndexSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new IndexSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.indexName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedIndex();
        }
        // 设置提示文本
        if (this.isUnsaved()) {
            this.text("* " + name + "@" + this.scope() + "(" + this.connectName() + ")");
        } else {
            this.text(name + "@" + this.scope() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 获取索引名称
     *
     * @return 索引名称
     */
    public String indexName() {
        DBIndex index = this.index();
        return index == null ? null : index.getName();
    }

    /**
     * 获取索引对象
     *
     * @return 索引对象
     */
    public DBIndex index() {
        return this.controller().getIndex();
    }

    /**
     * 获取索引所属范围
     *
     * @return 范围名称
     */
    public String scope() {
        ShellIndexTarget target = this.target();
        return target == null ? null : target.indexScope();
    }

    /**
     * 获取索引设计目标
     *
     * @return 索引设计目标
     */
    public ShellIndexTarget target() {
        return this.controller().getTarget();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String connectName() {
        ShellIndexTarget target = this.target();
        return target == null ? null : target.connectName();
    }

    /**
     * 初始化
     *
     * @param index  索引对象
     * @param target 索引设计目标
     */
    public void init(DBIndex index, ShellIndexTarget target) {
        this.controller().init(index, target);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellIndexDesignTabController controller() {
        return (ShellIndexDesignTabController) super.controller();
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
    public boolean isUnsaved() {
        return this.controller().isUnsaved();
    }

    /**
     * 是否新索引
     *
     * @return 是否新索引
     */
    public boolean isNew() {
        return this.controller().isNew();
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
