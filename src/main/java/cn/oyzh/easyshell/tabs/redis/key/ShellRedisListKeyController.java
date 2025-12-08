package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.fx.svg.pane.ExpandListSVGPane;
import cn.oyzh.easyshell.redis.key.ShellRedisKeyRow;
import cn.oyzh.easyshell.redis.key.ShellRedisListValue;
import cn.oyzh.easyshell.trees.redis.ShellRedisListKeyTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.redis.ShellRedisViewFactory;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.EditorFormatTypeComboBox;
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
 * list键tab内容组件
 *
 * @author oyzh
 * @since 2023/06/21
 */
public class ShellRedisListKeyController extends ShellRedisRowKeyController<ShellRedisListKeyTreeItem, ShellRedisListValue.RedisListRow> {

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
     * 数据监听器
     */
    private final ChangeListener<String> dataListener = (t1, t2, newValue) -> {
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

    /**
     * 刷新行
     */
    @FXML
    private void reloadRow() {
        // 放弃保存
        if (this.treeItem.isDataUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                // 刷新数据
                if (this.treeItem.reloadRow()) {
                    this.initRow(this.treeItem.currentRow());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @Override
    protected List<ShellRedisListValue.RedisListRow> getRows() {
        List<ShellRedisListValue.RedisListRow> rows = this.treeItem.rows();
        String filterKW = this.filter.getText();
        if (StringUtil.isNotEmpty(filterKW)) {
            rows = rows.parallelStream()
                    .filter(r -> StringUtil.containsIgnoreCase(r.getValue(), filterKW))
                    .collect(Collectors.toList());
        }
        return rows;
    }

    @FXML
    @Override
    protected void addRow() {
        StageAdapter adapter = ShellRedisViewFactory.redisListElementAdd(this.treeItem);
        // 操作成功
        if (adapter != null && BooleanUtil.isTrue(adapter.getProp("result"))) {
            this.firstPage();
            // 刷新内存占用
            this.treeItem.flushMemoryUsage();
        }
    }

//    @Override
//    protected void initRow(ShellRedisListValue.RedisListRow row) {
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
                    I18nHelper.element() + " : " + this.treeItem.currentRow().getValue();
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
        ShellRedisListValue.RedisListRow row = this.treeItem.data();
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
                ShellRedisListValue.RedisListRow row = this.treeItem.rawValue();
                byte[] bytes = row.getValue().getBytes();
                FileUtil.writeBytes(bytes, file);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    // @Override
    // protected void showData(EditorFormatType formatType) {
    //     ShellRedisListValue.RedisListRow row = this.treeItem.data();
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
        ShellRedisKeyRow row = this.treeItem.currentRow();
        if (!MessageBox.confirm(I18nHelper.deleteElement() + ":" + row.getValue())) {
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

//    /**
//     * list行添加事件
//     *
//     * @param msg 消息
//     */
//    @EventSubscribe
//    private void onListRowAdded(ShellRedisListRowAddedEvent msg) {
//        if (this.treeItem == msg.data()) {
//            this.firstPage();
//            // 刷新内存占用
//            this.treeItem.flushMemoryUsage();
//        }
//    }

    @FXML
    private void expendList() {
        if (this.expandPane.isCollapse()) {
            NodeGroupUtil.disappear(this.getTab(), "list_list");
            this.nodeData.setFlexHeight("100% - 54");
            this.expandPane.expand();
        } else {
            NodeGroupUtil.display(this.getTab(), "list_list");
            this.nodeData.setFlexHeight("100% - 379");
            this.expandPane.collapse();
        }
    }

    @Override
    public void destroy() {
        this.nodeData.destroy();
        super.destroy();
    }
}
