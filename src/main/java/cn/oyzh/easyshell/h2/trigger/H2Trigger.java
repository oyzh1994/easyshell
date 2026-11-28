package cn.oyzh.easyshell.h2.trigger;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.generator.trigger.H2TriggerCreateSqlGenerator;
import cn.oyzh.easyshell.h2.schema.H2SchemaObject;
import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * H2 触发器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2Trigger extends DBObject implements DBTrigger, ObjectCopier<H2Trigger> {

    private static final Pattern POLICY = Pattern.compile(
            "(?is)^\\s*CREATE\\s+(?:FORCE\\s+)?(?:TEMP(?:ORARY)?\\s+)?TRIGGER\\s+"
                    + "(?:IF\\s+NOT\\s+EXISTS\\s+)?"
                    + "(?:(?:\"[^\"]*\"|`[^`]*`|\\[[^]]*\\]|[^.\\s]+)\\s*\\.\\s*)?"
                    + "(?:\"[^\"]*\"|`[^`]*`|\\[[^]]*\\]|[^.\\s]+)\\s+"
                    + "(BEFORE|AFTER|INSTEAD\\s+OF)\\s+(INSERT|UPDATE|DELETE)\\b");

    private static final Pattern CALL_CLASS = Pattern.compile(
            "(?is)\\bCALL\\s+(?:\"((?:[^\"]|\"\")*)\"|'((?:[^']|'')*)'|([^\\s;]+))");

    private static final Pattern AS_SOURCE = Pattern.compile(
            "(?is)\\bAS\\s+((?:U&)?'(?:[^']|'')*')");

    private static final Pattern JAVA_SQL = Pattern.compile(
            "(?is)statement\\.execute\\((\"(?:\\\\.|[^\"])*\")\\)");

    private String name;

    private String originalName;

    private String policy;

    private String originalPolicy;

    private String definition;

    private String originalDefinition;

    private String tableName;

    private String createDefinition;

    public H2Trigger() {
    }

    public H2Trigger(String name) {
        this.setName(name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public String originalName() {
        return this.originalName == null
                ? (String) super.getOriginalData("name") : this.originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getPolicy() {
        return this.policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
        super.putOriginalData("policy", policy);
    }

    public String originalPolicy() {
        return this.originalPolicy;
    }

    public void setOriginalPolicy(String originalPolicy) {
        this.originalPolicy = originalPolicy;
    }

    public void setPolicy(String timing, String manipulation) {
        this.setPolicy(timing.toUpperCase() + " " + manipulation.toUpperCase());
    }

    public String getDefinition() {
        return this.definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
        super.putOriginalData("definition", definition);
    }

    public String getOriginalDefinition() {
        return this.originalDefinition;
    }

    public void setOriginalDefinition(String originalDefinition) {
        this.originalDefinition = originalDefinition;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        super.putOriginalData("tableName", tableName);
    }

    public String getCreateDefinition() {
        return this.createDefinition;
    }

    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
    }

    public boolean isChangedDefinition() {
        return !StringUtil.equals(this.originalDefinition, this.definition)
                || !StringUtil.equals(this.originalName(), this.name)
                || !StringUtil.equals(this.originalPolicy, this.policy);
    }

    @Override
    public boolean isInvalid() {
        return StringUtil.isBlank(this.name) || StringUtil.isBlank(this.definition);
    }

    public String normalizedDefinition(String previousTable, String targetTable) {
        String table = StringUtil.blankToDefault(targetTable, this.tableName);
        if (StringUtil.isBlank(table)) {
            table = previousTable;
        }
        return this.toCreateSql(table);
    }

    /**
     * 根据触发器属性生成完整创建SQL。
     *
     * @param tableName 表名
     * @return 创建触发器SQL
     */
    public String toCreateSql(String tableName) {
        return H2TriggerCreateSqlGenerator.generateSqlSingle(this, tableName);
    }

    /**
     * 获取用户输入的触发器主体SQL。
     *
     * @return 触发器主体SQL
     */
    public String triggerBody() {
        String sql = StringUtil.blankToDefault(this.definition, "").trim();
        if (sql.regionMatches(true, 0, "CREATE", 0, "CREATE".length())) {
            String classBody = triggerClassName(sql);
            if (StringUtil.isNotBlank(classBody)) {
                return classBody;
            }
            String sourceBody = triggerSqlBody(sql);
            if (sourceBody != null) {
                return sourceBody;
            }
            return triggerBody(sql);
        }
        return H2GeneratorUtil.stripSemicolon(sql);
    }

    private static String triggerClassName(String createSql) {
        Matcher matcher = CALL_CLASS.matcher(createSql);
        if (!matcher.find()) {
            return null;
        }
        for (int index = 1; index <= 3; index++) {
            String value = matcher.group(index);
            if (value != null) {
                return value.replace("\"\"", "\"").replace("''", "'");
            }
        }
        return null;
    }

    private static String triggerSqlBody(String createSql) {
        Matcher sourceMatcher = AS_SOURCE.matcher(createSql);
        if (!sourceMatcher.find()) {
            return null;
        }
        String literal = sourceMatcher.group(1);
        boolean unicode = literal.regionMatches(true, 0, "U&'", 0, 3);
        int start = unicode ? 3 : 1;
        String source = literal.substring(start, literal.length() - 1).replace("''", "'");
        if (unicode) {
            source = decodeUnicodeLiteral(source);
        }
        Matcher sqlMatcher = JAVA_SQL.matcher(source);
        List<String> statements = new ArrayList<>();
        while (sqlMatcher.find()) {
            String javaLiteral = sqlMatcher.group(1);
            statements.add(decodeJavaString(
                    javaLiteral.substring(1, javaLiteral.length() - 1)) + ";");
        }
        return statements.isEmpty() ? null : String.join(System.lineSeparator(), statements);
    }

    private static String decodeUnicodeLiteral(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current != '\\' || index + 1 >= value.length()) {
                result.append(current);
                continue;
            }
            char marker = value.charAt(index + 1);
            if (marker == '\\') {
                result.append('\\');
                index++;
                continue;
            }
            int digitStart = marker == '+' ? index + 2 : index + 1;
            int length = marker == '+' ? 6 : 4;
            if (digitStart + length <= value.length()) {
                String digits = value.substring(digitStart, digitStart + length);
                if (!digits.matches("[0-9A-Fa-f]+")) {
                    result.append(current);
                    continue;
                }
                try {
                    result.appendCodePoint(Integer.parseInt(digits, 16));
                    index = digitStart + length - 1;
                    continue;
                } catch (IllegalArgumentException ignored) {
                    // 保留原始转义文本
                }
            }
            result.append(current);
        }
        return result.toString();
    }

    private static String decodeJavaString(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current != '\\' || index + 1 >= value.length()) {
                result.append(current);
                continue;
            }
            char escaped = value.charAt(++index);
            switch (escaped) {
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                default -> result.append(escaped);
            }
        }
        return result.toString();
    }

    private static String triggerBody(String createSql) {
        int begin = H2GeneratorUtil.keywordIndex(createSql, "BEGIN", 0);
        if (begin < 0) {
            return "";
        }
        int end = -1;
        int cursor = begin + "BEGIN".length();
        while (true) {
            int index = H2GeneratorUtil.keywordIndex(createSql, "END", cursor);
            if (index < 0) {
                break;
            }
            end = index;
            cursor = index + "END".length();
        }
        if (end < 0) {
            return "";
        }
        return H2GeneratorUtil.stripSemicolon(createSql.substring(begin + "BEGIN".length(), end));
    }

    @Override
    public void copy(H2Trigger trigger) {
        if (trigger != null) {
            this.setName(trigger.getName());
            this.setOriginalName(trigger.originalName());
            this.setPolicy(trigger.getPolicy());
            this.setOriginalPolicy(trigger.originalPolicy());
            this.setDefinition(trigger.getDefinition());
            this.setOriginalDefinition(trigger.getOriginalDefinition());
            this.setTableName(trigger.getTableName());
            this.setCreateDefinition(trigger.getCreateDefinition());
        }
    }

    public static H2Trigger of(H2SchemaObject object) {
        H2Trigger trigger = new H2Trigger();
        trigger.setName(object.getName());
        trigger.setOriginalName(object.getName());
        trigger.setTableName(object.getTableName());
        String createSql = StringUtil.blankToDefault(object.getSql(), "");
        trigger.setCreateDefinition(createSql);
        trigger.setDefinition(createSql);
        String body = trigger.triggerBody();
        trigger.setDefinition(body);
        trigger.setOriginalDefinition(body);
        Matcher matcher = POLICY.matcher(createSql);
        if (matcher.find()) {
            trigger.setPolicy(matcher.group(1).toUpperCase(Locale.ROOT).replaceAll("\\s+", " "),
                    matcher.group(2).toUpperCase(Locale.ROOT));
            trigger.setOriginalPolicy(trigger.getPolicy());
        }
        return trigger;
    }

    public static H2Trigger newTrigger(String tableName) {
        H2Trigger trigger = new H2Trigger();
        trigger.setName(DBUtil.genTriggerName());
        trigger.setPolicy("AFTER INSERT");
        trigger.setDefinition("");
        trigger.setTableName(tableName);
        trigger.setCreated(true);
        return trigger;
    }
}
