package cn.oyzh.easyshell.controller.redis;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.connect.ShellConnectTextField;
import cn.oyzh.easyshell.fx.redis.RedisDatabaseComboBox;
import cn.oyzh.easyshell.handler.redis.RedisDataTransportHandler;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.redis.RedisClientUtil;
import cn.oyzh.easyshell.store.redis.RedisFilterStore;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.util.Counter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * db数据传输业务
 *
 * @author oyzh
 * @since 2024/09/05
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "data/redisTransportData.fxml"
)
public class RedisTransportDataController extends StageController {

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
     * 目标信息名称
     */
    @FXML
    private FXLabel targetInfoName;

    /**
     * 来源信息
     */
    @FXML
    private ShellConnectTextField sourceInfo;

    /**
     * 来源数据库
     */
    @FXML
    private RedisDatabaseComboBox sourceDatabase;

    /**
     * 来源数据库名称
     */
    @FXML
    private FXLabel sourceDatabaseName;

    /**
     * 目标信息
     */
    @FXML
    private ShellConnectTextField targetInfo;

    /**
     * 目标数据库
     */
    @FXML
    private RedisDatabaseComboBox targetDatabase;

    /**
     * 目标数据库名称
     */
    @FXML
    private FXLabel targetDatabaseName;

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
     * 来源客户端
     */
    private RedisClient sourceClient;

    /**
     * 目标客户端
     */
    private RedisClient targetClient;

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
     * 节点存在时处理策略
     */
    @FXML
    private FXToggleGroup existsPolicy;

    /**
     * 保留ttl
     */
    @FXML
    private FXCheckBox retainTTL;

    /**
     * 适用过滤配置
     */
    @FXML
    private FXCheckBox applyFilter;

    /**
     * 受影响的键
     */
    @FXML
    private ReadOnlyTextArea keys;

    /**
     * 键模式
     */
    @FXML
    private ClearableTextField pattern;

    /**
     * string类型
     */
    @FXML
    private FXCheckBox stringType;

    /**
     * list类型
     */
    @FXML
    private FXCheckBox listType;

    /**
     * stream类型
     */
    @FXML
    private FXCheckBox streamType;

    /**
     * set类型
     */
    @FXML
    private FXCheckBox setType;

    /**
     * zset类型
     */
    @FXML
    private FXCheckBox zsetType;

    /**
     * hash类型
     */
    @FXML
    private FXCheckBox hashType;

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
    private RedisDataTransportHandler transportHandler;

    /**
     * 过滤配置储存
     */
    private final RedisFilterStore filterStore = RedisFilterStore.INSTANCE;

    /**
     * 预选的db索引
     */
    private Integer presetDbIndex;

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
            this.transportHandler = new RedisDataTransportHandler();
            this.transportHandler.setMessageHandler(str -> this.transportMsg.appendLine(str));
            this.transportHandler.setProcessedHandler(count -> {
                if (count == 0) {
                    this.counter.updateIgnore();
                } else if (count < 0) {
                    this.counter.incrFail(count);
                } else {
                    this.counter.incrSuccess(count);
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
        // 来源字符集
        this.transportHandler.setSourceDatabase(this.sourceDatabase.getDB());
        // 目标字符集
        this.transportHandler.setTargetDatabase(this.targetDatabase.getDB());
        // 节点存在时处理策略
        this.transportHandler.setExistsPolicy(this.existsPolicy.selectedUserData());
        // 适用过滤
        if (this.applyFilter.isSelected()) {
            this.transportHandler.setFilters(this.filterStore.loadEnable(this.sourceClient.iid()));
        } else {
            this.transportHandler.setFilters(null);
        }
        // 键类型
        List<String> keyTypes = new ArrayList<>();
        if (this.setType.isSelected()) {
            keyTypes.add("set");
        }
        if (this.zsetType.isSelected()) {
            keyTypes.add("zset");
        }
        if (this.listType.isSelected()) {
            keyTypes.add("zset");
        }
        if (this.hashType.isSelected()) {
            keyTypes.add("hash");
        }
        if (this.streamType.isSelected()) {
            keyTypes.add("stream");
        }
        if (this.stringType.isSelected()) {
            keyTypes.add("string");
        }
        this.transportHandler.setKeyTypes(keyTypes);
        // 查询模式
        this.transportHandler.setPattern(this.pattern.getText());
        // 保留ttl
        this.transportHandler.setRetainTTL(this.retainTTL.isSelected());
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
            } catch (Exception ex) {
                if (ex.getClass().isAssignableFrom(InterruptedException.class)) {
                    this.updateStatus(I18nHelper.operationCancel());
                } else {
                    ex.printStackTrace();
                    this.updateStatus(I18nHelper.operationFail());
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
            if (newValue != null) {
                this.sourceHost.setText(newValue.getHost());
                this.sourceInfoName.setText(newValue.getName());
                this.initSourceDatabase(newValue);
            } else {
                this.sourceHost.clear();
                this.sourceInfoName.clear();
                this.sourceDatabase.clearItems();
            }
        });
        this.targetInfo.selectedItemChanged(newValue -> {
            if (newValue != null) {
                this.targetHost.setText(newValue.getHost());
                this.targetInfoName.setText(newValue.getName());
                this.initTargetDatabase(newValue);
            } else {
                this.targetHost.clear();
                this.targetInfoName.clear();
                this.targetDatabase.clearItems();
            }
        });
        this.sourceDatabase.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.sourceDatabaseName.setText(newValue);
            } else {
                this.sourceDatabaseName.clear();
            }
        });
        this.targetDatabase.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.targetDatabaseName.setText(newValue);
            } else {
                this.targetDatabaseName.clear();
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        // 来源连接不为null，则禁用来源选项
        ShellConnect sourceConnect = this.stage.getProp("sourceConnect");
        if (sourceConnect != null) {
            this.sourceInfo.selectItem(sourceConnect);
            this.sourceInfo.disable();
        }
        // 预选的db
        Integer dbIndex = this.stage.getProp("dbIndex");
        if (dbIndex != null) {
            this.presetDbIndex = dbIndex;
            this.sourceDatabase.disable();
        }
        this.stage.hideOnEscape();
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

    /**
     * 初始化来源连接
     *
     * @param sourceInfo 来源连接
     */
    private void initSourceDatabase(ShellConnect sourceInfo) {
        if (this.sourceClient != null) {
            this.sourceClient.close();
            this.sourceClient = null;
            this.sourceDatabase.clearItems();
        }
        this.appendTitle("===" + I18nHelper.connectIng() + "===");
        this.disable();
        try {
            DownLatch latch = DownLatch.of();
            ThreadUtil.start(() -> {
                try {
                    this.sourceClient = RedisClientUtil.newClient(sourceInfo);
                    this.sourceClient.start(2500);
                } finally {
                    latch.countDown();
                }
            });
            if (!latch.await(3000) || !this.sourceClient.isConnected()) {
                this.sourceClient.close();
                this.sourceClient = null;
                this.sourceInfo.requestFocus();
                MessageBox.warn(sourceInfo.getName() + " " + I18nHelper.connectInitFail());
                return;
            }

            // 初始化数据库
            this.sourceDatabase.setDbCount(this.sourceClient.databases());
            if (this.presetDbIndex == null) {
                this.sourceDatabase.selectFirst();
            } else {
                this.sourceDatabase.select(this.presetDbIndex);
            }
        } finally {
            this.restoreTitle();
            this.enable();
        }
    }

    /**
     * 初始化目标连接
     *
     * @param targetInfo 目标连接
     */
    private void initTargetDatabase(ShellConnect targetInfo) {
        if (this.targetClient != null) {
            this.targetClient.close();
            this.targetClient = null;
            this.targetDatabase.clearItems();
        }
        this.appendTitle("===" + I18nHelper.connectIng() + "===");
        this.disable();
        try {
            DownLatch latch = DownLatch.of();
            ThreadUtil.start(() -> {
                try {
                    this.targetClient = RedisClientUtil.newClient(targetInfo);
                    this.targetClient.start(2500);
                } finally {
                    latch.countDown();
                }
            });
            if (!latch.await(3000) || !this.targetClient.isConnected()) {
                this.targetClient.close();
                this.targetClient = null;
                this.targetInfo.requestFocus();
                MessageBox.warn(targetInfo.getName() + " " + I18nHelper.connectInitFail());
                return;
            }

            // 初始化数据库
            this.targetDatabase.setDbCount(this.targetClient.databases());
            this.targetDatabase.selectFirst();
        } finally {
            this.restoreTitle();
            this.enable();
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.transportTitle();
    }

    /**
     * 显示受影响的键
     */
    @FXML
    private void showKeys() {
        this.keys.clear();
        Set<String> allKeys = this.sourceClient.allKeys(this.sourceDatabase.getDB(), this.pattern.getText());
        if (CollectionUtil.isNotEmpty(allKeys)) {
            List<String> texts = new ArrayList<>(allKeys.size());
            int index = 0;
            for (String key : allKeys) {
                texts.add(++index + ". " + key);
            }
            this.keys.appendLines(texts);
        }
    }

    @FXML
    private void showStep1() {
        this.step2.disappear();
        this.step1.display();
    }

    @FXML
    private void showStep2() {
        ShellConnect sourceInfo = this.sourceInfo.getSelectedItem();
        ShellConnect targetInfo = this.targetInfo.getSelectedItem();
        if (sourceInfo == null) {
//            this.sourceInfo.requestFocus();
//            MessageBox.warn(I18nHelper.pleaseSelectSourceConnect());
            ValidatorUtil.validFail(this.sourceInfo);
            return;
        }
        if (targetInfo == null) {
//            this.targetInfo.requestFocus();
//            MessageBox.warn(I18nHelper.pleaseSelectTargetConnect());
            ValidatorUtil.validFail(this.targetInfo);
            return;
        }

        // 检查连接和库，不能是同一个连接的同一个库
        int sourceDb = this.sourceDatabase.getDB();
        int targetDb = this.targetDatabase.getDB();
        if (sourceInfo.compare(targetInfo) && sourceDb == targetDb) {
            this.sourceInfo.requestFocus();
            MessageBox.warn(I18nHelper.databasesCannotBeTheSame());
            return;
        }

        if (this.sourceClient == null || this.sourceClient.isClosed()) {
            MessageBox.warn(sourceInfo.getName() + " " + I18nHelper.connectFail());
            return;
        }

        if (this.targetClient == null || this.targetClient.isClosed()) {
            MessageBox.warn(targetInfo.getName() + " " + I18nHelper.connectFail());
            return;
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

    @FXML
    private void showStep4() {
        this.step3.disappear();
        this.step4.display();
    }
}
