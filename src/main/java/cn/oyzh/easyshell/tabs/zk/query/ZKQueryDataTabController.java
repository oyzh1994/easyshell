package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.query.zk.ZKQueryEditor;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.fx.editor.tm4javafx.EditorFormatType;
import cn.oyzh.fx.editor.tm4javafx.EditorFormatTypeComboBox;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

/**
 * 查询数据
 *
 * @author oyzh
 * @since 2025/01/20
 */
public class ZKQueryDataTabController extends RichTabController {

    /**
     * zk路径
     */
    private String path;

    /**
     * zk客户端
     */
    private ZKClient zkClient;

    /**
     * 保存
     */
    @FXML
    private SVGGlyph save;

    /**
     * 撤销
     */
    @FXML
    private SVGGlyph undo;

    /**
     * 重做
     */
    @FXML
    private SVGGlyph redo;

    /**
     * 过滤
     */
    @FXML
    private ClearableTextField filter;

    /**
     * 数据
     */
    @FXML
    private ZKQueryEditor data;

    /**
     * 格式
     */
    @FXML
    private EditorFormatTypeComboBox format;

    public void init(String path, byte[] bytes, ZKClient zkClient) {
        this.path = path;
        this.zkClient = zkClient;
        // 处理数据
        byte[] bytes1 = bytes == null ? new byte[]{} : bytes;
        // 显示检测后的数据
        EditorFormatType formatType = this.data.showDetectData(new String(bytes1));
        // 遗忘历史
        this.data.forgetHistory();
        // 选中格式
        this.format.selectObj(formatType);
        // 绑定监听器
        this.data.addTextChangeListener((observable, oldValue, newValue) -> this.save.enable());
        this.data.undoableProperty().addListener((observable, oldValue, newValue) -> this.undo.setDisable(!newValue));
        this.data.redoableProperty().addListener((observable, oldValue, newValue) -> this.redo.setDisable(!newValue));
        // // 格式监听
        // this.format.selectedItemChanged((t1, t2, t3) -> {
        //     if (this.format.isRawFormat()) {
        //         this.data.showStringData(bytes1);
        //         this.data.setEditable(true);
        //     } else if (this.format.isJsonFormat()) {
        //         this.data.showJsonData(bytes1);
        //         this.data.setEditable(true);
        //     } else if (this.format.isXmlFormat()) {
        //         this.data.showXmlData(bytes1);
        //         this.data.setEditable(true);
        //     } else if (this.format.isHtmlFormat()) {
        //         this.data.showHtmlData(bytes1);
        //         this.data.setEditable(true);
        //     } else if (this.format.isBinaryFormat()) {
        //         this.data.showBinaryData(bytes1);
        //         this.data.setEditable(false);
        //     } else if (this.format.isHexFormat()) {
        //         this.data.showHexData(bytes1);
        //         this.data.setEditable(false);
        //     } else if (this.format.isRawFormat()) {
        //         this.data.showRawData(bytes1);
        //         this.data.setEditable(this.data.getRealType() == RichDataType.STRING);
        //     }
        // });
        // 过滤内容
        this.filter.addTextChangeListener((observableValue, s, t1) -> {
            this.data.setHighlightText(t1);
        });
    }

    @FXML
    private void save() {
        try {
            this.save.disable();
            this.zkClient.setData(this.path, this.data.getText());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void undo() {
        this.data.undo();
        this.data.requestFocus();
    }

    @FXML
    private void redo() {
        this.data.undo();
        this.data.requestFocus();
    }

    @FXML
    private void onDataKeyPressed(KeyEvent event) {
        if (KeyboardUtil.isCtrlS(event)) {
            this.save();
        }
    }
}