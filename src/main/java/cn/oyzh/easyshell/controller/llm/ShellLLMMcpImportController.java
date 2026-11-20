package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.easyshell.llm.LLMMcpJsonConfigParser;
import cn.oyzh.easyshell.llm.LLMMcpServer;
import cn.oyzh.easyshell.store.llm.LLMMcpServerStore;
import cn.oyzh.fx.editor.incubator.control.JsonEditor;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.List;

/**
 * 通过标准 MCP JSON 导入 Server。
 *
 * @author oyzh
 * @since 2026-11-20
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMMcpImport.fxml"
)
public class ShellLLMMcpImportController extends StageController {

    @FXML
    private JsonEditor json;

    private final LLMMcpServerStore serverStore = LLMMcpServerStore.INSTANCE;

    @FXML
    private void importConfig() {
        if (this.json.isEmpty()) {
            return;
        }
        try {
            List<LLMMcpServer> servers = LLMMcpJsonConfigParser.parse(this.json.getText());
            LLMMcpServerStore.ImportResult result = this.serverStore.importServers(servers);
            if (result.failed() > 0) {
                MessageBox.warn(String.format(
                        I18nResourceBundle.i18nString("shell.llm.mcp.importPartial"),
                        result.total(), result.failed()), this.getWindow());
            } else {
                MessageBox.okToast(String.format(
                        I18nResourceBundle.i18nString("shell.llm.mcp.importSuccess"),
                        result.total(), result.inserted(), result.updated()), this.getWindow());
            }
            this.setProp("imported", result.total() > 0);
            this.setProp("result", result);
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.warn(String.format(
                    I18nResourceBundle.i18nString("shell.llm.mcp.importInvalid"),
                    ExceptionUtil.getMessage(ex)), this.getWindow());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.llm.mcp.importTitle");
    }
}
