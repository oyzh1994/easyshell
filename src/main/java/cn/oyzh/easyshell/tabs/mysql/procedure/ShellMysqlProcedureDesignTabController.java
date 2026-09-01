package cn.oyzh.easyshell.tabs.mysql.procedure;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlSecurityTypeComboBox;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlStatusTableView;
import cn.oyzh.easyshell.fx.mysql.routine.ShellMysqlCharacteristicCombobox;
import cn.oyzh.easyshell.mysql.generator.procedure.MysqlProcedureAlertSqlGenerator;
import cn.oyzh.easyshell.mysql.generator.procedure.MysqlProcedureCreateSqlGenerator;
import cn.oyzh.easyshell.mysql.procedure.MysqlAlertProcedureParam;
import cn.oyzh.easyshell.mysql.procedure.MysqlCreateProcedureParam;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.query.mysql.ShellMysqlQueryEditor;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.editor.incubator.Editor;
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
    public void init(MysqlProcedure procedure, ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
        this.procedure = procedure;
        // 更新新数据标志位
        this.newData = procedure.isNew();
        StageManager.showMask(this::doInit);
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
            DBStatusListenerManager.unbindListener(this.definer, this.listener);
            DBStatusListenerManager.unbindListener(this.comment, this.listener);
            DBStatusListenerManager.unbindListener(this.definition, this.listener);
            DBStatusListenerManager.unbindListener(this.securityType, this.listener);
            DBStatusListenerManager.unbindListener(this.characteristic, this.listener);
        }
        // 初始化监听器
        this.listener = new DBStatusListener(this.procedure.getDbName() + ":" + this.procedure.getName()) {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                initChangedFlag();
            }
        };
        DBStatusListenerManager.bindListener(this.definer, this.listener);
        DBStatusListenerManager.bindListener(this.comment, this.listener);
        DBStatusListenerManager.bindListener(this.definition, this.listener);
        DBStatusListenerManager.bindListener(this.securityType, this.listener);
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
                    
                    END
                    """;
            this.definition.setText(defDefinition);
            NodeGroupUtil.disappear(this.getTab(), "action3");
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
        if (!MessageBox.confirm(I18nHelper.refreshData() + "?")) {
            return;
        }
        try {
            this.init(this.procedure, this.dbItem);
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
                this.procedureName = MessageBox.prompt(I18nHelper.pleaseInputProcedureName(), this.procedureName);
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
                MysqlProcedure procedure = this.dbItem.selectProcedure(this.procedureName);
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

    /**
     * 初始化预览
     */
    private void initPreview() {
        MysqlProcedure temp = this.tempData();
        String sql;
        if (this.newData) {
            MysqlCreateProcedureParam param = new MysqlCreateProcedureParam();
            param.setProcedure(temp);
            param.setDbName(this.dbItem.dbName());
            if (StringUtil.isBlank(param.getProcedureName())) {
                param.setProcedureName("Unnamed_Procedure");
            }
            sql = MysqlProcedureCreateSqlGenerator.generateSqlSingle(param);
        } else {
            MysqlAlertProcedureParam param = new MysqlAlertProcedureParam();
            param.setProcedure(temp);
            param.setDbName(this.dbItem.dbName());
            sql = MysqlProcedureAlertSqlGenerator.generateSqlSingle(param);
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

    private void initParamTable() {
        for (MysqlRoutineParam index : this.paramTable.itemList()) {
            index.setDbClient(this.dbItem.client());
        }
    }
}
