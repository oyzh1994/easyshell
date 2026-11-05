package cn.oyzh.easyshell.sqlite.column;

import cn.oyzh.easyshell.sqlite.check.SQLiteCheck;
import cn.oyzh.easyshell.sqlite.foreignKey.SQLiteForeignKey;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteColumnUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQLite CREATE TABLE 字段定义解析器。
 *
 * @since 2026-11-02
 */
public final class SQLiteColumnDefinitionParser {

    private static final Pattern NAME = Pattern.compile(
            "^((?:\"(?:[^\"]|\"\")*\")|(?:\\[[^\\]]*\\])|(?:`[^`]*`)|(?:[^\\s(]+))");

    private static final Pattern DEFAULT = Pattern.compile(
            "\\bDEFAULT\\s+('(?:[^']|'')*'|\\([^)]*\\)|[^\\s,]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern COLLATE = Pattern.compile(
            "\\bCOLLATE\\s+([^\\s,]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern CHECK = Pattern.compile(
            "\\bCHECK\\s*\\((.*)\\)", Pattern.CASE_INSENSITIVE);

    private static final Pattern GENERATED = Pattern.compile(
            "\\bGENERATED\\s+ALWAYS\\s+AS\\s*\\((.*)\\)\\s*(VIRTUAL|STORED)?\\s*$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TABLE_CONSTRAINT = Pattern.compile(
            "^(?:CONSTRAINT\\s+[^\\s]+\\s+)?(?:PRIMARY\\s+KEY|UNIQUE|CHECK|FOREIGN\\s+KEY)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TYPE_END = Pattern.compile(
            "\\s+(?:CONSTRAINT|PRIMARY\\s+KEY|NOT\\s+NULL|UNIQUE|DEFAULT|CHECK|COLLATE|GENERATED|REFERENCES)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final String IDENTIFIER = "(?:\"(?:[^\"]|\"\")*\"|\\[[^\\]]*\\]|`[^`]*`|[^\\s(]+)";

    private static final Pattern CREATE_TABLE = Pattern.compile(
            "(?is)^\\s*CREATE\\s+(?:TEMP(?:ORARY)?\\s+)?TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?("
                    + IDENTIFIER + ")");

    private static final Pattern FOREIGN_KEY = Pattern.compile(
            "(?is)^(?:CONSTRAINT\\s+(" + IDENTIFIER + ")\\s+)?FOREIGN\\s+KEY\\s*\\((.*?)\\)\\s+"
                    + "REFERENCES\\s+(" + IDENTIFIER + ")\\s*(?:\\((.*?)\\))?(.*)$");

    private static final Pattern CHECK_CONSTRAINT = Pattern.compile(
            "(?is)^(?:CONSTRAINT\\s+(" + IDENTIFIER + ")\\s+)?CHECK\\s*\\((.*)\\)\\s*$");

    private SQLiteColumnDefinitionParser() {
    }

    public static void enrich(List<SQLiteColumn> columns, String createSql) {
        if (columns == null || columns.isEmpty() || StringUtil.isBlank(createSql)) {
            return;
        }
        for (String definition : splitDefinitions(body(createSql))) {
            Matcher nameMatcher = NAME.matcher(definition.trim());
            if (!nameMatcher.find()) {
                continue;
            }
            String name = SQLiteGeneratorUtil.unquote(nameMatcher.group(1));
            SQLiteColumn column = columns.stream()
                    .filter(item -> StringUtil.equalsIgnoreCase(item.getName(), name))
                    .findFirst().orElse(null);
            if (column == null) {
                continue;
            }
            applyDefinition(column, definition);
        }
    }

    /**
     * 解析单行字段定义。
     *
     * @param definition 字段定义
     * @return SQLite 字段
     */
    public static SQLiteColumn parseColumn(String definition) {
        if (StringUtil.isBlank(definition)) {
            throw new IllegalArgumentException("SQLite column definition is empty");
        }
        Matcher nameMatcher = NAME.matcher(definition.trim());
        if (!nameMatcher.find()) {
            throw new IllegalArgumentException("Invalid SQLite column definition: " + definition);
        }
        SQLiteColumn column = new SQLiteColumn();
        column.setName(SQLiteGeneratorUtil.unquote(nameMatcher.group(1)));
        String rest = definition.trim().substring(nameMatcher.end()).trim();
        String type = "";
        if (StringUtil.isNotBlank(rest)) {
            Matcher endMatcher = TYPE_END.matcher(" " + rest);
            int end = endMatcher.find() ? endMatcher.start() : rest.length();
            type = rest.substring(0, Math.min(end, rest.length())).trim();
        }
        column.setType(type);
        column.setSize(ShellSqliteColumnUtil.parseSize(type));
        column.setPrimaryKey(Pattern.compile("\\bPRIMARY\\s+KEY\\b", Pattern.CASE_INSENSITIVE)
                .matcher(definition).find());
        column.setNotNull(Pattern.compile("\\bNOT\\s+NULL\\b", Pattern.CASE_INSENSITIVE)
                .matcher(definition).find());
        applyDefinition(column, definition);
        return column;
    }

    private static void applyDefinition(SQLiteColumn column, String definition) {
        String upper = definition.toUpperCase(Locale.ROOT);
        column.setAutoIncrement(upper.contains("AUTOINCREMENT"));
        column.setUnique(containsWord(upper, "UNIQUE"));
        column.setDefaultValue(match(DEFAULT, definition));
        column.setCollate(match(COLLATE, definition));
        column.setCheck(match(CHECK, definition));
        Matcher generated = GENERATED.matcher(definition.trim());
        if (generated.find()) {
            column.setGenerated(true);
            column.setGeneratedExpression(generated.group(1).trim());
            column.setGeneratedStorage(StringUtil.blankToDefault(generated.group(2), "VIRTUAL"));
        }
    }

    /**
     * 提取 CREATE TABLE 中的表级约束。
     *
     * @param createSql CREATE TABLE SQL
     * @return 表级约束
     */
    public static String tableConstraints(String createSql) {
        if (StringUtil.isBlank(createSql)) {
            return null;
        }
        List<String> constraints = splitDefinitions(body(createSql)).stream()
                .map(String::trim)
                .filter(definition -> TABLE_CONSTRAINT.matcher(definition).find())
                .toList();
        return constraints.isEmpty() ? null : String.join(",\n", constraints);
    }

    public static boolean isTableConstraint(String definition) {
        return definition != null && TABLE_CONSTRAINT.matcher(definition.trim()).find();
    }

    public static boolean isForeignKeyConstraint(String definition) {
        return definition != null && Pattern.compile("(?is)^(?:CONSTRAINT\\s+" + IDENTIFIER
                + "\\s+)?FOREIGN\\s+KEY\\b").matcher(definition.trim()).find();
    }

    public static boolean isCheckConstraint(String definition) {
        return definition != null && Pattern.compile("(?is)^(?:CONSTRAINT\\s+" + IDENTIFIER
                + "\\s+)?CHECK\\s*\\(").matcher(definition.trim()).find();
    }

    /**
     * 获取除外键和表检查外的约束，用于设计器保留主键、唯一等高级约束。
     */
    public static String baseTableConstraints(String createSql) {
        if (StringUtil.isBlank(createSql)) {
            return null;
        }
        List<String> constraints = splitDefinitions(body(createSql)).stream()
                .map(String::trim)
                .filter(SQLiteColumnDefinitionParser::isTableConstraint)
                .filter(definition -> !isForeignKeyConstraint(definition))
                .filter(definition -> !isCheckConstraint(definition))
                .toList();
        return constraints.isEmpty() ? null : String.join(",\n", constraints);
    }

    public static List<SQLiteForeignKey> foreignKeys(String createSql) {
        List<SQLiteForeignKey> foreignKeys = new ArrayList<>();
        if (StringUtil.isBlank(createSql)) {
            return foreignKeys;
        }
        String tableName = tableName(createSql);
        int index = 0;
        for (String definition : splitDefinitions(body(createSql))) {
            Matcher matcher = FOREIGN_KEY.matcher(definition.trim());
            if (!matcher.find()) {
                continue;
            }
            index++;
            SQLiteForeignKey foreignKey = new SQLiteForeignKey();
            String name = matcher.group(1);
            foreignKey.setName(StringUtil.isBlank(name)
                    ? "fk_" + tableName + "_" + index : SQLiteGeneratorUtil.unquote(name));
            foreignKey.setColumns(splitIdentifiers(matcher.group(2)));
            foreignKey.setReferencedTable(SQLiteGeneratorUtil.unquote(matcher.group(3).trim()));
            foreignKey.setReferencedColumns(splitIdentifiers(matcher.group(4)));
            String actions = StringUtil.blankToDefault(matcher.group(5), "");
            Matcher deleteMatcher = Pattern.compile("(?i)ON\\s+DELETE\\s+(NO\\s+ACTION|RESTRICT|CASCADE|SET\\s+NULL|SET\\s+DEFAULT)")
                    .matcher(actions);
            if (deleteMatcher.find()) {
                foreignKey.setDeletePolicy(deleteMatcher.group(1).toUpperCase(Locale.ROOT));
            }
            Matcher updateMatcher = Pattern.compile("(?i)ON\\s+UPDATE\\s+(NO\\s+ACTION|RESTRICT|CASCADE|SET\\s+NULL|SET\\s+DEFAULT)")
                    .matcher(actions);
            if (updateMatcher.find()) {
                foreignKey.setUpdatePolicy(updateMatcher.group(1).toUpperCase(Locale.ROOT));
            }
            foreignKeys.add(foreignKey);
        }
        return foreignKeys;
    }

    public static List<SQLiteCheck> checks(String createSql) {
        List<SQLiteCheck> checks = new ArrayList<>();
        if (StringUtil.isBlank(createSql)) {
            return checks;
        }
        String tableName = tableName(createSql);
        int index = 0;
        for (String definition : splitDefinitions(body(createSql))) {
            Matcher matcher = CHECK_CONSTRAINT.matcher(definition.trim());
            if (!matcher.find()) {
                continue;
            }
            index++;
            SQLiteCheck check = new SQLiteCheck();
            String name = matcher.group(1);
            check.setName(StringUtil.isBlank(name)
                    ? "check_" + tableName + "_" + index : SQLiteGeneratorUtil.unquote(name));
            check.setClause(matcher.group(2).trim());
            checks.add(check);
        }
        return checks;
    }

    private static String tableName(String createSql) {
        Matcher matcher = CREATE_TABLE.matcher(createSql);
        return matcher.find() ? SQLiteGeneratorUtil.unquote(matcher.group(1)) : "table";
    }

    private static List<String> splitIdentifiers(String value) {
        List<String> identifiers = new ArrayList<>();
        if (StringUtil.isBlank(value)) {
            return identifiers;
        }
        for (String item : value.split("\\s*,\\s*")) {
            if (StringUtil.isNotBlank(item)) {
                identifiers.add(SQLiteGeneratorUtil.unquote(item.trim()));
            }
        }
        return identifiers;
    }

    private static String body(String createSql) {
        int start = createSql.indexOf('(');
        int end = createSql.lastIndexOf(')');
        return start >= 0 && end > start ? createSql.substring(start + 1, end) : createSql;
    }

    private static List<String> splitDefinitions(String body) {
        List<String> definitions = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        char quote = 0;
        for (int index = 0; index < body.length(); index++) {
            char value = body.charAt(index);
            if (quote != 0) {
                current.append(value);
                if (value == quote) {
                    if (index + 1 < body.length() && body.charAt(index + 1) == quote) {
                        current.append(body.charAt(++index));
                    } else {
                        quote = 0;
                    }
                }
                continue;
            }
            if (value == '\'' || value == '"' || value == '`') {
                quote = value;
                current.append(value);
            } else if (value == '[') {
                quote = ']';
                current.append(value);
            } else if (value == '(') {
                depth++;
                current.append(value);
            } else if (value == ')') {
                depth--;
                current.append(value);
            } else if (value == ',' && depth == 0) {
                definitions.add(current.toString());
                current.setLength(0);
            } else {
                current.append(value);
            }
        }
        if (!current.toString().isBlank()) {
            definitions.add(current.toString());
        }
        return definitions;
    }

    private static boolean containsWord(String value, String word) {
        return Pattern.compile("\\b" + word + "\\b").matcher(value).find();
    }

    private static String match(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? matcher.group(1) : null;
    }
}
