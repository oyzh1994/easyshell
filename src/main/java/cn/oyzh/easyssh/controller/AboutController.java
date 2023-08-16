package cn.oyzh.easyssh.controller;


import cn.oyzh.common.dto.Project;
import cn.oyzh.common.util.SpringUtil;
import cn.oyzh.easyfx.controller.FXController;
import cn.oyzh.easyfx.controls.FlexText;
import cn.oyzh.easyfx.view.FXWindow;
import cn.oyzh.easyssh.SSHConst;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 关于业务
 *
 * @author oyzh
 * @since 2022/06/22
 */
@FXWindow(
        resizeable = false,
        iconUrls = SSHConst.ICON_PATH,
        modality = Modality.APPLICATION_MODAL,
        value = SSHConst.FXML_BASE_PATH + "about.fxml"
)
public class AboutController extends FXController {

    @FXML
    private FlexText name;

    @FXML
    private FlexText version;

    @FXML
    private FlexText updateDate;

    @FXML
    private FlexText copyright;

    /**
     * 项目信息
     */
    private final Project project = SpringUtil.getBean(Project.class);

    @Override
    public void onViewShown(WindowEvent event) {
        this.name.setText(this.project.getName());
        this.version.setText("v" + this.project.getVersion());
        this.updateDate.setText(this.project.getUpdateDate());
        this.copyright.setText(this.project.getCopyright());
        this.view.hideOnEscape();
    }
}
