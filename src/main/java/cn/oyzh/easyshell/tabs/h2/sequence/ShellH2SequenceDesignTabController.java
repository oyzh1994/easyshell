package cn.oyzh.easyshell.tabs.h2.sequence;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.h2.generator.sequence.H2SequenceSqlGenerator;
import cn.oyzh.easyshell.h2.sequence.H2Sequence;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.trees.h2.sequence.ShellH2SequencesTreeItem;
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

import java.util.List;

/**
 * H2 序列设计器控制器。
 *
 * @since 2026-11-28
 */
public class ShellH2SequenceDesignTabController extends RichTabController {

    @FXML
    private ClearableTextField startValue;

    @FXML
    private ClearableTextField minimumValue;

    @FXML
    private ClearableTextField maximumValue;

    @FXML
    private ClearableTextField increment;

    @FXML
    private ClearableTextField cacheSize;

    @FXML
    private FXCheckBox cycle;

    @FXML
    private ShellDataEditor preview;

    private ShellH2SequencesTreeItem group;

    private H2Sequence sequence;

    private String originalName;

    private String pendingName;

    private boolean newData;

    private boolean unsaved;

    private boolean initiating;

    public void init(H2Sequence sequence, ShellH2SequencesTreeItem group) {
        this.group = group;
        this.sequence = sequence == null ? new H2Sequence() : sequence;
        this.sequence.setSchema(group.schema());
        this.newData = StringUtil.isBlank(this.sequence.getName());
        this.originalName = this.newData ? null : this.sequence.getName();
        this.pendingName = this.originalName;
        this.initiating = true;
        StageManager.showMask(this.getWindow(), this::initInfo);
    }

    private void initInfo() {
        try {
            if (!this.newData && this.originalName != null) {
                H2Sequence current = this.group.client().selectSequence(
                        this.group.schema(), this.originalName);
                if (current != null) {
                    this.sequence = current;
                }
            }
            String currentValue = this.newData ? "1" : StringUtil.blankToDefault(
                    this.sequence.getCurrentValue(), this.sequence.getStartValue());
            this.startValue.setText(StringUtil.blankToDefault(currentValue, ""));
            this.minimumValue.setText(StringUtil.blankToDefault(
                    this.sequence.getMinimumValue(), ""));
            this.maximumValue.setText(StringUtil.blankToDefault(
                    this.sequence.getMaximumValue(), ""));
            this.increment.setText(StringUtil.blankToDefault(
                    this.sequence.getIncrement(), this.newData ? "1" : ""));
            this.cacheSize.setText(StringUtil.blankToDefault(
                    this.sequence.getCacheSize(), ""));
            this.cycle.setSelected(this.sequence.isCycle());
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
        this.startValue.addTextChangeListener((observable, oldValue, newValue) -> this.changed());
        this.minimumValue.addTextChangeListener((observable, oldValue, newValue) -> this.changed());
        this.maximumValue.addTextChangeListener((observable, oldValue, newValue) -> this.changed());
        this.increment.addTextChangeListener((observable, oldValue, newValue) -> this.changed());
        this.cacheSize.addTextChangeListener((observable, oldValue, newValue) -> this.changed());
        this.cycle.selectedChanged((observable, oldValue, newValue) -> this.changed());
        NodeUtil.nodeOnCtrlS(this.getTab() == null ? null : this.getTab().getContent(), this::save);
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
            String targetName = this.originalName;
            if (this.newData) {
                targetName = MessageBox.prompt(I18nHelper.pleaseInputSequenceName(),
                        this.pendingName, this.getWindow());
                if (targetName == null) {
                    return;
                }
                targetName = targetName.trim();
                if (StringUtil.isBlank(targetName)) {
                    MessageBox.warn(I18nHelper.pleaseInputSequenceName(), this.getWindow());
                    return;
                }
                this.pendingName = targetName;
                if (this.group.client().selectSequence(this.group.schema(), targetName) != null) {
                    MessageBox.warn(ShellI18nHelper.alreadyExists(targetName), this.getWindow());
                    return;
                }
            }
            H2Sequence target = this.targetData(targetName);
            H2SequenceSqlGenerator.validate(target, this.newData ? null : this.sequence);
            H2Sequence saved = this.newData
                    ? this.group.client().createSequence(target)
                    : this.group.client().alterSequence(this.originalName, target);
            this.sequence = saved;
            this.originalName = targetName;
            this.pendingName = targetName;
            this.newData = false;
            this.unsaved = false;
            this.group.reloadChild();
            this.initInfo();
        } catch (IllegalArgumentException ex) {
            MessageBox.warn(ex.getMessage(), this.getWindow());
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.flushTab();
        }
    }

    @FXML
    private void refresh() {
        if (this.newData) {
            this.init(new H2Sequence(), this.group);
            return;
        }
        H2Sequence current = this.group.client().selectSequence(
                this.group.schema(), this.originalName);
        if (current == null) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        this.init(current, this.group);
    }

    private void initPreview() {
        String name = StringUtil.blankToDefault(this.originalName,
                StringUtil.blankToDefault(this.pendingName, "UNNAMED_SEQUENCE"));
        H2Sequence target = this.targetData(name);
        if (this.newData) {
            this.preview.text(H2SequenceSqlGenerator.create(target) + ";");
        } else {
            String original = this.originalName;
            List<String> scripts = H2SequenceSqlGenerator.alter(original, this.sequence, target);
            this.preview.text(scripts.isEmpty() ? "-- No changes" : String.join(";\n", scripts) + ";");
        }
    }

    private H2Sequence targetData(String name) {
        H2Sequence target = new H2Sequence();
        target.setSchema(this.group.schema());
        target.setName(name);
        target.setStartValue(this.startValue.getTextTrim());
        target.setMinimumValue(this.minimumValue.getTextTrim());
        target.setMaximumValue(this.maximumValue.getTextTrim());
        target.setIncrement(this.increment.getTextTrim());
        target.setCacheSize(this.cacheSize.getTextTrim());
        target.setCurrentValue(this.startValue.getTextTrim());
        target.setCycle(this.cycle.isSelected());
        return target;
    }

    public String sequenceName() {
        return StringUtil.blankToDefault(this.originalName, this.pendingName);
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
