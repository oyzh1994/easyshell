package cn.oyzh.easyshell.controller.redis.data;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.redis.ShellRedisDatabaseComboBox;
import cn.oyzh.easyshell.handler.redis.ShellRedisDataExportHandler;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisClientUtil;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.util.Counter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * redis数据导出业务
 *
 * @author oyzh
 * @since 2024/11/26
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "redis/data/shellRedisExportData.fxml"
)
public class ShellRedisExportDataController extends StageController {

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
     * 导出文件
     */
    private File exportFile;

    /**
     * 文件格式
     */
    @FXML
    private FXToggleGroup format;

    /**
     * 文件名
     */
    @FXML
    private FXText fileName;

    /**
     * 选择文件
     */
    @FXML
    private FXButton selectFile;

    /**
     * 保留ttl
     */
    @FXML
    private FXCheckBox retainTTL;

    /**
     * 包含标题
     */
    @FXML
    private FXCheckBox includeTitle;

    /**
     * 压缩
     */
    @FXML
    private FXCheckBox compress;

    // /**
    //  * 适用过滤配置
    //  */
    // @FXML
    // private FXCheckBox applyFilter;

    /**
     * 受影响的键
     */
    @FXML
    private ReadOnlyTextArea keys;

    /**
     * 数据库
     */
    @FXML
    private ShellRedisDatabaseComboBox db;

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
     * json类型
     */
    @FXML
    private FXCheckBox jsonType;

    /**
     * 结束导出按钮
     */
    @FXML
    private FXButton stopExportBtn;

    /**
     * 导出状态
     */
    @FXML
    private FXLabel exportStatus;

    /**
     * 导出消息
     */
    @FXML
    private MsgTextArea exportMsg;

    /*
     * 导出库
     */
    private Integer dbIndex;

    /**
     * 当前redis对象
     */
    private ShellConnect connect;

    /**
     * 当前redis客户端
     */
    private ShellRedisClient client;

    /**
     * 导出操作任务
     */
    private Thread execTask;

    /**
     * 计数器
     */
    private final Counter counter = new Counter();

    // /**
    //  * 过滤配置储存
    //  */
    // private final RedisFilterStore filterStore = RedisFilterStore.INSTANCE;

    /**
     * 导出处理器
     */
    private ShellRedisDataExportHandler exportHandler;

    /**
     * 执行导出
     */
    @FXML
    private void doExport() {
        // 重置参数
        // 清理信息
        this.counter.reset();
        this.exportMsg.clear();
        this.exportStatus.clear();
        NodeGroupUtil.disable(this.stage, "exec");
        this.stage.appendTitle("===" + I18nHelper.exportProcessing() + "===");
        // 生成导出处理器
        if (this.exportHandler == null) {
            this.exportHandler = new ShellRedisDataExportHandler();
            this.exportHandler.setMessageHandler(str -> this.exportMsg.appendLine(str));
            this.exportHandler.setProcessedHandler(count -> {
                        if (count == 0) {
                            this.counter.updateIgnore();
                        } else if (count < 0) {
                            this.counter.incrFail(count);
                        } else {
                            this.counter.incrSuccess(count);
                        }
                        this.updateStatus(I18nHelper.exportInProgress());
                    });
        } else {
            this.exportHandler.interrupt(false);
        }
        String fileType = this.format.selectedUserData();
        // 文件类型
        this.exportHandler.setFileType(fileType);
        // 客户端
        this.exportHandler.setClient(this.client);
        // 数据库
        if (this.dbIndex != null) {
            this.exportHandler.setDatabase(this.dbIndex);
        } else {
            int index = this.db.getSelectedIndex();
            this.exportHandler.setDatabase(index == 0 ? null : index - 1);
        }
        // 导出文件
        this.exportHandler.filePath(this.exportFile.getPath());
        // // 适用过滤
        // if (this.applyFilter.isSelected()) {
        //     this.exportHandler.setFilters(this.filterStore.loadEnable(this.client.iid()));
        // } else {
        //     this.exportHandler.setFilters(null);
        // }
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
        if (this.jsonType.isSelected()) {
            keyTypes.add("json");
        }
        if (this.streamType.isSelected()) {
            keyTypes.add("stream");
        }
        if (this.stringType.isSelected()) {
            keyTypes.add("string");
        }
        this.exportHandler.setKeyTypes(keyTypes);
        // 查询模式
        this.exportHandler.setPattern(this.pattern.getText());
        // 保留ttl
        this.exportHandler.setRetainTTL(this.retainTTL.isSelected());
        // 压缩
        this.exportHandler.compress(this.compress.isEnable() && this.compress.isSelected());
        // 包含标题
        this.exportHandler.includeTitle(this.includeTitle.isEnable() && this.includeTitle.isSelected());
        // 执行导出
        this.execTask = ThreadUtil.start(() -> {
            try {
                this.stopExportBtn.enable();
                // 更新状态
                this.updateStatus(I18nHelper.exportStarting());
                // 执行导出
                this.exportHandler.doExport();
                // 更新状态
                this.updateStatus(I18nHelper.exportFinished());
            } catch (Exception e) {
                if (e.getClass().isAssignableFrom(InterruptedException.class)) {
                    this.updateStatus(I18nHelper.operationCancel());
                    MessageBox.warn(I18nHelper.operationCancel());
                } else {
                    e.printStackTrace();
                    this.updateStatus(I18nHelper.operationFail());
                    MessageBox.warn(I18nHelper.operationFail());
                }
            } finally {
                NodeGroupUtil.enable(this.stage, "exec");
                this.stopExportBtn.disable();
                this.stage.restoreTitle();
            }
        });
    }

    /**
     * 结束导出
     */
    @FXML
    private void stopExport() {
        ThreadUtil.interrupt(this.execTask);
        this.execTask = null;
        if (this.exportHandler != null) {
            this.exportHandler.interrupt();
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.stage.hideOnEscape();
        // 格式选择监听
        this.format.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            this.exportFile = null;
            this.fileName.clear();
        });
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        this.stopExport();
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
        FXUtil.runLater(() -> this.exportStatus.setText(this.counter.unknownFormat()));
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.exportTitle();
    }

    @FXML
    private void showStep1() {
        this.step2.disappear();
        this.step3.disappear();
        this.step4.disappear();
        this.step1.display();
    }

    @FXML
    private void showStep2() {
        try {
            this.getStage().appendTitle("===" + I18nHelper.connectIng() + "===");
            this.getStage().disable();
            // 检查客户端
            if (this.client == null || this.client.isClosed()) {
                DownLatch latch = DownLatch.of();
                ThreadUtil.start(() -> {
                    try {
                        this.client = ShellRedisClientUtil.newClient(this.connect);
                        this.client.start(2500);
                    } finally {
                        latch.countDown();
                    }
                });
                if (!latch.await(3000) || !this.client.isConnected()) {
                    this.client.close();
                    this.client = null;
                    MessageBox.warn(I18nHelper.connectInitFail());
                    return;
                }
            }
            // 初始化数据库
            if (this.db.isItemEmpty()) {
                if (this.dbIndex != null) {
                    this.db.addItem("db" + this.dbIndex);
                    this.db.selectFirst();
                } else {
                    this.db.addItem(I18nHelper.allDatabase());
                    this.db.setDbCount(this.client.databases());
                    this.db.selectFirst();
                }
            }
        } finally {
            this.restoreTitle();
            this.enable();
        }
        this.step1.disappear();
        this.step3.disappear();
        this.step4.disappear();
        this.step2.display();
    }

    @FXML
    private void showStep3() {
        // 检查是否支持标题
        String fileType = this.format.selectedUserData();
        if (StringUtil.equalsAny(fileType, "xls", "xlsx", "csv")) {
            this.includeTitle.enable();
        } else {
            this.includeTitle.disable();
        }
        if (StringUtil.equalsAny(fileType, "xml", "json")) {
            this.compress.enable();
        } else {
            this.compress.disable();
        }
        this.step1.disappear();
        this.step2.disappear();
        this.step4.disappear();
        this.step3.display();
    }

    @FXML
    private void showStep4() {
        if (this.exportFile == null) {
//            this.selectFile.requestFocus();
//            MessageBox.warn(I18nHelper.pleaseSelectFile());
            ValidatorUtil.validFail(this.selectFile);
            return;
        }
        this.step1.disappear();
        this.step2.disappear();
        this.step3.disappear();
        this.step4.display();
    }

    /**
     * 选择文件
     */
    @FXML
    private void selectFile() {
        String fileType = this.format.selectedUserData();
        FileExtensionFilter filter = FXChooser.extensionFilter(fileType);
        String fileName = "Redis-" + this.connect.getName();
        if (this.dbIndex != null) {
            fileName += "-db" + this.dbIndex;
        } else if (this.db.getSelectedIndex() != 0) {
            fileName += "-db" + (this.db.getSelectedIndex() - 1);
        }
        fileName += "-" + I18nHelper.exportData() + "." + fileType;
        this.exportFile = FileChooserHelper.save(fileName, fileName, filter);
        if (this.exportFile != null) {
            // 删除文件
            if (this.exportFile.exists()) {
                FileUtil.del(this.exportFile);
            }
            this.fileName.setText(this.exportFile.getPath());
        } else {
            this.fileName.clear();
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.connect = this.getProp("connect");
        this.dbIndex = this.getProp("dbIndex");
    }

    /**
     * 显示受影响的键
     */
    @FXML
    private void showKeys() {
        this.keys.clear();
        if (this.db.getDB() == -1) {
            Map<Integer, Set<String>> fullKeys = this.client.fullKeys(this.pattern.getText());
            if (CollectionUtil.isNotEmpty(fullKeys)) {
                List<String> texts = new ArrayList<>(fullKeys.size());
                int index = 0;
                for (Map.Entry<Integer, Set<String>> entry : fullKeys.entrySet()) {
                    int db = entry.getKey();
                    for (String key : entry.getValue()) {
                        texts.add("(db" + db + ")" + ++index + ". " + key);
                    }
                    texts.add("\n");
                }
                this.keys.appendLines(texts);
            }
        } else {
            Set<String> allKeys = this.client.allKeys(this.db.getDB(), this.pattern.getText());
            if (CollectionUtil.isNotEmpty(allKeys)) {
                List<String> texts = new ArrayList<>(allKeys.size());
                int index = 0;
                for (String key : allKeys) {
                    texts.add(++index + ". " + key);
                }
                this.keys.appendLines(texts);
            }
        }
    }
}
