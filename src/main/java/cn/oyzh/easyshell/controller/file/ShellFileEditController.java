package cn.oyzh.easyshell.controller.file;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.EditorFormatTypeComboBox;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.font.FontSizeComboBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.WindowEvent;

import java.io.File;

/**
 * shell文件编辑业务
 *
 * @author oyzh
 * @since 2025/05/13
 */
@StageAttribute(
        multipliable = true,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "file/shellFileEdit.fxml"
)
public class ShellFileEditController extends StageController {

    /**
     * 远程文件
     */
    private ShellFile file;

    /**
     * 目标路径
     */
    private String destPath;

    /**
     * 文件客户端
     */
    private ShellFileClient client;

    /**
     * 数据
     */
    @FXML
    private ShellDataEditor data;

    /**
     * 格式
     */
    @FXML
    private EditorFormatTypeComboBox format;

    /**
     * 字体大小
     */
    @FXML
    private FontSizeComboBox fontSize;

    /**
     * 过滤
     */
    @FXML
    private ClearableTextField filter;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 设置存储
     */
    private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    /**
     * 保存文件
     */
    @FXML
    private void save() {
        StageManager.showMask(() -> {
            try {
                String content = this.data.getText();
                FileUtil.writeUtf8String(content, this.destPath);
                this.client.put(this.destPath, file.getFilePath());
                File localFile = new File(this.destPath);
                this.file.setFileSize(localFile.length());
                this.file.setModifyTime(DateHelper.formatDateTime());
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 初始化文件
     */
    private void init() {
        StageManager.showMask(() -> {
            try {
                FileUtil.touch(this.destPath);
                this.client.get(this.file, this.destPath);
                String extName = FileNameUtil.extName(this.file.getFilePath());
                EditorFormatType formatType = EditorFormatType.ofExtension(extName);
                this.data.showData(this.getData(), formatType);
                this.data.scrollToTop();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 获取数据
     *
     * @return 数据
     */
    private String getData() {
        if (this.data.isEmpty()) {
            byte[] content = FileUtil.readBytes(this.destPath);
            return content == null ? "" : new String(content);
        }
        return this.data.getText();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        // this.stage.switchOnTab();
        this.stage.hideOnEscape();
        this.file = this.getProp("file");
        this.client = this.getProp("client");
        this.appendTitle("-" + this.file.getFileName());
        // 目标路径
        this.destPath = ShellFileUtil.getTempFile(this.file);
        // 初始化字体设置
        this.fontSize.selectSize(this.setting.getEditorFontSize());
        //this.data.setFont(FontManager.toFont(this.setting.editorFontConfig()));
        // 初始化
        this.init();
    }

    @Override
    public void onWindowHiding(WindowEvent event) {
        super.onWindowHiding(event);
        FileUtil.del(this.destPath);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.data.formatTypeProperty().addListener((observableValue, formatType, t1) -> {
            this.format.select(t1);
        });
        this.format.selectedItemChanged((observableValue, formatType, t1) -> {
            this.data.setFormatType(t1);
        });
        this.fontSize.selectedItemChanged((observableValue, number, t1) -> {
            if (t1 != null) {
                this.data.setFontSize(t1);
                // 记录字体大小
                this.setting.setEditorFontSize(t1.byteValue());
                this.settingStore.update(this.setting);
            }
        });
        // 内容高亮
        this.filter.addTextChangeListener((observableValue, s, t1) -> {
            this.data.setHighlightText(t1);
        });
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.editFile();
    }

    /**
     * 搜索索引
     */
    private int searchIndex;

    /**
     * 搜索下一个
     */
    @FXML
    private void searchNext() {
        try {
            String filterText = this.filter.getText();
            if (StringUtil.isBlank(filterText)) {
                return;
            }
            String text = this.data.getText();
            if (this.searchIndex >= text.length()) {
                this.searchIndex = 0;
            }
            int index = text.indexOf(filterText, this.searchIndex);
            if (index == -1) {
                this.searchIndex = 0;
                return;
            }
            this.searchIndex = index + filterText.length();
            this.data.selectRange(index, index + filterText.length());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 过滤内容输入事件
     *
     * @param event 事件
     */
    @FXML
    private void onFilterKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            this.searchNext();
        }
    }

    /**
     * 数据内容输入事件
     *
     * @param event 事件
     */
    @FXML
    private void onDataKeyPressed(KeyEvent event) {
        if (KeyboardUtil.isCtrlS(event)) {
            this.save();
        }
    }

//    @Override
//    public void onStageInitialize(StageAdapter stage) {
//        super.onStageInitialize(stage);
//        // this.format.removeItem(RichDataType.HEX);
//        // this.format.removeItem(RichDataType.BINARY);
//        // this.format.addItem(RichDataType.JAVA);
//        // this.format.addItem(RichDataType.PYTHON);
//        // this.format.addItem(RichDataType.JAVASCRIPT);
//    }
}
