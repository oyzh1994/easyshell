package cn.oyzh.easyssh.controller;


import cn.oyzh.common.dto.Project;
import cn.oyzh.easyssh.SSHConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.window.StageAttribute;
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
        resizable  = false,
        modality = Modality.APPLICATION_MODAL,
        value = SSHConst.FXML_BASE_PATH + "about.fxml"
)
public class AboutController extends StageController {

    @FXML
    private FXText name;

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
        this.name.setText(this.project.getName());
        this.version.setText("v" + this.project.getVersion());
        this.updateDate.setText(this.project.getUpdateDate());
        this.copyright.setText(this.project.getCopyright());
        this.stage.hideOnEscape();
    }
}
