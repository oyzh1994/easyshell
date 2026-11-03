package cn.oyzh.easyshell.sqlite.index;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.sqlite.table.ShellSqliteIndexFieldTextFiled;
import cn.oyzh.easyshell.fx.sqlite.table.ShellSqliteIndexMethodComboBox;
import cn.oyzh.easyshell.fx.sqlite.table.ShellSqliteIndexTypeComboBox;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.control.EditorEnlargeTextFiled;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * SQLite 索引设计组件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteIndexControl extends SQLiteIndex {

    private static final Pattern UNIQUE_INDEX = Pattern.compile(
            "(?is)^\\s*CREATE\\s+UNIQUE\\s+INDEX\\b");

    private List<? extends SQLiteColumn> columnList;

    public void setColumnList(List<? extends SQLiteColumn> columnList) {
        this.columnList = columnList;
    }

    public ClearableTextField getNameControl() {
        ClearableTextField field = new ClearableTextField();
        if (StringUtil.isEmpty(this.getName())) {
            this.setName(DBUtil.genIndexName());
        }
        field.setPromptText(I18nHelper.pleaseInputName());
        field.setText(this.getName());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setName(newValue);
            this.setDefinition(this.buildDefinition(this.getName(), this.getColumnDefinition(), this.getWhere()));
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public ShellSqliteIndexFieldTextFiled getColumnControl() {
        if (this.columnList == null) {
            this.columnList = new ArrayList<>();
        }
        ShellSqliteIndexFieldTextFiled field = new ShellSqliteIndexFieldTextFiled(
                this.columnList, this.getColumns());
        field.setFlexWidth("100% - 12");
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setDefinition(this.buildDefinition(this.getName(),
                    this.buildColumnDefinition(field.getColumns()), this.getWhere()));
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public Set<String> getColumns() {
        Set<String> columns = new HashSet<>();
        for (String item : this.splitColumns(this.getColumnDefinition())) {
            String column = this.parseColumnName(item);
            if (StringUtil.isNotBlank(column)) {
                columns.add(column);
            }
        }
        return columns;
    }

    @Override
    public boolean isInvalid() {
        return super.isInvalid() || StringUtil.isBlank(this.getColumnDefinition());
    }

    private String buildColumnDefinition(List<String> columns) {
        List<String> definitions = new ArrayList<>();
        for (String column : columns) {
            if (StringUtil.isBlank(column)) {
                continue;
            }
            if (this.columnList == null || this.columnList.stream()
                    .anyMatch(item -> StringUtil.equals(item.getName(), column))) {
                definitions.add(SQLiteGeneratorUtil.quote(column));
            } else {
                definitions.add(column);
            }
        }
        return String.join(", ", definitions);
    }

    private List<String> splitColumns(String columns) {
        List<String> list = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        char quote = 0;
        int depth = 0;
        for (int index = 0; index < columns.length(); index++) {
            char value = columns.charAt(index);
            if (quote != 0) {
                builder.append(value);
                if (value == quote) {
                    if (quote != ']' && index + 1 < columns.length() && columns.charAt(index + 1) == quote) {
                        builder.append(columns.charAt(++index));
                    } else {
                        quote = 0;
                    }
                }
                continue;
            }
            if (value == '\'' || value == '"' || value == '`') {
                quote = value;
                builder.append(value);
            } else if (value == '[') {
                quote = ']';
                builder.append(value);
            } else if (value == '(') {
                depth++;
                builder.append(value);
            } else if (value == ')') {
                depth--;
                builder.append(value);
            } else if (value == ',' && depth == 0) {
                list.add(builder.toString().trim());
                builder.setLength(0);
            } else {
                builder.append(value);
            }
        }
        if (StringUtil.isNotBlank(builder.toString())) {
            list.add(builder.toString().trim());
        }
        return list;
    }

    private String parseColumnName(String definition) {
        String value = StringUtil.blankToDefault(definition, "").trim();
        if (value.isEmpty()) {
            return "";
        }
        char first = value.charAt(0);
        if (first == '"' || first == '`') {
            int close = value.indexOf(first, 1);
            return close < 0 ? value : SQLiteGeneratorUtil.unquote(value.substring(0, close + 1));
        }
        if (first == '[') {
            int close = value.indexOf(']', 1);
            return close < 0 ? value : SQLiteGeneratorUtil.unquote(value.substring(0, close + 1));
        }
        int end = 0;
        while (end < value.length()) {
            char item = value.charAt(end);
            if (!Character.isLetterOrDigit(item) && item != '_' && item != '$') {
                break;
            }
            end++;
        }
        return end == 0 || (end < value.length() && value.charAt(end) == '(')
                ? value : value.substring(0, end);
    }

    public ShellSqliteIndexTypeComboBox getTypeControl() {
        ShellSqliteIndexTypeComboBox comboBox = new ShellSqliteIndexTypeComboBox();
        comboBox.selectFirstIfNull(this.getIndexType());
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.setDefinition(this.buildDefinition(this.getName(), this.getColumnDefinition(), this.getWhere(), newValue));
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public ShellSqliteIndexMethodComboBox getMethodControl() {
        ShellSqliteIndexMethodComboBox comboBox = new ShellSqliteIndexMethodComboBox();
        comboBox.selectFirstIfNull("BTREE");
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public ClearableTextField getWhereControl() {
        ClearableTextField field = new ClearableTextField();
        field.setPromptText(I18nHelper.pleaseInputContent());
        field.setFlexWidth("100% - 12");
        field.setText(this.getWhere());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setDefinition(this.buildDefinition(this.getName(), this.getColumnDefinition(), newValue));
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public EditorEnlargeTextFiled getDefinitionControl() {
        EditorEnlargeTextFiled field = new EditorEnlargeTextFiled();
        field.setFormatType(EditorFormatType.SQL);
        field.setPromptText(I18nHelper.pleaseInputContent());
        field.setText(this.getDefinition());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setDefinition(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public String getColumnDefinition() {
        String sql = StringUtil.blankToDefault(this.getDefinition(), "");
        int open = this.indexColumnStart(sql);
        if (open < 0) {
            return "";
        }
        int close = matchingParenthesis(sql, open);
        return close < 0 ? sql.substring(open + 1).trim()
                : sql.substring(open + 1, close).trim();
    }

    public String getWhere() {
        String sql = StringUtil.blankToDefault(this.getDefinition(), "");
        int open = this.indexColumnStart(sql);
        if (open < 0) {
            return "";
        }
        int close = matchingParenthesis(sql, open);
        if (close < 0) {
            return "";
        }
        int where = SQLiteGeneratorUtil.keywordIndex(sql, "WHERE", close + 1);
        return where < 0 ? ""
                : SQLiteGeneratorUtil.stripSemicolon(sql.substring(where + 5));
    }

    public String getIndexType() {
        return UNIQUE_INDEX.matcher(StringUtil.blankToDefault(this.getDefinition(), "")).find()
                ? "UNIQUE" : "NORMAL";
    }

    private String buildDefinition(String name, String columns, String where) {
        return this.buildDefinition(name, columns, where, this.getIndexType());
    }

    private String buildDefinition(String name, String columns, String where, String type) {
        StringBuilder builder = new StringBuilder("CREATE ");
        if ("UNIQUE".equalsIgnoreCase(type)) {
            builder.append("UNIQUE ");
        }
        builder.append("INDEX ")
                .append(SQLiteGeneratorUtil.quote(name))
                .append(" ON ")
                .append(SQLiteGeneratorUtil.quote(this.getTableName()))
                .append(" (")
                .append(StringUtil.blankToDefault(columns, "").trim())
                .append(')');
        if (StringUtil.isNotBlank(where)) {
            builder.append(" WHERE ").append(where.trim());
        }
        return builder.append(';').toString();
    }

    private int indexColumnStart(String sql) {
        int index = SQLiteGeneratorUtil.keywordIndex(sql, "INDEX", 0);
        if (index < 0) {
            return -1;
        }
        int cursor = skipWhitespace(sql, index + "INDEX".length());
        if (matchesKeyword(sql, cursor, "IF")) {
            cursor = skipWhitespace(sql, cursor + "IF".length());
            if (matchesKeyword(sql, cursor, "NOT")) {
                cursor = skipWhitespace(sql, cursor + "NOT".length());
                if (matchesKeyword(sql, cursor, "EXISTS")) {
                    cursor = skipWhitespace(sql, cursor + "EXISTS".length());
                }
            }
        }
        cursor = skipIdentifier(sql, cursor);
        cursor = skipWhitespace(sql, cursor);
        if (cursor < sql.length() && sql.charAt(cursor) == '.') {
            cursor = skipWhitespace(sql, cursor + 1);
            cursor = skipIdentifier(sql, cursor);
            cursor = skipWhitespace(sql, cursor);
        }
        int on = SQLiteGeneratorUtil.keywordIndex(sql, "ON", cursor);
        if (on < 0) {
            return -1;
        }
        cursor = skipIdentifier(sql, on + 2);
        cursor = skipWhitespace(sql, cursor);
        if (cursor < sql.length() && sql.charAt(cursor) == '.') {
            cursor = skipWhitespace(sql, cursor + 1);
            cursor = skipIdentifier(sql, cursor);
            cursor = skipWhitespace(sql, cursor);
        }
        return sql.indexOf('(', cursor);
    }

    private int skipIdentifier(String sql, int start) {
        int cursor = skipWhitespace(sql, start);
        if (cursor >= sql.length()) {
            return cursor;
        }
        char quote = sql.charAt(cursor);
        if (quote == '"' || quote == '`' || quote == '[') {
            char close = quote == '[' ? ']' : quote;
            for (cursor++; cursor < sql.length(); cursor++) {
                if (sql.charAt(cursor) == close) {
                    if (close != ']' && cursor + 1 < sql.length()
                            && sql.charAt(cursor + 1) == close) {
                        cursor++;
                    } else {
                        return cursor + 1;
                    }
                }
            }
            return sql.length();
        }
        while (cursor < sql.length()) {
            char value = sql.charAt(cursor);
            if (Character.isWhitespace(value) || value == '.' || value == '('
                    || value == ')' || value == ';' || value == ',') {
                break;
            }
            cursor++;
        }
        return cursor;
    }

    private int skipWhitespace(String sql, int start) {
        int cursor = start;
        while (cursor < sql.length() && Character.isWhitespace(sql.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    private boolean matchesKeyword(String sql, int start, String keyword) {
        return sql.regionMatches(true, start, keyword, 0, keyword.length())
                && (start + keyword.length() == sql.length()
                || (!Character.isLetterOrDigit(sql.charAt(start + keyword.length()))
                && sql.charAt(start + keyword.length()) != '_'));
    }

    private static int matchingParenthesis(String sql, int openIndex) {
        int depth = 0;
        char quote = 0;
        for (int index = openIndex; index < sql.length(); index++) {
            char value = sql.charAt(index);
            if (quote != 0) {
                if (value == quote) {
                    quote = 0;
                }
                continue;
            }
            if (value == '\'' || value == '"' || value == '`') {
                quote = value;
            } else if (value == '(') {
                depth++;
            } else if (value == ')' && --depth == 0) {
                return index;
            }
        }
        return -1;
    }

    public static SQLiteIndexControl of(SQLiteIndex index) {
        SQLiteIndexControl control = new SQLiteIndexControl();
        control.copy(index);
        return control;
    }

    public static List<SQLiteIndexControl> of(List<SQLiteIndex> indexes) {
        List<SQLiteIndexControl> controls = new ArrayList<>();
        if (indexes != null) {
            for (SQLiteIndex index : indexes) {
                controls.add(of(index));
            }
        }
        return controls;
    }
}
