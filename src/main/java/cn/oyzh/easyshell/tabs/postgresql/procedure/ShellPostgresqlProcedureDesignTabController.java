package cn.oyzh.easyshell.tabs.postgresql.procedure;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.generator.procedure.PostgresqlProcedureAlertSqlGenerator;
import cn.oyzh.easyshell.postgresql.generator.procedure.PostgresqlProcedureCreateSqlGenerator;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlAlertProcedureParam;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlCreateProcedureParam;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlProcedure;
import cn.oyzh.easyshell.postgresql.routine.PostgresqlRoutineParam;
import cn.oyzh.easyshell.fx.postgresql.ShellPostgresqlRoutineSecurityTypeComboBox;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.db.ui.DBStatusTableView;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;


/**
 * db存储过程内容组件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlProcedureDesignTabController extends RichTabController {

    /**
     * 过程
     */
    private PostgresqlProcedure procedure;

    /**
     * 获取过程对象
     *
     * @return 过程对象
     */
    public PostgresqlProcedure getProcedure() {
        return procedure;
    }

    /**
     * db数据库树节点
     */
    private ShellPostgresqlSchemaTreeItem dbItem;

    /**
     * 定义
     */
    @FXML
    private Editor definition;

    /**
     * 预览
     */
    @FXML
    private Editor preview;

    /**
     * 切换面板
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 安全性
     */
    @FXML
    private ShellPostgresqlRoutineSecurityTypeComboBox securityType;

    /**
     * 特征，parallelEnable
     */
    @FXML
    private FXCheckBox parallelEnable;

    /**
     * 参数表单
     */
    @FXML
    private DBStatusTableView<PostgresqlRoutineParam> paramTable;

    /**
     * 数据监听器
     */
    private DBStatusListener listener;

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
     * 执行初始化
     *
     * @param procedure 查询对象
     * @param dbItem    db库树节点
     */
    public void init(PostgresqlProcedure procedure, ShellPostgresqlSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
        this.procedure = procedure;
        // 更新新数据标志位
        this.newData = procedure.isNew();
        StageManager.showMask(this.getWindow(), this::doInit);
    }

    /**
     * 执行初始化
     */
    private void doInit() {
        // 初始化监听器
        this.initDBListener();

        // 初始化信息
        FXUtil.runWait(this::initInfo);
    }

    /**
     * 初始化数据监听器
     */
    private void initDBListener() {
        // 销毁监听器
        if (this.listener != null) {
            this.listener.destroy();
            DBStatusListenerManager.unbindListener(this.definition, this.listener);
            DBStatusListenerManager.unbindListener(this.securityType, this.listener);
            DBStatusListenerManager.unbindListener(this.parallelEnable, this.listener);
        }
        // 初始化监听器
        this.listener = new DBStatusListener(this.procedure.getSchema() + ":" + this.procedure.getName()) {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                initChangedFlag();
            }
        };
        DBStatusListenerManager.bindListener(this.definition, this.listener);
        DBStatusListenerManager.bindListener(this.securityType, this.listener);
        DBStatusListenerManager.bindListener(this.parallelEnable, this.listener);
        this.paramTable.setStatusListener(this.listener);
    }

    /**
     * 初始化变更标志
     */
    private void initChangedFlag() {
        if (!this.initiating) {
            this.unsaved = true;
            this.flushTab();
        }
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
            String defDefinition = """
                    BEGIN
                        -- Routine body goes here...
                    
                        RETURN;
                    END;
                    """;
            this.definition.setText(defDefinition);
            this.securityType.select("INVOKER");
            NodeGroupUtil.disappear(this.getTab(), "action3");
        } else {
            // 查询函数信息
            this.procedure = this.dbItem.selectProcedure(this.procedure.getName());
            // 初始化数据
            this.definition.setText(this.procedure.getDefinition());
            this.definition.forgetHistory();
            this.paramTable.setItem(this.procedure.getParams());
            this.securityType.select(this.procedure.getSecurityType());
            String characteristic = this.procedure.getCharacteristic();
            if (StringUtil.isNotBlank(characteristic)) {
                if (characteristic.contains("PARALLEL_ENABLE")) {
                    this.parallelEnable.setSelected(true);
                }
            }
            NodeGroupUtil.display(this.getTab(), "action3");
        }

        // 标记为结束
        FXUtil.runPulse(() -> this.initiating = false);
    }

    /**
     * 刷新
     */
    @FXML
    private void refresh() {
        if (!MessageBox.confirm(ShellI18nHelper.confirm("refreshData"), this.getWindow())) {
            return;
        }
        try {
            this.init(this.procedure, this.dbItem);
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
     * 过程名称
     */
    private String procedureName;

    /**
     * 执行保存
     */
    private void doSave() {
        try {
            // 创建临时对象
            PostgresqlProcedure tempProcedure = this.tempData();

            if (this.newData) {
                this.procedureName = MessageBox.prompt(I18nHelper.pleaseInputProcedureName(), this.procedureName, this.getWindow());
                if (this.procedureName == null) {
                    return;
                }
                tempProcedure.setName(this.procedureName);
            } else {
                this.procedureName = tempProcedure.getName();
            }

            // 创建过程
            if (this.newData) {
                this.dbItem.createProcedure(tempProcedure);
                PostgresqlProcedure procedure = this.dbItem.selectProcedure(this.procedureName);
                this.dbItem.getProcedureTypeChild().addProcedure(procedure);
                this.initDBListener();
            } else {// 修改过程
                this.dbItem.alertProcedure(tempProcedure);
            }
            // 更新保存标志位
            this.unsaved = false;
            // 更新新数据标志位
            this.newData = false;
            this.procedure = tempProcedure;
            // 刷新tab
            FXUtil.runWait(this::initInfo);
            // 重置表格
            this.paramTable.reset();
            // 初始化预览
            this.initPreview();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.flushTab();
        }
    }

    /**
     * 获取临时数据
     *
     * @return 临时数据
     */
    private PostgresqlProcedure tempData() {
        // 创建临时对象
        PostgresqlProcedure tempProcedure = new PostgresqlProcedure();
        tempProcedure.setName(this.procedure.getName());

        // 基本信息处理
        tempProcedure.setSchema(this.procedure.getSchema());
        tempProcedure.setParams(this.paramTable.getItems());
        tempProcedure.setDefinition(this.definition.getTextTrim());
        tempProcedure.setSecurityType(this.securityType.getSelectedItem());

        // 特征处理
        StringBuilder characteristic = new StringBuilder();
        if (this.parallelEnable.isSelected()) {
            characteristic.append(",PARALLEL_ENABLE");
        }
        if (!characteristic.isEmpty()) {
            tempProcedure.setCharacteristic(characteristic.substring(1));
        }

        return tempProcedure;
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        PostgresqlProcedure temp = this.tempData();
        String sql;
        if (this.newData) {
            PostgresqlCreateProcedureParam param = new PostgresqlCreateProcedureParam();
            param.setProcedure(temp);
            param.setSchema(this.schema());
            if (StringUtil.isBlank(param.getProcedureName())) {
                param.setProcedureName("Unnamed_Procedure");
            }
            sql = PostgresqlProcedureCreateSqlGenerator.generateSqlSingle(param);
        } else {
            PostgresqlAlertProcedureParam param = new PostgresqlAlertProcedureParam();
            param.setProcedure(temp);
            param.setSchema(this.schema());
            sql = PostgresqlProcedureAlertSqlGenerator.generateSqlSingle(param);
        }
        this.preview.text(sql);
    }

    /**
     * 添加参数
     */
    @FXML
    private void addParam() {
        try {
            PostgresqlRoutineParam param = new PostgresqlRoutineParam();
            param.setCreated(true);
            this.paramTable.addItem(param);
            this.paramTable.selectLast();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 删除参数
     */
    @FXML
    private void deleteParam() {
        try {
            PostgresqlRoutineParam param = this.paramTable.getSelectedItem();
            if (param != null) {
                // 非新增的数据进行提示
                if (!param.isCreated() && !MessageBox.confirm(ShellI18nHelper.confirm("delete", param.getName()), this.getWindow())) {
                    return;
                }
                this.paramTable.getItems().remove(param);
                // 从table移除数据
                if (param.isCreated()) {
                    this.paramTable.removeItem(param);
                } else {// 标记为删除
                    param.setDeleted(true);
                }
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 上移参数
     */
    @FXML
    private void moveParamUp() {
        try {
            TableViewUtil.moveUp(this.paramTable);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 下移参数
     */
    @FXML
    private void moveParamDown() {
        try {
            TableViewUtil.moveDown(this.paramTable);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellPostgresqlSchemaTreeItem getDbItem() {
        return dbItem;
    }

    /**
     * 设置数据库树节点
     *
     * @param dbItem 数据库树节点
     */
    public void setDbItem(ShellPostgresqlSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.dbItem.schema();
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
     * 设置未保存标志
     *
     * @param unsaved 未保存标志
     */
    public void setUnsaved(boolean unsaved) {
        this.unsaved = unsaved;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 初始化参数列表
        this.paramTable.itemList().addListener((ListChangeListener<PostgresqlRoutineParam>) c -> {
            while (c.next() && (c.wasAdded() || c.wasReplaced())) {
                this.initParamTable();
            }
        });
        this.initParamTable();

        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab() == null ? null : this.getTab().getContent(), this::save);
        NodeUtil.nodeOnCtrlS(this.securityType, this::save);
        NodeUtil.nodeOnCtrlS(this.parallelEnable, this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
        NodeUtil.nodeOnCtrlS(this.preview, this::save);
        this.paramTable.setCtrlSAction(this::save);
        // 切换面板监听
        this.tabPane.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 1) {
                NodeGroupUtil.display(this.getTab(), "param");
            } else {
                NodeGroupUtil.disappear(this.getTab(), "param");
            }
            if (newValue.intValue() == 3) {
                this.initPreview();
            }
        });
    }

    /**
     * 初始化参数表格
     */
    private void initParamTable() {
        for (PostgresqlRoutineParam index : this.paramTable.itemList()) {
            index.setDbClient(this.dbItem.client());
        }
    }
}
