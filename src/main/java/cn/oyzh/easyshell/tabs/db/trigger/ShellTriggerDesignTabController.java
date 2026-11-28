package cn.oyzh.easyshell.tabs.db.trigger;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.easyshell.db.trigger.ShellTriggerHelper;
import cn.oyzh.easyshell.db.trigger.ShellTriggerTarget;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.sql.DBSqlParser;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.util.List;

/**
 * 触发器设计标签内容组件
 * <p>
 * 触发器直接以“定义”（DDL语句）的形式编辑，保存时执行编辑器中的SQL，各数据库共用此实现。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTriggerDesignTabController extends RichTabController {

    /**
     * 触发器对象
     */
    private DBTrigger trigger;

    /**
     * 触发器设计目标
     */
    private ShellTriggerTarget target;

    /**
     * 触发器定义
     */
    @FXML
    private ShellDataEditor definition;

    /**
     * 未保存标志位
     */
    private boolean unsaved;

    /**
     * 新数据标志位
     */
    private boolean newData;

    /**
     * 初始化中标志位
     */
    private boolean initiating;

    /**
     * 组件监听是否已绑定
     */
    private boolean listenersBound;

    /**
     * 初始化
     *
     * @param trigger 触发器对象
     * @param target  触发器设计目标
     */
    public void init(DBTrigger trigger, ShellTriggerTarget target) {
        this.target = target;
        this.trigger = trigger;
        // 更新新数据标志位（名称为空视为新建触发器）
        this.newData = trigger == null || StringUtil.isBlank(trigger.getName());
        StageManager.showMask(this.getWindow(), this::doInit);
    }

    /**
     * 执行初始化
     */
    private void doInit() {
        // 初始化组件监听
        this.initListeners();
        // 初始化信息
        FXUtil.runWait(this::initInfo);
    }

    /**
     * 初始化组件监听
     */
    private void initListeners() {
        // 监听只绑定一次，刷新时复用
        if (this.listenersBound) {
            return;
        }
        this.listenersBound = true;
        this.definition.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.initiating) {
                this.unsaved = true;
                this.flushTab();
            }
        });
    }

    /**
     * 初始化信息
     */
    protected void initInfo() {
        // 更新初始化标志位
        this.initiating = true;
        // 如果是新数据，则默认触发变更
        if (this.newData) {
            this.unsaved = true;
            this.definition.text(this.newDefinition());
            this.definition.forgetHistory();
            NodeGroupUtil.disappear(this.getTab(), "action3");
        } else {// 查询触发器定义
            String definition = this.target.selectTriggerDefinition(this.trigger.getName());
            // 查询不到定义时保留编辑器内容，避免用户编辑的内容丢失
            if (StringUtil.isNotBlank(definition)) {
                this.definition.text(definition);
            }
            this.definition.forgetHistory();
            NodeGroupUtil.display(this.getTab(), "action3");
        }
        // 标记为结束
        FXUtil.runPulse(() -> this.initiating = false);
    }

    /**
     * 新建触发器的默认定义
     *
     * @return 定义
     */
    private String newDefinition() {
        String scope = this.target.triggerScope();
        String prefix = StringUtil.isBlank(scope) ? "" : scope + ".";
        return "-- " + ShellI18nHelper.triggerDefinitionTip() + "\n"
                + "-- CREATE TRIGGER TRIGGER_NAME\n"
                + "-- AFTER INSERT ON " + prefix + "TABLE_NAME\n"
                + "-- FOR EACH ROW\n"
                + "-- BEGIN\n"
                + "--     ...\n"
                + "-- END;";
    }

    /**
     * 刷新
     */
    @FXML
    private void refresh() {
        if (!MessageBox.confirm(ShellI18nHelper.confirmRefreshData(), this.getWindow())) {
            return;
        }
        try {
            this.init(this.trigger, this.target);
            this.flushTab();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 保存
     */
    @FXML
    private void save() {
        StageManager.showMask(this.getWindow(), this::doSave);
    }

    /**
     * 执行保存
     */
    private void doSave() {
        try {
            String definition = this.definition.getTextTrim();
            // 解析为多条语句，注释内容不参与执行
            List<String> sqlList = DBSqlParser.getParser(definition, this.dialect()).parseSql()
                    .stream().filter(StringUtil::isNotBlank).toList();
            if (CollectionUtil.isEmpty(sqlList)) {
                MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
                return;
            }
            // 修改触发器时，如果提交的是创建语句，则先删除原触发器（多数数据库不支持直接修改触发器定义）
            if (!this.newData && this.needDropBeforeSave(sqlList)) {
                this.target.dropTrigger(this.trigger.getName(), this.trigger.getTableName());
            }
            // 执行触发器定义
            this.target.executeTriggerSql(sqlList);
            // 更新保存标志位
            this.unsaved = false;
            // 更新新数据标志位
            this.newData = false;
            // 更新触发器名称与所属表
            String triggerName = this.parseTriggerName(sqlList);
            if (StringUtil.isNotBlank(triggerName)) {
                this.refreshTriggerInfo(triggerName);
            }
            // 刷新树节点
            this.target.reloadTriggers();
            // 重新读取触发器定义
            FXUtil.runWait(this::initInfo);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.flushTab();
        }
    }

    /**
     * 更新触发器名称与所属表
     *
     * @param triggerName 触发器名称
     */
    private void refreshTriggerInfo(String triggerName) {
        this.trigger.setName(triggerName);
        try {
            for (DBTrigger trigger : this.target.selectTriggers()) {
                if (StringUtil.equalsIgnoreCase(trigger.getName(), triggerName)) {
                    this.trigger.setTableName(trigger.getTableName());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 解析触发器名称
     *
     * @param sqlList SQL列表
     * @return 触发器名称，未解析到返回null
     */
    private String parseTriggerName(List<String> sqlList) {
        for (String sql : sqlList) {
            String triggerName = ShellTriggerHelper.parseTriggerName(sql);
            if (StringUtil.isNotBlank(triggerName)) {
                return triggerName;
            }
        }
        return null;
    }

    /**
     * 保存前是否需要删除原触发器
     *
     * @param sqlList SQL列表
     * @return 是否需要删除
     */
    private boolean needDropBeforeSave(List<String> sqlList) {
        // 触发器名称不存在，或者未提交创建语句，无需删除
        if (StringUtil.isBlank(this.trigger.getName())
                || !ShellTriggerHelper.requiresDropBeforeCreate(sqlList)) {
            return false;
        }
        return this.target.existTrigger(this.trigger.getName());
    }

    /**
     * 获取数据库方言
     *
     * @return 数据库方言
     */
    private DBDialect dialect() {
        return this.target.dialect();
    }

    /**
     * 获取触发器设计目标
     *
     * @return 触发器设计目标
     */
    public ShellTriggerTarget getTarget() {
        return target;
    }

    /**
     * 获取触发器对象
     *
     * @return 触发器对象
     */
    public DBTrigger getTrigger() {
        return trigger;
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
    public boolean isUnsaved() {
        return unsaved;
    }

    /**
     * 是否新触发器
     *
     * @return 是否新触发器
     */
    public boolean isNew() {
        return newData;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab() == null ? null : this.getTab().getContent(), this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
    }
}
