package cn.oyzh.easyshell.h2.column;

import cn.oyzh.easyshell.h2.check.H2Check;
import cn.oyzh.easyshell.h2.foreignKey.H2ForeignKey;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.h2.ShellH2ColumnUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * H2 CREATE TABLE 字段定义解析器。
 *
 * @since 2026-11-02
 */
public final class H2ColumnDefinitionParser {

    private static final Pattern NAME = Pattern.compile(
            "^((?:\"(?:[^\"]|\"\")*\")|(?:\\[[^\\]]*\\])|(?:`[^`]*`)|(?:[^\\s(]+))");

    private static final Pattern DEFAULT = Pattern.compile(
            "\\bDEFAULT\\s+('(?:[^']|'')*'|\\([^)]*\\)|[^\\s,]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern CHECK = Pattern.compile(
            "\\bCHECK\\s*\\((.*)\\)", Pattern.CASE_INSENSITIVE);

    private static final Pattern COMMENT = Pattern.compile(
            "\\bCOMMENT\\s+('(?:[^']|'')*')", Pattern.CASE_INSENSITIVE);

    private static final Pattern GENERATED = Pattern.compile(
            "\\bGENERATED\\s+ALWAYS\\s+AS\\s*\\((.*)\\)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TABLE_CONSTRAINT = Pattern.compile(
            "^(?:CONSTRAINT\\s+[^\\s]+\\s+)?(?:PRIMARY\\s+KEY|UNIQUE|CHECK|FOREIGN\\s+KEY)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TYPE_END = Pattern.compile(
            "\\s+(?:CONSTRAINT|PRIMARY\\s+KEY|NOT\\s+NULL|UNIQUE|DEFAULT|CHECK|COMMENT|GENERATED|REFERENCES)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final String IDENTIFIER = "(?:\"(?:[^\"]|\"\")*\"|\\[[^\\]]*\\]|`[^`]*`|[^\\s(.]+)";

    private static final String QUALIFIED_IDENTIFIER = IDENTIFIER
            + "(?:\\s*\\.\\s*" + IDENTIFIER + ")*";

    private static final Pattern CREATE_TABLE = Pattern.compile(
            "(?is)^\\s*CREATE\\s+(?:(?:GLOBAL|LOCAL)\\s+)?"
                    + "(?:(?:TEMP(?:ORARY)?|CACHED|MEMORY)\\s+)?TABLE\\s+"
                    + "(?:IF\\s+NOT\\s+EXISTS\\s+)?("
                    + QUALIFIED_IDENTIFIER + ")");

    private static final Pattern FOREIGN_KEY = Pattern.compile(
            "(?is)^(?:CONSTRAINT\\s+(" + QUALIFIED_IDENTIFIER + ")\\s+)?FOREIGN\\s+KEY\\s*\\((.*?)\\)\\s+"
                    + "REFERENCES\\s+(" + QUALIFIED_IDENTIFIER + ")\\s*(?:\\((.*?)\\))?(.*)$");

    private static final Pattern CHECK_CONSTRAINT = Pattern.compile(
            "(?is)^(?:CONSTRAINT\\s+(" + QUALIFIED_IDENTIFIER + ")\\s+)?"
                    + "CHECK\\s*\\((.*)\\)(?:\\s+NOCHECK)?\\s*$");

    private H2ColumnDefinitionParser() {
    }

    public static void enrich(List<H2Column> columns, String createSql) {
        if (columns == null || columns.isEmpty() || StringUtil.isBlank(createSql)) {
            return;
        }
        for (String definition : splitDefinitions(body(createSql))) {
            Matcher nameMatcher = NAME.matcher(definition.trim());
            if (!nameMatcher.find()) {
                continue;
            }
            String name = H2GeneratorUtil.unquote(nameMatcher.group(1));
            H2Column column = columns.stream()
                    .filter(item -> StringUtil.equalsIgnoreCase(item.getName(), name))
                    .findFirst().orElse(null);
            if (column == null) {
                continue;
            }
            applyDefinition(column, definition);
        }
        enrichColumnChecks(columns, createSql);
    }

    /**
     * 将只引用一个字段的检查约束回填到字段模型。
     *
     * @param columns   字段列表
     * @param createSql CREATE TABLE 定义
     */
    public static void enrichColumnChecks(List<H2Column> columns, String createSql) {
        if (columns == null || columns.isEmpty()) {
            return;
        }
        for (H2Check check : checks(createSql)) {
            List<H2Column> referenced = columns.stream()
                    .filter(column -> referencesColumn(check.getClause(), column.getName()))
                    .toList();
            if (referenced.size() == 1) {
                H2Column column = referenced.getFirst();
                column.setCheck(check.getClause());
                column.setCheckName(check.getName());
            }
        }
    }

    /**
     * 获取不应在字段检查中重复显示的检查约束。
     *
     * @param createSql CREATE TABLE 定义
     * @param columns   字段列表
     * @return 表级检查约束
     */
    public static List<H2Check> checksExcludingColumns(String createSql, List<H2Column> columns) {
        Set<String> columnCheckNames = new java.util.HashSet<>();
        if (columns != null) {
            columns.stream()
                    .map(H2Column::getCheckName)
                    .filter(StringUtil::isNotBlank)
                    .map(name -> name.toLowerCase(Locale.ROOT))
                    .forEach(columnCheckNames::add);
        }
        return checks(createSql).stream()
                .filter(check -> !columnCheckNames.contains(
                        StringUtil.blankToDefault(check.getName(), "").toLowerCase(Locale.ROOT)))
                .toList();
    }

    private static boolean referencesColumn(String clause, String columnName) {
        if (StringUtil.isBlank(clause) || StringUtil.isBlank(columnName)) {
            return false;
        }
        return Pattern.compile("(?i)(?<![\\w$])\"?\\Q" + columnName
                + "\\E\"?(?![\\w$])").matcher(clause).find();
    }

    /**
     * 解析单行字段定义。
     *
     * @param definition 字段定义
     * @return H2 字段
     */
    public static H2Column parseColumn(String definition) {
        if (StringUtil.isBlank(definition)) {
            throw new IllegalArgumentException("H2 column definition is empty");
        }
        Matcher nameMatcher = NAME.matcher(definition.trim());
        if (!nameMatcher.find()) {
            throw new IllegalArgumentException("Invalid H2 column definition: " + definition);
        }
        H2Column column = new H2Column();
        column.setName(H2GeneratorUtil.unquote(nameMatcher.group(1)));
        String rest = definition.trim().substring(nameMatcher.end()).trim();
        String type = "";
        if (StringUtil.isNotBlank(rest)) {
            Matcher endMatcher = TYPE_END.matcher(" " + rest);
            int end = endMatcher.find() ? endMatcher.start() : rest.length();
            type = rest.substring(0, Math.min(end, rest.length())).trim();
        }
        column.setType(type);
        column.setSize(ShellH2ColumnUtil.parseSize(type));
        column.setDigits(ShellH2ColumnUtil.parseDigits(type));
        column.setPrimaryKey(Pattern.compile("\\bPRIMARY\\s+KEY\\b", Pattern.CASE_INSENSITIVE)
                .matcher(definition).find());
        column.setNotNull(Pattern.compile("\\bNOT\\s+NULL\\b", Pattern.CASE_INSENSITIVE)
                .matcher(definition).find());
        applyDefinition(column, definition);
        return column;
    }

    private static void applyDefinition(H2Column column, String definition) {
        String upper = definition.toUpperCase(Locale.ROOT);
        column.setIdentity(upper.contains("IDENTITY"));
        column.setUnique(containsWord(upper, "UNIQUE"));
        String defaultValue = defaultValue(definition);
        if (defaultValue != null) {
            column.setDefaultValue(defaultValue);
        }
        column.setCheck(match(CHECK, definition));
        String comment = match(COMMENT, definition);
        if (comment != null) {
            column.setComment(sqlLiteral(comment));
        }
        Matcher generated = GENERATED.matcher(definition.trim());
        if (generated.find()) {
            column.setGenerated(true);
            column.setGeneratedExpression(generated.group(1).trim());
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
     * 替换 CREATE TABLE 的目标模式和表名，保留字段及约束定义。
     *
     * @param createSql CREATE TABLE 定义
     * @param schema    目标模式
     * @param tableName 目标表名
     * @return 替换后的定义
     */
    public static String requalifyTable(String createSql, String schema, String tableName) {
        if (StringUtil.isBlank(createSql)) {
            return createSql;
        }
        Matcher matcher = CREATE_TABLE.matcher(createSql);
        if (!matcher.find()) {
            return createSql;
        }
        return createSql.substring(0, matcher.start(1))
                + H2GeneratorUtil.qualified(schema, tableName)
                + createSql.substring(matcher.end(1));
    }

    /**
     * 移除表级外键定义，用于 dump/传输时先创建全部表再补外键。
     *
     * @param createSql CREATE TABLE 定义
     * @return 不含外键的 CREATE TABLE 定义
     */
    public static String withoutForeignKeys(String createSql) {
        if (StringUtil.isBlank(createSql)) {
            return createSql;
        }
        int start = createSql.indexOf('(');
        int end = createSql.lastIndexOf(')');
        if (start < 0 || end <= start) {
            return createSql;
        }
        List<String> definitions = splitDefinitions(createSql.substring(start + 1, end)).stream()
                .map(String::trim)
                .filter(definition -> !isForeignKeyConstraint(definition))
                .toList();
        return createSql.substring(0, start + 1) + "\n    "
                + String.join(",\n    ", definitions)
                + createSql.substring(end);
    }

    /**
     * 移除一个约束定义中的约束名。H2 约束名在 schema 内全局唯一，克隆表时必须重新生成。
     */
    public static String withoutConstraintName(String definition) {
        if (StringUtil.isBlank(definition)) {
            return definition;
        }
        return definition.replaceFirst(
                "(?is)^\\s*CONSTRAINT\\s+(?:\"(?:[^\"]|\"\")*\"|`[^`]*`|\\[[^\\]]*\\]|[^\\s]+)\\s+",
                "");
    }

    /**
     * 移除一组表约束中的约束名。
     */
    public static String withoutConstraintNames(String constraints) {
        if (StringUtil.isBlank(constraints)) {
            return constraints;
        }
        return splitDefinitions(constraints).stream()
                .map(H2ColumnDefinitionParser::withoutConstraintName)
                .collect(java.util.stream.Collectors.joining(",\n"));
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
                .filter(H2ColumnDefinitionParser::isTableConstraint)
                // 主键由字段网格维护，避免保存时生成重复主键定义
                .filter(definition -> !Pattern.compile("\\bPRIMARY\\s+KEY\\b",
                        Pattern.CASE_INSENSITIVE).matcher(definition).find())
                .filter(definition -> !isForeignKeyConstraint(definition))
                .filter(definition -> !isCheckConstraint(definition))
                .toList();
        return constraints.isEmpty() ? null : String.join(",\n", constraints);
    }

    public static List<H2ForeignKey> foreignKeys(String createSql) {
        List<H2ForeignKey> foreignKeys = new ArrayList<>();
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
            H2ForeignKey foreignKey = new H2ForeignKey();
            String name = matcher.group(1);
            foreignKey.setName(StringUtil.isBlank(name)
                    ? "fk_" + tableName + "_" + index : lastIdentifier(name));
            foreignKey.setColumns(splitIdentifiers(matcher.group(2)));
            foreignKey.setReferencedTable(lastIdentifier(matcher.group(3)));
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

    public static List<H2Check> checks(String createSql) {
        List<H2Check> checks = new ArrayList<>();
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
            H2Check check = new H2Check();
            String name = matcher.group(1);
            check.setName(StringUtil.isBlank(name)
                    ? "check_" + tableName + "_" + index : lastIdentifier(name));
            check.setClause(matcher.group(2).trim());
            checks.add(check);
        }
        return checks;
    }

    private static String tableName(String createSql) {
        Matcher matcher = CREATE_TABLE.matcher(createSql);
        return matcher.find() ? lastIdentifier(matcher.group(1)) : "table";
    }

    private static String lastIdentifier(String value) {
        if (StringUtil.isBlank(value)) {
            return value;
        }
        int start = 0;
        char quote = 0;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (quote == 0) {
                if (current == '"' || current == '`' || current == '[') {
                    quote = current == '[' ? ']' : current;
                } else if (current == '.') {
                    start = index + 1;
                }
            } else if (current == quote) {
                if (current != ']' && index + 1 < value.length()
                        && value.charAt(index + 1) == quote) {
                    index++;
                } else {
                    quote = 0;
                }
            }
        }
        return H2GeneratorUtil.unquote(value.substring(start).trim());
    }

    private static List<String> splitIdentifiers(String value) {
        List<String> identifiers = new ArrayList<>();
        if (StringUtil.isBlank(value)) {
            return identifiers;
        }
        for (String item : value.split("\\s*,\\s*")) {
            if (StringUtil.isNotBlank(item)) {
                identifiers.add(H2GeneratorUtil.unquote(item.trim()));
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

    /**
     * 提取真正的默认值，跳过 IDENTITY 定义中的 BY DEFAULT。
     */
    private static String defaultValue(String definition) {
        Matcher matcher = DEFAULT.matcher(definition);
        while (matcher.find()) {
            String prefix = definition.substring(0, matcher.start()).stripTrailing();
            if (prefix.toUpperCase(Locale.ROOT).endsWith(" BY")
                    || prefix.equalsIgnoreCase("BY")) {
                continue;
            }
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 去掉 SQL 单引号字面量外层引号，并还原转义的单引号。
     */
    private static String sqlLiteral(String value) {
        if (StringUtil.isBlank(value)) {
            return value;
        }
        String result = value.trim();
        if (result.length() >= 2 && result.charAt(0) == '\''
                && result.charAt(result.length() - 1) == '\'') {
            return result.substring(1, result.length() - 1).replace("''", "'");
        }
        return result;
    }
}
