package cn.oyzh.easyshell.tabs.db.trigger;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.easyshell.db.trigger.ShellTriggerTarget;
import cn.oyzh.fx.gui.svg.glyph.database.TriggerSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * 触发器设计标签页
 * <p>
 * 各数据库共用此标签页，触发器的取定义、执行SQL、删除等操作由 {@link ShellTriggerTarget} 提供。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTriggerDesignTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "db/trigger/shellTriggerDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        TriggerSVGGlyph graphic = (TriggerSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new TriggerSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.triggerName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedTrigger();
        }
        // 设置提示文本
        if (this.isUnsaved()) {
            this.text("* " + name + "@" + this.scope() + "(" + this.connectName() + ")");
        } else {
            this.text(name + "@" + this.scope() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 获取触发器名称
     *
     * @return 触发器名称
     */
    public String triggerName() {
        DBTrigger trigger = this.trigger();
        return trigger == null ? null : trigger.getName();
    }

    /**
     * 获取触发器对象
     *
     * @return 触发器对象
     */
    public DBTrigger trigger() {
        return this.controller().getTrigger();
    }

    /**
     * 获取触发器所属范围
     *
     * @return 范围名称
     */
    public String scope() {
        ShellTriggerTarget target = this.target();
        return target == null ? null : target.triggerScope();
    }

    /**
     * 获取触发器设计目标
     *
     * @return 触发器设计目标
     */
    public ShellTriggerTarget target() {
        return this.controller().getTarget();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String connectName() {
        ShellTriggerTarget target = this.target();
        return target == null ? null : target.connectName();
    }

    /**
     * 初始化
     *
     * @param trigger 触发器对象
     * @param target  触发器设计目标
     */
    public void init(DBTrigger trigger, ShellTriggerTarget target) {
        this.controller().init(trigger, target);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellTriggerDesignTabController controller() {
        return (ShellTriggerDesignTabController) super.controller();
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
     * 是否新触发器
     *
     * @return 是否新触发器
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
