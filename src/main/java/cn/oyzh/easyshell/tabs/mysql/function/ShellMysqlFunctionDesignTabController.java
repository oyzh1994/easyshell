package cn.oyzh.easyshell.tabs.mysql.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlCharsetComboBox;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlSecurityTypeComboBox;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlStatusTableView;
import cn.oyzh.easyshell.fx.mysql.routine.ShellMysqlCharacteristicCombobox;
import cn.oyzh.easyshell.fx.mysql.table.ShellMysqlEnumTextFiled;
import cn.oyzh.easyshell.mysql.function.MysqlAlertFunctionParam;
import cn.oyzh.easyshell.mysql.function.MysqlCreateFunctionParam;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.mysql.generator.function.MysqlFunctionAlertSqlGenerator;
import cn.oyzh.easyshell.mysql.generator.function.MysqlFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.mysql.query.ShellMysqlQueryEditor;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.db.ui.DBFiledTypeComboBox;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;

/**
 * db函数内容组件
 *
 * @author oyzh
 * @since 2024/07/08
 */
public class ShellMysqlFunctionDesignTabController extends RichTabController {

    /**
     * 函数
     */
    private MysqlFunction function;

    public MysqlFunction getFunction() {
        return function;
    }

    /**
     * db数据库树节点
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    /**
     * 定义
     */
    @FXML
    private ShellMysqlQueryEditor definition;

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
     * 注释
     */
    @FXML
    private FXTextArea comment;

    /**
     * 定义者
     */
    @FXML
    private FXTextField definer;

    /**
     * 安全性
     */
    @FXML
    private ShellMysqlSecurityTypeComboBox securityType;

    /**
     * 特征
     */
    @FXML
    private ShellMysqlCharacteristicCombobox characteristic;

    /**
     * 参数表单
     */
    @FXML
    private ShellMysqlStatusTableView<MysqlRoutineParam> paramTable;

    /**
     * 返回值类型
     */
    @FXML
    private DBFiledTypeComboBox returnType;

    /**
     * 返回值列表
     */
    @FXML
    private ShellMysqlEnumTextFiled returnValues;

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
     * 返回值字符集
     */
    @FXML
    private ShellMysqlCharsetComboBox returnCharset;

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
    public void init(MysqlFunction function, ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
        this.function = function;
        // 更新新数据标志位
        this.newData = this.function.isNew();
        StageManager.showMask(this::doInit);
    }

    /**
     * 执行初始化
     */
    private void doInit() {
        // 初始化字符集列表
        this.returnCharset.init(this.dbItem.client());

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
            DBStatusListenerManager.unbindListener(this.definer, this.listener);
            DBStatusListenerManager.unbindListener(this.comment, this.listener);
            DBStatusListenerManager.unbindListener(this.definition, this.listener);
            DBStatusListenerManager.unbindListener(this.returnType, this.listener);
            DBStatusListenerManager.unbindListener(this.returnSize, this.listener);
            DBStatusListenerManager.unbindListener(this.returnDigits, this.listener);
            DBStatusListenerManager.unbindListener(this.securityType, this.listener);
            DBStatusListenerManager.unbindListener(this.returnValues, this.listener);
            DBStatusListenerManager.unbindListener(this.returnCharset, this.listener);
            DBStatusListenerManager.unbindListener(this.characteristic, this.listener);
        }
        // 初始化监听器
        this.listener = new DBStatusListener(this.function.getDbName() + ":" + this.function.getName()) {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                initChangedFlag();
            }
        };
        DBStatusListenerManager.bindListener(this.definer, this.listener);
        DBStatusListenerManager.bindListener(this.comment, this.listener);
        DBStatusListenerManager.bindListener(this.definition, this.listener);
        DBStatusListenerManager.bindListener(this.returnType, this.listener);
        DBStatusListenerManager.bindListener(this.returnSize, this.listener);
        DBStatusListenerManager.bindListener(this.returnDigits, this.listener);
        DBStatusListenerManager.bindListener(this.securityType, this.listener);
        DBStatusListenerManager.bindListener(this.returnValues, this.listener);
        DBStatusListenerManager.bindListener(this.returnCharset, this.listener);
        DBStatusListenerManager.bindListener(this.characteristic, this.listener);
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
            this.definer.setText("`root`@`%`");
            String defDefinition = """
                    BEGIN
                        #Routine body goes here...
                    
                        RETURN 0;
                    END
                    """;
            this.definition.setText(defDefinition);
            NodeGroupUtil.disappear(this.getTab(), "action3");
        } else {
            // 查询函数信息
            this.function = this.dbItem.selectFunction(this.function.getName());
            // 初始化数据
            this.definer.setText(this.function.getDefiner());
            this.comment.setText(this.function.getComment());
            this.definition.setText(this.function.getDefinition());
            this.definition.forgetHistory();
            this.paramTable.setItem(this.function.getParams());
            this.securityType.select(this.function.getSecurityType());
            this.characteristic.select(this.function.getCharacteristic());
            NodeGroupUtil.display(this.getTab(), "action3");
        }

        // 返回值处理
        MysqlRoutineParam returnParam = this.function.getReturnParam();
        if (returnParam != null) {
            this.returnType.select(returnParam.getType());
            if (returnParam.getSize() != null) {
                this.returnSize.setValue(returnParam.getSize());
            }
            if (returnParam.getDigits() != null) {
                this.returnDigits.setValue(returnParam.getDigits());
            }
            if (StringUtil.isNotBlank(returnParam.getCharset())) {
                this.returnCharset.setValue(returnParam.getCharset());
            }
            if (StringUtil.isNotBlank(returnParam.getValue())) {
                this.returnValues.setValues(returnParam.getValueList());
            }
        }

        // 标记为结束
        FXUtil.runPulse(() -> this.initiating = false);
    }

    /**
     * 刷新
     */
    @FXML
    private void refresh() {
        if (!MessageBox.confirm(I18nHelper.refreshData() + "?")) {
            return;
        }
        try {
            this.init(this.function, this.dbItem);
            this.flushTab();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 保存
     */
    @FXML
    private void save() {
        StageManager.showMask(this::doSave);
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
            MysqlFunction tempFunction = this.tempData();

            // 函数名称
            if (this.newData) {
                this.functionName = MessageBox.prompt(I18nHelper.pleaseInputFunctionName(), functionName);
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
                MysqlFunction function = this.dbItem.selectFunction(this.functionName);
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
            MessageBox.exception(ex);
        } finally {
            this.flushTab();
        }
    }

    /**
     * 获取临时数据
     *
     * @return 临时数据
     */
    private MysqlFunction tempData() {
        // 创建临时对象
        MysqlFunction tempFunction = new MysqlFunction();
        tempFunction.setName(this.function.getName());

        // 基本信息处理
        tempFunction.setDbName(this.function.getDbName());
        tempFunction.setParams(this.paramTable.getItems());
        tempFunction.setDefiner(this.definer.getTextTrim());
        tempFunction.setComment(this.comment.getTextTrim());
        tempFunction.setDefinition(this.definition.getTextTrim());
        tempFunction.setSecurityType(this.securityType.getSelectedItem());
        tempFunction.setCharacteristic(this.characteristic.getSelectedItem());

        // 返回值处理
        MysqlRoutineParam returnParam = new MysqlRoutineParam();
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
        if (this.returnCharset.isEnable()) {
            returnParam.setCharset(this.returnCharset.getSelectedItem());
        }
        tempFunction.setReturnParam(returnParam);

        return tempFunction;
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        MysqlFunction temp = this.tempData();
        String sql;
        if (this.newData) {
            MysqlCreateFunctionParam param = new MysqlCreateFunctionParam();
            param.setFunction(temp);
            param.setDbName(this.dbItem.dbName());
            if (StringUtil.isBlank(param.getFunctionName())) {
                param.setFunctionName("Unnamed_Function");
            }
            sql = MysqlFunctionCreateSqlGenerator.generateSqlSingle(param);
        } else {
            MysqlAlertFunctionParam param = new MysqlAlertFunctionParam();
            param.setFunction(temp);
            param.setDbName(this.dbItem.dbName());
            sql = MysqlFunctionAlertSqlGenerator.generateSqlSingle(param);
        }
        this.preview.text(sql);
    }

    /**
     * 添加参数
     */
    @FXML
    private void addParam() {
        try {
            MysqlRoutineParam param = new MysqlRoutineParam();
            param.setCreated(true);
            this.paramTable.addItem(param);
            this.paramTable.selectLast();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 删除参数
     */
    @FXML
    private void deleteParam() {
        try {
            MysqlRoutineParam param = this.paramTable.getSelectedItem();
            if (param != null) {
                // 非新增的数据进行提示
                if (!param.isCreated() && !MessageBox.confirm(I18nHelper.delete() + " " + param.getName())) {
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
            MessageBox.exception(ex);
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
            MessageBox.exception(ex);
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
            MessageBox.exception(ex);
        }
    }

    public String dbName() {
        return this.dbItem.dbName();
    }

    public boolean isUnsaved() {
        return unsaved;
    }

    public void setUnsaved(boolean unsaved) {
        this.unsaved = unsaved;
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 初始化参数列表
        this.paramTable.itemList().addListener((ListChangeListener<MysqlRoutineParam>) c -> {
            while (c.next() && (c.wasAdded() || c.wasReplaced())) {
                this.initParamTable();
            }
        });
        this.initParamTable();

        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        NodeUtil.nodeOnCtrlS(this.definer, this::save);
        NodeUtil.nodeOnCtrlS(this.comment, this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
        NodeUtil.nodeOnCtrlS(this.returnSize, this::save);
        NodeUtil.nodeOnCtrlS(this.returnType, this::save);
        NodeUtil.nodeOnCtrlS(this.returnValues, this::save);
        NodeUtil.nodeOnCtrlS(this.returnDigits, this::save);
        NodeUtil.nodeOnCtrlS(this.securityType, this::save);
        NodeUtil.nodeOnCtrlS(this.returnCharset, this::save);
        NodeUtil.nodeOnCtrlS(this.characteristic, this::save);
        NodeUtil.nodeOnCtrlS(this.preview, this::save);
        this.paramTable.setCtrlSAction(this::save);

        // 返回值监听
        this.returnType.selectedItemChanged((observable, oldValue, newValue) -> {
            if (this.returnType.supportCharset()) {
                this.returnCharset.enable();
            } else {
                this.returnCharset.disable();
            }
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

    private void initParamTable() {
        for (MysqlRoutineParam index : this.paramTable.itemList()) {
            index.setDbClient(this.dbItem.client());
        }
    }
}
