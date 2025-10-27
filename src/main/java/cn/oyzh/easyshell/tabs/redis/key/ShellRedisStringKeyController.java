package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.popups.redis.ShellRedisKeyQRCodePopupController;
import cn.oyzh.easyshell.trees.redis.ShellRedisStringKeyTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.EditorFormatTypeComboBox;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.util.Objects;

/**
 * string键tab内容组件
 *
 * @author oyzh
 * @since 2023/06/31
 */
public class ShellRedisStringKeyController extends ShellRedisKeyController<ShellRedisStringKeyTreeItem> {

    /**
     * 数据撤销
     */
    @FXML
    private SVGGlyph dataUndo;

    /**
     * 数据重做
     */
    @FXML
    private SVGGlyph dataRedo;

    /**
     * 二进制数据
     */
    @FXML
    private FXText binary;

    /**
     * 数据保存按钮
     */
    @FXML
    private SVGGlyph saveNodeData;

    /**
     * 格式
     */
    @FXML
    private EditorFormatTypeComboBox format;

    /**
     * 数据组件
     */
    @FXML
    private ShellDataEditor nodeData;

    // /**
    //  * 格式监听器
    //  */
    // private final ChangeListener<RichDataType> formatListener = (t1, t2, t3) -> {
    //     if (this.format.isStringFormat()) {
    //         this.showData(RichDataType.STRING);
    //         this.nodeData.setEditable(true);
    //     } else if (this.format.isJsonFormat()) {
    //         this.showData(RichDataType.JSON);
    //         this.nodeData.setEditable(true);
    //     } else if (this.format.isXmlFormat()) {
    //         this.showData(RichDataType.XML);
    //         this.nodeData.setEditable(true);
    //     } else if (this.format.isHtmlFormat()) {
    //         this.showData(RichDataType.HTML);
    //         this.nodeData.setEditable(true);
    //     } else if (this.format.isBinaryFormat()) {
    //         this.showData(RichDataType.BINARY);
    //         this.nodeData.setEditable(false);
    //     } else if (this.format.isHexFormat()) {
    //         this.showData(RichDataType.HEX);
    //         this.nodeData.setEditable(false);
    //     } else if (this.format.isRawFormat()) {
    //         this.showData(RichDataType.RAW);
    //         this.nodeData.setEditable(true);
    //     }
    // };

    /**
     * 忽略数据变化
     */
    private boolean ignoreDataChange = false;

    /**
     * redis数据监听器
     */
    private final ChangeListener<String> dataListener = (observable, oldValue, newValue) -> {
        if (!this.nodeData.isDisable() && !this.ignoreDataChange && !Objects.equals(this.treeItem.rawData(), newValue)) {
            this.saveNodeData.enable();
            this.treeItem.data(newValue);
        }
    };

    @Override
    protected void initKey() {
        // 刷新二进制处理
        this.flushBinary();
        // 按钮状态处理
        this.saveNodeData.setDisable(!this.treeItem.isDataUnsaved());
//            // 如果是raw格式，则选择binary
//            if (this.treeItem.isRawEncoding()) {
//                this.format.selectBinary();
//            } else {// 自动匹配
//            RichDataType dataType = this.nodeData.showDetectData(this.treeItem.data());
//            this.format.selectObj(dataType);
//            }
        // 检测数据是否太大
        if (this.treeItem.isDataTooBig()) {
            // 状态处理
            this.nodeData.disable();
            this.ignoreDataChange = true;
            this.nodeData.clear();
            NodeGroupUtil.disable(this.getTab(), "dataToBig");
            // 异步处理，避免阻塞主程序
            TaskManager.startDelay(() -> {
                if (MessageBox.confirm(I18nHelper.tips(), ShellI18nHelper.redisKeyTip9(), null, StageManager.getPrimaryStage())) {
                    this.saveBinaryFile();
                }
            }, 10);
            return;
        }
        // 状态处理
        this.nodeData.enable();
        this.ignoreDataChange = false;
        NodeGroupUtil.enable(this.getTab(), "dataToBig");
        // 数据处理
        this.firstShowData();
        // 获取数据
        Object rawData = this.treeItem.data();
        // 数据太大，设置为raw格式
        if (treeItem.isDataTooBig()) {
            this.nodeData.showData(rawData);
            this.format.setFormat(EditorFormatType.RAW);
        } else {// 正常处理
            // 检测类型
            EditorFormatType formatType = this.nodeData.showDetectData(rawData);
            // 设置类型
            this.format.select(formatType);
        }
//         byte detectType = TextUtil.detectType(rawData);
//         if (detectType == 1) {
// //            this.nodeData.showJsonData(rawData);
//             this.format.selectObj(RichDataType.JSON);
//         } else if (detectType == 2) {
// //            this.nodeData.showXmlData(rawData);
//             this.format.selectObj(RichDataType.XML);
//         } else if (detectType == 3) {
// //            this.nodeData.showHtmlData(rawData);
//             this.format.selectObj(RichDataType.HTML);
//         } else {
// //            this.nodeData.showStringData(rawData);
//             this.format.selectObj(RichDataType.STRING);
//         }
    }

    /**
     * 保存为二进制文件
     */
    @FXML
    private void saveBinaryFile() {
        try {
            File file = FileChooserHelper.save(I18nHelper.saveFile(), this.treeItem.key(), FXChooser.allExtensionFilter());
            if (file != null) {
                Object data = this.treeItem.rawValue();
                byte[] bytes = new byte[0];
                if (data instanceof String s) {
                    bytes = s.getBytes();
                } else if (data instanceof byte[] s) {
                    bytes = s;
                }
                FileUtil.writeBytes(bytes, file);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 刷新二进制处理
     */
    private void flushBinary() {
        // 如果是raw格式，则选择binary
        if (this.treeItem.isRawEncoding()) {
            this.binary.display();
        } else {
            this.binary.disappear();
        }
    }

    /**
     * 重载数据
     */
    @FXML
    private void reloadData() {
        // 放弃保存
        if (this.treeItem.isDataUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                // 刷新数据
                this.treeItem.refreshKeyValue();
                // 初始化数据
                this.initKey();
                // 刷新内存占用
                this.treeItem.flushMemoryUsage();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @FXML
    @Override
    protected void saveKeyValue() {
        if (this.treeItem.isDataTooBig()) {
            MessageBox.warn(I18nHelper.dataTooLarge());
            return;
        }
        if (this.treeItem.isDataUnsaved()) {
            StageManager.showMask(() -> {
                try {
                    // 保存数据
                    this.treeItem.saveKeyValue();
                    // 刷新二进制标志位
                    this.flushBinary();
                    // 保存监听
                    this.saveNodeData.disable();
                    // 刷新内存占用
                    this.treeItem.flushMemoryUsage();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                }
            });
        }
    }

    /**
     * 数据撤销
     */
    @FXML
    protected void dataUndo() {
        this.nodeData.undo();
        this.nodeData.requestFocus();
    }

    /**
     * 数据重做
     */
    @FXML
    protected void dataRedo() {
        this.nodeData.redo();
        this.nodeData.requestFocus();
    }

    /**
     * 粘贴数据
     */
    @FXML
    protected void pasteData() {
        this.nodeData.paste();
        this.nodeData.requestFocus();
    }

    /**
     * 清除数据
     */
    @FXML
    protected void clearData() {
        this.nodeData.clear();
        this.nodeData.requestFocus();
    }

    @Override
    protected void firstShowData() {
        this.nodeData.showData(this.treeItem.data());
        this.nodeData.forgetHistory();
    }

    // @Override
    // protected void showData(EditorFormatType formatType) {
    //     this.nodeData.showData(this.treeItem.data(), formatType);
    // }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 格式监听
        this.format.selectedItemChanged((observableValue, old, t1) -> {
            this.nodeData.setFormatType(t1);
        });
        // 键数据处理
        this.nodeData.addTextChangeListener(this.dataListener);
        this.nodeData.undoableProperty().addListener((observableValue, aBoolean, t1) -> this.dataUndo.setDisable(!t1));
        this.nodeData.redoableProperty().addListener((observableValue, aBoolean, t1) -> this.dataRedo.setDisable(!t1));
    }

    /**
     * 健值转二维码
     */
    @FXML
    private void key2QRCode(MouseEvent event) {
        try {
            PopupAdapter adapter = PopupManager.parsePopup(ShellRedisKeyQRCodePopupController.class);
            adapter.setProp("key", this.treeItem.value());
            adapter.setProp("keyData", this.nodeData.getTextTrim());
            adapter.showPopup((Node) event.getSource());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
