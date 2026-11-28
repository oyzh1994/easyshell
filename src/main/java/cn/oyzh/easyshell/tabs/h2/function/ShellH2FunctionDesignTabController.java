package cn.oyzh.easyshell.tabs.h2.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.h2.generator.function.H2FunctionSqlGenerator;
import cn.oyzh.easyshell.h2.routine.H2Routine;
import cn.oyzh.easyshell.query.h2.ShellH2QueryEditor;
import cn.oyzh.easyshell.trees.h2.function.H2RoutineKind;
import cn.oyzh.easyshell.trees.h2.function.ShellH2RoutinesTreeItem;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * H2 函数设计器控制器。
 *
 * @since 2026-11-28
 */
public class ShellH2FunctionDesignTabController extends RichTabController {

    @FXML
    private ShellH2QueryEditor source;

    @FXML
    private ClearableTextField method;

    @FXML
    private FXCheckBox deterministic;

    @FXML
    private ShellDataEditor preview;

    private H2Routine function;

    private ShellH2RoutinesTreeItem group;

    private String originalName;

    private boolean newData;

    private boolean unsaved;

    private boolean initiating;

    public void init(H2Routine function, ShellH2RoutinesTreeItem group) {
        this.group = group;
        this.function = function == null ? new H2Routine() : function;
        this.function.setSchema(group.schema());
        this.function.setType(group.kind().name());
        this.newData = StringUtil.isBlank(this.function.getName());
        this.originalName = this.newData ? null : this.function.getName();
        this.initiating = true;
        StageManager.showMask(this.getWindow(), this::initInfo);
    }

    private void initInfo() {
        try {
            if (!this.newData && this.originalName != null) {
                H2Routine current = this.group.client().selectRoutine(
                        this.group.schema(), this.originalName);
                if (current != null) {
                    this.function = current;
                }
            }
            this.source.text(this.newData && StringUtil.isBlank(this.function.getDefinition())
                    ? (this.isProcedure()
                    ? """
                      void execute(String value) {
                          System.out.println(value);
                      }
                      """
                    : """
                      int evaluate(int value) {
                          return value;
                      }
                      """)
                    : StringUtil.blankToDefault(this.function.getDefinition(), ""));
            this.source.forgetHistory();
            this.method.setText(StringUtil.blankToDefault(this.function.getExternalName(), ""));
            this.deterministic.setSelected(this.function.isDeterministic());
            this.unsaved = this.newData;
            this.initPreview();
            this.flushTab();
        } finally {
            FXUtil.runPulse(() -> this.initiating = false);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.source.textProperty().addListener((observable, oldValue, newValue) -> this.changed());
        this.method.addTextChangeListener((observable, oldValue, newValue) -> this.changed());
        this.deterministic.selectedChanged((observable, oldValue, newValue) -> this.changed());
        NodeUtil.nodeOnCtrlS(this.getTab() == null ? null : this.getTab().getContent(), this::save);
        NodeUtil.nodeOnCtrlS(this.source, this::save);
        NodeUtil.nodeOnCtrlS(this.method, this::save);
    }

    private void changed() {
        if (!this.initiating) {
            this.unsaved = true;
            this.initPreview();
            this.flushTab();
        }
    }

    @FXML
    private void save() {
        StageManager.showMask(this.getWindow(), this::doSave);
    }

    private void doSave() {
        try {
            String definition = this.source.getText().strip();
            String externalName = this.method.getTextTrim();
            if (StringUtil.isBlank(definition) && StringUtil.isBlank(externalName)) {
                MessageBox.warn("Java source or external method is required", this.getWindow());
                return;
            }
            if (StringUtil.isNotBlank(definition) && StringUtil.isNotBlank(externalName)) {
                MessageBox.warn("Java source and external method cannot both be set", this.getWindow());
                return;
            }
            String targetName = this.originalName;
            if (this.newData) {
                targetName = MessageBox.prompt(this.isProcedure()
                                ? I18nHelper.pleaseInputProcedureName()
                                : I18nHelper.pleaseInputFunctionName(),
                        targetName, this.getWindow());
                if (targetName == null) {
                    return;
                }
                targetName = targetName.trim();
                if (StringUtil.isBlank(targetName)) {
                    MessageBox.warn(this.isProcedure()
                            ? I18nHelper.pleaseInputProcedureName()
                            : I18nHelper.pleaseInputFunctionName(), this.getWindow());
                    return;
                }
                if (this.group.client().selectRoutine(this.group.schema(), targetName) != null) {
                    MessageBox.warn(ShellI18nHelper.alreadyExists(targetName), this.getWindow());
                    return;
                }
            }
            this.originalName = targetName;
            H2Routine target = new H2Routine();
            target.setSchema(this.group.schema());
            target.setName(targetName);
            target.setType(this.group.kind().name());
            target.setDefinition(StringUtil.blankToDefault(definition, null));
            target.setExternalName(StringUtil.blankToDefault(externalName, null));
            target.setDeterministic(this.deterministic.isSelected());
            H2Routine saved = this.newData
                    ? this.group.client().createRoutine(target)
                    : this.group.client().alterRoutine(this.originalName, target);
            this.function = saved;
            this.originalName = targetName;
            this.newData = false;
            this.unsaved = false;
            this.group.reloadChild();
            this.initInfo();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.flushTab();
        }
    }

    @FXML
    private void refresh() {
        if (!MessageBox.confirm(ShellI18nHelper.confirmRefreshData(), this.getWindow())) {
            return;
        }
        if (this.newData) {
            this.init(new H2Routine(), this.group);
            return;
        }
        H2Routine current = this.group.client().selectRoutine(
                this.group.schema(), this.originalName);
        if (current == null) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        this.init(current, this.group);
    }

    private void initPreview() {
        H2Routine target = this.targetData(
                StringUtil.blankToDefault(this.originalName, "UNNAMED_FUNCTION"));
        this.preview.text(H2FunctionSqlGenerator.create(target) + ";");
    }

    private H2Routine targetData(String name) {
        H2Routine target = new H2Routine();
        target.setSchema(this.group.schema());
        target.setName(name);
        target.setType(this.group.kind().name());
        target.setDefinition(StringUtil.blankToDefault(this.source.getText().strip(), null));
        target.setExternalName(StringUtil.blankToDefault(this.method.getTextTrim(), null));
        target.setDeterministic(this.deterministic.isSelected());
        return target;
    }

    public String functionName() {
        return StringUtil.blankToDefault(this.originalName,
                this.function == null ? null : this.function.getName());
    }

    public String originalName() {
        return this.originalName;
    }

    public boolean isNewData() {
        return this.newData;
    }

    public boolean isUnsaved() {
        return this.unsaved;
    }

    public String schema() {
        return this.group.schema();
    }

    public H2Routine getFunction() {
        return this.function;
    }

    public boolean isProcedure() {
        return this.group.kind() == H2RoutineKind.PROCEDURE;
    }

    public ShellH2RootTreeItem getDbItem() {
        return this.group.parent().parent();
    }

    @Override
    public void destroy() {
        if (this.preview != null) {
            this.preview.destroy();
        }
        super.destroy();
    }
}
