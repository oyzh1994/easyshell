package cn.oyzh.easyshell.tabs.zk.node;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.popups.zk.ShellZKNodeQRCodePopupController;
import cn.oyzh.easyshell.trees.zk.ShellZKNodeTreeItem;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.EditorFormatTypeComboBox;
import cn.oyzh.fx.gui.combobox.CharsetComboBox;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.thread.RenderService;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.nio.charset.Charset;

/**
 * zk节点data组件
 *
 * @author oyzh
 * @since 2025/04/11
 */
public class ShellZKNodeDataTabController extends SubTabController {

    /**
     * 内容过滤组件
     */
    @FXML
    private ClearableTextField dataSearch;

    /**
     * 右侧zk权限视图切换按钮
     */
    @FXML
    private CharsetComboBox charset;

    /**
     * 数据大小
     */
    @FXML
    private FXText dataSize;

    /**
     * 加载耗时
     */
    @FXML
    private FXText loadTime;

    /**
     * 节点路径
     */
    @FXML
    private FXText nodePath;

    /**
     * zk数据保存
     */
    @FXML
    private SVGGlyph dataSave;

    /**
     * zk数据撤销
     */
    @FXML
    private SVGGlyph dataUndo;

    /**
     * zk数据重做
     */
    @FXML
    private SVGGlyph dataRedo;

    /**
     * 右侧zk数据
     */
    @FXML
    private ShellDataEditor nodeData;

    /**
     * 格式
     */
    @FXML
    protected EditorFormatTypeComboBox format;

    /**
     * 数据面板
     */
    @FXML
    private FXTab dataTab;

    /**
     * 复制节点路径及数据
     */
    @FXML
    private void copyNode() {
        try {
            byte[] bytes = this.activeItem().getData();
            String data = this.activeItem().decodeNodePath() + " " + new String(bytes);
            ClipboardUtil.setStringAndTip(data);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 复制节点路径
     */
    @FXML
    private void copyNodePath() {
        ClipboardUtil.setStringAndTip(this.activeItem().decodeNodePath());
    }

    /**
     * 保存为二进制文件
     */
    @FXML
    private void saveBinaryFile() {
        try {
            File file = FileChooserHelper.save(I18nHelper.saveFile(), "", FXChooser.allExtensionFilter());
            if (file != null) {
                FileUtil.writeBytes(this.activeItem().getNodeData(), file);
                MessageBox.info(I18nHelper.operationSuccess());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 刷新zk节点数据
     */
    @FXML
    private void reloadData() {
        // 放弃保存
        if (this.activeItem().isDataUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
            return;
        }
        DownLatch latch = DownLatch.of();
        // 刷新数据
        StageManager.showMask(() -> {
            try {
                this.activeItem().refreshData();
                this.activeItem().refresh();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            } finally {
                latch.countDown();
            }
        });
        latch.await();
        // 数据变更
        this.showData();
        // 刷新tab颜色
        this.flushTabGraphicColor();
        // 按钮处理
        this.nodeData.forgetHistory();
        this.dataSave.disable();
    }

    /**
     * 保存节点数据
     */
    @FXML
    private void saveNodeData() {
        // if (this.activeItem().isDataTooBig()) {
        //    MessageBox.warn(I18nHelper.dataTooLarge());
        //    return;
        //}
        // 保存数据
        if (!this.activeItem().isDataUnsaved()) {
            return;
        }
        // 保存数据
        RenderService.submit(() -> {
            // 保存数据
            if (this.activeItem().saveData()) {
                // 禁用图标
                this.dataSave.disable();
                // 刷新数据大小
                this.flushDataSize();
                // 刷新tab颜色
                this.flushTabGraphicColor();
            }
        });
    }

    /**
     * 数据撤销
     */
    @FXML
    private void dataUndo() {
        this.nodeData.undo();
        this.nodeData.requestFocus();
    }

    /**
     * 数据重做
     */
    @FXML
    private void dataRedo() {
        this.nodeData.redo();
        this.nodeData.requestFocus();
    }

    /**
     * 粘贴数据
     */
    @FXML
    private void pasteData() {
        this.nodeData.paste();
        this.nodeData.requestFocus();
    }

    /**
     * 清空数据
     */
    @FXML
    private void clearData() {
        this.nodeData.clear();
        this.nodeData.requestFocus();
    }

    /**
     * zk节点转二维码
     */
    @FXML
    private void node2QRCode(MouseEvent event) {
        try {
            PopupAdapter adapter = PopupManager.parsePopup(ShellZKNodeQRCodePopupController.class);
            adapter.setProp("zkNode", this.activeItem().value());
            adapter.setProp("nodeData", this.nodeData.getTextTrim());
            adapter.showPopup((Node) event.getSource());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    ///**
    // * 认证节点
    // */
    //@FXML
    // private void authNode() {
    //    this.activeItem().authNode();
    //}

    /**
     * zk数据控件按键事件
     *
     * @param e 事件
     */
    @FXML
    private void onNodeDataKeyPressed(KeyEvent e) {
        // 保存节点数据
        if (KeyboardUtil.isCtrlS(e)) {
            this.saveNodeData();
            e.consume();
        }
    }

    ///**
    // * 显示历史
    // */
    //@FXML
    // private void showHistory() {
    //    if (this.activeItem() != null) {
    //        ShellZKEventUtil.historyShow(this.activeItem());
    //    }
    //}

    /**
     * 显示数据
     */
    protected void showData() {
        ShellZKNodeTreeItem item = this.activeItem();
        if (item == null) {
            return;
        }
        //// 检测数据是否太大
        // if (item.isDataTooBig()) {
        //    this.nodeData.disable();
        //    this.nodeData.clear();
        //    NodeGroupUtil.disable(this.dataTab, "dataToBig");
        //    // 异步处理，避免阻塞主程序
        //    TaskManager.startDelay(() -> {
        //        if (MessageBox.confirm(I18nHelper.tips(), ShellI18nHelper.zkNodeTip7(), null, StageManager.getPrimaryStage())) {
        //            this.saveBinaryFile();
        //        }
        //    }, 10);
        //    return;
        //}
        NodeGroupUtil.enable(this.dataTab, "dataToBig");
        byte[] bytes = item.getData();
        // 转换编码
        bytes = TextUtil.changeCharset(bytes, Charset.defaultCharset(), this.charset.getCharset());
        // 显示检测后的数据
        // RichDataType dataType = this.nodeData.showDetectData(new String(bytes, this.charset.getCharset()));
        // // 选中格式
        // this.format.selectObj(dataType);
        // 选中格式
        EditorFormatType formatType = this.nodeData.showDetectData(new String(bytes, this.charset.getCharset()));
        this.format.selectObj(formatType);
    }

    // /**
    //  * 显示数据
    //  *
    //  * @param dataType 数据类型
    //  */
    // protected void showData(RichDataType dataType) {
    //     byte[] bytes = this.activeItem().getData();
    //     bytes = TextUtil.changeCharset(bytes, Charset.defaultCharset(), this.charset.getCharset());
    //     byte[] finalBytes = bytes;
    //     StageManager.showMask(() -> FXUtil.runWait(() -> this.nodeData.showData(dataType, finalBytes)));
    // }

    /**
     * 初始化数据
     */
    public void initData() {
        // 显示数据
        this.showData();
        // 刷新数据大小
        this.flushDataSize();
        // 遗忘历史
        this.nodeData.forgetHistory();
        // 按钮处理
        this.dataUndo.disable();
        this.dataRedo.disable();
        if (this.activeItem() != null) {
            this.dataSave.setDisable(!this.activeItem().isDataUnsaved());
            // 节点路径处理
            this.nodePath.text(this.activeItem().nodePath());
            this.loadTime.text(I18nHelper.cost() + " : " + this.activeItem().loadTime() + "ms");
        }
    }

    /**
     * 刷新数据大小
     */
    private void flushDataSize() {
        if (this.activeItem() == null) {
            return;
        }
        // 数据大小处理
        this.dataSize.text(I18nHelper.size() + " : " + this.activeItem().dataSizeInfo());
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // undo监听
        this.nodeData.undoableProperty().addListener((observableValue, aBoolean, t1) -> this.dataUndo.setDisable(!t1));
        // redo监听
        this.nodeData.redoableProperty().addListener((observableValue, aBoolean, t1) -> this.dataRedo.setDisable(!t1));
        // 字符集选择事件
        this.charset.selectedItemChanged((t3, t2, t1) -> {
            // StageManager.showMask(this::showData)
            this.showData();
        });
        // 节点内容过滤
        this.dataSearch.addTextChangeListener((observable, oldValue, newValue) -> {
            // StageManager.showMask(() -> this.nodeData.setHighlightText(newValue))
            this.nodeData.setHighlightText(newValue);
        });
        // 格式监听
        this.format.selectedItemChanged((t3, t2, t1) -> {
            this.nodeData.setFormatType(t1);
        });
        // // 格式监听
        // this.format.selectedItemChanged((t1, t2, t3) -> {
        //     // StageManager.showMask(() -> {
        //     try {
        //         if (this.format.isStringFormat()) {
        //             this.showData(RichDataType.STRING);
        //             this.nodeData.setEditable(true);
        //         } else if (this.format.isJsonFormat()) {
        //             this.showData(RichDataType.JSON);
        //             this.nodeData.setEditable(true);
        //         } else if (this.format.isXmlFormat()) {
        //             this.showData(RichDataType.XML);
        //             this.nodeData.setEditable(true);
        //         } else if (this.format.isHtmlFormat()) {
        //             this.showData(RichDataType.HTML);
        //             this.nodeData.setEditable(true);
        //         } else if (this.format.isBinaryFormat()) {
        //             this.showData(RichDataType.BINARY);
        //             this.nodeData.setEditable(false);
        //         } else if (this.format.isHexFormat()) {
        //             this.showData(RichDataType.HEX);
        //             this.nodeData.setEditable(false);
        //         } else if (this.format.isRawFormat()) {
        //             this.showData(RichDataType.RAW);
        //             this.nodeData.setEditable(this.nodeData.getRealType() == RichDataType.STRING);
        //         }
        //     } catch (Exception ex) {
        //         ex.printStackTrace();
        //         MessageBox.exception(ex);
        //     }
        //     // });
        // });
        // 节点内容变更
        this.nodeData.addTextChangeListener((observable, oldValue, newValue) -> {
            if (this.nodeData.isDisable()) {
                return;
            }
            this.dataSave.enable();
            if (this.activeItem() != null) {
                byte[] bytes = newValue == null ? new byte[]{} : newValue.getBytes(this.charset.getCharset());
                this.activeItem().nodeData(bytes);
                this.flushTabGraphicColor();
            }
        });
    }

    /**
     * 设置数据高亮
     *
     * @param highlight 高亮内容
     */
    public void setDataHighlight(String highlight) {
        if (StringUtil.isBlank(highlight)) {
            highlight = this.dataSearch.getTextTrim();
        }
        this.nodeData.setHighlightText(highlight);
    }

    private ShellZKNodeTreeItem activeItem() {
        return this.parent().getActiveItem();
    }

    @Override
    public ShellZKNodeTabController parent() {
        return (ShellZKNodeTabController) super.parent();
    }

}