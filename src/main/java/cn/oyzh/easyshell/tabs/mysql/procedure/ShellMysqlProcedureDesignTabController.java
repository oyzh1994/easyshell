package cn.oyzh.easyshell.tabs.mysql.procedure;

import cn.oyzh.common.cache.CacheHelper;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.DBObjectStatus;
import cn.oyzh.easyshell.db.listener.DBStatusListener;
import cn.oyzh.easyshell.db.listener.DBStatusListenerManager;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlEditor;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlSecurityTypeComboBox;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlStatusTableView;
import cn.oyzh.easyshell.fx.mysql.routine.ShellMysqlCharacteristicCombobox;
import cn.oyzh.easyshell.mysql.generator.routine.MysqlProcedureSqlGenerator;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
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
 * db存储过程内容组件
 *
 * @author oyzh
 * @since 2024/07/08
 */
public class ShellMysqlProcedureDesignTabController extends RichTabController {

    /**
     * 过程
     */
    private MysqlProcedure procedure;

    public MysqlProcedure getProcedure() {
        return procedure;
    }

    /**
     * db数据库树节点
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    /**
     * 定义
     */
    @FXML
    private ShellMysqlEditor definition;

    /**
     * 预览
     */
    @FXML
    private ShellMysqlEditor preview;

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
    //
    // /**
    //  * 参数模式
    //  */
    // @FXML
    // private FXTableColumn<MysqlRoutineParam, String> paramMode;

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
     * @param procedure 查询对象
     * @param dbItem    db库树节点
     */
    public void init(MysqlProcedure procedure, ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
        this.procedure = procedure;
        // 更新新数据标志位
        this.newData = procedure.isNew();
        StageManager.showMask(this::doInit);
    }

    /**
     * 执行初始化
     *
     */
    private void doInit() {
//        // 查询最新数据
//        if (!this.procedure.isNew()) {
//            this.procedure = this.dbItem.selectProcedure(procedure.getName());
//        }

        // 初始化监听器
        this.initDBListener();

        // 初始化信息
        FXUtil.runWait(this::initInfo);
//        this.initInfo();

        // 监听组件
        CacheHelper.set("dbClient", this.dbItem.client());
        DBStatusListenerManager.bindListener(this.definer, this.listener);
        DBStatusListenerManager.bindListener(this.comment, this.listener);
        DBStatusListenerManager.bindListener(this.definition, this.listener);
        DBStatusListenerManager.bindListener(this.securityType, this.listener);
        DBStatusListenerManager.bindListener(this.characteristic, this.listener);
        // this.paramTable.itemList().addListener(this.listChangeListener);
        this.paramTable.setStatusListener(this.listener);
    }

    /**
     * 初始化数据监听器
     */
    private void initDBListener() {
        // 初始化监听器
        this.listener = new DBStatusListener(this.procedure.getDbName() + ":" + this.procedure.getName()) {
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

//        // 更新新表标志位
//        this.newData = this.procedure.isNew();

        // 如果是新数据，则默认触发变更
        if (this.procedure.isNew()) {
            this.unsaved = true;
            this.definer.setText("`root`@`%`");
            String defDefinition = """
                    BEGIN
                        #Routine body goes here...
                    
                    END
                    """;
            this.definition.setText(defDefinition);
        } else {
            // 查询过程信息
            this.procedure = this.dbItem.selectProcedure(this.procedure.getName());
            // 初始化数据
            this.definer.setText(this.procedure.getDefiner());
            this.comment.setText(this.procedure.getComment());
            this.definition.setText(this.procedure.getDefinition());
            this.definition.forgetHistory();
            this.paramTable.setItem(this.procedure.getParams());
            this.securityType.select(this.procedure.getSecurityType());
            this.characteristic.select(this.procedure.getCharacteristic());
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
     * 过程名称
     */
    private String procedureName;

    /**
     * 执行保存
     */
    private void doSave() {
        try {
            // 创建临时对象
            MysqlProcedure tempProcedure = this.tempData();

            if (this.newData) {
                procedureName = MessageBox.prompt(I18nHelper.pleaseInputProcedureName(), procedureName);
                if (procedureName == null) {
                    return;
                }
                tempProcedure.setName(procedureName);
            } else {
                procedureName = tempProcedure.getName();
            }

            // this.disableTab();

            // 创建过程
            if (this.newData) {
                this.dbItem.createProcedure(tempProcedure);
                MysqlProcedure procedure = this.dbItem.selectProcedure(procedureName);
                this.dbItem.getProcedureTypeChild().addProcedure(procedure);
                // ShellMysqlEventUtil.procedureAdded(this.dbItem);
                this.initDBListener();
            } else {// 修改过程
                this.dbItem.alertProcedure(tempProcedure);
                // ShellMysqlEventUtil.procedureAlerted(procedureName, this.dbItem);
            }
            // // 刷新数据
            // this.dbItem.getProcedureTypeChild().reloadChild();
            // 更新保存标志位
            this.unsaved = false;
            // 更新新数据标志位
            this.newData = false;
//            // 重载表数据
//            this.procedure = this.dbItem.selectProcedure(procedureName);
            this.procedure = tempProcedure;
            // 刷新tab
            FXUtil.runWait(this::initInfo);
//            this.initInfo();
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
    private MysqlProcedure tempData() {
        // 创建临时对象
        MysqlProcedure tempProcedure = new MysqlProcedure();
        tempProcedure.setName(this.procedure.getName());

        // 基本信息处理
        tempProcedure.setDbName(this.procedure.getDbName());
        tempProcedure.setParams(this.paramTable.getItems());
        tempProcedure.setDefiner(this.definer.getTextTrim());
        tempProcedure.setComment(this.comment.getTextTrim());
        tempProcedure.setDefinition(this.definition.getTextTrim());
        tempProcedure.setSecurityType(this.securityType.getSelectedItem());
        tempProcedure.setCharacteristic(this.characteristic.getSelectedItem());

        return tempProcedure;
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        NodeUtil.nodeOnCtrlS(this.definer, this::save);
        NodeUtil.nodeOnCtrlS(this.comment, this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
        this.paramTable.setCtrlSAction(this::save);
        // // 绑定属性
        // this.paramName.setCellValueFactory(new PropertyValueFactory<>("nameControl"));
        // this.paramMode.setCellValueFactory(new PropertyValueFactory<>("modeControl"));
        // this.paramType.setCellValueFactory(new PropertyValueFactory<>("typeControl"));
        // this.paramSize.setCellValueFactory(new PropertyValueFactory<>("sizeControl"));
        // this.paramValue.setCellValueFactory(new PropertyValueFactory<>("valueControl"));
        // this.paramDigits.setCellValueFactory(new PropertyValueFactory<>("digitsControl"));
        // this.paramCharset.setCellValueFactory(new PropertyValueFactory<>("charsetControl"));
        // this.paramCollation.setCellValueFactory(new PropertyValueFactory<>("collationControl"));

        // 切换面板监听
        this.tabPane.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 1) {
                NodeGroupUtil.display(this.getTab(), "param");
            } else {
                NodeGroupUtil.disappear(this.getTab(), "param");
            }
            if (newValue.intValue() == 3) {
                // MysqlProcedure temp = this.tempData();
                // if (StringUtil.isBlank(temp.getName())) {
                //     temp.setName("Unnamed_Procedure");
                // }
                // String sql = MysqlProcedureSqlGenerator.INSTANCE.generate(temp);
                // this.preview.setText(sql);
                this.initPreview();
            }
        });
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        MysqlProcedure temp = this.tempData();
        if (StringUtil.isBlank(temp.getName())) {
            temp.setName("Unnamed_Procedure");
        }
        String sql = MysqlProcedureSqlGenerator.INSTANCE.generate(temp);
        this.preview.setText(sql);
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

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }

    public boolean isUnsaved() {
        return unsaved;
    }

    public void setUnsaved(boolean unsaved) {
        this.unsaved = unsaved;
    }
}
