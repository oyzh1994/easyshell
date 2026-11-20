package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.fx.llm.LLMMcpServerTableView;
import cn.oyzh.easyshell.fx.llm.LLMMcpUiUtil;
import cn.oyzh.easyshell.llm.LLMMcpClientFactory;
import cn.oyzh.easyshell.llm.LLMMcpServer;
import cn.oyzh.easyshell.store.llm.LLMMcpServerStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.svg.glyph.llm.TestSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import dev.langchain4j.mcp.client.McpClient;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * MCP Server 列表。
 *
 * @author oyzh
 * @since 2026-11-20
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMMcpServerList.fxml"
)
public class ShellLLMMcpServerListController extends StageController {

    @FXML
    private LLMMcpServerTableView serverTableView;

    @FXML
    private TestSVGGlyph testGlyph;

    @FXML
    private FXLabel testResult;

    private final LLMMcpServerStore serverStore = LLMMcpServerStore.INSTANCE;

    private boolean testing;

    @FXML
    private void add() {
        StageAdapter adapter = ShellViewFactory.addLLMMcpServer(this.getWindow());
        if (adapter == null) {
            return;
        }
        LLMMcpServer server = adapter.getProp("server");
        if (server != null) {
            this.serverTableView.addItem(server);
            LLMMcpUiUtil.clearStatus(this.testResult);
        }
    }

    @FXML
    private void importConfig() {
        StageAdapter adapter = ShellViewFactory.importLLMMcpServers(this.getWindow());
        if (adapter == null || !Boolean.TRUE.equals(adapter.getProp("imported"))) {
            return;
        }
        this.serverTableView.setItem(this.serverStore.selectList());
        LLMMcpUiUtil.clearStatus(this.testResult);
    }

    @FXML
    private void update() {
        LLMMcpServer server = this.serverTableView.getSelectedItem();
        if (server == null) {
            return;
        }
        StageAdapter adapter = ShellViewFactory.updateLLMMcpServer(server, this.getWindow());
        if (adapter != null) {
            this.serverTableView.refresh();
            LLMMcpUiUtil.clearStatus(this.testResult);
        }
    }

    @FXML
    private void delete() {
        LLMMcpServer server = this.serverTableView.getSelectedItem();
        if (server == null) {
            return;
        }
        String message = String.format(
                I18nResourceBundle.i18nString("shell.llm.mcp.deleteConfirm"), server.getName());
        if (MessageBox.confirm(message, this.getWindow())) {
            if (this.serverStore.delete(server)) {
                this.serverTableView.removeItem(server);
                LLMMcpUiUtil.clearStatus(this.testResult);
                MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        }
    }

    @FXML
    private void test() {
        if (this.testing) {
            return;
        }
        LLMMcpServer server = this.serverTableView.getSelectedItem();
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

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.serverTableView.setAddAction(this::add);
        this.serverTableView.setTestAction(this::test);
        this.serverTableView.setEditAction(this::update);
        this.serverTableView.setDeleteAction(this::delete);
        this.serverTableView.setItem(this.serverStore.selectList());
        LLMMcpUiUtil.clearStatus(this.testResult);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.llm.mcp.servers");
    }
}
