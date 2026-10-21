package cn.oyzh.easyshell.tabs.dameng.sequence;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.generator.sequence.DamengSequenceAlertSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.sequence.DamengSequenceCreateSqlGenerator;
import cn.oyzh.easyshell.dameng.sequence.DamengAlertSequenceParam;
import cn.oyzh.easyshell.dameng.sequence.DamengCreateSequenceParam;
import cn.oyzh.easyshell.dameng.sequence.DamengSequence;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;


/**
 * db序列内容组件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengSequenceDesignTabController extends RichTabController {

    /**
     * 序列
     */
    private DamengSequence sequence;

    /**
     * 获取序列对象
     *
     * @return 序列对象
     */
    public DamengSequence getSequence() {
        return sequence;
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
     * 最小值
     */
    @FXML
    private NumberTextField minVal;

    /**
     * 当前值
     */
    @FXML
    private NumberTextField currentVal;

    /**
     * 最大值
     */
    @FXML
    private NumberTextField maxVal;

    /**
     * 递增
     */
    @FXML
    private NumberTextField incrementBy;

    /**
     * 缓存
     */
    @FXML
    private NumberTextField cacheSize;

    /**
     * 循环
     */
    @FXML
    private FXCheckBox cycle;

    /**
     * 顺序
     */
    @FXML
    private FXCheckBox order;

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
     * @param sequence 查询对象
     * @param dbItem   db库树节点
     */
    public void init(DamengSequence sequence, ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
        this.sequence = sequence;
        // 更新新数据标志位
        this.newData = sequence.isNew();
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
            //            DBStatusListenerManager.unbindListener(this.definition, this.listener);
            DBStatusListenerManager.unbindListener(this.cycle, this.listener);
            DBStatusListenerManager.unbindListener(this.order, this.listener);
            DBStatusListenerManager.unbindListener(this.maxVal, this.listener);
            DBStatusListenerManager.unbindListener(this.minVal, this.listener);
            DBStatusListenerManager.unbindListener(this.cacheSize, this.listener);
            DBStatusListenerManager.unbindListener(this.currentVal, this.listener);
            DBStatusListenerManager.unbindListener(this.incrementBy, this.listener);
        }
        // 初始化监听器
        this.listener = new DBStatusListener(this.sequence.getSchema() + ":" + this.sequence.getName()) {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                initChangedFlag();
            }
        };
        //        DBStatusListenerManager.bindListener(this.definition, this.listener);
        DBStatusListenerManager.bindListener(this.cycle, this.listener);
        DBStatusListenerManager.bindListener(this.minVal, this.listener);
        DBStatusListenerManager.bindListener(this.maxVal, this.listener);
        DBStatusListenerManager.bindListener(this.cacheSize, this.listener);
        DBStatusListenerManager.bindListener(this.currentVal, this.listener);
        DBStatusListenerManager.bindListener(this.incrementBy, this.listener);
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
        if (!this.newData) {
            // 查询函数信息
            this.sequence = this.dbItem.selectSequence(this.sequence.getName());
            // 初始化数据
            this.definition.setText(this.sequence.getCreateDefinition());
            if (this.sequence.getMinVal() != null) {
                this.minVal.setValue(this.sequence.getMinVal());
            }
            if (this.sequence.getMaxVal() != null) {
                this.maxVal.setValue(this.sequence.getMaxVal());
            }
            if (this.sequence.getCurrentVal() != null) {
                this.currentVal.setValue(this.sequence.getCurrentVal());
            }
            if (this.sequence.getCacheSize() != null) {
                this.cacheSize.setValue(this.sequence.getCacheSize());
            }
            if (this.sequence.getIncrementBy() != null) {
                this.incrementBy.setValue(this.sequence.getIncrementBy());
            }
            this.order.setSelected(this.sequence.isOrder());
            this.cycle.setSelected(this.sequence.isCycle());
        }

        // 标记为结束
        FXUtil.runPulse(() -> this.initiating = false);
    }

    /**
     * 刷新
     */
    @FXML
    private void refresh() {
        if (!MessageBox.confirm(I18nHelper.refreshData() + "?", this.getWindow())) {
            return;
        }
        try {
            this.init(this.sequence, this.dbItem);
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
     * 序列名称
     */
    private String sequenceName;

    /**
     * 执行保存
     */
    private void doSave() {
        try {
            // 创建临时对象
            DamengSequence tempSequence = this.tempData();

            if (this.newData) {
                this.sequenceName = MessageBox.prompt(I18nHelper.pleaseInputSequenceName(), this.sequenceName, this.getWindow());
                if (this.sequenceName == null) {
                    return;
                }
                tempSequence.setName(this.sequenceName);
            } else {
                this.sequenceName = tempSequence.getName();
            }

            // 创建序列
            if (this.newData) {
                this.dbItem.createSequence(tempSequence);
                DamengSequence sequence = this.dbItem.selectSequence(this.sequenceName);
                this.dbItem.getSequenceTypeChild().addSequence(sequence);
                this.initDBListener();
            } else {// 修改序列
                this.dbItem.alertSequence(tempSequence);
            }
            // 更新保存标志位
            this.unsaved = false;
            // 更新新数据标志位
            this.newData = false;
            this.sequence = tempSequence;
            // 刷新tab
            FXUtil.runWait(this::initInfo);
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
    private DamengSequence tempData() {
        // 创建临时对象
        DamengSequence tempSequence = new DamengSequence();
        tempSequence.setName(this.sequence.getName());

        // 基本信息处理
        tempSequence.setSchema(this.sequence.getSchema());
        if (StringUtil.isNotBlank(this.minVal.getText())) {
            tempSequence.setMinVal(this.minVal.getValue());
        }
        if (StringUtil.isNotBlank(this.maxVal.getText())) {
            tempSequence.setMaxVal(this.maxVal.getValue());
        }
        if (StringUtil.isNotBlank(this.cacheSize.getText())) {
            tempSequence.setCacheSize(this.cacheSize.getValue());
        }
        if (StringUtil.isNotBlank(this.currentVal.getText())) {
            tempSequence.setCurrentVal(this.currentVal.getValue());
        }
        if (StringUtil.isNotBlank(this.incrementBy.getText())) {
            tempSequence.setIncrementBy(this.incrementBy.getValue());
        }
        tempSequence.setOrder(this.order.isSelected());
        tempSequence.setCycle(this.cycle.isSelected());
        return tempSequence;
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        DamengSequence temp = this.tempData();
        String sql;
        if (this.newData) {
            DamengCreateSequenceParam param = new DamengCreateSequenceParam();
            param.setSequence(temp);
            param.setSchema(this.schema());
            if (StringUtil.isBlank(param.getSequenceName())) {
                param.setSequenceName("Unnamed_Sequence");
            }
            sql = DamengSequenceCreateSqlGenerator.generateSqlSingle(param);
        } else {
            DamengAlertSequenceParam param = new DamengAlertSequenceParam();
            param.setSequence(temp);
            param.setSchema(this.schema());
            sql = DamengSequenceAlertSqlGenerator.generateSqlSingle(param);
        }
        this.preview.text(sql);
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    /**
     * 设置数据库树节点
     *
     * @param dbItem 数据库树节点
     */
    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
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

        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
        NodeUtil.nodeOnCtrlS(this.preview, this::save);
        // 切换面板监听
        this.tabPane.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 2) {
                this.initPreview();
            }
        });
    }
}
