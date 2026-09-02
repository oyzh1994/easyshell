package cn.oyzh.easyshell.tabs.dameng.event;//package cn.oyzh.easyshell.tabs.dameng.event;
//
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyshell.dameng.event.DamengEvent;
//import cn.oyzh.easyshell.event.dameng.DamengEventUtil;
//import cn.oyzh.easyshell.fx.dameng.DBEditor;
//import cn.oyzh.easyshell.fx.dameng.event.DamengEventIntervalTypeCombobox;
//import cn.oyzh.easyshell.fx.dameng.event.DamengEventOnCompletionCombobox;
//import cn.oyzh.easyshell.fx.dameng.event.DamengEventStatusCombobox;
//import cn.oyzh.easydameng.generator.event.DamengEventCreateSqlGenerator;
//import cn.oyzh.easydameng.generator.event.EventAlertSqlGenerator;
//import cn.oyzh.easydameng.listener.DBStatusListener;
//import cn.oyzh.easydameng.listener.DBStatusListenerManager;
//import cn.oyzh.easyshell.trees.dameng.database.DamengSchemaTreeItem;
//import cn.oyzh.fx.gui.tabs.RichTabController;
//import cn.oyzh.fx.gui.text.field.DateTimeTextField;
//import cn.oyzh.fx.gui.text.field.NumberTextField;
//import cn.oyzh.fx.plus.controls.button.FXCheckBox;
//import cn.oyzh.fx.plus.controls.button.FXRadioButton;
//import cn.oyzh.fx.plus.controls.tab.FXTabPane;
//import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
//import cn.oyzh.fx.plus.controls.text.field.FXTextField;
//import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
//import cn.oyzh.fx.plus.information.MessageBox;
//import cn.oyzh.fx.plus.node.NodeUtil;
//import cn.oyzh.fx.plus.util.FXUtil;
//import cn.oyzh.fx.plus.window.StageManager;
//import cn.oyzh.i18n.I18nHelper;
//import javafx.beans.value.ObservableValue;
//import javafx.fxml.FXML;
//
//import java.net.URL;
//import java.util.ResourceBundle;
//
//
///**
// * @author oyzh
// * @since 2024/09/09
// */
//public class DamengEventDesignTabController extends RichTabController {
//
//    /**
//     * 事件
//     */
//    private DamengEvent event;
//
//    public DamengEvent getEvent() {
//        return event;
//    }
//
//    /**
//     * db数据库树节点
//     */
//    private DamengSchemaTreeItem dbItem;
//
//    public DamengSchemaTreeItem getDbItem() {
//        return dbItem;
//    }
//
//    /**
//     * 定义
//     */
//    @FXML
//    private DBEditor definition;
//
//    /**
//     * 预览
//     */
//    @FXML
//    private DBEditor preview;
//
//    /**
//     * 计划类型
//     */
//    @FXML
//    private FXToggleGroup planType;
//
//    /**
//     * 单次类型
//     */
//    @FXML
//    private FXRadioButton onetimeType;
//
//    /**
//     * 单次执行时间
//     */
//    @FXML
//    private DateTimeTextField onetime;
//
//    /**
//     * 单次循环组件
//     */
//    @FXML
//    private FXCheckBox onetimeInterval;
//
//    /**
//     * 单次循环值
//     */
//    @FXML
//    private NumberTextField onetimeIntervalValue;
//
//    /**
//     * 单次循环类型
//     */
//    @FXML
//    private DamengEventIntervalTypeCombobox onetimeIntervalType;
//
//    /**
//     * 切换面板
//     */
//    @FXML
//    private FXTabPane tabPane;
//
//    /**
//     * 注释
//     */
//    @FXML
//    private FXTextArea comment;
//
//    /**
//     * 定义者
//     */
//    @FXML
//    private FXTextField definer;
//
//    /**
//     * 状态
//     */
//    @FXML
//    private DamengEventStatusCombobox status;
//
//    /**
//     * 完成时
//     */
//    @FXML
//    private DamengEventOnCompletionCombobox onCompletion;
//
//    /**
//     * 数据监听器
//     */
//    private DBStatusListener listener;
//
//    /**
//     * 未保存标志位
//     */
//    private boolean unsaved;
//
//    /**
//     * 新数据标志位
//     */
//    private boolean newData;
//
//    /**
//     * 初始化中标志位
//     */
//    private boolean initiating;
//
//    /**
//     * 执行初始化
//     *
//     * @param event  事件对象
//     * @param dbItem db库树节点
//     */
//    public void init(DamengEvent event, DamengSchemaTreeItem dbItem) {
//        this.event = event;
//        this.dbItem = dbItem;
//
//        // 初始化监听器
//        this.initDBListener();
//
//        // 初始化信息
//        this.initInfo();
//        //
//        //// 监听组件
//        // CacheHelper.set("dameng:dbClient", this.dbItem.client());
//
//        // 基础
//        DBStatusListenerManager.bindListener(this.status, this.listener);
//        DBStatusListenerManager.bindListener(this.definer, this.listener);
//        DBStatusListenerManager.bindListener(this.comment, this.listener);
//        DBStatusListenerManager.bindListener(this.definition, this.listener);
//        DBStatusListenerManager.bindListener(this.onCompletion, this.listener);
//
//        // 单次类型
//        DBStatusListenerManager.bindListener(this.onetime, this.listener);
//        DBStatusListenerManager.bindListener(this.onetimeType, this.listener);
//        DBStatusListenerManager.bindListener(this.onetimeInterval, this.listener);
//        DBStatusListenerManager.bindListener(this.onetimeIntervalValue, this.listener);
//        DBStatusListenerManager.bindListener(this.onetimeIntervalType, this.listener);
//    }
//
//    /**
//     * 初始化数据监听器
//     */
//    private void initDBListener() {
//        // 初始化监听器
//        this.listener = new DBStatusListener(this.event.getDbName() + ":" + this.event.getName()) {
//            @Override
//            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
//                initChangedFlag();
//            }
//        };
//    }
//
//    /**
//     * 初始化变更标志
//     */
//    private void initChangedFlag() {
//        if (!this.initiating) {
//            this.unsaved = true;
//            this.flushTab();
//        }
//    }
//
//    /**
//     * 初始化信息
//     */
//    protected void initInfo() {
//        // 更新初始化标志位
//        this.initiating = true;
//
//        // 更新新表标志位
//        this.newData = this.event.isNew();
//
//        // 初始化数据
//        this.status.select(this.event.getStatus());
//        this.definer.setText(this.event.getDefiner());
//        this.comment.setText(this.event.getComment());
//        this.definition.setText(this.event.getDefinition());
//        this.definition.forgetHistory();
//        this.definition.setDialect(this.dbItem.dialect());
//        this.onCompletion.select(this.event.getOnCompletion());
//
//        // 清理旧设置
//        this.onetime.clear();
//        this.onetimeInterval.setSelected(false);
//        this.onetimeIntervalValue.clear();
//        this.onetimeIntervalType.selectFirst();
//
//        // 处理时间
//        this.onetimeType.setSelected(true);
//        if (this.event.getExecuteAt() != null) {
//            this.onetime.setValue(this.event.getExecuteAt());
//        } else {
//            this.onetime.setText("SYSDATE");
//        }
//
//        // 如果是新数据，则默认触发变更
//        if (this.newData) {
//            this.unsaved = true;
//        }
//
//        // 标记为结束
//        FXUtil.runPulse(() -> this.initiating = false);
//    }
//
//    /**
//     * 保存
//     */
//    @FXML
//    private void save() {
//        StageManager.showMask(this::doSave);
//    }
//
//    /**
//     * 执行保存
//     */
//    private void doSave() {
//        try {
//            // 创建临时对象
//            DamengEvent temp = this.tempData();
//
//            // 事件名称
//            String eventName;
//            if (this.newData) {
//                eventName = MessageBox.prompt(I18nHelper.pleaseInputEventName());
//                if (eventName == null) {
//                    return;
//                }
//                temp.setName(eventName);
//            } else {
//                eventName = temp.getName();
//            }
//
//            // this.disableTab();
//
//            // 创建事件
//            if (this.newData) {
//                this.dbItem.createEvent(temp);
//                DamengEvent event = this.dbItem.selectEvent(eventName);
//                this.dbItem.getEventTypeChild().addEvent(event);
//                DamengEventUtil.eventAdded(this.dbItem);
//                this.initDBListener();
//            } else {// 修改事件
//                this.dbItem.alertEvent(temp);
//                DamengEventUtil.eventAlerted(eventName, this.dbItem);
//            }
//            // // 刷新数据
//            // this.dbItem.getEventTypeChild().reloadChild();
//            // 更新保存标志位
//            this.unsaved = false;
//            // 重载数据
//            this.event = this.dbItem.selectEvent(eventName);
//            // 刷新tab
//            this.initInfo();
//            // 初始化预览
//            this.initPreview();
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        } finally {
//            // this.enableTab();
//            this.flushTab();
//        }
//    }
//
//    /**
//     * 获取临时数据
//     *
//     * @return 临时数据
//     */
//    private DamengEvent tempData() {
//        // 创建临时对象
//        DamengEvent temp = new DamengEvent();
//
//        // 基本信息处理
//        temp.setName(this.event.getName());
//        temp.setDbName(this.event.getDbName());
//
//        // 定义者
//        if (!StringUtil.equalsIgnoreCase(this.event.getDefiner(), this.definer.getTextTrim())) {
//            temp.setDefiner(this.definer.getTextTrim());
//        }
//
//        // 注释
//        if (!StringUtil.equalsIgnoreCase(this.event.getComment(), this.comment.getTextTrim())) {
//            temp.setComment(this.comment.getTextTrim());
//        }
//
//        // 定义
//        if (!StringUtil.equalsIgnoreCase(this.event.getDefinition(), this.definition.getTextTrim())) {
//            temp.setDefinition(this.definition.getTextTrim());
//        }
//
//        // 状态
//        if (!this.status.isSameStatus(this.event.getStatus())) {
//            temp.setStatus(this.status.getSelectedItem());
//        }
//
//        // 完成时
//        if (!StringUtil.equalsIgnoreCase(this.event.getOnCompletion(), this.onCompletion.getSelectedItem())) {
//            temp.setOnCompletion(this.onCompletion.getSelectedItem());
//        }
//
//        // 类型
//        temp.setType(this.planType.selectedUserData());
//        // 时间
//        if (StringUtil.equalsIgnoreCase("SYSDATE", this.onetime.getText())) {
//            temp.setExecuteAt("SYSDATE");
//        } else {
//            temp.setExecuteAt(this.onetime.getValue());
//        }
//        if (this.onetimeInterval.isSelected()) {
//            temp.setIntervalValue(this.onetimeIntervalValue.getIntValue());
//            temp.setIntervalField(this.onetimeIntervalType.getSelectedItem());
//        }
//        return temp;
//    }
//
//    @Override
//    protected void bindListeners() {
//        super.bindListeners();
//        this.onetimeType.selectedProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue) {
//                this.onetime.setDisable(false);
//                this.onetimeInterval.setDisable(false);
//            } else {
//                this.onetime.setDisable(true);
//                this.onetimeInterval.setDisable(true);
//            }
//        });
//        this.onetimeInterval.selectedProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue) {
//                this.onetimeIntervalType.setDisable(false);
//                this.onetimeIntervalValue.setDisable(false);
//            } else {
//                this.onetimeIntervalType.setDisable(true);
//                this.onetimeIntervalValue.setDisable(true);
//            }
//        });
//    }
//
//    @Override
//    public void initialize(URL location, ResourceBundle resourceBundle) {
//        super.initialize(location, resourceBundle);
//        // 监听事件
//        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
//        NodeUtil.nodeOnCtrlS(this.definer, this::save);
//        NodeUtil.nodeOnCtrlS(this.comment, this::save);
//        NodeUtil.nodeOnCtrlS(this.status, this::save);
//        NodeUtil.nodeOnCtrlS(this.definition, this::save);
//        NodeUtil.nodeOnCtrlS(this.onetime, this::save);
//        NodeUtil.nodeOnCtrlS(this.onetimeIntervalValue, this::save);
//        // 切换面板监听
//        this.tabPane.selectedIndexChanged((observable, oldValue, newValue) -> {
//            if (newValue.intValue() == 3) {
//                this.initPreview();
//            }
//        });
//    }
//
//    /**
//     * 初始化预览
//     */
//    private void initPreview() {
//        String sql;
//        DamengEvent temp = this.tempData();
//        if (this.newData) {
//            if (StringUtil.isBlank(temp.getName())) {
//                temp.setName("Unnamed_Event");
//            }
//            sql = DamengEventCreateSqlGenerator.generateSql(temp);
//        } else {
//            sql = EventAlertSqlGenerator.generateSql(temp);
//        }
//        this.preview.text(sql);
//    }
//
//    public boolean isUnsaved() {
//        return unsaved;
//    }
//
//    public void setUnsaved(boolean unsaved) {
//        this.unsaved = unsaved;
//    }
//}
