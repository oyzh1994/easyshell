package cn.oyzh.easyshell.tabs.mysql.event;

import cn.oyzh.common.cache.CacheHelper;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mysql.DBEditor;
import cn.oyzh.easyshell.fx.mysql.event.MysqlEventIntervalTypeCombobox;
import cn.oyzh.easyshell.fx.mysql.event.MysqlEventOnCompletionCombobox;
import cn.oyzh.easyshell.fx.mysql.event.MysqlEventStatusCombobox;
import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.mysql.generator.event.EventAlertSqlGenerator;
import cn.oyzh.easyshell.mysql.generator.event.EventCreateSqlGenerator;
import cn.oyzh.easyshell.mysql.listener.DBStatusListener;
import cn.oyzh.easyshell.mysql.listener.DBStatusListenerManager;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.field.DateTimeTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.button.FXRadioButton;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;


/**
 * @author oyzh
 * @since 2024/09/09
 */
public class MysqlEventDesignTabController extends RichTabController {

    /**
     * 事件
     */
    private MysqlEvent event;

    public MysqlEvent getEvent() {
        return event;
    }

    /**
     * db数据库树节点
     */
    private MysqlDatabaseTreeItem dbItem;

    public MysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

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
     * 计划类型
     */
    @FXML
    private FXToggleGroup planType;

    /**
     * 单次类型
     */
    @FXML
    private FXRadioButton onetimeType;

    /**
     * 单次执行时间
     */
    @FXML
    private DateTimeTextField onetime;

    /**
     * 单次循环组件
     */
    @FXML
    private FXCheckBox onetimeInterval;

    /**
     * 单次循环值
     */
    @FXML
    private NumberTextField onetimeIntervalValue;

    /**
     * 单次循环类型
     */
    @FXML
    private MysqlEventIntervalTypeCombobox onetimeIntervalType;

    /**
     * 周期类型
     */
    @FXML
    private FXRadioButton loopType;

    /**
     * 周期循环值
     */
    @FXML
    private NumberTextField loopIntervalValue;

    /**
     * 周期循环类型
     */
    @FXML
    private MysqlEventIntervalTypeCombobox loopIntervalType;

    /**
     * 周期循环开始
     */
    @FXML
    private FXCheckBox loopStart;

    /**
     * 周期循环开始时间
     */
    @FXML
    private DateTimeTextField loopStartTime;

    /**
     * 周期循环开始组件
     */
    @FXML
    private FXCheckBox loopStartInterval;

    /**
     * 周期循环开始值
     */
    @FXML
    private NumberTextField loopStartIntervalValue;

    /**
     * 周期循环开始类型
     */
    @FXML
    private MysqlEventIntervalTypeCombobox loopStartIntervalType;

    /**
     * 周期循环结束
     */
    @FXML
    private FXCheckBox loopEnd;

    /**
     * 周期循环结束时间
     */
    @FXML
    private DateTimeTextField loopEndTime;

    /**
     * 周期循环结束类型
     */
    @FXML
    private FXCheckBox loopEndInterval;

    /**
     * 周期循环结束值
     */
    @FXML
    private NumberTextField loopEndIntervalValue;

    /**
     * 周期循环结束类型
     */
    @FXML
    private MysqlEventIntervalTypeCombobox loopEndIntervalType;

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
     * 状态
     */
    @FXML
    private MysqlEventStatusCombobox status;

    /**
     * 完成时
     */
    @FXML
    private MysqlEventOnCompletionCombobox onCompletion;

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
     * @param event  事件对象
     * @param dbItem db库树节点
     */
    public void init(MysqlEvent event, MysqlDatabaseTreeItem dbItem) {
        this.event = event;
        this.dbItem = dbItem;

        // 初始化监听器
        this.initDBListener();

        // 初始化信息
        this.initInfo();

        // 监听组件
        CacheHelper.set("dbClient", this.dbItem.client());

        // 基础
        DBStatusListenerManager.bindListener(this.status, this.listener);
        DBStatusListenerManager.bindListener(this.definer, this.listener);
        DBStatusListenerManager.bindListener(this.comment, this.listener);
        DBStatusListenerManager.bindListener(this.definition, this.listener);
        DBStatusListenerManager.bindListener(this.onCompletion, this.listener);

        // 单次类型
        DBStatusListenerManager.bindListener(this.onetime, this.listener);
        DBStatusListenerManager.bindListener(this.onetimeType, this.listener);
        DBStatusListenerManager.bindListener(this.onetimeInterval, this.listener);
        DBStatusListenerManager.bindListener(this.onetimeIntervalValue, this.listener);
        DBStatusListenerManager.bindListener(this.onetimeIntervalType, this.listener);

        // 周期类型
        DBStatusListenerManager.bindListener(this.loopType, this.listener);
        DBStatusListenerManager.bindListener(this.loopStart, this.listener);
        DBStatusListenerManager.bindListener(this.loopStartTime, this.listener);
        DBStatusListenerManager.bindListener(this.loopStartInterval, this.listener);
        DBStatusListenerManager.bindListener(this.loopStartIntervalValue, this.listener);
        DBStatusListenerManager.bindListener(this.loopStartIntervalType, this.listener);
        DBStatusListenerManager.bindListener(this.loopEnd, this.listener);
        DBStatusListenerManager.bindListener(this.loopEndTime, this.listener);
        DBStatusListenerManager.bindListener(this.loopEndInterval, this.listener);
        DBStatusListenerManager.bindListener(this.loopEndIntervalValue, this.listener);
        DBStatusListenerManager.bindListener(this.loopEndIntervalType, this.listener);
    }

    /**
     * 初始化数据监听器
     */
    private void initDBListener() {
        // 初始化监听器
        this.listener = new DBStatusListener(this.event.getDbName() + ":" + this.event.getName()) {
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
        this.newData = this.event.isNew();

        // 初始化数据
        this.status.select(this.event.getStatus());
        this.definer.setText(this.event.getDefiner());
        this.comment.setText(this.event.getComment());
        this.definition.setText(this.event.getDefinition());
        this.definition.forgetHistory();
        this.definition.setDialect(this.dbItem.dialect());
        this.onCompletion.select(this.event.getOnCompletion());

        // 清理旧设置
        this.onetime.clear();
        this.onetimeInterval.setSelected(false);
        this.onetimeIntervalValue.clear();
        this.onetimeIntervalType.selectFirst();
        this.loopStart.setSelected(false);
        this.loopStartTime.clear();
        this.loopStartInterval.setSelected(false);
        this.loopStartIntervalValue.clear();
        this.loopStartIntervalType.selectFirst();
        this.loopEnd.setSelected(false);
        this.loopEndTime.clear();
        this.loopEndInterval.setSelected(false);
        this.loopEndIntervalValue.clear();
        this.loopEndIntervalType.selectFirst();

        // 处理时间
        if (this.event.isRecurringType()) {
            this.loopType.setSelected(true);
            this.loopIntervalType.select(this.event.getIntervalField());
            this.loopIntervalValue.setValue(this.event.getIntervalValue());
            if (this.event.getStarts() != null) {
                this.loopStart.setSelected(true);
                this.loopStartTime.setValue(this.event.getStarts());
            }
            if (this.event.getEnds() != null) {
                this.loopEnd.setSelected(true);
                this.loopEndTime.setValue(this.event.getEnds());
            }
        } else {
            this.onetimeType.setSelected(true);
            if (this.event.getExecuteAt() != null) {
                this.onetime.setValue(this.event.getExecuteAt());
            } else {
                this.onetime.setText("CURRENT_TIMESTAMP");
            }
        }

        // 如果是新数据，则默认触发变更
        if (this.newData) {
            this.unsaved = true;
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
            MysqlEvent temp = this.tempData();

            // 事件名称
            String eventName;
            if (this.newData) {
                eventName = MessageBox.prompt(I18nHelper.pleaseInputEventName());
                if (eventName == null) {
                    return;
                }
                temp.setName(eventName);
            } else {
                eventName = temp.getName();
            }

            // this.disableTab();

            // 创建事件
            if (this.newData) {
                this.dbItem.createEvent(temp);
                MysqlEvent event = this.dbItem.selectEvent(eventName);
                this.dbItem.getEventTypeChild().addEvent(event);
                // MysqlEventUtil.eventAdded(this.dbItem);
                this.initDBListener();
            } else {// 修改事件
                this.dbItem.alertEvent(temp);
                // MysqlEventUtil.eventAlerted(eventName, this.dbItem);
            }
            // // 刷新数据
            // this.dbItem.getEventTypeChild().reloadChild();
            // 更新保存标志位
            this.unsaved = false;
            // 重载数据
            this.event = this.dbItem.selectEvent(eventName);
            // 刷新tab
            this.initInfo();
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
    private MysqlEvent tempData() {
        // 创建临时对象
        MysqlEvent temp = new MysqlEvent();

        // 基本信息处理
        temp.setName(this.event.getName());
        temp.setDbName(this.event.getDbName());

        // 定义者
        if (!StringUtil.equalsIgnoreCase(this.event.getDefiner(), this.definer.getTextTrim())) {
            temp.setDefiner(this.definer.getTextTrim());
        }

        // 注释
        if (!StringUtil.equalsIgnoreCase(this.event.getComment(), this.comment.getTextTrim())) {
            temp.setComment(this.comment.getTextTrim());
        }

        // 定义
        if (!StringUtil.equalsIgnoreCase(this.event.getDefinition(), this.definition.getTextTrim())) {
            temp.setDefinition(this.definition.getTextTrim());
        }

        // 状态
        if (!this.status.isSameStatus(this.event.getStatus())) {
            temp.setStatus(this.status.getSelectedItem());
        }

        // 完成时
        if (!StringUtil.equalsIgnoreCase(this.event.getOnCompletion(), this.onCompletion.getSelectedItem())) {
            temp.setOnCompletion(this.onCompletion.getSelectedItem());
        }

        // 类型
        temp.setType(this.planType.selectedUserData());
        // 时间
        if (temp.isOnTimeType()) {
            temp.setExecuteAt(this.onetime.getObjectValue());
            if (this.onetimeInterval.isSelected()) {
                temp.setIntervalValue(this.onetimeIntervalValue.getIntValue());
                temp.setIntervalField(this.onetimeIntervalType.getSelectedItem());
            }
        } else {
            temp.setIntervalValue(this.loopIntervalValue.getIntValue());
            temp.setIntervalField(this.loopIntervalType.getSelectedItem());
            if (this.loopStart.isSelected()) {
                temp.setStarts(this.loopStartTime.getObjectValue());
                if (this.loopStartInterval.isSelected()) {
                    temp.setStartIntervalValue(this.loopStartIntervalValue.getIntValue());
                    temp.setStartIntervalField(this.loopStartIntervalType.getSelectedItem());
                }
            }
            if (this.loopEnd.isSelected()) {
                temp.setEnds(this.loopEndTime.getObjectValue());
                if (this.loopEndInterval.isSelected()) {
                    temp.setEndIntervalValue(this.loopEndIntervalValue.getIntValue());
                    temp.setEndIntervalField(this.loopEndIntervalType.getSelectedItem());
                }
            }
        }
        return temp;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.onetimeType.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.onetime.setDisable(false);
                this.onetimeInterval.setDisable(false);
            } else {
                this.onetime.setDisable(true);
                this.onetimeInterval.setDisable(true);
            }
        });
        this.onetimeInterval.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.onetimeIntervalType.setDisable(false);
                this.onetimeIntervalValue.setDisable(false);
            } else {
                this.onetimeIntervalType.setDisable(true);
                this.onetimeIntervalValue.setDisable(true);
            }
        });
        this.loopType.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.loopIntervalType.setDisable(false);
                this.loopIntervalValue.setDisable(false);

                this.loopStart.setDisable(false);
                if (this.loopStart.isSelected()) {
                    this.loopStartInterval.setDisable(false);
                    if (this.loopStartInterval.isSelected()) {
                        this.loopStartIntervalType.setDisable(false);
                        this.loopStartIntervalValue.setDisable(false);
                    }
                }

                this.loopEnd.setDisable(false);
                if (this.loopEnd.isSelected()) {
                    this.loopEndInterval.setDisable(false);
                    if (this.loopEndInterval.isSelected()) {
                        this.loopEndIntervalType.setDisable(false);
                        this.loopEndIntervalValue.setDisable(false);
                    }
                }
            } else {
                this.loopIntervalType.setDisable(true);
                this.loopIntervalValue.setDisable(true);
                this.loopStart.setDisable(true);
                this.loopStartInterval.setDisable(true);
                this.loopEnd.setDisable(true);
                this.loopEndInterval.setDisable(true);
            }
        });
        this.loopStart.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.loopStartTime.setDisable(false);
                this.loopStartInterval.setDisable(false);
                if (this.loopStartInterval.isSelected()) {
                    this.loopStartIntervalType.setDisable(false);
                    this.loopStartIntervalValue.setDisable(false);
                }
            } else {
                this.loopStartTime.setDisable(true);
                this.loopStartInterval.setDisable(true);
            }
        });
        this.loopEnd.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.loopEndTime.setDisable(false);
                this.loopEndInterval.setDisable(false);
                if (this.loopEndInterval.isSelected()) {
                    this.loopEndIntervalType.setDisable(false);
                    this.loopEndIntervalValue.setDisable(false);
                }
            } else {
                this.loopEndTime.setDisable(true);
                this.loopEndInterval.setDisable(true);
            }
        });
        this.loopStartInterval.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.loopStartIntervalValue.setDisable(false);
                this.loopStartIntervalType.setDisable(false);
            } else {
                this.loopStartIntervalValue.setDisable(true);
                this.loopStartIntervalType.setDisable(true);
            }
        });
        this.loopEndInterval.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.loopEndIntervalValue.setDisable(false);
                this.loopEndIntervalType.setDisable(false);
            } else {
                this.loopEndIntervalValue.setDisable(true);
                this.loopEndIntervalType.setDisable(true);
            }
        });
        this.loopStartInterval.disableProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.loopStartIntervalValue.setDisable(newValue);
                this.loopStartIntervalType.setDisable(newValue);
            }
        });
        this.loopEndInterval.disableProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.loopEndIntervalValue.setDisable(newValue);
                this.loopEndIntervalType.setDisable(newValue);
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        NodeUtil.nodeOnCtrlS(this.definer, this::save);
        NodeUtil.nodeOnCtrlS(this.comment, this::save);
        NodeUtil.nodeOnCtrlS(this.status, this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
        NodeUtil.nodeOnCtrlS(this.onetime, this::save);
        NodeUtil.nodeOnCtrlS(this.onetimeIntervalValue, this::save);
        NodeUtil.nodeOnCtrlS(this.loopStartTime, this::save);
        NodeUtil.nodeOnCtrlS(this.loopStartIntervalValue, this::save);
        NodeUtil.nodeOnCtrlS(this.loopEndTime, this::save);
        NodeUtil.nodeOnCtrlS(this.loopEndIntervalValue, this::save);
        // 切换面板监听
        this.tabPane.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 3) {
                // String sql;
                // MysqlEvent temp = this.tempData();
                // if (this.newData) {
                //     if (StringUtil.isBlank(temp.getName())) {
                //         temp.setName("Unnamed_Event");
                //     }
                //     sql = EventCreateSqlGenerator.generate(this.dbItem.dialect(), temp);
                // } else {
                //     sql = EventAlertSqlGenerator.generate(this.dbItem.dialect(), temp);
                // }
                // this.preview.setText(sql);
                this.initPreview();
            }
        });
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        String sql;
        MysqlEvent temp = this.tempData();
        if (this.newData) {
            if (StringUtil.isBlank(temp.getName())) {
                temp.setName("Unnamed_Event");
            }
            sql = EventCreateSqlGenerator.generate(this.dbItem.dialect(), temp);
        } else {
            sql = EventAlertSqlGenerator.generate(this.dbItem.dialect(), temp);
        }
        this.preview.text(sql);
    }

    public boolean isUnsaved() {
        return unsaved;
    }

    public void setUnsaved(boolean unsaved) {
        this.unsaved = unsaved;
    }
}
