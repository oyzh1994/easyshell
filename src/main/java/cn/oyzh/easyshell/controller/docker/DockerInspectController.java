package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.ShellJsonTextAreaPane;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * docker信息业务
 *
 * @author oyzh
 * @since 2025/03/13
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/dockerInspect.fxml"
)
public class DockerInspectController extends StageController {

    /**
     * 信息
     */
    @FXML
    private ShellJsonTextAreaPane data;

    /**
     * 过滤
     */
    @FXML
    private ClearableTextField filter;

    @FXML
    private void copyInspect() {
        ClipboardUtil.copy(this.data.getText());
        MessageBox.okToast(I18nHelper.operationSuccess());
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 内容高亮
        this.filter.addTextChangeListener((observableValue, s, t1) -> {
            this.data.setHighlightText(t1);
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        String inspect = this.getProp("inspect");
        Boolean image = this.getProp("image");
        this.data.setText(inspect);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        if (BooleanUtil.isTrue(image)) {
            this.stage.title(I18nHelper.imageInspect());
        } else {
            this.stage.title(I18nHelper.containerInspect());
        }
    }

//    @Override
//    public String getViewTitle() {
//        return I18nHelper.info();
//    }

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
            this.data.selectRangeAndGoto(index, index + filterText.length());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
