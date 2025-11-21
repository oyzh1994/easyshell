package cn.oyzh.easyshell.controller.mysql.data;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.handler.DBDataTransportHandler;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.connect.ShellConnectTextField;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlDatabaseComboBox;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataTransportEventListView;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataTransportFunctionListView;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataTransportProcedureListView;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataTransportTableListView;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataTransportTriggerListView;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataTransportViewListView;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.pane.FXTitledPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.util.Counter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;


/**
 * mysql数据传输业务
 *
 * @author oyzh
 * @since 2024/09/05
 */
@StageAttribute(
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "mysql/data/shellMysqlDataTransport.fxml"
)
public class ShellMysqlDataTransportController extends StageController {

    /**
     * 第一步
     */
    @FXML
    private FXVBox step1;

    /**
     * 第二步
     */
    @FXML
    private FXVBox step2;

    /**
     * 第三步
     */
    @FXML
    private FXVBox step3;

    /**
     * 来源信息名称
     */
    @FXML
    private FXLabel sourceInfoName;

    /**
     * 来源库名称
     */
    @FXML
    private FXLabel sourceDatabaseName;

    /**
     * 目标信息名称
     */
    @FXML
    private FXLabel targetInfoName;

    /**
     * 目标库名称
     */
    @FXML
    private FXLabel targetDatabaseName;

    /**
     * 来源信息
     */
    @FXML
    private ShellConnectTextField sourceInfo;

    /**
     * 目标信息
     */
    @FXML
    private ShellConnectTextField targetInfo;

    /**
     * 来源库组件
     */
    @FXML
    private ShellMysqlDatabaseComboBox sourceDatabase;

    /**
     * 目标库组件
     */
    @FXML
    private ShellMysqlDatabaseComboBox targetDatabase;

    /**
     * 来源主机
     */
    @FXML
    private FXLabel sourceHost;

    /**
     * 目标主机
     */
    @FXML
    private FXLabel targetHost;

    /**
     * 来源服务版本
     */
    @FXML
    private FXLabel sourceVersion;

    /**
     * 目标服务版本
     */
    @FXML
    private FXLabel targetVersion;

    /**
     * 来源服务类型
     */
    @FXML
    private FXLabel sourceType;

    /**
     * 目标服务类型
     */
    @FXML
    private FXLabel targetType;

    /**
     * 来源客户端
     */
    private ShellMysqlClient sourceClient;

    /**
     * 目标客户端
     */
    private ShellMysqlClient targetClient;

    /**
     * 结束传输按钮
     */
    @FXML
    private FXButton stopTransportBtn;

    /**
     * 传输状态
     */
    @FXML
    private FXLabel transportStatus;

    /**
     * 传输消息
     */
    @FXML
    private MsgTextArea transportMsg;

    /**
     * 表组件
     */
    @FXML
    private FXTitledPane tablePane;

    /**
     * 视图组件
     */
    @FXML
    private FXTitledPane viewPane;

    /**
     * 函数组件
     */
    @FXML
    private FXTitledPane functionPane;

    /**
     * 过程组件
     */
    @FXML
    private FXTitledPane procedurePane;

    /**
     * 触发器组件
     */
    @FXML
    private FXTitledPane triggerPane;

    /**
     * 事件组件
     */
    @FXML
    private FXTitledPane eventPane;

    /**
     * 表列表
     */
    @FXML
    private ShellMysqlDataTransportTableListView tableList;

    /**
     * 事件列表
     */
    @FXML
    private ShellMysqlDataTransportEventListView eventList;

    /**
     * 视图列表
     */
    @FXML
    private ShellMysqlDataTransportViewListView viewList;

    /**
     * 函数列表
     */
    @FXML
    private ShellMysqlDataTransportFunctionListView functionList;

    /**
     * 过程列表
     */
    @FXML
    private ShellMysqlDataTransportProcedureListView procedureList;

    /**
     * 触发器列表
     */
    @FXML
    private ShellMysqlDataTransportTriggerListView triggerList;

    /**
     * 传输操作任务
     */
    private Thread execTask;

    /**
     * 计数器
     */
    private final Counter counter = new Counter();

    /**
     * 传输处理器
     */
    private DBDataTransportHandler transportHandler;

    /**
     * 执行传输
     */
    @FXML
    private void doTransport() {
        // 重置参数
        this.counter.reset();
        // 清理信息
        this.transportMsg.clear();
        this.transportStatus.clear();
        // 生成传输处理器
        if (this.transportHandler == null || this.transportHandler.getDialect() != this.sourceClient.dialect()) {
            this.transportHandler = DBDataTransportHandler.newHandler(this.sourceClient.dialect());
            this.transportHandler.setMessageHandler(str -> this.transportMsg.appendLine(str))
                    .setProcessedHandler(count -> {
                        if (count > 0) {
                            this.counter.incrSuccess(count);
                        } else {
                            this.counter.incrFail(Math.abs(count));
                        }
                        this.updateStatus(I18nHelper.transportInProgress());
                    });
        } else {
            this.transportHandler.interrupt(false);
        }
        // 来源客户端
        this.transportHandler.setSourceClient(this.sourceClient);
        // 目标客户端
        this.transportHandler.setTargetClient(this.targetClient);
        // 来源库
        this.transportHandler.setSourceDatabase(this.sourceDatabase.getSelectedItem());
        // 目标库
        this.transportHandler.setTargetDatabase(this.targetDatabase.getSelectedItem());
        // 视图
        this.transportHandler.setViews(this.viewList.getSelectedViews());
        // 事件
        this.transportHandler.setEvents(this.eventList.getSelectedEvents());
        // 表
        this.transportHandler.setTables(this.tableList.getSelectedTables());
        // 触发器
        this.transportHandler.setTriggers(this.triggerList.getSelectedTriggers());
        // 函数
        this.transportHandler.setFunctions(this.functionList.getSelectedFunctions());
        // 过程
        this.transportHandler.setProcedures(this.procedureList.getSelectedProcedures());
        // 开始处理
        NodeGroupUtil.disable(this.stage, "exec");
        this.stage.appendTitle("===" + I18nHelper.transportInProgress() + "===");
        // 执行传输
        this.execTask = ThreadUtil.start(() -> {
            try {
                this.stopTransportBtn.enable();
                // 更新状态
                this.updateStatus(I18nHelper.transportStarting());
                // 执行传输
                this.transportHandler.doTransport();
                // 更新状态
                this.updateStatus(I18nHelper.transportFinished());
            } catch (Exception e) {
                if (e.getClass().isAssignableFrom(InterruptedException.class)) {
                    this.updateStatus(I18nHelper.operationCancel());
                    MessageBox.okToast(I18nHelper.operationCancel());
                } else {
                    e.printStackTrace();
                    this.updateStatus(I18nHelper.operationFail());
                    MessageBox.warn(I18nHelper.operationFail());
                }
            } finally {
                // 结束处理
                NodeGroupUtil.enable(this.stage, "exec");
                this.stopTransportBtn.disable();
                this.stage.restoreTitle();
                SystemUtil.gcLater();
            }
        });
    }

    /**
     * 结束传输
     */
    @FXML
    private void stopTransport() {
        ThreadUtil.interrupt(this.execTask);
        this.execTask = null;
        if (this.transportHandler != null) {
            this.transportHandler.interrupt();
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.sourceInfo.selectedItemChanged(newValue -> {
            StageManager.showMask(() -> this.doConnect(1, newValue));
            // if (newValue != null) {
            //     try {
            //         this.sourceHost.setText(newValue.getHost());
            //         this.sourceType.setText(newValue.getType());
            //         this.sourceInfoName.setText(newValue.getName());
            //         if (this.sourceClient != null) {
            //             this.sourceClient.close();
            //         }
            //         this.sourceClient = ShellClientUtil.newClient(newValue);
            //         this.sourceClient.start();
            //         this.sourceDatabase.init(this.sourceClient);
            //         this.sourceVersion.setText(this.sourceClient.selectVersion());
            //     } catch (Throwable ex) {
            //         MessageBox.warn(I18nHelper.connectInitFail());
            //         ex.printStackTrace();
            //     }
            // } else {
            //     this.sourceHost.clear();
            //     this.sourceType.clear();
            //     this.sourceVersion.clear();
            //     this.sourceInfoName.clear();
            // }
            // this.clearList();
        });
        this.targetInfo.selectedItemChanged(newValue -> {
            StageManager.showMask(() -> this.doConnect(2, newValue));
            // if (newValue != null) {
            //     try {
            //         this.targetHost.setText(newValue.getHost());
            //         this.targetType.setText(newValue.getType());
            //         this.targetInfoName.setText(newValue.getName());
            //         if (this.targetClient != null) {
            //             this.targetClient.close();
            //         }
            //         this.targetClient = ShellClientUtil.newClient(newValue);
            //         this.targetClient.start();
            //         this.targetDatabase.init(this.targetClient);
            //         this.targetVersion.setText(this.targetClient.selectVersion());
            //     } catch (Throwable ex) {
            //         MessageBox.warn(I18nHelper.connectInitFail());
            //         ex.printStackTrace();
            //     }
            // } else {
            //     this.targetHost.clear();
            //     this.targetType.clear();
            //     this.targetVersion.clear();
            //     this.targetInfoName.clear();
            // }
            // this.clearList();
        });
        this.sourceDatabase.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.sourceDatabaseName.setText(newValue);
            } else {
                this.sourceDatabaseName.clear();
            }
            this.clearList();
        });
        this.targetDatabase.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.targetDatabaseName.setText(newValue);
            } else {
                this.targetDatabaseName.clear();
            }
            this.clearList();
        });

        this.viewList.setSelectedChanged(() -> this.flushPaneText("view"));
        this.tableList.setSelectedChanged(() -> this.flushPaneText("table"));
        this.eventList.setSelectedChanged(() -> this.flushPaneText("event"));
        this.triggerList.setSelectedChanged(() -> this.flushPaneText("trigger"));
        this.functionList.setSelectedChanged(() -> this.flushPaneText("function"));
        this.procedureList.setSelectedChanged(() -> this.flushPaneText("procedure"));

        // this.viewPane.expandedProperty().addListener((observable, oldValue, newValue) -> this.flushPaneLayout(this.viewPane, newValue));
        // this.eventPane.expandedProperty().addListener((observable, oldValue, newValue) -> this.flushPaneLayout(this.eventPane, newValue));
        // this.tablePane.expandedProperty().addListener((observable, oldValue, newValue) -> this.flushPaneLayout(this.tablePane, newValue));
        // this.triggerPane.expandedProperty().addListener((observable, oldValue, newValue) -> this.flushPaneLayout(this.triggerPane, newValue));
        // this.functionPane.expandedProperty().addListener((observable, oldValue, newValue) -> this.flushPaneLayout(this.functionPane, newValue));
        // this.procedurePane.expandedProperty().addListener((observable, oldValue, newValue) -> this.flushPaneLayout(this.procedurePane, newValue));
    }

    /**
     * 执行连接
     *
     * @param type    类型 1: 源 2: 目标
     * @param connect 连接
     */
    private void doConnect(int type, ShellConnect connect) {
        try {
            if (type == 1) {
                if (connect != null) {
                    this.sourceHost.text(connect.getHost());
                    this.sourceType.text(connect.getType());
                    this.sourceInfoName.text(connect.getName());
                    if (this.sourceClient != null) {
                        this.sourceClient.close();
                    }
                    this.sourceClient = ShellClientUtil.newClient(connect);
                    this.sourceClient.start();
                    this.sourceVersion.text(this.sourceClient.selectVersion());
                    this.sourceDatabase.enable();
                    this.sourceDatabase.init(this.sourceClient);
                } else {
                    this.sourceHost.clear();
                    this.sourceType.clear();
                    this.sourceVersion.clear();
                    this.sourceInfoName.clear();
                    this.sourceDatabase.disable();
                    this.sourceDatabase.clearItems();
                }
            } else if (type == 2) {
                if (connect != null) {
                    this.targetHost.text(connect.getHost());
                    this.targetType.text(connect.getType());
                    this.targetInfoName.text(connect.getName());
                    if (this.targetClient != null) {
                        this.targetClient.close();
                    }
                    this.targetClient = ShellClientUtil.newClient(connect);
                    this.targetClient.start();
                    this.targetVersion.text(this.targetClient.selectVersion());
                    this.targetDatabase.enable();
                    this.targetDatabase.init(this.targetClient);
                } else {
                    this.targetHost.clear();
                    this.targetType.clear();
                    this.targetVersion.clear();
                    this.targetInfoName.clear();
                    this.targetDatabase.disable();
                    this.targetDatabase.clearItems();
                }
            }
            this.clearList();
        } catch (Throwable ex) {
            MessageBox.warn(I18nHelper.connectInitFail());
            ex.printStackTrace();
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.hideOnEscape();
        ShellConnect connect = this.getProp("connect");
        if (connect != null) {
            this.sourceInfo.selectItem(connect);
        }
        // String dbName = this.getProp("dbName");
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        this.stopTransport();
    }

    /**
     * 更新状态
     *
     * @param extraMsg 额外信息
     */
    private void updateStatus(String extraMsg) {
        if (extraMsg != null) {
            this.counter.setExtraMsg(extraMsg);
        }
        FXUtil.runLater(() -> this.transportStatus.setText(this.counter.unknownFormat()));
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.transportTitle();
    }

    // @Override
    // public void onStageInitialize(StageAdapter stage) {
    //     super.onStageInitialize(stage);
    //     this.step1.managedBindVisible();
    //     this.step2.managedBindVisible();
    //     this.step3.managedBindVisible();
    // }

    @FXML
    private void showStep1() {
        this.step2.disappear();
        this.step1.display();
    }

    @FXML
    private void showStep2() {
        ShellConnect sourceInfo = this.sourceInfo.getSelectedItem();
        ShellConnect targetInfo = this.targetInfo.getSelectedItem();
        String sourceDatabase = this.sourceDatabase.getSelectedItem();
        String targetDatabase = this.targetDatabase.getSelectedItem();
        if (sourceInfo == null) {
            this.sourceInfo.requestFocus();
            MessageBox.warn(I18nHelper.pleaseSelectSourceConnect());
            return;
        }
        if (targetInfo == null) {
            this.targetInfo.requestFocus();
            MessageBox.warn(I18nHelper.pleaseSelectTargetConnect());
            return;
        }
        if (sourceDatabase == null) {
            this.sourceDatabase.requestFocus();
            MessageBox.warn(I18nHelper.pleaseSelectSourceDatabase());
            return;
        }
        if (targetDatabase == null) {
            this.targetDatabase.requestFocus();
            MessageBox.warn(I18nHelper.pleaseSelectTargetDatabase());
            return;
        }
        if (StringUtil.equalsIgnoreCase(sourceInfo.getName(), targetInfo.getName())
                && StringUtil.equalsIgnoreCase(sourceDatabase, targetDatabase)) {
            this.targetDatabase.requestFocus();
            MessageBox.warn(I18nHelper.pleaseCheckDatabase());
            return;
        }
        if (this.viewList.isItemEmpty()) {
            this.viewList.of(this.sourceClient.selectViews(this.sourceDatabase.getSelectedItem()));
        }
        if (this.eventList.isItemEmpty()) {
            this.eventList.of(this.sourceClient.events(this.sourceDatabase.getSelectedItem()));
        }
        if (this.tableList.isItemEmpty()) {
            this.tableList.of(this.sourceClient.selectTables(this.sourceDatabase.getSelectedItem()));
        }
        if (this.triggerList.isItemEmpty()) {
            this.triggerList.of(this.sourceClient.triggers(this.sourceDatabase.getSelectedItem()));
        }
        if (this.functionList.isItemEmpty()) {
            this.functionList.of(this.sourceClient.selectFunctions(this.sourceDatabase.getSelectedItem()));
        }
        if (this.procedureList.isItemEmpty()) {
            this.procedureList.of(this.sourceClient.selectProcedures(this.sourceDatabase.getSelectedItem()));
        }
        this.step1.disappear();
        this.step3.disappear();
        this.step2.display();
    }

    @FXML
    private void showStep3() {
        this.step2.disappear();
        this.step3.display();
    }

    /**
     * 清楚数据列表
     */
    private void clearList() {
        this.viewList.clearItems();
        this.eventList.clearItems();
        this.tableList.clearItems();
        this.functionList.clearItems();
        this.procedureList.clearItems();
    }

    /**
     * 刷新数据面板文字
     *
     * @param name 当前面板名称
     */
    private void flushPaneText(String name) {
        if (StringUtil.equalsIgnoreCase(name, "view")) {
            String viewTipText = "(" + this.viewList.getSelectedSize() + "/" + this.viewList.getItemSize() + ")";
            this.viewPane.setAppendText(viewTipText);
        } else if (StringUtil.equalsIgnoreCase(name, "event")) {
            String eventTipText = "(" + this.eventList.getSelectedSize() + "/" + this.eventList.getItemSize() + ")";
            this.eventPane.setAppendText(eventTipText);
        } else if (StringUtil.equalsIgnoreCase(name, "table")) {
            String tableTipText = "(" + this.tableList.getSelectedSize() + "/" + this.tableList.getItemSize() + ")";
            this.tablePane.setAppendText(tableTipText);
        } else if (StringUtil.equalsIgnoreCase(name, "trigger")) {
            String triggerTipText = "(" + this.triggerList.getSelectedSize() + "/" + this.triggerList.getItemSize() + ")";
            this.triggerPane.setAppendText(triggerTipText);
        } else if (StringUtil.equalsIgnoreCase(name, "function")) {
            String functionTipText = "(" + this.functionList.getSelectedSize() + "/" + this.functionList.getItemSize() + ")";
            this.functionPane.setAppendText(functionTipText);
        } else if (StringUtil.equalsIgnoreCase(name, "procedure")) {
            String procedureTipText = "(" + this.procedureList.getSelectedSize() + "/" + this.procedureList.getItemSize() + ")";
            this.procedurePane.setAppendText(procedureTipText);
        }
    }

    // /**
    //  * 刷新数据面板布局
    //  *
    //  * @param curr   当前面板
    //  * @param extend 是否展开
    //  */
    // private void flushPaneLayout(FXTitledPane curr, boolean extend) {
    //     if (extend) {
    //         curr.setFlexHeight("100% - 150");
    //         List<NodeGroup> groups = NodeGroupUtil.list(this.getStage(), "config");
    //         for (NodeGroup group : groups) {
    //             FXTitledPane pane = (FXTitledPane) group;
    //             if (pane != curr) {
    //                 pane.setExpanded(false);
    //                 pane.setFlexHeight("50");
    //             }
    //         }
    //     }
    //     curr.parentAutosize();
    // }
}
