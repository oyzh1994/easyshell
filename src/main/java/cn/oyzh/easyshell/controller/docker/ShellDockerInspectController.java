package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.fx.editor.incubator.EditorUtil;
import cn.oyzh.fx.gui.text.field.HighlightTextField;
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
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/shellDockerInspect.fxml"
)
public class ShellDockerInspectController extends StageController {

    /**
     * 信息
     */
    @FXML
    private ShellDataEditor data;

    /**
     * 过滤
     */
    @FXML
    private HighlightTextField filter;

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
//            this.data.setHighlightText(t1);
            EditorUtil.clearHighlightSearchIndex(this.data);
        });
        EditorUtil.bindHighlight(this.data, this.filter);
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

    /**
     * 搜索下一个
     */
    @FXML
    private void searchNext() {
        EditorUtil.searchNextHighlight(this.data, this.filter);
    }
}
