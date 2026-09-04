package cn.oyzh.easyshell.tabs.dameng.procedure;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.generator.procedure.DamengProcedureAlertSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.procedure.DamengProcedureCreateSqlGenerator;
import cn.oyzh.easyshell.dameng.procedure.DamengAlertProcedureParam;
import cn.oyzh.easyshell.dameng.procedure.DamengCreateProcedureParam;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.dameng.routine.DamengRoutineParam;
import cn.oyzh.easyshell.fx.dameng.ShellDamengSecurityTypeComboBox;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
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
public class ShellDamengProcedureDesignTabController extends RichTabController {

    /**
     * 过程
     */
    private DamengProcedure procedure;

    public DamengProcedure getProcedure() {
        return procedure;
    }

    /**
     * db数据库树节点
     */
    private ShellDamengSchemaTreeItem dbItem;

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
    private ShellDamengSecurityTypeComboBox securityType;

    /**
     * 特征，parallelEnable
     */
    @FXML
    private FXCheckBox parallelEnable;

    /**
     * 参数表单
     */
    @FXML
    private DBStatusTableView<DamengRoutineParam> paramTable;

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
    public void init(DamengProcedure procedure, ShellDamengSchemaTreeItem dbItem) {
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
            DamengProcedure tempProcedure = this.tempData();

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
                DamengProcedure procedure = this.dbItem.selectProcedure(this.procedureName);
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
    private DamengProcedure tempData() {
        // 创建临时对象
        DamengProcedure tempProcedure = new DamengProcedure();
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
        DamengProcedure temp = this.tempData();
        String sql;
        if (this.newData) {
            DamengCreateProcedureParam param = new DamengCreateProcedureParam();
            param.setProcedure(temp);
            param.setSchema(this.schema());
            if (StringUtil.isBlank(param.getProcedureName())) {
                param.setProcedureName("Unnamed_Procedure");
            }
            sql = DamengProcedureCreateSqlGenerator.generateSqlSingle(param);
        } else {
            DamengAlertProcedureParam param = new DamengAlertProcedureParam();
            param.setProcedure(temp);
            param.setSchema(this.schema());
            sql = DamengProcedureAlertSqlGenerator.generateSqlSingle(param);
        }
        this.preview.text(sql);
    }

    /**
     * 添加参数
     */
    @FXML
    private void addParam() {
        try {
            DamengRoutineParam param = new DamengRoutineParam();
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
            DamengRoutineParam param = this.paramTable.getSelectedItem();
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

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }

    public String schema() {
        return this.dbItem.schema();
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
        this.paramTable.itemList().addListener((ListChangeListener<DamengRoutineParam>) c -> {
            while (c.next() && (c.wasAdded() || c.wasReplaced())) {
                this.initParamTable();
            }
        });
        this.initParamTable();

        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
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

    private void initParamTable() {
        for (DamengRoutineParam index : this.paramTable.itemList()) {
            index.setDbClient(this.dbItem.client());
        }
    }
}
