package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.fx.gui.text.field.FilterTextField;
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
 * docker容器日志业务
 *
 * @author oyzh
 * @since 2025/03/13
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/shellDockerLogs.fxml"
)
public class ShellDockerLogsController extends StageController {

    /**
     * 日志
     */
    @FXML
    private ShellDataEditor data;

    /**
     * 过滤
     */
    @FXML
    private FilterTextField filter;

    @FXML
    private void copyLogs() {
        ClipboardUtil.copy(this.data.getText());
        MessageBox.okToast(I18nHelper.operationSuccess());
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 内容高亮
        this.filter.addTextChangeListener((observableValue, s, t1) -> {
//            this.data.setHighlightText(t1);
            this.searchIndex = 0;
        });
        this.data.highlightProperty().bind(this.filter.textProperty());
        this.data.highlightRegexProperty().bind(this.filter.regexPropery());
        this.data.highlightMacthCaseProperty().bind(this.filter.matchCasePropery());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        String logs = this.getProp("logs");
        this.data.setText(logs);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.containerLogs();
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
            String text = this.data.getText();
            if (this.searchIndex >= text.length()) {
                this.searchIndex = 0;
            }
            TextUtil.MatchText matchText = TextUtil.findText(text, filterText, this.searchIndex, this.filter.isMatchCase(), this.filter.isRegex());
            if (matchText == TextUtil.MatchText.NOT_FOUND) {
                this.searchIndex = 0;
                return;
            }
            this.searchIndex = matchText.index() + matchText.text().length();
            this.data.selectRange(matchText.index(), matchText.index() + matchText.text().length());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
