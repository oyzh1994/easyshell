package cn.oyzh.easyshell.controller.zk.node;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dto.zk.ZKNodeExport;
import cn.oyzh.easyshell.exception.zk.ZKExceptionParser;
import cn.oyzh.easyshell.util.zk.ZKExportUtil;
import cn.oyzh.easyshell.util.zk.ZKI18nHelper;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.fx.gui.combobox.CharsetComboBox;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.util.Counter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * zk节点导入业务
 *
 * @author oyzh
 * @since 2020/10/14
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "node/zkNodeImport.fxml"
)
public class ZKNodeImportController extends StageController {

    /**
     * zk客户端
     */
    private ZKClient zkClient;

    /**
     * 脚本信息
     */
    @FXML
    private FXText scriptInfo;

    /**
     * 忽略已存在的节点
     */
    @FXML
    private FXCheckBox ignoreExist;

    /**
     * 导入字符集
     */
    @FXML
    private CharsetComboBox charset;

    /**
     * 导入按钮
     */
    @FXML
    private FXButton importBtn;

    // /**
    //  * 状态管理器
    //  */
    // @FXML
    // private StateManager stateManager;

    /**
     * 结束导入按钮
     */
    @FXML
    private FXButton stopImportBtn;

    /**
     * 导入状态
     */
    @FXML
    private FXLabel importStatus;

    /**
     * 导入消息
     */
    @FXML
    private MsgTextArea importMsg;

    /**
     * 导入操作任务
     */
    private Thread execTask;

    /**
     * 导入数据
     */
    private ZKNodeExport nodeExport;

    /**
     * 计数器
     */
    private final Counter counter = new Counter();

    /**
     * 拖拽文件
     *
     * @param event 事件
     */
    private void dragFile(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        List<File> files = dragboard.getFiles();
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        if (files.size() != 1) {
            MessageBox.warn(I18nHelper.onlySupportSingleFile());
            return;
        }
        File file = files.getFirst();
        // 解析文件
        this.parseFile(file);
    }

    /**
     * 选择脚本文件
     */
    @FXML
    private void chooseFile() {
        FileExtensionFilter filter1 = new FileExtensionFilter("JSON files|TXT files", "*.json", "*.txt");
        File file = FileChooserHelper.choose(I18nHelper.chooseFile(), filter1);
        // 解析文件
        this.parseFile(file);
    }

    /**
     * 解析文件
     *
     * @param file 文件
     */
    private void parseFile(File file) {
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            MessageBox.warn(I18nHelper.fileNotExists());
            return;
        }
        if (file.isDirectory()) {
            MessageBox.warn(I18nHelper.notSupportFolder());
            return;
        }
        if (!FileNameUtil.isType(file.getName(), "txt", "text", "json")) {
            MessageBox.warn(I18nHelper.invalidFormat());
            return;
        }
        if (file.length() == 0) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty());
            return;
        }
        try {
            // 解析数据
            this.nodeExport = ZKExportUtil.fromFile(file);
            // 初始化信息
            this.importMsg.clear();
            this.importBtn.enable();
            // 脚本信息
            String info = I18nHelper.fileName() + " " + file.getName() + "，" +
                    I18nHelper.total() + " " + this.nodeExport.counts() + I18nHelper.line() + "，" +
                    I18nHelper.size() + " " + Math.max(1, file.length() / 1024) + "Kb，" +
                    I18nHelper.version() + " " + this.nodeExport.version() + "，" +
                    I18nHelper.platform() + " " + this.nodeExport.platform() + "，" +
                    I18nHelper.charset() + " " + this.nodeExport.charset();
            if (this.nodeExport.hasPrefix()) {
                info += "，" + I18nHelper.prefix() + this.nodeExport.getPrefix();
            }
            this.scriptInfo.setText(info);
            this.charset.select(this.nodeExport.getCharset());
        } catch (Exception ex) {
            ex.printStackTrace();
            this.nodeExport = null;
            this.importBtn.disable();
            MessageBox.exception(ex, I18nHelper.parseFail());
        }
    }

    /**
     * 执行导入
     */
    @FXML
    private void doImport() {
        // 重置参数
        this.importMsg.clear();
        this.counter.reset();
        this.counter.setSum(this.nodeExport.counts());
        // 开始处理
        this.importBtn.disable();
        // this.stateManager.disable();
        NodeGroupUtil.disable(this.stage, "exec");
        this.stage.appendTitle("===" + I18nHelper.importProcessing() + "===");
        // 忽略已存在节点
        boolean ignoreExist = this.ignoreExist.isSelected();
        // 执行导入
        this.execTask = ThreadUtil.start(() -> {
            try {
                this.stopImportBtn.enable();
                for (Map<String, String> zkNode : this.nodeExport.getNodes()) {
                    // 取消操作
                    if (ThreadUtil.isInterrupted(this.execTask)) {
                        JulLog.warn("import canceled!");
                        break;
                    }
                    // 获取数据
                    String path = zkNode.get("path");
                    String data = zkNode.get("data");
                    byte[] bytes = this.nodeExport.getDateBytes(data, this.charset.getCharset());
                    // 状态
                    int status = 1;
                    // 异常
                    Exception exception = null;
                    try {
                        // 设置数据
                        if (this.zkClient.exists(path)) {
                            if (ignoreExist) {
                                status = 2;
                            } else {
                                this.zkClient.setData(path, bytes);
                            }
                        } else {// 创建节点
                            this.zkClient.createIncludeParents(path, bytes);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        status = 0;
                        exception = ex;
                    }
                    this.updateStatus(path, status, exception);
                }
                // 收尾工作
                this.updateStatus(I18nHelper.processing());
                this.updateStatus(I18nHelper.operationSuccess());
                MessageBox.okToast(I18nHelper.operationSuccess());

            } catch (Exception ex) {
                if (ex.getClass().isAssignableFrom(InterruptedException.class)) {
                    this.updateStatus(I18nHelper.operationCancel());
                    MessageBox.okToast(I18nHelper.operationCancel());
                } else {
                    ex.printStackTrace();
                    this.updateStatus(I18nHelper.operationFail());
                    MessageBox.warn(I18nHelper.operationFail());
                }
            } finally {
                // 结束处理
                this.importBtn.enable();
                // this.stateManager.enable();
                NodeGroupUtil.enable(this.stage, "exec");
                this.stopImportBtn.disable();
                this.stage.restoreTitle();
            }
        });
    }

    /**
     * 结束导入
     */
    @FXML
    private void stopImport() {
        ThreadUtil.interrupt(this.execTask);
        this.execTask = null;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 文件拖拽相关
        this.stage.scene().setOnDragOver(event1 -> {
            this.stage.disable();
            this.stage.appendTitle("===" + I18nHelper.dragTip1() + "===");
            event1.acceptTransferModes(TransferMode.ANY);
            event1.consume();
        });
        this.stage.scene().setOnDragExited(event1 -> {
            this.stage.enable();
            this.stage.restoreTitle();
            event1.consume();
        });
        this.stage.scene().setOnDragDropped(event1 -> {
            this.dragFile(event1);
            event1.setDropCompleted(true);
            event1.consume();
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.zkClient = this.getProp("zkClient");
        this.scriptInfo.managedProperty().bind(this.scriptInfo.visibleProperty());
        this.scriptInfo.addTextChangeListener((observableValue, s, t1) -> this.scriptInfo.setVisible(StringUtil.isNotBlank(t1)));
        super.onWindowShown(event);
        this.stage.hideOnEscape();
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        this.stopImport();
    }

    /**
     * 更新状态
     *
     * @param path   路径
     * @param status 状态 0:失败 1:成功 2:忽略
     * @param ex     异常
     */
    private void updateStatus(String path, int status, Exception ex) {
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        this.counter.update(status);
        String msg;
        if (status == 1) {
            msg = I18nHelper.importNode() + " " + path + " " + I18nHelper.success();
        } else if (status == 2) {
            msg = I18nHelper.importNode() + " " + path + " " + ZKI18nHelper.nodeTip3();
        } else {
            msg = I18nHelper.importNode() + " " + path + " " + I18nHelper.fail();
            if (ex != null) {
                msg += "，" + I18nHelper.errorInfo() + ZKExceptionParser.INSTANCE.apply(ex);
            }
        }
        this.importMsg.appendLine(msg);
        this.updateStatus(null);
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
        FXUtil.runLater(() -> this.importStatus.setText(this.counter.knownFormat()));
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("base.title.import");
    }
}
