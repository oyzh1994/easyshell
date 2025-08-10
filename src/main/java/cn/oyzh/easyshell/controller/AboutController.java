package cn.oyzh.easyshell.controller;


import cn.oyzh.common.dto.Project;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
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

    /**
     * 项目信息
     */
    private final Project project = Project.load();

    @Override
    public void onWindowShown(WindowEvent event) {
        // 当舞台被显示时，设置名称文本框的文本为项目名称
        this.name.setText(this.project.getName());
        // 设置版本文本框的文本为项目版本号
        this.version.setText("v" + this.project.getVersion());
        // 设置更新日期文本框的文本为项目的更新日期
        this.updateDate.setText(this.project.getUpdateDate());
        // 设置版权文本框的文本为项目的版权信息
        this.copyright.setText(this.project.getCopyright());
        this.type.setText(StringUtil.equals(this.project.getType(), "build") ? I18nHelper.buildType1() : I18nHelper.buildType2());
        // 设置标题
        this.stage.appendTitle(" " + this.project.getName());
        this.stage.hideOnEscape();
        super.onWindowShown(event);
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("base.title.about");
    }
}
