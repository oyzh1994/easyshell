package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.fx.llm.LLMMcpServerTableView;
import cn.oyzh.easyshell.fx.llm.LLMMcpUiUtil;
import cn.oyzh.easyshell.fx.llm.LLMMcpServerEnabledTableColumn;
import cn.oyzh.easyshell.llm.mcp.LLMMcpClientFactory;
import cn.oyzh.easyshell.llm.mcp.LLMMcpConnectionState;
import cn.oyzh.easyshell.llm.mcp.LLMMcpServer;
import cn.oyzh.easyshell.store.llm.LLMMcpServerStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.svg.glyph.llm.TestSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.RefreshSVGGlyph;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    private RefreshSVGGlyph refreshGlyph;

    @FXML
    private LLMMcpServerEnabledTableColumn enabledColumn;

    @FXML
    private FXLabel testResult;

    private final LLMMcpServerStore serverStore = LLMMcpServerStore.INSTANCE;

    private boolean testing;

    private boolean refreshing;

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
            if (server.isEnabled()) {
                this.changeEnabled(server, true);
            }
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
            server.setConnectionState(LLMMcpConnectionState.UNKNOWN);
            this.serverTableView.refreshRow(server);
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
        server.setChecking(true);
        server.setConnectionState(LLMMcpConnectionState.CHECKING);
        this.serverTableView.refreshRow(server);
        LLMMcpUiUtil.showStatus(this.testResult,
                I18nResourceBundle.i18nString("shell.llm.mcp.testing"),
                null);
        ThreadUtil.startVirtual(() -> {
            try (McpClient client = LLMMcpClientFactory.createClient(server)) {
                int toolCount = client.listTools().size();
                FXUtil.runLater(() -> {
                    server.setConnectionState(LLMMcpConnectionState.CONNECTED, toolCount, null);
                    this.serverTableView.refreshRow(server);
                    LLMMcpUiUtil.showStatus(this.testResult,
                            String.format(I18nResourceBundle.i18nString("shell.llm.mcp.testSuccess"), toolCount),
                            null);
                });
            } catch (Exception ex) {
                String detail = ExceptionUtil.getMessage(ex);
                FXUtil.runLater(() -> {
                    server.setConnectionState(LLMMcpConnectionState.FAILED, 0, detail);
                    this.serverTableView.refreshRow(server);
                    LLMMcpUiUtil.showStatus(this.testResult,
                            String.format(I18nResourceBundle.i18nString("shell.llm.mcp.testFail"), detail),
                            detail);
                });
            } finally {
                FXUtil.runLater(() -> {
                    this.testing = false;
                    server.setChecking(false);
                    this.testGlyph.stopWaiting();
                    this.testGlyph.setDisable(false);
                    this.serverTableView.refreshRow(server);
                });
            }
        });
    }

    @FXML
    private void refreshAll() {
        if (this.refreshing) {
            return;
        }
        List<LLMMcpServer> servers = this.serverTableView.getItems().stream()
                .filter(LLMMcpServer::isEnabled)
                .toList();
        if (servers.isEmpty()) {
            return;
        }
        this.refreshing = true;
        this.refreshGlyph.setDisable(true);
        this.refreshGlyph.startWaiting();
        this.enabledColumn.setAllDisabled(true);
        for (LLMMcpServer server : this.serverTableView.getItems()) {
            if (server.isEnabled()) {
                server.setChecking(true);
                server.setConnectionState(LLMMcpConnectionState.CHECKING);
            }
            this.serverTableView.refreshRow(server);
        }
        LLMMcpUiUtil.showStatus(this.testResult,
                String.format(I18nResourceBundle.i18nString("shell.llm.mcp.progress.start"),
                        servers.size()),
                null);
        ThreadUtil.startVirtual(() -> this.refreshAllAsync(servers));
    }

    private void refreshAllAsync(List<LLMMcpServer> servers) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CompletionService<ServerCheckResult> completion = new ExecutorCompletionService<>(executor);
        List<Future<ServerCheckResult>> futures = new ArrayList<>();
        for (LLMMcpServer server : servers) {
            futures.add(completion.submit(() -> this.checkServer(server)));
        }
        int connected = 0;
        try {
            for (int index = 0; index < servers.size(); index++) {
                ServerCheckResult result = completion.take().get();
                if (result.connected()) {
                    connected++;
                }
                int currentConnected = connected;
                FXUtil.runLater(() -> {
                    result.server().setConnectionState(
                            result.connected()
                                    ? LLMMcpConnectionState.CONNECTED
                                    : LLMMcpConnectionState.FAILED,
                            result.toolCount(), result.error());
                    this.serverTableView.refreshRow(result.server());
                    LLMMcpUiUtil.showStatus(this.testResult,
                            String.format(I18nResourceBundle.i18nString("shell.llm.mcp.refreshSummary"),
                                    currentConnected, servers.size()),
                            null);
                });
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            futures.forEach(future -> future.cancel(true));
        } catch (Exception ex) {
            futures.forEach(future -> future.cancel(true));
            JulLog.warn("refresh mcp server connections failed", ex);
        } finally {
            executor.shutdownNow();
            FXUtil.runLater(() -> {
                this.refreshing = false;
                for (LLMMcpServer server : this.serverTableView.getItems()) {
                    if (server.isEnabled()) {
                        server.setChecking(false);
                    }
                    this.serverTableView.refreshRow(server);
                }
                this.enabledColumn.setAllDisabled(false);
                this.refreshGlyph.stopWaiting();
                this.refreshGlyph.setDisable(false);
            });
        }
    }

    private ServerCheckResult checkServer(LLMMcpServer server) {
        try (McpClient client = LLMMcpClientFactory.createClient(server)) {
            return new ServerCheckResult(server, true, client.listTools().size(), null);
        } catch (Exception ex) {
            return new ServerCheckResult(server, false, 0, ExceptionUtil.getMessage(ex));
        }
    }

    private void changeEnabled(LLMMcpServer server, boolean enabled) {
        if (!enabled) {
            server.setEnabling(false);
            if (this.serverStore.setEnabled(server, false)) {
                server.setEnabled(false);
                server.setConnectionState(LLMMcpConnectionState.UNKNOWN);
                LLMMcpUiUtil.clearStatus(this.testResult);
            } else {
                server.setConnectionState(LLMMcpConnectionState.FAILED, 0, I18nHelper.operationFail());
                LLMMcpUiUtil.showStatus(this.testResult, I18nHelper.operationFail(), null);
            }
            this.serverTableView.refreshRow(server);
            return;
        }
        if (server.isEnabling()) {
            return;
        }
        boolean wasEnabled = server.isEnabled();
        if (!this.serverStore.setEnabled(server, true)) {
            server.setEnabled(wasEnabled);
            LLMMcpUiUtil.showStatus(this.testResult, I18nHelper.operationFail(), null);
            this.serverTableView.refreshRow(server);
            return;
        }
        server.setEnabled(true);
        server.setEnabling(true);
        server.setConnectionState(LLMMcpConnectionState.CHECKING);
        this.serverTableView.refreshRow(server);
        LLMMcpUiUtil.showStatus(this.testResult,
                I18nResourceBundle.i18nString("shell.llm.mcp.testing"), null);
        ThreadUtil.startVirtual(() -> {
            try (McpClient client = LLMMcpClientFactory.createClient(server)) {
                int toolCount = client.listTools().size();
                FXUtil.runLater(() -> {
                    server.setEnabling(false);
                    server.setEnabled(true);
                    server.setConnectionState(LLMMcpConnectionState.CONNECTED, toolCount, null);
                    LLMMcpUiUtil.showStatus(this.testResult,
                            String.format(I18nResourceBundle.i18nString("shell.llm.mcp.testSuccess"), toolCount),
                            null);
                    this.serverTableView.refreshRow(server);
                });
            } catch (Exception ex) {
                String detail = ExceptionUtil.getMessage(ex);
                FXUtil.runLater(() -> {
                    server.setEnabling(false);
                    server.setEnabled(true);
                    server.setConnectionState(LLMMcpConnectionState.FAILED, 0, detail);
                    this.serverTableView.refreshRow(server);
                    LLMMcpUiUtil.showStatus(this.testResult,
                            String.format(I18nResourceBundle.i18nString("shell.llm.mcp.testFail"), detail),
                            detail);
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
        this.enabledColumn.setEnabledAction(this::changeEnabled);
        this.serverTableView.setItem(this.serverStore.selectList());
        LLMMcpUiUtil.clearStatus(this.testResult);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        this.refreshAll();
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.llm.mcp.servers");
    }

    private record ServerCheckResult(LLMMcpServer server,
                                     boolean connected,
                                     int toolCount,
                                     String error) {
    }
}
