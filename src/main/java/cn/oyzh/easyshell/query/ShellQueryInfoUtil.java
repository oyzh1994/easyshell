package cn.oyzh.easyshell.query;

import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.db.query.DBQueryResult;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.editor.incubator.Editor;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询信息输出工具。
 *
 * <p>不要逐行使用 {@link Editor#appendLine(String)} 再追加空行，否则编辑器文本末尾会多出
 * 两个空行。这里统一收集文本行后一次性写入，不在末尾保留换行符。</p>
 *
 * @author oyzh
 * @since 2026-11-11
 */
public final class ShellQueryInfoUtil {

    private ShellQueryInfoUtil() {
    }

    /**
     * 将查询结果信息写入信息编辑区。
     *
     * @param editor  信息编辑区
     * @param results 查询结果
     */
    public static void initInfo(Editor editor, DBQueryResults<?> results) {
        editor.clear();
        List<String> lines = new ArrayList<>();
        if (results.isSuccess()) {
            List<? extends DBQueryResult> queryResults = results.getResults();
            for (int i = 0; i < queryResults.size(); i++) {
                DBQueryResult result = queryResults.get(i);
                // 多个结果之间保留一个空行
                if (i > 0) {
                    lines.add("");
                }
                addLine(lines, result.getContent());
                if (result.isSuccess()) {
                    if (result.getUpdateCount() > 0) {
                        addLine(lines, "> Affected rows: " + result.getUpdateCount());
                    } else {
                        addLine(lines, "> OK");
                    }
                } else {
                    addLine(lines, "> " + result.getMsg());
                }
                addLine(lines, ShellI18nHelper.queryTimeLine(result.getUsedMs()));
            }
        } else {
            addLine(lines, results.getErrMsg());
        }
        if (!lines.isEmpty()) {
            editor.appendContent(String.join(editor.lineEndingText(), lines));
        }
    }

    private static void addLine(List<String> lines, String line) {
        if (line != null) {
            lines.add(line);
        }
    }
}
