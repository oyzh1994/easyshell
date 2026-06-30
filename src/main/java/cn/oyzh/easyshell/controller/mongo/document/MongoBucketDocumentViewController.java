package cn.oyzh.easyshell.controller.mongo.document;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.fx.mongo.ShellDataEditor;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.MongoColumn;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.EditorFormatTypeComboBox;
import cn.oyzh.fx.editor.incubator.EditorUtil;
import cn.oyzh.fx.gui.media.MediaControlBox;
import cn.oyzh.fx.gui.svg.glyph.MusicSVGGlyph;
import cn.oyzh.fx.gui.text.field.HighlightTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.image.FXImageView;
import cn.oyzh.fx.plus.controls.media.FXMediaView;
import cn.oyzh.fx.plus.font.FontSizeComboBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

import java.io.File;

/**
 * shell文件查看业务
 *
 * @author oyzh
 * @since 2025/07/16
 */
@StageAttribute(
        multipliable = true,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "mongo/document/mongoBucketDocumentView.fxml"
)
public class MongoBucketDocumentViewController extends StageController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * 远程文件
     */
    private MongoRecord file;

    /**
     * 目标路径
     */
    private String destPath;

    /**
     * 文件客户端
     */
    private ShellMongoClient client;

    /**
     * 文本
     */
    @FXML
    private ShellDataEditor txt;

    /**
     * 过滤组件
     */
    @FXML
    private FXHBox filterBox;

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
    private HighlightTextField filter;

    /**
     * 图片
     */
    @FXML
    private FXImageView img;

    /**
     * 视频
     */
    @FXML
    private FXMediaView video;

    /**
     * 音频
     */
    @FXML
    private FXMediaView audio;

    /**
     * 音乐图标
     */
    @FXML
    private MusicSVGGlyph music;

    /**
     * 媒体控制
     */
    @FXML
    private MediaControlBox mediaControl;

    /**
     * 类型
     */
    private String type;

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
                String content = this.txt.getText();
                FileUtil.writeUtf8String(content, this.destPath);
                Object idValue = this.file._idValue();
                MongoColumn idColumn = this.file._idColumn();
                File localFile = new File(this.destPath);
                String filename = (String) this.file.getValue("filename");
                this.client.reuploadBucketRecord(idColumn.getDbName(), idColumn.getCollectionName(), idValue, filename, localFile);
                // 更新内容长度
                this.file.putValue("length", NumberUtil.formatSize(localFile.length(), 2));
                this.restoreTitle();
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
                Object idValue = this.file._idValue();
                MongoColumn idColumn = this.file._idColumn();
                this.client.downloadBucketRecord(idColumn.getDbName(), idColumn.getCollectionName(), idValue, this.destPath);
                this.initView();
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
        if (this.txt.isEmpty()) {
            byte[] content = FileUtil.readBytes(this.destPath);
            return content == null ? "" : new String(content);
        }
        return this.txt.getText();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        if (this.isTxtType() || this.isUnknownType()) {
            // TODO: 监听事件
            // 状态
            this.txt.addTextChangeListener((observableValue, s, t1) -> {
                this.stage.restoreTitle();
                this.stage.appendTitle(" *");
            });
            EditorUtil.bindHighlight(this.txt, this.filter);
            // 编辑器格式变化
            this.txt.formatTypeProperty().addListener((observableValue, old, t1) -> {
                this.format.select(t1);
            });
            // 下拉框格式变化
            this.format.selectedItemChanged((observableValue, old, t1) -> {
                this.txt.setFormatType(t1);
            });
            // 字体大小变化
            this.fontSize.selectedItemChanged((observableValue, number, t1) -> {
                if (t1 != null) {
                    this.txt.setFontSize(t1);
                    // 记录字体大小
                    this.setting.setEditorFontSize(t1.byteValue());
                    this.settingStore.update(this.setting);
                }
            });
            // 初始化字体配置
            this.fontSize.selectSize(this.setting.getEditorFontSize());
            String filename = (String) this.file.getValue("filename");
            String extName = FileNameUtil.extName(filename);
            if (StringUtil.isNotBlank(extName)) {
                EditorFormatType formatType = EditorFormatType.ofExtension(extName);
                this.txt.showData(this.getData(), formatType);
            } else {
                this.txt.showDetectData(this.getData());
            }
            this.txt.showLineNum();
            this.txt.scrollToTop();
            this.txt.display();
        } else if (this.isImageType()) {
            this.img.setUrl(this.destPath);
            this.img.display();
            // 布局
            this.layoutRoot();
        } else if (this.isVideoType()) {
            this.video.setUrl(this.destPath);
            this.mediaControl.setup(this.video.getMediaPlayer());
            this.video.play();
            this.video.display();
            this.mediaControl.display();
            // 布局
            this.layoutRoot();
        } else if (this.isAudioType()) {
            // TODO: 监听事件
            // 宽度变化
            this.root.widthProperty().addListener((observableValue, number, t1) -> {
                this.layoutRoot();
            });
            // 高度变化
            this.root.heightProperty().addListener((observableValue, number, t1) -> {
                this.layoutRoot();
            });
            // 图标布局
            this.layoutRoot();
            this.audio.setUrl(this.destPath);
            this.mediaControl.setup(this.audio.getMediaPlayer());
            this.audio.play();
            this.music.display();
            this.mediaControl.display();
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.type = this.getProp("type");
        super.onWindowShown(event);
        this.stage.hideOnEscape();
        this.file = this.getProp("file");
        this.client = this.getProp("client");
        String filename = (String) this.file.getValue("filename");
        this.setTitle(this.getTitle() + "-" + filename);
        // 目标路径
        this.destPath = ShellFileUtil.getTempFile(FileNameUtil.extName(filename));
        // 初始化
        this.init();
    }

    /**
     * 对root重新布局
     *
     */
    private void layoutRoot() {
        if (this.isImageType()) {
            FXUtil.runPulse(() -> {
                double w = this.img.getRealWidth();
                if (OSUtil.isWindows()) {
                    w += 35;
                } else {
                    w += 20;
                }
                this.stage.setWidth(w);
            });
        } else if (this.isVideoType()) {
            FXUtil.runTimer(() -> {
                double w = this.video.getRealWidth();
                w += 20;
                this.stage.setWidth(Math.max(300, w));
            }, 20);
        } else if (this.isAudioType()) {
            double width = this.root.getRealWidth();
            double height = this.root.getRealHeight();
            double size = height - 100;
            this.music.setSize(size);
            VBox.setMargin(this.music, new Insets(10, 0, 0, (width - size) / 2));
        }
    }

    private boolean isAudioType() {
        return "audio".equalsIgnoreCase(this.type);
    }

    private boolean isVideoType() {
        return "video".equalsIgnoreCase(this.type);
    }

    private boolean isImageType() {
        return "img".equalsIgnoreCase(this.type);
    }

    private boolean isTxtType() {
        return "txt".equalsIgnoreCase(this.type);
    }

    private boolean isUnknownType() {
        return "unknown".equalsIgnoreCase(this.type);
    }

    @Override
    public void onWindowHiding(WindowEvent event) {
        super.onWindowHiding(event);
        FileUtil.del(this.destPath);
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.view1File();
    }

    /**
     * 搜索下一个
     */
    @FXML
    private void searchNext() {
        EditorUtil.searchNextHighlight(this.txt, this.filter);
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

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        this.filterBox.visibleProperty().bind(this.txt.visibleProperty());
    }

    @Override
    public void destroy() {
        this.txt.destroy();
        this.img.destroy();
        this.video.destroy();
        this.audio.destroy();
        this.mediaControl.destroy();
        super.destroy();
    }
}
