package cn.oyzh.easyshell.controller.file;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.bucket.MongoBucketFile;
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
 * 文件查看业务
 *
 * @author oyzh
 * @since 2025/07/16
 */
@StageAttribute(
        multipliable = true,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "file/shellFileView.fxml"
)
public class ShellFileViewController extends StageController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * 远程文件
     */
    private Object file;

    /**
     * 目标路径
     */
    private String destPath;

    /**
     * 文件客户端
     */
    private ShellBaseClient client;

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
        if (this.client instanceof ShellFileClient<?> fileClient) {
            ShellFile shellFile = (ShellFile) file;
            StageManager.showMask(() -> {
                try {
                    String content = this.txt.getText();
                    FileUtil.writeUtf8String(content, this.destPath);
                    fileClient.put(this.destPath, shellFile.getFilePath());
                    File localFile = new File(this.destPath);
                    shellFile.setFileSize(localFile.length());
                    shellFile.setModifyTime(DateHelper.formatDateTime());
                    this.restoreTitle();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                }
            });
        } else if (this.client instanceof ShellMongoClient mongoClient) {
            MongoBucketFile record = (MongoBucketFile) this.file;
            StageManager.showMask(() -> {
                try {
                    String content = this.txt.getText();
                    FileUtil.writeUtf8String(content, this.destPath);
                    Object idValue = record.getId();
                    File localFile = new File(this.destPath);
                    String filename = record.getFileName();
                    mongoClient.reuploadBucketRecord(record.getDbName(), record.getBucketName(), idValue, filename, localFile);
                    // 更新内容长度
                    record.setLength(localFile.length());
                    this.restoreTitle();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                }
            });
        }
    }

    /**
     * 初始化文件
     */
    private void init() {
        if (this.client instanceof ShellFileClient fileClient) {
            ShellFile shellFile = (ShellFile) this.file;
            StageManager.showMask(() -> {
                try {
                    FileUtil.touch(this.destPath);
                    fileClient.get(shellFile, this.destPath);
                    this.initView();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                }
            });
        } else if (this.client instanceof ShellMongoClient mongoClient) {
            MongoBucketFile record = (MongoBucketFile) this.file;
            StageManager.showMask(() -> {
                try {
                    FileUtil.touch(this.destPath);
                    Object idValue = record.getId();
                    mongoClient.downloadBucketRecord(record.getDbName(), record.getBucketName(), idValue, this.destPath);
                    this.initView();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                }
            });
        }
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
            String extName = null;
            if (this.file instanceof ShellFile shellFile) {
                extName = shellFile.getExtName();
            } else if (this.file instanceof MongoBucketFile record) {
                extName = record.getExtName();
            }
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
        if (this.file instanceof ShellFile shellFile) {
            this.setTitle(this.getTitle() + "-" + shellFile.getFileName());
            // 目标路径
            this.destPath = ShellFileUtil.getTempFile(shellFile.getExtName());
        } else if (this.file instanceof MongoBucketFile record) {
            this.setTitle(this.getTitle() + "-" + record.getFileName());
            // 目标路径
            this.destPath = ShellFileUtil.getTempFile(record.getExtName());
        }
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
