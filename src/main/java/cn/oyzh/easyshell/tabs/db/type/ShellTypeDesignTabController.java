package cn.oyzh.easyshell.tabs.db.type;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.type.ShellTypeHelper;
import cn.oyzh.easyshell.db.type.ShellTypeTarget;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBTypeDefinition;
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
 * 类型设计标签内容组件
 * <p>
 * 类型直接以“定义”（DDL语句）的形式编辑，保存时执行编辑器中的SQL，各数据库共用此实现。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTypeDesignTabController extends RichTabController {

    /**
     * 类型对象
     */
    private DBTypeDefinition type;

    /**
     * 类型设计目标
     */
    private ShellTypeTarget target;

    /**
     * 类型定义
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
     * @param type   类型对象
     * @param target 类型设计目标
     */
    public void init(DBTypeDefinition type, ShellTypeTarget target) {
        this.target = target;
        this.type = type;
        // 更新新数据标志位（名称为空视为新建类型）
        this.newData = type == null || StringUtil.isBlank(type.getName());
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
        } else {// 查询类型定义
            String definition = this.target.selectTypeDefinition(this.type.getName());
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
     * 新建类型的默认定义
     *
     * @return 定义
     */
    private String newDefinition() {
        return "-- " + ShellI18nHelper.typeDefinitionTip() + "\n"
                + "-- CREATE TYPE TYPE_NAME AS OBJECT (COLUMN_NAME VARCHAR2(100));\n"
                + "-- CREATE TYPE TYPE_NAME AS ENUM ('A', 'B');\n"
                + "-- CREATE DOMAIN TYPE_NAME AS INTEGER;\n"
                + "-- CREATE CLASS TYPE_NAME AS OBJECT (COLUMN_NAME INT);";
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
            this.init(this.type, this.target);
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
            // 修改类型时，如果提交的是创建语句，则先删除原类型（多数数据库不支持直接修改类型定义）
            if (!this.newData && this.needDropBeforeSave(sqlList)) {
                this.target.dropType(this.type.getName(), this.type.getObjectType());
            }
            // 执行类型定义
            this.target.executeTypeSql(sqlList);
            // 更新保存标志位
            this.unsaved = false;
            // 更新新数据标志位
            this.newData = false;
            // 更新类型名称与对象类型
            String typeName = this.parseTypeName(sqlList);
            if (StringUtil.isNotBlank(typeName)) {
                this.refreshTypeInfo(typeName);
            }
            // 刷新树节点
            this.target.reloadTypes();
            // 重新读取类型定义
            FXUtil.runWait(this::initInfo);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.flushTab();
        }
    }

    /**
     * 更新类型名称与对象类型
     *
     * @param typeName 类型名称
     */
    private void refreshTypeInfo(String typeName) {
        this.type.setName(typeName);
        try {
            for (DBTypeDefinition type : this.target.selectTypes()) {
                if (StringUtil.equalsIgnoreCase(type.getName(), typeName)) {
                    this.type.setObjectType(type.getObjectType());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 解析类型名称
     *
     * @param sqlList SQL列表
     * @return 类型名称，未解析到返回null
     */
    private String parseTypeName(List<String> sqlList) {
        for (String sql : sqlList) {
            String typeName = ShellTypeHelper.parseTypeName(sql);
            if (StringUtil.isNotBlank(typeName)) {
                return typeName;
            }
        }
        return null;
    }

    /**
     * 保存前是否需要删除原类型
     *
     * @param sqlList SQL列表
     * @return 是否需要删除
     */
    private boolean needDropBeforeSave(List<String> sqlList) {
        // 类型名称不存在，或者未提交创建语句，无需删除
        if (StringUtil.isBlank(this.type.getName()) || sqlList.stream().noneMatch(ShellTypeHelper::isCreateTypeSql)) {
            return false;
        }
        // 已经包含删除语句，或者使用了 OR REPLACE / ALTER 等原地修改语句，无需删除
        if (sqlList.stream().anyMatch(ShellTypeHelper::isDropTypeSql)
                || sqlList.stream().anyMatch(ShellTypeHelper::isReplaceTypeSql)) {
            return false;
        }
        return this.target.existType(this.type.getName());
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
     * 获取类型设计目标
     *
     * @return 类型设计目标
     */
    public ShellTypeTarget getTarget() {
        return target;
    }

    /**
     * 获取类型对象
     *
     * @return 类型对象
     */
    public DBTypeDefinition getType() {
        return type;
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
     * 是否新类型
     *
     * @return 是否新类型
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
