package cn.oyzh.easyshell.tabs.db.type;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.type.ShellTypeTarget;
import cn.oyzh.fx.db.DBTypeDefinition;
import cn.oyzh.fx.gui.svg.glyph.database.TypeSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * 类型设计标签页
 * <p>
 * 各数据库共用此标签页，类型的取定义、执行SQL、删除等操作由 {@link ShellTypeTarget} 提供。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTypeDesignTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "db/type/shellTypeDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        TypeSVGGlyph graphic = (TypeSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new TypeSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.typeName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedType();
        }
        // 设置提示文本
        if (this.isUnsaved()) {
            this.text("* " + name + "@" + this.scope() + "(" + this.connectName() + ")");
        } else {
            this.text(name + "@" + this.scope() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String typeName() {
        DBTypeDefinition type = this.type();
        return type == null ? null : type.getName();
    }

    /**
     * 获取类型对象
     *
     * @return 类型对象
     */
    public DBTypeDefinition type() {
        return this.controller().getType();
    }

    /**
     * 获取类型所属范围
     *
     * @return 范围名称
     */
    public String scope() {
        ShellTypeTarget target = this.target();
        return target == null ? null : target.typeScope();
    }

    /**
     * 获取类型设计目标
     *
     * @return 类型设计目标
     */
    public ShellTypeTarget target() {
        return this.controller().getTarget();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String connectName() {
        ShellTypeTarget target = this.target();
        return target == null ? null : target.connectName();
    }

    /**
     * 初始化
     *
     * @param type   类型对象
     * @param target 类型设计目标
     */
    public void init(DBTypeDefinition type, ShellTypeTarget target) {
        this.controller().init(type, target);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellTypeDesignTabController controller() {
        return (ShellTypeDesignTabController) super.controller();
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
     * 是否新类型
     *
     * @return 是否新类型
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
