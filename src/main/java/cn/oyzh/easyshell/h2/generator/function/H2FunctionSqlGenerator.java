package cn.oyzh.easyshell.h2.generator.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.routine.H2Routine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * H2 函数别名 SQL 生成器。
 *
 * @since 2026-11-28
 */
public final class H2FunctionSqlGenerator {

    private H2FunctionSqlGenerator() {
    }

    public static String create(H2Routine routine) {
        H2GeneratorUtil.requireName(routine == null ? null : routine.getName());
        StringBuilder sql = new StringBuilder("CREATE ALIAS ")
                .append(H2GeneratorUtil.qualified(routine.getSchema(), routine.getName()));
        if (routine.isDeterministic()) {
            sql.append(" DETERMINISTIC");
        }
        if (StringUtil.isNotBlank(routine.getDefinition())) {
            return sql.append(" AS ")
                    .append(H2GeneratorUtil.literal(
                            normalizeSource(routine.getDefinition())))
                    .toString();
        }
        if (StringUtil.isNotBlank(routine.getExternalName())) {
            return sql.append(" FOR ")
                    .append(H2GeneratorUtil.literal(routine.getExternalName().strip()))
                    .toString();
        }
        throw new IllegalArgumentException("H2 function source or external method is required");
    }

    public static String drop(String schema, String name) {
        H2GeneratorUtil.requireName(name);
        return "DROP ALIAS IF EXISTS " + H2GeneratorUtil.qualified(schema, name);
    }

    /**
     * H2 的 AS 参数是方法源码，不是完整 Java 类。兼容用户粘贴完整单方法类。
     */
    static String normalizeSource(String source) {
        String value = source.strip();
        if (!Pattern.compile("(?is)\\b(class|interface|enum)\\s+[A-Za-z_$]")
                .matcher(value).find()) {
            return removeStaticModifiers(value);
        }
        Matcher method = Pattern.compile(
                        "(?is)public\\s+static\\s+([^{;]+\\([^)]*\\))\\s*\\{")
                .matcher(value);
        if (!method.find()) {
            throw new IllegalArgumentException(
                    "H2 function source requires one public static Java method");
        }
        int brace = value.indexOf('{', method.start());
        int end = matchingBrace(value, brace);
        if (end < 0) {
            throw new IllegalArgumentException("H2 function source has unbalanced braces");
        }
        return method.group(1).strip() + " {\n"
                + value.substring(brace + 1, end).stripIndent().stripTrailing()
                + "\n}";
    }

    private static String removeStaticModifiers(String source) {
        Matcher matcher = Pattern.compile("^(public\\s+)?static\\s+")
                .matcher(source);
        return matcher.find() ? source.substring(matcher.end()) : source;
    }

    private static int matchingBrace(String source, int start) {
        if (start < 0) {
            return -1;
        }
        int depth = 0;
        boolean string = false;
        boolean character = false;
        boolean escape = false;
        for (int index = start; index < source.length(); index++) {
            char value = source.charAt(index);
            if (escape) {
                escape = false;
                continue;
            }
            if ((string || character) && value == '\\') {
                escape = true;
                continue;
            }
            if (!character && value == '"') {
                string = !string;
                continue;
            }
            if (!string && value == '\'') {
                character = !character;
                continue;
            }
            if (string || character) {
                continue;
            }
            if (value == '{') {
                depth++;
            } else if (value == '}' && --depth == 0) {
                return index;
            }
        }
        return -1;
    }
}
