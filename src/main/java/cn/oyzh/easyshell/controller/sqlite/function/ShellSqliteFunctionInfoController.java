package cn.oyzh.easyshell.controller.sqlite.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.generator.function.SQLiteFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.trees.sqlite.function.ShellSqliteFunctionTreeItem;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 函数信息业务。
 *
 * @author oyzh
 * @since 2026-11-02
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "sqlite/function/shellSqliteFunctionInfo.fxml"
)
public class ShellSqliteFunctionInfoController extends StageController {

    @FXML
    private ReadOnlyTextField name;

    /**
     * 注释
     */
    @FXML
    private ReadOnlyTextArea comment;

    @FXML
    private ReadOnlyTextField kind;

    @FXML
    private ReadOnlyTextField argumentCount;

    /**
     * 参数数量区域
     */
    @FXML
    private FXVBox argumentCountBox;

    /**
     * 返回类型区域
     */
    @FXML
    private FXVBox returnTypeBox;

    /**
     * 目标表区域
     */
    @FXML
    private FXVBox targetTableBox;

    /**
     * 函数属性
     */
    @FXML
    private ReadOnlyTextField attributes;

    /**
     * 函数属性区域
     */
    @FXML
    private FXVBox attributesBox;

    /**
     * 返回类型
     */
    @FXML
    private ReadOnlyTextField returnType;

    /**
     * 目标表
     */
    @FXML
    private ReadOnlyTextField targetTable;

    @FXML
    private Editor definition;

    /**
     * DDL
     */
    @FXML
    private Editor createDefinition;

    /**
     * 函数节点
     */
    private ShellSqliteFunctionTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        SQLiteFunction function = this.treeItem.client().selectFunction(this.treeItem.functionName());
        if (function == null) {
            MessageBox.warn(I18nResourceBundle.i18nString(I18nHelper.NOT_FOUND), this.getWindow());
            this.stage.close();
            return;
        }
        this.name.setText(function.getName());
        this.kind.setText(function.getKind());
        this.argumentCount.setText(String.valueOf(function.getArgumentCount()));
        this.returnType.setText(StringUtil.blankToDefault(function.getReturnType(), ""));
        this.targetTable.setText(StringUtil.blankToDefault(function.getTargetTable(), ""));

        if (function.getArgumentCount() >= 0) {
            this.argumentCountBox.display();
        }
        if (StringUtil.isNotBlank(function.getReturnType())) {
            this.returnTypeBox.display();
        }
        if (StringUtil.isNotBlank(function.getTargetTable())) {
            this.targetTableBox.display();
        }
        List<String> attributeList = new ArrayList<>();
        if (function.isDeterministic()) {
            attributeList.add(I18nResourceBundle.i18nString("shell.sqlite.deterministic"));
        }
        if (function.isDirectOnly()) {
            attributeList.add(I18nResourceBundle.i18nString("shell.sqlite.directOnly"));
        }
        if (function.isFinalFunction()) {
            attributeList.add(I18nResourceBundle.i18nString("shell.sqlite.finalFunction"));
        }
        if (!attributeList.isEmpty()) {
            this.attributes.setText(String.join(", ", attributeList));
            this.attributesBox.display();
        }
        this.comment.setText("");
        this.definition.setText(StringUtil.blankToDefault(function.getExpression(), function.getDefinition()));
        if (!SQLiteFunction.KIND_BUILTIN.equals(function.getKind())) {
            this.createDefinition.setText(SQLiteFunctionCreateSqlGenerator.createFunction(function));
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.treeItem = this.getProp("item");
        StageManager.showMask(this.getWindow(), this::initInfo);
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.functionInfo();
    }

    @Override
    public void destroy() {
        this.definition.destroy();
        this.createDefinition.destroy();
        super.destroy();
    }
}
