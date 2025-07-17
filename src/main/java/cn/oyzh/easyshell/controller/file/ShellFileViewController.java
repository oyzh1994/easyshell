package cn.oyzh.easyshell.controller.file;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.image.FXImageView;
import cn.oyzh.fx.plus.controls.media.FXMediaView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.rich.richtextfx.data.RichDataTextAreaPane;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
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
     * txt
     */
    @FXML
    private RichDataTextAreaPane txt;

    /**
     * img
     */
    @FXML
    private FXImageView img;

    /**
     * video
     */
    @FXML
    private FXMediaView video;

    /**
     * 类型
     */
    private String type;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

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
        if ("txt".equalsIgnoreCase(this.type)) {
            byte[] content = FileUtil.readBytes(this.destPath);
            String extName = FileNameUtil.extName(this.file.getFilePath());
            if (FileNameUtil.isJsonType(extName)) {
                this.txt.showJsonData(content);
            } else if (FileNameUtil.isHtmType(extName) || FileNameUtil.isHtmlType(extName)) {
                this.txt.showHtmlData(content);
            } else if (FileNameUtil.isXmlType(extName)) {
                this.txt.showXmlData(content);
            } else if (FileNameUtil.isYamlType(extName) || FileNameUtil.isYmlType(extName)) {
                this.txt.showYamlData(content);
            } else {
                this.txt.showStringData(content == null ? "" : content);
            }
            this.txt.display();
        } else if ("img".equalsIgnoreCase(this.type)) {
            this.img.setUrl(this.destPath);
            this.img.display();
        } else if ("video".equalsIgnoreCase(this.type)) {
            this.video.setUrl(this.destPath);
            this.video.setOnError(e -> {
                MessageBox.error(I18nHelper.loadVideoFail());
            });
            this.video.play();
            this.video.display();
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        this.file = this.getProp("file");
        this.type = this.getProp("type");
        this.client = this.getProp("client");
        this.appendTitle("-" + this.file.getFileName());
        // 目标路径
        this.destPath = ShellConst.getCachePath() + UUIDUtil.uuidSimple() + "." + this.file.getExtName();
        // 初始化字体设置
        this.txt.setFontSize(this.setting.getEditorFontSize());
        this.txt.setFontFamily(this.setting.getEditorFontFamily());
        this.txt.setFontWeight2(this.setting.getEditorFontWeight());
        // 初始化
        this.init();
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
}
