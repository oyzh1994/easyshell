package cn.oyzh.easyshell.controller.file;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.fx.ShellDataEditorPane;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.editor.EditorLineNumPolicy;
import cn.oyzh.fx.editor.tm4javafx.EditorFormatType;
import cn.oyzh.fx.editor.tm4javafx.EditorFormatTypeComboBox;
import cn.oyzh.fx.gui.media.MediaControlBox;
import cn.oyzh.fx.gui.svg.glyph.MusicSVGGlyph;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.image.FXImageView;
import cn.oyzh.fx.plus.controls.media.FXMediaView;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.font.FontSizeComboBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
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

/**
 * shell文件查看业务
 *
 * @author oyzh
 * @since 2025/07/16
 */
@StageAttribute(
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
     * 文本
     */
    @FXML
    private ShellDataEditorPane txt;

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
    private ClearableTextField filter;

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
     * 初始化文件
     */
    private void init() {
        StageManager.showMask(() -> {
            try {
                FileUtil.touch(this.destPath);
                this.client.get(this.file, this.destPath);
                this.initView();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 初始化视图
     */
    private void initView() {
        if (this.isTxtType() || this.isUnknownType()) {
            // TODO: 监听事件
            // 内容高亮
            this.filter.addTextChangeListener((observableValue, s, t1) -> {
                this.txt.setHighlightText(t1);
            });
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
            this.txt.setFont(FontManager.toFont(this.setting.editorFontConfig()));
            String extName = FileNameUtil.extName(this.file.getFilePath());
            EditorFormatType formatType = EditorFormatType.ofExtension(extName);
            this.txt.showData(this.getData(), formatType);
            this.txt.setLineNumPolicy(EditorLineNumPolicy.ALWAYS);
            this.txt.scrollToTop();
            this.txt.display();
        } else if (this.isImageType()) {
            this.img.setUrl(this.destPath);
            this.img.display();
            // 布局
            this.layoutRoot(1);
        } else if (this.isVideoType()) {
            this.video.setUrl(this.destPath);
            this.mediaControl.setup(this.video.getMediaPlayer());
            this.video.play();
            this.video.display();
            this.mediaControl.display();
            // 布局
            this.layoutRoot(2);
        } else if (this.isAudioType()) {
            // TODO: 监听事件
            // 宽度变化
            this.root.widthProperty().addListener((observableValue, number, t1) -> {
                this.layoutMusic();
            });
            // 高度变化
            this.root.heightProperty().addListener((observableValue, number, t1) -> {
                this.layoutMusic();
            });
            // 图标布局
            this.layoutMusic();
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
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        this.file = this.getProp("file");
        this.client = this.getProp("client");
        this.appendTitle("-" + this.file.getFileName());
        // 目标路径
        this.destPath = ShellFileUtil.getTempFile(this.file);
        // 初始化
        this.init();
    }

    /**
     * 对music图标进行布局
     */
    private void layoutMusic() {
        if (!"audio".equals(this.type)) {
            return;
        }
        double width = this.root.realWidth();
        double height = this.root.realHeight();
        double size = height - 70;
        this.music.setSize(size);
        VBox.setMargin(this.music, new Insets(10, 0, 0, (width - size) / 2));
    }

    /**
     * 对root重新布局
     *
     * @param type 类型
     */
    private void layoutRoot(int type) {
        FXUtil.runPulse(() -> {
            double w = -1;
            if (type == 1) {
                w = this.img.getRealWidth();
                w += 20;
            } else if (type == 2) {
                w = this.video.getRealWidth();
                w += 20;
            }
            this.stage.setWidth(Math.max(300, w));
        }, 20);
    }

    /**
     * 获取数据
     *
     * @return 数据
     */
    private String getData() {
        byte[] content = FileUtil.readBytes(this.destPath);
        return content == null ? "" : new String(content);
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
        // 销毁播放器
        if (this.video.getMediaPlayer() != null) {
            this.video.stop();
            this.video.dispose();
        }
        if (this.audio.getMediaPlayer() != null) {
            this.audio.stop();
            this.audio.dispose();
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.view1File();
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        this.filterBox.visibleProperty().bind(this.txt.visibleProperty());
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
            String text = this.txt.getText();
            if (this.searchIndex >= text.length()) {
                this.searchIndex = 0;
            }
            int index = text.indexOf(filterText, this.searchIndex);
            if (index == -1) {
                this.searchIndex = 0;
                return;
            }
            this.searchIndex = index + filterText.length();
            this.txt.selectRange(index, index + filterText.length());
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
}
