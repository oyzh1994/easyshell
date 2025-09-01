package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.fx.svg.pane.ExpandListSVGPane;
import cn.oyzh.easyshell.redis.key.RedisKeyRow;
import cn.oyzh.easyshell.redis.key.RedisSetValue;
import cn.oyzh.easyshell.trees.redis.RedisSetKeyTreeItem;
import cn.oyzh.easyshell.util.RedisI18nHelper;
import cn.oyzh.easyshell.util.RedisViewFactory;
import cn.oyzh.fx.editor.tm4javafx.EditorFormatType;
import cn.oyzh.fx.editor.tm4javafx.EditorFormatTypeComboBox;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * set键tab内容组件
 *
 * @author oyzh
 * @since 2023/06/21
 */
public class RedisSetKeyController extends RedisRowKeyController<RedisSetKeyTreeItem, RedisSetValue.RedisSetRow> {

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
     * redis数据保存按钮
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

    /**
     * 展开列表面板
     */
    @FXML
    private ExpandListSVGPane expandPane;

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
        if (!this.ignoreDataChange && !Objects.equals(this.treeItem.rawData(), newValue)) {
            this.saveNodeData.enable();
            if (this.treeItem.unsavedValue() == null) {
                this.treeItem.data(this.treeItem.currentRow());
            }
            if (this.treeItem.unsavedValue() != null) {
                this.treeItem.unsavedValue().setValue(newValue);
            }
        }
    };

    @Override
    protected void initKey() {
        // 初始化表单
        this.initTable();
        // 显示首页
        this.firstPage();
    }

    @Override
    protected List<RedisSetValue.RedisSetRow> getRows() {
        List<RedisSetValue.RedisSetRow> rows = this.treeItem.rows();
        String filterKW = this.filter.getText();
        if (StringUtil.isNotEmpty(filterKW)) {
            rows = rows.parallelStream()
                    .filter(r -> StringUtil.containsIgnoreCase(r.getValue(), filterKW))
                    .collect(Collectors.toList());
        }
        return rows;
    }

    /**
     * 添加行
     */
    @FXML
    @Override
    protected void addRow() {
//        StageAdapter adapter = StageManager.parseStage(RedisSetMemberAddController.class, this.treeItem.window());
//        adapter.setProp("treeItem", this.treeItem);
//        adapter.display();
        StageAdapter adapter = RedisViewFactory.setMemberAdd(this.treeItem);
        // 操作成功
        if (adapter != null && BooleanUtil.isTrue(adapter.getProp("result"))) {
            this.firstPage();
            // 刷新内存占用
            this.treeItem.flushMemoryUsage();
        }
    }

//    @Override
//    protected void initRow(RedisSetValue.RedisSetRow row) {
//        super.initRow(row);
//        if (row == null) {
//            this.nodeData.clear();
//            this.nodeData.disable();
//        } else {
//            this.nodeData.enable();
//        }
//    }

    @FXML
    @Override
    protected void saveKeyValue() {
        if (this.treeItem.checkRowExists()) {
            MessageBox.warn(I18nHelper.dataAlreadyExists());
            return;
        }
        if (this.treeItem.isDataTooBig()) {
            MessageBox.warn(I18nHelper.dataTooLarge());
            return;
        }
        if (this.treeItem.isDataUnsaved()) {
            StageManager.showMask(() -> {
                try {
                    this.treeItem.saveKeyValue();
                    this.listTable.refresh();
                    this.saveNodeData.disable();
                    this.treeItem.flushMemoryUsage();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                }
            });
        }
    }

    @FXML
    @Override
    protected void copyRow() {
        if (this.treeItem.isSelectRow()) {
            String builder = I18nHelper.keyName() + " : " + this.treeItem.key() + System.lineSeparator() +
                    I18nHelper.member() + " : " + this.treeItem.currentRow().getValue();
            ClipboardUtil.setStringAndTip(builder);
        }
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
     * 清除数据
     */
    @FXML
    private void clearData() {
        this.nodeData.clear();
        this.nodeData.requestFocus();
    }

    @Override
    protected void firstShowData() {
        RedisSetValue.RedisSetRow row = this.treeItem.data();
        if (row == null) {
            return;
        }
        // 数据太大
        if (this.treeItem.isDataTooBig()) {
            // 状态处理
            this.nodeData.disable();
            this.ignoreDataChange = true;
            this.nodeData.clear();
            NodeGroupUtil.disable(this.getTab(), "dataToBig");
            // 异步处理，避免阻塞主程序
            TaskManager.startDelay(() -> {
                if (MessageBox.confirm(I18nHelper.tips(), RedisI18nHelper.keyTip9(), null, StageManager.getPrimaryStage())) {
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
        EditorFormatType formatType = this.nodeData.showDetectData(row.getValue());
        this.format.select(formatType);
        this.nodeData.forgetHistory();
        this.saveNodeData.disable();
    }

    /**
     * 保存为二进制文件
     */
    @FXML
    private void saveBinaryFile() {
        try {
            File file = FileChooserHelper.save(I18nHelper.saveFile(), this.treeItem.key(), FXChooser.allExtensionFilter());
            if (file != null) {
                RedisSetValue.RedisSetRow row = this.treeItem.rawValue();
                byte[] bytes = row.getValue().getBytes();
                FileUtil.writeBytes(bytes, file);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    // @Override
    // protected void showData(EditorFormatType formatType) {
    //     RedisSetValue.RedisSetRow row = this.treeItem.data();
    //     if (row != null) {
    //         this.nodeData.showData(row.getValue(), formatType);
    //     }
    // }

    @FXML
    @Override
    protected void deleteRow() {
        if (!this.treeItem.isSelectRow()) {
            return;
        }
        RedisKeyRow row = this.treeItem.currentRow();
        if (!MessageBox.confirm(I18nHelper.deleteMember() + ":" + row.getValue())) {
            return;
        }
        if (!this.treeItem.deleteRow()) {
            return;
        }
        // 移除
        if (this.listTable.getItemSize() > 1) {
            this.listTable.removeItem(row);
        } else {// 刷新
            this.firstPage();
        }
        // 刷新内存占用
        this.treeItem.flushMemoryUsage();
    }

    @Override
    protected void clearRow() {
        this.nodeData.clear();
        this.nodeData.disable();
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 格式监听
        this.format.selectedItemChanged((observableValue, formatType, t1) -> {
            this.nodeData.setFormatType(t1);
        });
        // 键数据处理
        this.nodeData.addTextChangeListener(this.dataListener);
        this.nodeData.undoableProperty().addListener((observableValue, aBoolean, t1) -> this.dataUndo.setDisable(!t1));
        this.nodeData.redoableProperty().addListener((observableValue, aBoolean, t1) -> this.dataRedo.setDisable(!t1));
        // 操作绑定
        this.dataAction.disableProperty().bind(this.nodeData.disableProperty());
    }

    @FXML
    private void expendList() {
        if (this.expandPane.isCollapse()) {
            NodeGroupUtil.disappear(this.getTab(), "set_list");
            this.nodeData.setFlexHeight("100% - 54");
            this.expandPane.expand();
        } else {
            NodeGroupUtil.display(this.getTab(), "set_list");
            this.nodeData.setFlexHeight("100% - 379");
            this.expandPane.collapse();
        }
    }

//    /**
//     * set成员添加事件
//     *
//     * @param msg 消息
//     */
//    @EventSubscribe
//    private void onSetMemberAdded(RedisSetMemberAddedEvent msg) {
//        if (this.treeItem == msg.data()) {
//            this.firstPage();
//            // 刷新内存占用
//            this.treeItem.flushMemoryUsage();
//        }
//    }
}
