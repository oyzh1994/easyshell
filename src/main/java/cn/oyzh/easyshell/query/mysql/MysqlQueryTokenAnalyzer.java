package cn.oyzh.easyshell.query.mysql;


import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.query.ShellQueryTokenAnalyzer;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * db查询文本域
 *
 * @author oyzh
 * @since 2024/02/18
 */
public class MysqlQueryTokenAnalyzer extends ShellQueryTokenAnalyzer<MysqlQueryPromptItem, MysqlQueryToken> {

    public static final MysqlQueryTokenAnalyzer INSTANCE = new MysqlQueryTokenAnalyzer();

    @Override
    public MysqlQueryToken currentToken(String content, int currentIndex) {
        try {
            if (StringUtil.isEmpty(content)) {
                return null;
            }
            if (currentIndex <= 0) {
                return null;
            }
            if (currentIndex > content.length()) {
                return null;
            }
            MysqlQueryToken token = new MysqlQueryToken();
            // 截取字符串
            content = content.substring(0, currentIndex);
            // 当前位置
            int tokenIndex = 0;
            // token类型
            Character tokenType = null;
            char[] chars = ArrayUtil.reverse(content.toCharArray());
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                // 寻找操作符
                if (c == '\n' || c == ' ' || c == '`' || c == '.' || c == ',') {
                    tokenType = c;
                    tokenIndex = chars.length - i - 1;
                    break;
                }
            }
            // 特殊类型，默认为关键字
            if (tokenType == null) {
                tokenType = '\0';
            }
            String tokenContent;
            if (tokenType != '\0') {
                tokenContent = content.substring(tokenIndex + 1);
            } else {
                tokenContent = content.substring(tokenIndex);
            }
            token.setToken(tokenType);
            if (tokenType != '\0') {
                token.setStartIndex(tokenIndex + 1);
                token.setEndIndex(currentIndex);
            } else {
                token.setStartIndex(tokenIndex);
                token.setEndIndex(currentIndex);
            }
            token.setContent(tokenContent.trim());
            return token;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<MysqlQueryPromptItem> initPrompts(MysqlQueryToken token, float minCorr) {
        if (token == null || token.isEmpty()) {
            return Collections.emptyList();
        }
        // 当前提示词
        String text = token.getContent().toUpperCase();
        // 提示词列表
        final List<MysqlQueryPromptItem> items = new CopyOnWriteArrayList<>();
        // 任务列表
        List<Runnable> tasks = new ArrayList<>();
        // 关键字
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> MysqlQueryUtil.getKeywords().parallelStream().forEach(keyword -> {
                // 计算相关度
                double corr = TextUtil.clacCorr(keyword, text);
                if (corr > minCorr) {
                    MysqlQueryPromptItem item = new MysqlQueryPromptItem();
                    item.setType((byte) 4);
                    item.setContent(keyword);
                    item.setCorrelation(corr);
                    items.add(item);
                }
            }));
        }
        // 库
        if (token.isPossibilityDatabase()) {
            tasks.add(() -> MysqlQueryUtil.getDatabases().parallelStream().forEach(database -> {
                // 计算相关度
                double corr = TextUtil.clacCorr(database.getName(), text);
                if (corr > minCorr) {
                    MysqlQueryPromptItem item = new MysqlQueryPromptItem();
                    item.setType((byte) 1);
                    item.setContent(database.getName());
                    item.setCorrelation(corr);
                    items.add(item);
                }
            }));
        }
        // 表
        if (token.isPossibilityTable()) {
            tasks.add(() -> MysqlQueryUtil.getTables().parallelStream().forEach(dbTable -> {
                // 计算相关度
                double corr = TextUtil.clacCorr(dbTable.getName(), text);
                if (corr > minCorr) {
                    MysqlQueryPromptItem item = new MysqlQueryPromptItem();
                    item.setType((byte) 2);
                    item.setCorrelation(corr);
                    item.setContent(dbTable.getName());
                    item.setExtContent(dbTable.getDbName());
                    items.add(item);
                }
            }));
        }
        // 视图
        if (token.isPossibilityView()) {
            tasks.add(() -> MysqlQueryUtil.getViews().parallelStream().forEach(dbTable -> {
                // 计算相关度
                double corr = TextUtil.clacCorr(dbTable.getName(), text);
                if (corr > minCorr) {
                    MysqlQueryPromptItem item = new MysqlQueryPromptItem();
                    item.setType((byte) 5);
                    item.setCorrelation(corr);
                    item.setContent(dbTable.getName());
                    item.setExtContent(dbTable.getDbName());
                    items.add(item);
                }
            }));
        }
        // 函数
        if (token.isPossibilityFunction()) {
            tasks.add(() -> MysqlQueryUtil.getFunctions().parallelStream().forEach(function -> {
                // 计算相关度
                double corr = TextUtil.clacCorr(function.getName(), text);
                if (corr > minCorr) {
                    MysqlQueryPromptItem item = new MysqlQueryPromptItem();
                    item.setType((byte) 6);
                    item.setCorrelation(corr);
                    item.setContent(function.getName());
                    item.setExtContent(function.getDbName());
                    items.add(item);
                }
            }));
        }
        // 过程
        if (token.isPossibilityProcedure()) {
            tasks.add(() -> MysqlQueryUtil.getProcedures().parallelStream().forEach(procedure -> {
                // 计算相关度
                double corr = TextUtil.clacCorr(procedure.getName(), text);
                if (corr > minCorr) {
                    MysqlQueryPromptItem item = new MysqlQueryPromptItem();
                    item.setType((byte) 7);
                    item.setCorrelation(corr);
                    item.setContent(procedure.getName());
                    item.setExtContent(procedure.getDbName());
                    items.add(item);
                }
            }));
        }
        // 字段
        if (token.isPossibilityColumn()) {
            tasks.add(() -> MysqlQueryUtil.getColumns().parallelStream().forEach(column -> {
                // 计算相关度
                double corr = TextUtil.clacCorr(column.getName(), text);
                if (corr > minCorr) {
                    MysqlQueryPromptItem item = new MysqlQueryPromptItem();
                    item.setType((byte) 3);
                    item.setCorrelation(corr);
                    item.setContent(column.getName());
                    item.setExtContent(ShellMysqlUtil.wrap(column.getDbName(), column.getTableName(), DBDialect.MYSQL));
                    items.add(item);
                }
            }));
        }
        // 执行任务
        ThreadUtil.submit(tasks);
        // 根据相关度排序
        List<MysqlQueryPromptItem> itemList = items.parallelStream().sorted(Comparator.comparingDouble(MysqlQueryPromptItem::getCorrelation)).collect(Collectors.toList());
        // 反转列表
        return itemList.reversed();
    }
}
