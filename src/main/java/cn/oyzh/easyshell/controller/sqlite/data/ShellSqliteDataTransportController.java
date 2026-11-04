package cn.oyzh.easyshell.controller.sqlite.data;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.sqlite.handler.SQLiteDataTransportHandler;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.connect.ShellConnectTextField;
import cn.oyzh.easyshell.fx.sqlite.ShellSqliteDatabaseComboBox;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.fx.db.data.ui.DBDataDumpTypeComboBox;
import cn.oyzh.fx.db.data.ui.DBDataTransportNameListView;
import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.util.Counter;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.stage.WindowEvent;

import java.nio.file.Path;


/**
 * sqlite数据传输业务
 *
 * @author oyzh
 * @since 2026-11-02
 */
@StageAttribute(
        multipliable = true,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "sqlite/data/shellSqliteDataTransport.fxml"
)
public class ShellSqliteDataTransportController extends StageController {

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
     * 第四步
     */
    @FXML
    private FXVBox step4;

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
     * 来源数据库
     */
    @FXML
    private ShellSqliteDatabaseComboBox sourceDatabase;

    /**
     * 目标数据库
     */
    @FXML
    private ShellSqliteDatabaseComboBox targetDatabase;

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
    private ShellSqliteClient sourceClient;

    /**
     * 目标客户端
     */
    private ShellSqliteClient targetClient;

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
     * 对象选择容器
     */
    @FXML
    private FXTabPane tableRoot;

    /**
     * 表组件
     */
    @FXML
    private FXTab tablePane;

    /**
     * 视图组件
     */
    @FXML
    private FXTab viewPane;

    /**
     * 表列表
     */
    @FXML
    private DBDataTransportNameListView tableList;

    /**
     * 视图列表
     */
    @FXML
    private DBDataTransportNameListView viewList;

    /**
     * 遇到错误时继续
     */
    @FXML
    private FXCheckBox continueWithErrors;

    /**
     * 数据类型
     */
    @FXML
    private DBDataDumpTypeComboBox dataType;

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
    private SQLiteDataTransportHandler transportHandler;

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
        if (this.transportHandler == null) {
            this.transportHandler = new SQLiteDataTransportHandler();
            this.transportHandler.setMessageHandler(str -> this.transportMsg.appendLine(str))
                    .setProcessedHandler(count -> {
                        this.counter.incr(count);
                        this.updateStatus(I18nHelper.transportInProgress());
                    });
        } else {
            this.transportHandler.interrupt(false);
        }
        // 数据类型
        this.transportHandler.setDataType(this.dataType.getSelectedIndex());
        // 错误时继续
        this.transportHandler.setContinueWithErrors(this.continueWithErrors.isSelected());
        // 来源客户端
        this.transportHandler.setSourceClient(this.sourceClient);
        // 目标客户端
        this.transportHandler.setTargetClient(this.targetClient);
        // 来源库
        this.transportHandler.setSourceDatabase("main");
        // 目标库
        this.transportHandler.setTargetDatabase("main");
        // 视图
        this.transportHandler.setViews(this.viewList.getSelectedObjects());
        // 表
        this.transportHandler.setTables(this.tableList.getSelectedObjects());
        // 开始处理
        NodeGroupUtil.disable(this.stage, "exec");
        this.stage.appendTitle(ShellI18nHelper.phaseTransportInProgress());
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
                    MessageBox.okToast(I18nHelper.operationCancel(), this.getWindow());
                } else {
                    e.printStackTrace();
                    this.updateStatus(I18nHelper.operationFail());
                    MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
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
            StageManager.showMask(this.getWindow(), () -> this.doConnect(1, newValue));
        });
        this.targetInfo.selectedItemChanged(newValue -> {
            StageManager.showMask(this.getWindow(), () -> this.doConnect(2, newValue));
        });
        this.sourceDatabase.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue == null) {
                this.sourceDatabaseName.clear();
            } else {
                this.sourceDatabaseName.text(newValue);
            }
            this.clearList();
        });
        this.targetDatabase.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue == null) {
                this.targetDatabaseName.clear();
            } else {
                this.targetDatabaseName.text(newValue);
            }
            this.clearList();
        });
        this.tableRoot.selectedItemChanged((observable, oldValue, newValue) -> this.flushPaneTexts());
        this.viewList.setSelectedChanged(() -> this.flushPaneText("view"));
        this.tableList.setSelectedChanged(() -> this.flushPaneText("table"));
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
                    this.sourceDatabase.init(this.sourceClient, "main");
                } else {
                    this.sourceHost.clear();
                    this.sourceType.clear();
                    this.sourceVersion.clear();
                    this.sourceInfoName.clear();
                    this.sourceDatabase.disable();
                    this.sourceDatabase.clearItems();
                }
            } else {
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
                    this.targetDatabase.init(this.targetClient, "main");
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
            MessageBox.warn(I18nHelper.connectInitFail(), this.getWindow());
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
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        this.stopTransport();
        if (this.sourceClient != null) {
            this.sourceClient.close();
        }
        if (this.targetClient != null) {
            this.targetClient.close();
        }
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
        this.transportStatus.text(this.counter.unknownFormat());
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.transportTitle();
    }

    /**
     * 显示第一步
     */
    @FXML
    private void showStep1() {
        this.step2.disappear();
        this.step1.display();
    }

    /**
     * 显示第二步
     */
    @FXML
    private void showStep2() {
        ShellConnect sourceConnect = this.sourceInfo.getSelectedItem();
        ShellConnect targetConnect = this.targetInfo.getSelectedItem();
        if (sourceConnect == null) {
            this.sourceInfo.requestFocus();
            MessageBox.warn(I18nHelper.pleaseSelectSourceConnect(), this.getWindow());
            return;
        }
        if (targetConnect == null) {
            this.targetInfo.requestFocus();
            MessageBox.warn(I18nHelper.pleaseSelectTargetConnect(), this.getWindow());
            return;
        }
        Path sourcePath = Path.of(sourceConnect.getHost())
                .toAbsolutePath().normalize();
        Path targetPath = Path.of(targetConnect.getHost())
                .toAbsolutePath().normalize();
        if (sourcePath.equals(targetPath)) {
            this.targetInfo.requestFocus();
            MessageBox.warn(I18nHelper.pleaseCheckDatabase(), this.getWindow());
            return;
        }
        this.step1.disappear();
        this.step3.disappear();
        this.step4.disappear();
        this.step2.display();
    }

    /**
     * 显示第三步
     */
    @FXML
    private void showStep3() {
        if (this.viewList.isItemEmpty()) {
            this.viewList.of(this.sourceClient.selectViews());
        }
        if (this.tableList.isItemEmpty()) {
            this.tableList.of(this.sourceClient.selectTables());
        }
        this.step2.disappear();
        this.step4.disappear();
        this.flushPaneTexts();
        this.step3.display();
    }

    /**
     * 显示第四步
     */
    @FXML
    private void showStep4() {
        this.step3.disappear();
        this.step4.display();
    }

    /**
     * 清理数据列表
     */
    private void clearList() {
        this.viewList.clearItems();
        this.tableList.clearItems();
    }

    /**
     * 刷新数据面板文字
     */
    private void flushPaneTexts() {
        this.flushPaneText("view");
        this.flushPaneText("table");
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
        } else if (StringUtil.equalsIgnoreCase(name, "table")) {
            String tableTipText = "(" + this.tableList.getSelectedSize() + "/" + this.tableList.getItemSize() + ")";
            this.tablePane.setAppendText(tableTipText);
        }
    }

    /**
     * 全选当前对象列表
     */
    @FXML
    private void selectAll() {
        this.selectObjects(true);
    }

    /**
     * 取消全选当前对象列表
     */
    @FXML
    private void unselectAll() {
        this.selectObjects(false);
    }

    /**
     * 设置当前对象列表的选中状态
     *
     * @param selected 选中状态
     */
    private void selectObjects(boolean selected) {
        Tab tab = this.tableRoot.getSelectedItem();
        if (tab == null || !(tab.getContent() instanceof DBDataTransportObjectListView listView)) {
            return;
        }
        for (FXCheckBox item : listView.getItems()) {
            item.setSelected(selected);
        }
        this.flushPaneTexts();
    }

}
