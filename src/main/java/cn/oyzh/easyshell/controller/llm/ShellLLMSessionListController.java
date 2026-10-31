package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.easyshell.fx.llm.LLMSessionTableView;
import cn.oyzh.easyshell.llm.LLMSession;
import cn.oyzh.easyshell.llm.LLMSessionUtil;
import cn.oyzh.easyshell.store.llm.LLMSessionStore;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.TableRow;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * LLM会话历史
 *
 * @author oyzh
 * @since 2026-10-31
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMSessionList.fxml"
)
public class ShellLLMSessionListController extends StageController {

    @FXML
    private LLMSessionTableView sessionTableView;

    private final LLMSessionStore sessionStore = LLMSessionStore.INSTANCE;

    @FXML
    private void restore() {
        LLMSession session = this.sessionTableView.getSelectedItem();
        if (session == null) {
            return;
        }
        this.setProp("session", session);
        this.closeWindow();
    }

    @FXML
    private void update() {
        LLMSession session = this.sessionTableView.getSelectedItem();
        if (session == null) {
            return;
        }
        String title = MessageBox.prompt(I18nHelper.pleaseInputName(), LLMSessionUtil.getDisplayTitle(session), this.getWindow());
        if (title == null) {
            return;
        }
        title = title.trim();
        if (title.isEmpty()) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty(), this.getWindow());
            return;
        }
        session.setTitle(title);
        if (this.sessionStore.save(session)) {
            this.sessionTableView.refresh();
            MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
        } else {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
        }
    }

    @FXML
    private void delete() {
        LLMSession session = this.sessionTableView.getSelectedItem();
        if (session == null) {
            return;
        }
        String message = I18nResourceBundle.i18nString("base.delete") + " "
                + LLMSessionUtil.getDisplayTitle(session) + " ?";
        if (MessageBox.confirm(message, this.getWindow())) {
            if (this.sessionStore.delete(session)) {
                this.sessionTableView.removeItem(session);
                MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.sessionTableView.setEditAction(this::update);
        this.sessionTableView.setDeleteAction(this::delete);
        this.sessionTableView.setRestoreAction(this::restore);
        this.sessionTableView.setItem(this.sessionStore.loadRecent());
        this.initRowActions();
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    private void initRowActions() {
        this.sessionTableView.setRowFactory(tableView -> {
            TableRow<LLMSession> row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                if (!row.isEmpty() && mouseEvent.getClickCount() == 2) {
                    this.sessionTableView.select(row.getItem());
                    this.restore();
                }
            });
            return row;
        });
    }

    @Override
    public String getViewTitle() {
        return LLMSessionUtil.sessionHistory();
    }
}
