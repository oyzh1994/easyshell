package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.fx.svg.pane.ExpandListSVGPane;
import cn.oyzh.easyshell.redis.key.ShellRedisKeyRow;
import cn.oyzh.easyshell.redis.key.ShellRedisZSetValue;
import cn.oyzh.easyshell.trees.redis.key.RedisZSetKeyTreeItem;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.editor.tm4javafx.EditorFormatType;
import cn.oyzh.fx.editor.tm4javafx.EditorFormatTypeComboBox;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * zset键地理坐标tab内容组件
 *
 * @author oyzh
 * @since 2023/06/30
 */
public class RedisCoordinateKeyController extends RedisRowKeyController<RedisZSetKeyTreeItem, ShellRedisZSetValue.RedisZSetRow> {

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
     * 经度值
     */
    @FXML
    private DecimalTextField longitudeVal;

    /**
     * 纬度值
     */
    @FXML
    private DecimalTextField latitudeVal;

    /**
     * 数据组件
     */
    @FXML
    private ShellDataEditor nodeData;

    /**
     * 格式
     */
    @FXML
    private EditorFormatTypeComboBox format;

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

    /**
     * 经度值监听器
     */
    private final ChangeListener<String> longitudeValListener = (observable, oldValue, newValue) -> {
        Number value = this.longitudeVal.getValue();
        ShellRedisZSetValue.RedisZSetRow row = this.treeItem.rawValue();
        if (!this.ignoreDataChange && row != null && !Objects.equals(row.getLongitude(), value.doubleValue())) {
            this.saveNodeData.enable();
            if (this.treeItem.unsavedValue() == null) {
                this.treeItem.data(this.treeItem.currentRow());
            }
            if (this.treeItem.unsavedValue() != null) {
                this.treeItem.unsavedValue().setLongitude(value.doubleValue());
            }
        }
    };

    /**
     * 纬度值监听器
     */
    private final ChangeListener<String> latitudeValListener = (observable, oldValue, newValue) -> {
        Number value = this.longitudeVal.getValue();
        ShellRedisZSetValue.RedisZSetRow row = this.treeItem.rawValue();
        if (!this.ignoreDataChange && row != null && !Objects.equals(row.getLatitude(), value.doubleValue())) {
            this.saveNodeData.enable();
            if (this.treeItem.unsavedValue() == null) {
                this.treeItem.data(this.treeItem.currentRow());
            }
            if (this.treeItem.unsavedValue() != null) {
                this.treeItem.unsavedValue().setLatitude(value.doubleValue());
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
    protected List<ShellRedisZSetValue.RedisZSetRow> getRows() {
        List<ShellRedisZSetValue.RedisZSetRow> rows = this.treeItem.rows();
        String filterKW = this.filter.getText();
        if (StringUtil.isNotEmpty(filterKW)) {
            rows = rows.parallelStream()
                    .filter(r -> StringUtil.containsIgnoreCase(r.getValue(), filterKW) || StringUtil.containsIgnoreCase(String.valueOf(r.getLatitude()), filterKW) || StringUtil.containsIgnoreCase(String.valueOf(r.getLongitude()), filterKW))
                    .collect(Collectors.toList());
        }
        return rows;
    }

    @FXML
    @Override
    protected void addRow() {
        StageAdapter adapter = ShellViewFactory.redisZSetCoordinateAdd(this.treeItem);
        // 操作成功
        if (adapter != null && BooleanUtil.isTrue(adapter.getProp("result"))) {
            this.firstPage();
            // 刷新内存占用
            this.treeItem.flushMemoryUsage();
        }
    }

    @Override
    protected void initRow(ShellRedisZSetValue.RedisZSetRow row) {
        super.initRow(row);
        if (row == null) {
//            this.nodeData.clear();
//            this.nodeData.disable();
            this.latitudeVal.clear();
            this.longitudeVal.clear();
        } else {
            this.latitudeVal.setValue(row.getLatitude());
            this.longitudeVal.setValue(row.getLongitude());
//            this.nodeData.enable();
            this.saveNodeData.disable();
            this.treeItem.clearData();
        }
    }

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
                    I18nHelper.coordinates() + " : " + this.treeItem.currentRow().getValue() + System.lineSeparator() +
                    I18nHelper.longitude() + " : " + this.treeItem.currentRow().getLongitude() + System.lineSeparator() +
                    I18nHelper.latitude() + " : " + this.treeItem.currentRow().getLatitude();
            ClipboardUtil.setStringAndTip(builder);
        }
    }

    /**
     * 反转视图
     */
    @FXML
    private void reverseView() {
        this.treeItem.reverseView();
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
        ShellRedisZSetValue.RedisZSetRow row = this.treeItem.data();
        if (row == null) {
            return;
        }
        // 数据太大
        if (this.treeItem.isDataTooBig()) {
            // 状态处理
            this.nodeData.clear();
            this.nodeData.disable();
            this.ignoreDataChange = true;
            NodeGroupUtil.disable(this.getTab(), "dataToBig");
            MessageBox.warn(I18nHelper.dataTooLarge());
            return;
        }
        // 状态处理
        this.nodeData.enable();
        this.ignoreDataChange = false;
        NodeGroupUtil.enable(this.getTab(), "dataToBig");
        EditorFormatType formatType = this.nodeData.showDetectData(row.getValue());
        this.format.select(formatType);
        this.nodeData.forgetHistory();
        this.saveNodeData.disable();
    }

    // @Override
    // protected void showData(EditorFormatType formatType) {
    //     ShellRedisZSetValue.RedisZSetRow row = this.treeItem.data();
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
        if (!MessageBox.confirm(I18nHelper.deleteCoordinate() + ":" + row.getValue())) {
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
        // 经度处理
        this.longitudeVal.addTextChangeListener(this.longitudeValListener);
        this.longitudeVal.disableProperty().bind(this.nodeData.disabledProperty());
        this.longitudeVal.editableProperty().bind(this.nodeData.editableProperty());
        // 纬度处理
        this.latitudeVal.addTextChangeListener(this.latitudeValListener);
        this.latitudeVal.editableProperty().bind(this.nodeData.editableProperty());
        this.latitudeVal.disableProperty().bind(this.nodeData.disabledProperty());
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
//     * zset坐标添加事件
//     *
//     * @param event 事件
//     */
//    @EventSubscribe
//    private void zSetCoordinateAdded(RedisZSetCoordinateAddedEvent event) {
//        if (this.treeItem == event.data()) {
//            this.firstPage();
//            // 刷新内存占用
//            this.treeItem.flushMemoryUsage();
//        }
//    }

    @FXML
    private void expendList() {
        if (this.expandPane.isCollapse()) {
            NodeGroupUtil.disappear(this.getTab(), "coordinate_list");
            this.nodeData.setFlexHeight("100% - 152");
            this.expandPane.expand();
        } else {
            NodeGroupUtil.display(this.getTab(), "coordinate_list");
            this.nodeData.setFlexHeight("100% - 478");
            this.expandPane.collapse();
        }
    }
}
