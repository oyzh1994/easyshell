package cn.oyzh.easyshell.tabs.postgresql.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.function.PostgresqlAlertFunctionParam;
import cn.oyzh.easyshell.postgresql.function.PostgresqlCreateFunctionParam;
import cn.oyzh.easyshell.postgresql.function.PostgresqlFunction;
import cn.oyzh.easyshell.postgresql.generator.function.PostgresqlFunctionAlertSqlGenerator;
import cn.oyzh.easyshell.postgresql.generator.function.PostgresqlFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.postgresql.routine.PostgresqlRoutineParam;
import cn.oyzh.easyshell.fx.postgresql.ShellPostgresqlRoutineSecurityTypeComboBox;
import cn.oyzh.easyshell.fx.db.ShellDBEnumTextFiled;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.db.ui.DBFiledTypeComboBox;
import cn.oyzh.fx.db.ui.DBStatusTableView;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.field.NumberTextField;
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
 * db函数内容组件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlFunctionDesignTabController extends RichTabController {

    /**
     * 函数
     */
    private PostgresqlFunction function;

    /**
     * 获取函数对象
     *
     * @return 函数对象
     */
    public PostgresqlFunction getFunction() {
        return function;
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
     * 特征，deterministic
     */
    @FXML
    private FXCheckBox deterministic;

    /**
     * 特征，resultCache
     */
    @FXML
    private FXCheckBox resultCache;

    /**
     * 特征，pipelined
     */
    @FXML
    private FXCheckBox pipelined;

    /**
     * 特征，aggregate
     */
    @FXML
    private FXCheckBox aggregate;

    /**
     * 参数表单
     */
    @FXML
    private DBStatusTableView<PostgresqlRoutineParam> paramTable;

    /**
     * 返回值类型
     */
    @FXML
    private DBFiledTypeComboBox returnType;

    /**
     * 返回值列表
     */
    @FXML
    private ShellDBEnumTextFiled returnValues;

    /**
     * 返回值小数
     */
    @FXML
    private NumberTextField returnDigits;

    /**
     * 返回值长度
     */
    @FXML
    private NumberTextField returnSize;

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
     * @param function 查询对象
     * @param dbItem   db库树节点
     */
    public void init(PostgresqlFunction function, ShellPostgresqlSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
        this.function = function;
        // 更新新数据标志位
        this.newData = this.function.isNew();
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
            DBStatusListenerManager.unbindListener(this.returnType, this.listener);
            DBStatusListenerManager.unbindListener(this.returnSize, this.listener);
            DBStatusListenerManager.unbindListener(this.returnDigits, this.listener);
            DBStatusListenerManager.unbindListener(this.securityType, this.listener);
            DBStatusListenerManager.unbindListener(this.returnValues, this.listener);
            DBStatusListenerManager.unbindListener(this.parallelEnable, this.listener);
            DBStatusListenerManager.unbindListener(this.aggregate, this.listener);
            DBStatusListenerManager.unbindListener(this.pipelined, this.listener);
            DBStatusListenerManager.unbindListener(this.deterministic, this.listener);
            DBStatusListenerManager.unbindListener(this.resultCache, this.listener);
        }
        // 初始化监听器
        this.listener = new DBStatusListener(this.function.getSchema() + ":" + this.function.getName()) {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                initChangedFlag();
            }
        };
        DBStatusListenerManager.bindListener(this.definition, this.listener);
        DBStatusListenerManager.bindListener(this.returnType, this.listener);
        DBStatusListenerManager.bindListener(this.returnSize, this.listener);
        DBStatusListenerManager.bindListener(this.returnDigits, this.listener);
        DBStatusListenerManager.bindListener(this.securityType, this.listener);
        DBStatusListenerManager.bindListener(this.returnValues, this.listener);
        DBStatusListenerManager.bindListener(this.parallelEnable, this.listener);
        DBStatusListenerManager.bindListener(this.aggregate, this.listener);
        DBStatusListenerManager.bindListener(this.pipelined, this.listener);
        DBStatusListenerManager.bindListener(this.deterministic, this.listener);
        DBStatusListenerManager.bindListener(this.resultCache, this.listener);
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
                    
                        RETURN 0;
                    END;
                    """;
            this.definition.setText(defDefinition);
            this.securityType.select("INVOKER");
            NodeGroupUtil.disappear(this.getTab(), "action3");
        } else {
            // 查询函数信息
            this.function = this.dbItem.selectFunction(this.function.getName());
            // 初始化数据
            this.definition.setText(this.function.getDefinition());
            this.definition.forgetHistory();
            this.paramTable.setItem(this.function.getParams());
            this.securityType.select(this.function.getSecurityType());
            String characteristic = this.function.getCharacteristic();
            if (StringUtil.isNotBlank(characteristic)) {
                if (characteristic.contains("PARALLEL_ENABLE")) {
                    this.parallelEnable.setSelected(true);
                }
                if (characteristic.contains("AGGREGATE")) {
                    this.aggregate.setSelected(true);
                }
                if (characteristic.contains("PIPELINED")) {
                    this.pipelined.setSelected(true);
                }
                if (characteristic.contains("DETERMINISTIC")) {
                    this.deterministic.setSelected(true);
                }
                if (characteristic.contains("RESULT_CACHE")) {
                    this.resultCache.setSelected(true);
                }
            }
            NodeGroupUtil.display(this.getTab(), "action3");
        }

        // 返回值处理
        PostgresqlRoutineParam returnParam = this.function.getReturnParam();
        if (returnParam != null) {
            this.returnType.select(returnParam.getType());
            if (returnParam.getSize() != null) {
                this.returnSize.setValue(returnParam.getSize());
            }
            if (returnParam.getDigits() != null) {
                this.returnDigits.setValue(returnParam.getDigits());
            }
            if (StringUtil.isNotBlank(returnParam.getValue())) {
                this.returnValues.setValues(returnParam.getValueList());
            }
        }

        // 标记为结束；已有函数初始化完成后清除参数行的残留变更状态
        FXUtil.runPulse(() -> {
            if (!this.newData) {
                try {
                    this.paramTable.reset();
                } catch (Exception ignored) {
                }
                this.unsaved = false;
                this.flushTab();
            }
            this.initiating = false;
        });
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
            this.init(this.function, this.dbItem);
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
     * 函数名称
     */
    private String functionName;

    /**
     * 执行保存
     */
    private void doSave() {
        try {
            // 创建临时对象
            PostgresqlFunction tempFunction = this.tempData();

            // 函数名称
            if (this.newData) {
                this.functionName = MessageBox.prompt(I18nHelper.pleaseInputFunctionName(), this.functionName, this.getWindow());
                if (this.functionName == null) {
                    return;
                }
                tempFunction.setName(this.functionName);
            } else {
                this.functionName = tempFunction.getName();
            }

            // 创建函数
            if (this.newData) {
                this.dbItem.createFunction(tempFunction);
                PostgresqlFunction function = this.dbItem.selectFunction(this.functionName);
                this.dbItem.getFunctionTypeChild().addFunction(function);
                this.initDBListener();
            } else {// 修改过程
                this.dbItem.alertFunction(tempFunction);
            }
            // 更新保存标志位
            this.unsaved = false;
            // 更新新数据标志位
            this.newData = false;
            this.function = tempFunction;
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
    /**
     * 是否存在 OUT/INOUT 参数
     *
     * @return 结果
     */
    private boolean hasOutputParam() {
        for (PostgresqlRoutineParam param : this.paramTable.getItems()) {
            if (!param.isDeleted() && StringUtil.containsIgnoreCase(param.getMode(), "OUT")) {
                return true;
            }
        }
        return false;
    }

    private PostgresqlFunction tempData() {
        // 创建临时对象
        PostgresqlFunction tempFunction = new PostgresqlFunction();
        tempFunction.setName(this.function.getName());

        // 基本信息处理
        tempFunction.setSchema(this.function.getSchema());
        tempFunction.setOriginalParams(this.function.getParams());
        tempFunction.setOriginalReturnParam(this.function.getReturnParam());
        tempFunction.setParams(this.paramTable.getItems());
        String definition = this.definition.getTextTrim();
        // 含 OUT/INOUT 参数时，默认模板里的 RETURN 0 不合法，改为 RETURN
        if (this.hasOutputParam() && definition.contains("-- Routine body goes here...")) {
            definition = definition.replaceAll("(?i)RETURN\\s+0\\s*;", "RETURN;");
        }
        tempFunction.setDefinition(definition);
        tempFunction.setSecurityType(this.securityType.getSelectedItem());

        // 特征处理
        StringBuilder characteristic = new StringBuilder();
        if (this.parallelEnable.isSelected()) {
            characteristic.append(",PARALLEL_ENABLE");
        }
        if (this.aggregate.isSelected()) {
            characteristic.append(",AGGREGATE");
        }
        if (this.pipelined.isSelected()) {
            characteristic.append(",PIPELINED");
        }
        if (this.deterministic.isSelected()) {
            characteristic.append(",DETERMINISTIC");
        }
        if (this.resultCache.isSelected()) {
            characteristic.append(",RESULT_CACHE");
        }
        if (!characteristic.isEmpty()) {
            tempFunction.setCharacteristic(characteristic.substring(1));
        }
        // 返回值处理
        PostgresqlRoutineParam returnParam = new PostgresqlRoutineParam();
        returnParam.setType(this.returnType.getValue());
        if (this.returnSize.isEnable()) {
            returnParam.setSize(this.returnSize.getIntValue());
        }
        if (this.returnValues.isEnable()) {
            returnParam.setValue(this.returnValues.getTextTrim());
        }
        if (this.returnDigits.isEnable()) {
            returnParam.setDigits(this.returnDigits.getIntValue());
        }
        tempFunction.setReturnParam(returnParam);

        return tempFunction;
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        PostgresqlFunction temp = this.tempData();
        String sql;
        if (this.newData) {
            PostgresqlCreateFunctionParam param = new PostgresqlCreateFunctionParam();
            param.setFunction(temp);
            param.setSchema(this.schema());
            if (StringUtil.isBlank(param.getFunctionName())) {
                param.setFunctionName("Unnamed_Function");
            }
            sql = PostgresqlFunctionCreateSqlGenerator.generateSqlSingle(param);
        } else {
            PostgresqlAlertFunctionParam param = new PostgresqlAlertFunctionParam();
            param.setFunction(temp);
            param.setSchema(this.schema());
            sql = PostgresqlFunctionAlertSqlGenerator.generateSqlSingle(param);
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
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
        NodeUtil.nodeOnCtrlS(this.returnSize, this::save);
        NodeUtil.nodeOnCtrlS(this.returnType, this::save);
        NodeUtil.nodeOnCtrlS(this.returnValues, this::save);
        NodeUtil.nodeOnCtrlS(this.returnDigits, this::save);
        NodeUtil.nodeOnCtrlS(this.securityType, this::save);
        NodeUtil.nodeOnCtrlS(this.parallelEnable, this::save);
        NodeUtil.nodeOnCtrlS(this.aggregate, this::save);
        NodeUtil.nodeOnCtrlS(this.pipelined, this::save);
        NodeUtil.nodeOnCtrlS(this.deterministic, this::save);
        NodeUtil.nodeOnCtrlS(this.resultCache, this::save);
        NodeUtil.nodeOnCtrlS(this.preview, this::save);
        this.paramTable.setCtrlSAction(this::save);

        // 返回值监听
        this.returnType.selectedItemChanged((observable, oldValue, newValue) -> {
            if (this.returnType.supportSize()) {
                this.returnSize.enable();
            } else {
                this.returnSize.disable();
            }
            if (this.returnType.supportDigits()) {
                this.returnDigits.enable();
            } else {
                this.returnDigits.disable();
            }
            if (this.returnType.supportValue()) {
                this.returnValues.enable();
            } else {
                this.returnValues.disable();
            }
        });

        // 切换面板监听
        this.tabPane.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 1) {
                NodeGroupUtil.display(this.getTab(), "param");
            } else {
                NodeGroupUtil.disappear(this.getTab(), "param");
            }
            if (newValue.intValue() == 4) {
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
