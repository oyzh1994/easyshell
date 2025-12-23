package cn.oyzh.easyshell.controller;


import cn.oyzh.common.dto.Project;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 关于业务
 *
 * @author oyzh
 * @since 2022/06/22
 */
@StageAttribute(
        resizable = false,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "about.fxml"
)
public class AboutController extends StageController {

    @FXML
    private FXText name;

    @FXML
    private FXText type;

    @FXML
    private FXText version;

    @FXML
    private FXText updateDate;

    @FXML
    private FXText copyright;

    @FXML
    private FXText jdkArch;

    @FXML
    private FXText jdkName;

    @FXML
    private FXText jdkVendor;

    @FXML
    private FXText jdkVersion;

    /**
     * 项目信息
     */
    private final Project project = Project.load();

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        // 项目信息
        this.name.setText(this.project.getName());
        this.copyright.setText(this.project.getCopyright());
        this.version.setText("v" + this.project.getVersion());
        this.updateDate.setText(this.project.getUpdateDate());
        this.type.setText(StringUtil.equals(this.project.getType(), "build") ? I18nHelper.buildType1() : I18nHelper.buildType2());

        // jdk信息
        this.jdkArch.setText(System.getProperty("os.arch"));
        this.jdkName.setText(System.getProperty("java.vm.name"));
        this.jdkVendor.setText(System.getProperty("java.vm.vendor"));
        this.jdkVersion.setText(System.getProperty("java.vm.version"));

        // 设置标题
        this.stage.appendTitle(" " + this.project.getName());
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.aboutTitle();
    }
}
