package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.llm.mcp.LLMMcpClientFactory;
import cn.oyzh.easyshell.llm.mcp.LLMMcpServer;
import cn.oyzh.easyshell.llm.mcp.LLMMcpTransport;
import cn.oyzh.easyshell.fx.llm.LLMMcpUiUtil;
import cn.oyzh.easyshell.store.llm.LLMMcpServerStore;
import cn.oyzh.fx.gui.svg.glyph.llm.TestSVGGlyph;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import dev.langchain4j.mcp.client.McpClient;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 编辑 MCP Server。
 *
 * @author oyzh
 * @since 2026-11-20
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMUpdateMcpServer.fxml"
)
public class ShellLLMUpdateMcpServerController extends StageController {

    @FXML
    private ClearableTextField name;

    @FXML
    private FXComboBox<LLMMcpTransport> transport;

    @FXML
    private FXVBox stdioBox;

    @FXML
    private FXVBox remoteBox;

    @FXML
    private FXTextArea command;

    @FXML
    private ClearableTextField workingDirectory;

    @FXML
    private FXTextArea environment;

    @FXML
    private ClearableTextField url;

    @FXML
    private FXTextArea headers;

    @FXML
    private FXTextArea toolNames;

    @FXML
    private ClearableTextField timeout;

    @FXML
    private TestSVGGlyph testGlyph;

    @FXML
    private FXLabel testResult;

    private LLMMcpServer server;

    private final LLMMcpServerStore serverStore = LLMMcpServerStore.INSTANCE;

    private boolean testing;

    @FXML
    private void update() {
        LLMMcpServer server = this.readServer(this.server, true);
        if (server == null) {
            return;
        }
        try {
            if (this.serverStore.update(server)) {
                this.setProp("server", server);
                MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
                this.closeWindow();
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @FXML
    private void test() {
        if (this.testing) {
            return;
        }
        LLMMcpServer server = this.readServer(new LLMMcpServer(), false);
        if (server == null) {
            return;
        }
        this.testing = true;
        this.testGlyph.setDisable(true);
        this.testGlyph.startWaiting();
        LLMMcpUiUtil.showStatus(this.testResult,
                I18nResourceBundle.i18nString("shell.llm.mcp.testing"),
                null);
        ThreadUtil.startVirtual(() -> {
            try (McpClient client = LLMMcpClientFactory.createClient(server)) {
                int toolCount = client.listTools().size();
                FXUtil.runLater(() -> LLMMcpUiUtil.showStatus(this.testResult,
                        String.format(I18nResourceBundle.i18nString("shell.llm.mcp.testSuccess"), toolCount),
                        null));
            } catch (Exception ex) {
                String detail = ExceptionUtil.getMessage(ex);
                FXUtil.runLater(() -> LLMMcpUiUtil.showStatus(this.testResult,
                        String.format(I18nResourceBundle.i18nString("shell.llm.mcp.testFail"), detail),
                        detail));
            } finally {
                FXUtil.runLater(() -> {
                    this.testing = false;
                    this.testGlyph.stopWaiting();
                    this.testGlyph.setDisable(false);
                });
            }
        });
    }

    private LLMMcpServer readServer(LLMMcpServer server, boolean validateName) {
        if (server == null || validateName && !this.name.validate()) {
            return null;
        }
        LLMMcpTransport selectedTransport = this.transport.getSelectedItem();
        if (selectedTransport == null) {
            return null;
        }
        if (selectedTransport == LLMMcpTransport.STDIO) {
            if (this.command.getText() == null || this.command.getText().isBlank()) {
                this.command.validate();
                return null;
            }
        } else if (!this.url.validate()) {
            return null;
        }
        int timeoutSeconds;
        try {
            timeoutSeconds = Integer.parseInt(this.timeout.getTextTrim());
        } catch (Exception ex) {
            MessageBox.warn(I18nResourceBundle.i18nString("shell.llm.mcp.timeoutInvalid"), this.getWindow());
            return null;
        }
        if (timeoutSeconds < 1 || timeoutSeconds > 3600) {
            MessageBox.warn(I18nResourceBundle.i18nString("shell.llm.mcp.timeoutInvalid"), this.getWindow());
            return null;
        }
        server.setName(this.name.getTextTrim());
        server.setTransport(selectedTransport);
        server.setCommandText(this.command.getText());
        server.setWorkingDirectory(this.workingDirectory.getTextTrim());
        server.setEnvironmentText(this.environment.getText());
        server.setUrl(this.url.getTextTrim());
        server.setHeadersText(this.headers.getText());
        server.setToolNamesText(this.toolNames.getText());
        server.setTimeoutSeconds(timeoutSeconds);
        try {
            LLMMcpClientFactory.validateConfiguration(server);
        } catch (Exception ex) {
            MessageBox.warn(ExceptionUtil.getMessage(ex), this.getWindow());
            return null;
        }
        return server;
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.server = this.getProp("server");
        this.transport.getItems().setAll(LLMMcpTransport.values());
        this.name.setText(this.server.getName());
        this.transport.getSelectionModel().select(this.server.getTransport());
        this.command.setText(this.server.getCommandText());
        this.workingDirectory.setText(this.server.getWorkingDirectory());
        this.environment.setText(this.server.getEnvironmentText());
        this.url.setText(this.server.getUrl());
        this.headers.setText(this.server.getHeadersText());
        this.toolNames.setText(this.server.getToolNamesText());
        this.timeout.setText(String.valueOf(this.server.getTimeoutSeconds()));
        this.updateTransportFields();
        LLMMcpUiUtil.clearStatus(this.testResult);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.transport.selectedItemChanged((observable, oldValue, newValue) -> this.updateTransportFields());
    }

    private void updateTransportFields() {
        boolean stdio = this.transport.getSelectedItem() == LLMMcpTransport.STDIO;
        this.stdioBox.setVisible(stdio);
        this.stdioBox.setManaged(stdio);
        this.remoteBox.setVisible(!stdio);
        this.remoteBox.setManaged(!stdio);
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.llm.mcp.edit");
    }
}
