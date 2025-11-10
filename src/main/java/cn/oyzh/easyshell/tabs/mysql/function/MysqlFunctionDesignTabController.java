package cn.oyzh.easyshell.tabs.mysql.function;

import cn.oyzh.common.cache.CacheHelper;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mysql.DBCharsetComboBox;
import cn.oyzh.easyshell.fx.mysql.DBEditor;
import cn.oyzh.easyshell.fx.mysql.DBSecurityTypeComboBox;
import cn.oyzh.easyshell.fx.mysql.DBStatusTableView;
import cn.oyzh.easyshell.fx.mysql.routine.MysqlCharacteristicCombobox;
import cn.oyzh.easyshell.fx.mysql.table.DBEnumTextFiled;
import cn.oyzh.easyshell.fx.mysql.table.MysqlFiledTypeComboBox;
import cn.oyzh.easyshell.db.DBObjectStatus;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.mysql.generator.routine.MysqlFunctionSqlGenerator;
import cn.oyzh.easyshell.db.listener.DBStatusListener;
import cn.oyzh.easyshell.db.listener.DBStatusListenerManager;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
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

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * db函数内容组件
 *
 * @author oyzh
 * @since 2024/07/08
 */
public class MysqlFunctionDesignTabController extends RichTabController {

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
    private MysqlDatabaseTreeItem dbItem;

    /**
     * 定义
     */
    @FXML
    private DBEditor definition;

    /**
     * 预览
     */
    @FXML
    private DBEditor preview;

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
    private DBSecurityTypeComboBox securityType;

    /**
     * 特征
     */
    @FXML
    private MysqlCharacteristicCombobox characteristic;

    /**
     * 参数表单
     */
    @FXML
    private DBStatusTableView<MysqlRoutineParam> paramTable;

    // /**
    //  * 参数类型
    //  */
    // @FXML
    // private FXTableColumn<MysqlRoutineParam, String> paramType;
    //
    // /**
    //  * 参数长度
    //  */
    // @FXML
    // private FXTableColumn<MysqlRoutineParam, String> paramSize;
    //
    // /**
    //  * 参数值
    //  */
    // @FXML
    // private FXTableColumn<MysqlRoutineParam, String> paramValue;
    //
    // /**
    //  * 参数小数
    //  */
    // @FXML
    // private FXTableColumn<MysqlRoutineParam, String> paramDigits;
    //
    // /**
    //  * 参数字符集
    //  */
    // @FXML
    // private FXTableColumn<MysqlRoutineParam, String> paramCharset;
    //
    // /**
    //  * 参数排序
    //  */
    // @FXML
    // private FXTableColumn<MysqlRoutineParam, String> paramCollation;
    //
    // /**
    //  * 参数名称
    //  */
    // @FXML
    // private FXTableColumn<MysqlRoutineParam, String> paramName;

    /**
     * 返回值类型
     */
    @FXML
    private MysqlFiledTypeComboBox returnType;

    /**
     * 返回值列表
     */
    @FXML
    private DBEnumTextFiled returnValues;

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
    private DBCharsetComboBox returnCharset;

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
     * 数据列表监听器
     */
    private final ListChangeListener<DBObjectStatus> listChangeListener = c -> {
        if (c.next()) {
            if (c.wasRemoved() || c.wasAdded() || c.wasReplaced()) {
                this.initChangedFlag();
            }
            if (c.wasReplaced() || c.wasAdded()) {
                List<DBObjectStatus> list = null;
                if (c.wasReplaced()) {
                    list = (List<DBObjectStatus>) c.getList();
                } else if (c.wasAdded()) {
                    list = (List<DBObjectStatus>) c.getAddedSubList();
                }
                if (list != null) {
                    for (DBObjectStatus status : list) {
                        DBStatusListenerManager.bindListener(status.statusProperty(), this.listener);
                    }
                }
            }
        }
    };

    /**
     * 执行初始化
     *
     * @param function 查询对象
     * @param dbItem   db库树节点
     */
    public void init(MysqlFunction function, MysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
        this.function = function;
        StageManager.showMask(this::doInit);
    }

    /**
     * 执行初始化
     */
    private void doInit(){
        // 查询最新数据
        if (!this.function.isNew()) {
            this.function = this.dbItem.selectFunction(function.getName());
        }

        // 初始化字符集列表
        this.returnCharset.init(this.dbItem.client());

        // 初始化监听器
        this.initDBListener();

        // 初始化信息

        FXUtil.runWait(this::initInfo);
        // this.initInfo();

        // 监听组件
        CacheHelper.set("dbClient", this.dbItem.client());
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
        // this.paramTable.itemList().addListener(this.listChangeListener);
    }

    /**
     * 初始化数据监听器
     */
    private void initDBListener() {
        // 初始化监听器
        this.listener = new DBStatusListener(this.function.getDbName() + ":" + this.function.getName()) {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                initChangedFlag();
            }
        };
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

        // 更新新表标志位
        this.newData = this.function.isNew();

        // 初始化数据
        this.definer.setText(this.function.getDefiner());
        this.comment.setText(this.function.getComment());
        this.definition.setText(this.function.getDefinition());
        this.definition.forgetHistory();
        this.definition.setDialect(this.dbItem.dialect());
        this.paramTable.setItem(this.function.getParams());
        this.securityType.select(this.function.getSecurityType());
        this.characteristic.select(this.function.getCharacteristic());

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
        }

        // 标记为结束
        FXUtil.runPulse(() -> this.initiating = false);
    }

    /**
     * 保存
     */
    @FXML
    private void save() {
        StageManager.showMask(this::doSave);
    }

    /**
     * 执行保存
     */
    private void doSave() {
        try {
            // 创建临时对象
            MysqlFunction tempFunction = this.tempData();

            // 函数名称
            String functionName;
            if (this.newData) {
                functionName = MessageBox.prompt(I18nHelper.pleaseInputFunctionName());
                if (functionName == null) {
                    return;
                }
                tempFunction.setName(functionName);
            } else {
                functionName = tempFunction.getName();
            }

            // this.disableTab();

            // 创建函数
            if (this.newData) {
                this.dbItem.createFunction(tempFunction);
                MysqlFunction function = this.dbItem.selectFunction(functionName);
                this.dbItem.getFunctionTypeChild().addFunction(function);
                // MysqlEventUtil.functionAdded(this.dbItem);
                this.initDBListener();
            } else {// 修改过程
                this.dbItem.alertFunction(tempFunction);
                // MysqlEventUtil.functionAlerted(functionName, this.dbItem);
            }
            // // 刷新数据
            // this.dbItem.getFunctionTypeChild().reloadChild();
            // 更新保存标志位
            this.unsaved = false;
            // 重载表数据
            this.function = this.dbItem.selectFunction(functionName);
            // 刷新tab
            // this.initInfo();
            FXUtil.runWait(this::initInfo);
            // 重置表格
            this.paramTable.reset();
            // 初始化预览
            this.initPreview();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        } finally {
            // this.enableTab();
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

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
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
        this.paramTable.setCtrlSAction(this::save);
        // 绑定属性
        // this.paramName.setCellValueFactory(new PropertyValueFactory<>("nameControl"));
        // this.paramType.setCellValueFactory(new PropertyValueFactory<>("typeControl"));
        // this.paramSize.setCellValueFactory(new PropertyValueFactory<>("sizeControl"));
        // this.paramValue.setCellValueFactory(new PropertyValueFactory<>("valueControl"));
        // this.paramDigits.setCellValueFactory(new PropertyValueFactory<>("digitsControl"));
        // this.paramCharset.setCellValueFactory(new PropertyValueFactory<>("charsetControl"));
        // this.paramCollation.setCellValueFactory(new PropertyValueFactory<>("collationControl"));

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
                // MysqlFunction temp = this.tempData();
                // if (StringUtil.isBlank(temp.getName())) {
                //     temp.setName("Unnamed_Function");
                // }
                // String sql = MysqlFunctionSqlGenerator.INSTANCE.generate(temp);
                // this.preview.setText(sql);
                this.initPreview();
            }
        });
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        MysqlFunction temp = this.tempData();
        if (StringUtil.isBlank(temp.getName())) {
            temp.setName("Unnamed_Function");
        }
        String sql = MysqlFunctionSqlGenerator.INSTANCE.generate(temp);
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
            // this.tabPane.refresh();
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
            // this.tabPane.refresh();
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

    public boolean isUnsaved() {
        return unsaved;
    }

    public void setUnsaved(boolean unsaved) {
        this.unsaved = unsaved;
    }

    public MysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(MysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
