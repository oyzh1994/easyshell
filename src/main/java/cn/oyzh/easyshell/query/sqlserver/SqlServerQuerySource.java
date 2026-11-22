package cn.oyzh.easyshell.query.sqlserver;

import cn.oyzh.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 可从查询结果安全定位的单表来源。
 *
 * @param schema 模式
 * @param table  表或视图
 */
record SqlServerQuerySource(String schema, String table) {

    /**
     * 解析单表行查询。
     * 仅接受列名未被改名、没有聚合和多表来源的 SELECT，避免把查询结果写到错误的表。
     *
     * @param sql SQL
     * @return 来源，无法安全定位时返回 null
     */
    static SqlServerQuerySource parse(String sql) {
        if (StringUtil.isBlank(sql)) {
            return null;
        }
        String content = removeComments(sql).strip();
        if (!startsWithKeyword(content, 0, "SELECT")) {
            return null;
        }
        int selectEnd = 6;
        int from = findTopLevelKeyword(content, "FROM", selectEnd);
        if (from < 0
                || hasTopLevelKeyword(content, "UNION")
                || hasTopLevelKeyword(content, "EXCEPT")
                || hasTopLevelKeyword(content, "INTERSECT")
                || hasTopLevelKeyword(content, "GROUP")
                || hasTopLevelKeyword(content, "HAVING")) {
            return null;
        }
        int into = findTopLevelKeyword(content, "INTO", selectEnd);
        if (into >= 0 && into < from) {
            return null;
        }
        if (!isPlainSelectList(content.substring(selectEnd, from))) {
            return null;
        }
        int sourceStart = skipSpaces(content, from + 4);
        if (sourceStart >= content.length() || content.charAt(sourceStart) == '(') {
            return null;
        }
        ParsedName source = parseName(content, sourceStart);
        if (source == null || source.parts().size() > 3) {
            return null;
        }
        int afterSource = skipSpaces(content, source.end());
        if (afterSource < content.length() && content.charAt(afterSource) == '(') {
            return null;
        }
        int clauseEnd = findFromClauseEnd(content, afterSource);
        if (hasSourceSeparator(content, afterSource, clauseEnd)) {
            return null;
        }
        int semicolon = findTopLevelChar(content, ';', sourceStart);
        if (semicolon >= 0 && StringUtil.isNotBlank(content.substring(semicolon + 1))) {
            return null;
        }
        List<String> parts = source.parts();
        String table = parts.getLast();
        String schema = parts.size() > 1 ? parts.get(parts.size() - 2) : null;
        return new SqlServerQuerySource(schema, table);
    }

    /**
     * 判断查询列表是否全部为未改名的普通字段
     *
     * @param selectList 查询列表
     * @return 结果
     */
    private static boolean isPlainSelectList(String selectList) {
        String content = selectList.substring(skipSpaces(selectList, 0));
        if (startsWithKeyword(content, 0, "DISTINCT") || startsWithKeyword(content, 0, "ALL")) {
            int offset = startsWithKeyword(content, 0, "DISTINCT") ? 8 : 3;
            content = content.substring(skipSpaces(content, offset));
        }
        if (startsWithKeyword(content, 0, "TOP")) {
            content = skipTop(content);
            if (content == null) {
                return false;
            }
        }
        List<String> items = splitTopLevel(content, ',');
        if (items.isEmpty()) {
            return false;
        }
        for (String rawItem : items) {
            if (!isPlainSelectItem(rawItem.strip())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断单个查询字段是否未改名
     *
     * @param item 查询字段
     * @return 结果
     */
    private static boolean isPlainSelectItem(String item) {
        if ("*".equals(item)) {
            return true;
        }
        ParsedName name = parseName(item, 0);
        if (name == null) {
            return false;
        }
        String column = name.parts().getLast();
        boolean allColumn = "*".equals(column);
        int offset = skipSpaces(item, name.end());
        if (offset == item.length()) {
            return true;
        }
        if (allColumn) {
            return false;
        }
        if (startsWithKeyword(item, offset, "AS")) {
            offset = skipSpaces(item, offset + 2);
        }
        ParsedName alias = parseName(item, offset);
        return alias != null
                && alias.parts().size() == 1
                && skipSpaces(item, alias.end()) == item.length()
                && StringUtil.equalsIgnoreCase(column, alias.parts().getFirst());
    }

    /**
     * 跳过 TOP 表达式
     *
     * @param content 查询列表
     * @return 剩余内容，非法时返回 null
     */
    private static String skipTop(String content) {
        int offset = skipSpaces(content, 3);
        if (offset >= content.length()) {
            return null;
        }
        if (content.charAt(offset) == '(') {
            int end = findMatchingParen(content, offset);
            if (end < 0) {
                return null;
            }
            offset = end + 1;
        } else {
            int start = offset;
            while (offset < content.length() && !Character.isWhitespace(content.charAt(offset))) {
                offset++;
            }
            if (offset == start) {
                return null;
            }
        }
        offset = skipSpaces(content, offset);
        if (startsWithKeyword(content, offset, "PERCENT")) {
            offset = skipSpaces(content, offset + 7);
        }
        if (startsWithKeyword(content, offset, "WITH")) {
            offset = skipSpaces(content, offset + 4);
            if (!startsWithKeyword(content, offset, "TIES")) {
                return null;
            }
            offset = skipSpaces(content, offset + 4);
        }
        return content.substring(offset);
    }

    /**
     * 查找 FROM 子句末尾
     *
     * @param content SQL
     * @param start   起点
     * @return 末尾
     */
    private static int findFromClauseEnd(String content, int start) {
        String[] keywords = {"WHERE", "GROUP", "HAVING", "ORDER", "OPTION", "FOR",
                "OFFSET", "FETCH", "UNION", "EXCEPT", "INTERSECT", "QUALIFY"};
        int result = content.length();
        for (String keyword : keywords) {
            int index = findTopLevelKeyword(content, keyword, start);
            if (index >= 0 && index < result) {
                result = index;
            }
        }
        int semicolon = findTopLevelChar(content, ';', start);
        if (semicolon >= 0 && semicolon < result) {
            result = semicolon;
        }
        return result;
    }

    /**
     * 判断 FROM 子句是否存在多表、JOIN、APPLY 或 PIVOT
     *
     * @param content SQL
     * @param start   起点
     * @param end     终点
     * @return 结果
     */
    private static boolean hasSourceSeparator(String content, int start, int end) {
        return findTopLevelChar(content, ',', start, end) >= 0
                || findTopLevelKeyword(content, "JOIN", start, end) >= 0
                || findTopLevelKeyword(content, "APPLY", start, end) >= 0
                || findTopLevelKeyword(content, "PIVOT", start, end) >= 0
                || findTopLevelKeyword(content, "UNPIVOT", start, end) >= 0;
    }

    /**
     * 解析标识符名称
     *
     * @param content SQL
     * @param start   起点
     * @return 名称
     */
    private static ParsedName parseName(String content, int start) {
        int offset = start;
        List<String> parts = new ArrayList<>();
        while (offset < content.length()) {
            ParsedPart part = parseIdentifier(content, offset);
            if (part == null) {
                return null;
            }
            parts.add(part.value());
            offset = skipSpaces(content, part.end());
            if (offset >= content.length() || content.charAt(offset) != '.') {
                break;
            }
            offset = skipSpaces(content, offset + 1);
            if (offset < content.length() && content.charAt(offset) == '*') {
                parts.add("*");
                offset++;
                break;
            }
        }
        return parts.isEmpty() ? null : new ParsedName(parts, offset);
    }

    /**
     * 解析单个标识符
     *
     * @param content SQL
     * @param start   起点
     * @return 标识符
     */
    private static ParsedPart parseIdentifier(String content, int start) {
        if (start >= content.length()) {
            return null;
        }
        char first = content.charAt(start);
        if (first == '[') {
            StringBuilder value = new StringBuilder();
            for (int i = start + 1; i < content.length(); i++) {
                char ch = content.charAt(i);
                if (ch == ']') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == ']') {
                        value.append(']');
                        i++;
                        continue;
                    }
                    return new ParsedPart(value.toString(), i + 1);
                }
                value.append(ch);
            }
            return null;
        }
        if (first == '"') {
            StringBuilder value = new StringBuilder();
            for (int i = start + 1; i < content.length(); i++) {
                char ch = content.charAt(i);
                if (ch == '"') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
                        value.append('"');
                        i++;
                        continue;
                    }
                    return new ParsedPart(value.toString(), i + 1);
                }
                value.append(ch);
            }
            return null;
        }
        if (!isIdentifierStart(first)) {
            return null;
        }
        int end = start + 1;
        while (end < content.length() && isIdentifierPart(content.charAt(end))) {
            end++;
        }
        return new ParsedPart(content.substring(start, end), end);
    }

    /**
     * 顶层分段
     *
     * @param content  内容
     * @param separator 分隔符
     * @return 分段
     */
    private static List<String> splitTopLevel(String content, char separator) {
        List<String> result = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < content.length(); i++) {
            int skipped = skipQuoted(content, i);
            if (skipped > i) {
                i = skipped - 1;
                continue;
            }
            char ch = content.charAt(i);
            if (ch == '(') {
                int end = findMatchingParen(content, i);
                if (end < 0) {
                    return List.of();
                }
                i = end;
            } else if (ch == separator) {
                if (content.substring(start, i).isBlank()) {
                    return List.of();
                }
                result.add(content.substring(start, i));
                start = i + 1;
            }
        }
        if (!content.substring(start).isBlank()) {
            result.add(content.substring(start));
        }
        return result;
    }

    /**
     * 查找顶层关键字
     *
     * @param content SQL
     * @param keyword 关键字
     * @return 位置
     */
    private static int findTopLevelKeyword(String content, String keyword) {
        return findTopLevelKeyword(content, keyword, 0, content.length());
    }

    /**
     * 查找顶层关键字
     *
     * @param content SQL
     * @param keyword 关键字
     * @param start   起点
     * @return 位置
     */
    private static int findTopLevelKeyword(String content, String keyword, int start) {
        return findTopLevelKeyword(content, keyword, start, content.length());
    }

    /**
     * 查找顶层关键字
     *
     * @param content SQL
     * @param keyword 关键字
     * @param start   起点
     * @param end     终点
     * @return 位置
     */
    private static int findTopLevelKeyword(String content, String keyword, int start, int end) {
        int depth = 0;
        for (int i = start; i < end; i++) {
            int skipped = skipQuoted(content, i);
            if (skipped > i) {
                i = skipped - 1;
                continue;
            }
            char ch = content.charAt(i);
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth = Math.max(0, depth - 1);
            } else if (depth == 0 && startsWithKeyword(content, i, keyword)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 是否包含顶层关键字
     *
     * @param content SQL
     * @param keyword 关键字
     * @return 结果
     */
    private static boolean hasTopLevelKeyword(String content, String keyword) {
        return findTopLevelKeyword(content, keyword) >= 0;
    }

    /**
     * 查找顶层字符
     *
     * @param content 内容
     * @param target  字符
     * @param start   起点
     * @return 位置
     */
    private static int findTopLevelChar(String content, char target, int start) {
        return findTopLevelChar(content, target, start, content.length());
    }

    /**
     * 查找顶层字符
     *
     * @param content 内容
     * @param target  字符
     * @param start   起点
     * @param end     终点
     * @return 位置
     */
    private static int findTopLevelChar(String content, char target, int start, int end) {
        int depth = 0;
        for (int i = start; i < end; i++) {
            int skipped = skipQuoted(content, i);
            if (skipped > i) {
                i = skipped - 1;
                continue;
            }
            char ch = content.charAt(i);
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth = Math.max(0, depth - 1);
            } else if (depth == 0 && ch == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 查找匹配括号
     *
     * @param content 内容
     * @param start   左括号位置
     * @return 右括号位置
     */
    private static int findMatchingParen(String content, int start) {
        int depth = 0;
        for (int i = start; i < content.length(); i++) {
            int skipped = skipQuoted(content, i);
            if (skipped > i) {
                i = skipped - 1;
                continue;
            }
            char ch = content.charAt(i);
            if (ch == '(') {
                depth++;
            } else if (ch == ')' && --depth == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 跳过引号或方括号内容
     *
     * @param content 内容
     * @param start   起点
     * @return 跳过后的位置
     */
    private static int skipQuoted(String content, int start) {
        char first = content.charAt(start);
        if (first != '\'' && first != '"' && first != '[') {
            return start;
        }
        char closing = first == '[' ? ']' : first;
        for (int i = start + 1; i < content.length(); i++) {
            if (content.charAt(i) == closing) {
                if (i + 1 < content.length() && content.charAt(i + 1) == closing) {
                    i++;
                    continue;
                }
                return i + 1;
            }
        }
        return content.length();
    }

    /**
     * 移除注释，同时保留字符串和标识符
     *
     * @param content SQL
     * @return 结果
     */
    private static String removeComments(String content) {
        StringBuilder builder = new StringBuilder(content.length());
        for (int i = 0; i < content.length(); i++) {
            int skipped = skipQuoted(content, i);
            if (skipped > i) {
                builder.append(content, i, skipped);
                i = skipped - 1;
            } else if (content.startsWith("--", i)) {
                int end = content.indexOf('\n', i + 2);
                i = end < 0 ? content.length() : end;
                if (end >= 0) {
                    builder.append('\n');
                }
            } else if (content.startsWith("/*", i)) {
                int end = content.indexOf("*/", i + 2);
                i = end < 0 ? content.length() : end + 1;
                builder.append(' ');
            } else {
                builder.append(content.charAt(i));
            }
        }
        return builder.toString();
    }

    private static boolean startsWithKeyword(String content, int start, String keyword) {
        int end = start + keyword.length();
        if (start < 0 || end > content.length()
                || !content.regionMatches(true, start, keyword, 0, keyword.length())) {
            return false;
        }
        return (start == 0 || !isIdentifierPart(content.charAt(start - 1)))
                && (end == content.length() || !isIdentifierPart(content.charAt(end)));
    }

    private static int skipSpaces(String content, int start) {
        int offset = start;
        while (offset < content.length() && Character.isWhitespace(content.charAt(offset))) {
            offset++;
        }
        return offset;
    }

    private static boolean isIdentifierStart(char ch) {
        return Character.isLetter(ch) || ch == '_' || ch == '#' || ch == '@';
    }

    private static boolean isIdentifierPart(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '$' || ch == '#' || ch == '@';
    }

    private record ParsedName(List<String> parts, int end) {
    }

    private record ParsedPart(String value, int end) {
    }
}
