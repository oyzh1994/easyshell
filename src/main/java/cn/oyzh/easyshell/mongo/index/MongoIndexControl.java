package cn.oyzh.easyshell.mongo.index;

import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;
import org.bson.Document;

/**
 * MongoDB 索引编辑控件。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class MongoIndexControl extends MongoIndex {

    /**
     * 索引字段编辑文本
     */
    private String keysText;

    /**
     * 部分过滤表达式编辑文本
     */
    private String partialFilterExpressionText;

    /**
     * 获取索引字段编辑文本。
     *
     * @return 索引字段编辑文本
     */
    public String getKeysText() {
        return this.keysText == null ? super.keyText() : this.keysText;
    }

    /**
     * 设置索引字段编辑文本。
     *
     * @param keysText 索引字段编辑文本
     */
    public void setKeysText(String keysText) {
        this.keysText = keysText;
        super.setChanged(true);
        try {
            Document keys = MongoIndex.parseKeys(keysText);
            if (!MongoIndex.isEmptyKeys(keys)) {
                super.setKeys(keys);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取部分过滤表达式编辑文本。
     *
     * @return 部分过滤表达式编辑文本
     */
    public String getPartialFilterExpressionText() {
        return this.partialFilterExpressionText == null
                ? super.partialFilterText() : this.partialFilterExpressionText;
    }

    /**
     * 设置部分过滤表达式编辑文本。
     *
     * @param text 部分过滤表达式编辑文本
     */
    public void setPartialFilterExpressionText(String text) {
        this.partialFilterExpressionText = text;
        super.setChanged(true);
        try {
            Document document = MongoIndex.parseDocument(text);
            super.setPartialFilterExpression(document);
        } catch (Exception ignored) {
        }
    }

    /**
     * 解析当前索引字段。
     *
     * @return 索引字段
     */
    public Document resolveKeys() {
        Document keys = MongoIndex.parseKeys(this.getKeysText());
        if (MongoIndex.isEmptyKeys(keys)) {
            throw new IllegalArgumentException("Index keys can not be empty");
        }
        super.setKeys(keys);
        return keys;
    }

    /**
     * 解析当前部分过滤表达式。
     *
     * @return 部分过滤表达式
     */
    public Document resolvePartialFilterExpression() {
        Document document = MongoIndex.parseDocument(this.getPartialFilterExpressionText());
        super.setPartialFilterExpression(document);
        return document;
    }

    /**
     * 转换为只包含设计数据的索引对象。
     *
     * @return 索引对象
     */
    public MongoIndex toIndex() {
        if (this.getExpireAfterSeconds() != null && this.getExpireAfterSeconds() < 0) {
            throw new IllegalArgumentException("TTL can not be negative");
        }
        MongoIndex index = new MongoIndex();
        index.setName(this.getName());
        index.setKeys(this.resolveKeys());
        index.setUnique(this.isUnique());
        index.setSparse(this.isSparse());
        index.setBackground(this.isBackground());
        index.setHidden(this.isHidden());
        index.setExpireAfterSeconds(this.getExpireAfterSeconds());
        index.setPartialFilterExpression(this.resolvePartialFilterExpression());
        return index;
    }

    /**
     * 校验索引编辑文本。
     */
    public void validateText() {
        Document keys = MongoIndex.parseKeys(this.getKeysText());
        if (MongoIndex.isEmptyKeys(keys)) {
            throw new IllegalArgumentException("Index keys can not be empty");
        }
        MongoIndex.parseDocument(this.getPartialFilterExpressionText());
        if (this.getExpireAfterSeconds() != null && this.getExpireAfterSeconds() < 0) {
            throw new IllegalArgumentException("TTL can not be negative");
        }
    }

    public ClearableTextField getNameControl() {
        ClearableTextField textField = new ClearableTextField();
        if (cn.oyzh.common.util.StringUtil.isBlank(this.getName())) {
            this.setName(DBUtil.genIndexName());
        }
        textField.setText(this.getName());
        textField.setPromptText(I18nHelper.pleaseInputName());
        textField.setEditable(!this.isIdIndex());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setName(newValue));
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    public ClearableTextField getKeysControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setFlexWidth("100% - 12");
        textField.setPromptText("field:1, other:-1");
        textField.setText(this.getKeysText());
        textField.setEditable(!this.isIdIndex());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setKeysText(newValue));
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    public FXCheckBox getUniqueControl() {
        FXCheckBox checkBox = new FXCheckBox(this.isUnique());
        checkBox.setDisable(this.isIdIndex());
        checkBox.selectedChanged((observable, oldValue, newValue) -> this.setUnique(newValue));
        TableViewUtil.rowOnCtrlS(checkBox);
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    public FXCheckBox getSparseControl() {
        FXCheckBox checkBox = new FXCheckBox(this.isSparse());
        checkBox.setDisable(this.isIdIndex());
        checkBox.selectedChanged((observable, oldValue, newValue) -> this.setSparse(newValue));
        TableViewUtil.rowOnCtrlS(checkBox);
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    public FXCheckBox getBackgroundControl() {
        FXCheckBox checkBox = new FXCheckBox(this.isBackground());
        checkBox.setDisable(this.isIdIndex());
        checkBox.selectedChanged((observable, oldValue, newValue) -> this.setBackground(newValue));
        TableViewUtil.rowOnCtrlS(checkBox);
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    public FXCheckBox getHiddenControl() {
        FXCheckBox checkBox = new FXCheckBox(this.isHidden());
        checkBox.setDisable(this.isIdIndex());
        checkBox.selectedChanged((observable, oldValue, newValue) -> this.setHidden(newValue));
        TableViewUtil.rowOnCtrlS(checkBox);
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    public NumberTextField getExpireAfterSecondsControl() {
        NumberTextField textField = new NumberTextField();
        textField.setFlexWidth("100% - 12");
        textField.setValue(this.getExpireAfterSeconds());
        textField.setDisable(this.isIdIndex());
        textField.addTextChangeListener((observable, oldValue, newValue) ->
                this.setExpireAfterSeconds(textField.getLongValue()));
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    public ClearableTextField getPartialFilterExpressionControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setFlexWidth("100% - 12");
        textField.setPromptText("{\"status\":\"A\"}");
        textField.setText(this.getPartialFilterExpressionText());
        textField.setEditable(!this.isIdIndex());
        textField.addTextChangeListener((observable, oldValue, newValue) ->
                this.setPartialFilterExpressionText(newValue));
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 将索引转换为索引编辑控件。
     *
     * @param index 索引
     * @return 索引编辑控件
     */
    public static MongoIndexControl of(MongoIndex index) {
        MongoIndexControl control = new MongoIndexControl();
        control.copy(index);
        return control;
    }
}
