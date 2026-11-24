package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.fx.llm.LLMSessionTableView;
import cn.oyzh.easyshell.llm.session.LLMSession;
import cn.oyzh.easyshell.llm.session.LLMSessionUtil;
import cn.oyzh.easyshell.store.llm.LLMSessionStore;
import cn.oyzh.easyshell.util.llm.ShellLLMListSearchUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.TableRow;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;

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

    @FXML
    private ClearableTextField searchField;

    private final LLMSessionStore sessionStore = LLMSessionStore.INSTANCE;

    private List<LLMSession> allItems = new ArrayList<>();

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
            this.applyFilter();
        } else {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
        }
    }

    @FXML
    private void delete() {
        List<LLMSession> sessions = new ArrayList<>(this.sessionTableView.getSelectedItems());
        if (CollectionUtil.isEmpty(sessions)) {
            return;
        }
        String message;
        if (sessions.size() == 1) {
            LLMSession session = sessions.getFirst();
            message = I18nHelper.delete() + " " + LLMSessionUtil.getDisplayTitle(session) + " ?";
        } else {
            message = I18nHelper.deleteSession() + " ?";
        }
        if (MessageBox.confirm(message, this.getWindow())) {
            for (LLMSession session : sessions) {
                if (this.sessionStore.delete(session)) {
                    this.allItems.remove(session);
                    this.applyFilter();
                } else {
                    MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
                }
            }
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> this.applyFilter());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.sessionTableView.setEditAction(this::update);
        this.sessionTableView.setDeleteAction(this::delete);
        this.sessionTableView.setRestoreAction(this::restore);
        this.allItems = new ArrayList<>(this.sessionStore.loadRecent());
        this.applyFilter();
        this.initRowActions();
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    private void applyFilter() {
        this.sessionTableView.setItem(ShellLLMListSearchUtil.filter(
                this.allItems,
                this.searchField == null ? null : this.searchField.getText(),
                LLMSessionUtil::getDisplayTitle));
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
