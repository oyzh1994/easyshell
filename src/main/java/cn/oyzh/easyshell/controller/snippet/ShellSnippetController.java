package cn.oyzh.easyshell.controller.snippet;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSnippet;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.snippet.ShellSnippetEditor;
import cn.oyzh.easyshell.store.ShellSnippetStore;
import cn.oyzh.easyshell.trees.snippet.ShellSnippetTreeItem;
import cn.oyzh.easyshell.trees.snippet.ShellSnippetTreeView;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 片段业务
 *
 * @author oyzh
 * @since 2025/06/11
 */
@StageAttribute(
        modality = Modality.NONE,
        value = FXConst.FXML_PATH + "snippet/shellSnippet.fxml"
)
public class ShellSnippetController extends StageController {

    /**
     * 发送到所有
     */
    @FXML
    private FXCheckBox sendAll;

    /**
     * 发送后清除
     */
    @FXML
    private FXCheckBox sendClear;

    /**
     * 发送时添加换行符
     */
    @FXML
    private FXCheckBox sendLine;

    /**
     * 片段内容
     */
    @FXML
    private ShellSnippetEditor content;

    /**
     * 片段列表
     */
    @FXML
    private ShellSnippetTreeView snippetTreeView;

    /**
     * 片段存储
     */
    private final ShellSnippetStore snippetStore = ShellSnippetStore.INSTANCE;

    /**
     * 当前片段
     */
    private ShellSnippet snippet;

    @Override
    protected void bindListeners() {
        super.bindListeners();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.snippet();
    }

    /**
     * 运行片段
     */
    @FXML
    private void run() {
        String content = this.content.getText();
        if (StringUtil.isBlank(content)) {
            return;
        }
        // 追加换行符
        if (this.sendLine.isSelected()) {
            if (!content.endsWith("\n") && !content.endsWith("\r")) {
                content = content.concat("\r");
            }
        }
        content = content.replace("\\t", "\t");
        content = content.replace("\\n", "\n");
        content = content.replace("\\r", "\r");
        content = content.replace("\\b", "\b");
        // 执行片段
        ShellEventUtil.runSnippet(content, this.sendAll.isSelected());
        // 清除内容
        if (this.sendClear.isSelected()) {
            this.content.clear();
            this.snippet = null;
        }
    }

    /**
     * 保存片段
     */
    @FXML
    private void save() {
        if (this.snippet == null) {
            this.snippet = new ShellSnippet();
        }
        String name = this.snippet.getName();
        if (StringUtil.isBlank(name)) {
            name = MessageBox.prompt(I18nHelper.pleaseInputName());
            if (StringUtil.isBlank(name)) {
                return;
            }
        }
        this.snippet.setName(name);
        this.snippet.setContent(this.content.getText());
        boolean isNew = this.snippet.getId() == null;
        if (this.snippetStore.replace(this.snippet) && isNew) {
            this.snippetTreeView.addSnippet(this.snippet);
        }
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        // 片段选择事件
        this.snippetTreeView.selectedItemChanged((ChangeListener<TreeItem<?>>) (observableValue, snippet, t1) -> {
            if (t1 instanceof ShellSnippetTreeItem item) {
                this.doEdit(item.value());
            } else {
                this.doEdit(null);
            }
        });
        // 片段编辑回调
        this.snippetTreeView.setEditCallback(this::doEdit);
        // 片段删除回调
        this.snippetTreeView.setDeleteCallback(this::doDelete);
        // 片段新增回调
        this.snippetTreeView.setAddCallback(() -> this.doEdit(null));
    }

    /**
     * 编辑片段
     *
     * @param snippet 片段
     */
    private void doEdit(ShellSnippet snippet) {
        this.snippet = snippet;
        if (snippet == null) {
            this.content.clear();
            this.stage.restoreTitle();
        } else {
            this.content.setText(snippet.getContent());
            this.stage.appendTitle("-" + snippet.getName());
        }
    }

    /**
     * 删除片段
     *
     * @param snippet 片段
     */
    private void doDelete(ShellSnippet snippet) {
        if (snippet == this.snippet) {
            this.snippet = null;
            this.content.clear();
            this.stage.restoreTitle();
        }
    }

    /**
     * 内容按键事件
     *
     * @param e 事件
     */
    @FXML
    private void contentKeyPressed(KeyEvent e) {
        if (KeyboardUtil.isCtrlS(e)) {
            this.save();
        } else if (KeyboardUtil.isCtrlR(e)) {
            this.run();
        }
    }
}
