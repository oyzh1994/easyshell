package cn.oyzh.easyshell.query.mongo;


import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;

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
public class MongoQueryTokenAnalyzer {

    public static final MongoQueryTokenAnalyzer INSTANCE = new MongoQueryTokenAnalyzer();

    public MongoQueryToken currentToken(String content, int currentIndex) {
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
            MongoQueryToken token = new MongoQueryToken();
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
                if (c == '\n' || c == ' ' || c == '.' || c == '"') {
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

    /**
     * 初始化提示词
     *
     * @param token   提示词
     * @param minCorr 最低相关度
     * @return 结果
     */
    public List<MongoQueryPromptItem> initPrompts(MongoQueryToken token, float minCorr) {
        if (token == null || token.isEmpty()) {
            return Collections.emptyList();
        }
        // 当前提示词
        String text = token.getContent().toUpperCase();
        // 提示词列表
        final List<MongoQueryPromptItem> items = new CopyOnWriteArrayList<>();
        // 任务列表
        List<Runnable> tasks = new ArrayList<>();
        // 关键字
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> MongoQueryUtil.getKeywords().parallelStream().forEach(keyword -> {
                // 计算相关度
                double corr = TextUtil.clacCorr(keyword, text);
                if (corr > minCorr) {
                    MongoQueryPromptItem item = new MongoQueryPromptItem();
                    item.setType((byte) 4);
                    item.setContent(keyword);
                    item.setCorrelation(corr);
                    items.add(item);
                }
            }));
        }
        // 集合
        if (token.isPossibilityCollection()) {
            tasks.add(() -> MongoQueryUtil.getCollections().parallelStream().forEach(collection -> {
                // 计算相关度
                double corr = TextUtil.clacCorr(collection.getName(), text);
                if (corr > minCorr) {
                    MongoQueryPromptItem item = new MongoQueryPromptItem();
                    item.setType((byte) 1);
                    item.setContent(collection.getName());
                    item.setCorrelation(corr);
                    items.add(item);
                }
            }));
        }
        // 函数
        if (token.isPossibilityFunction()) {
            tasks.add(() -> MongoQueryUtil.getFunctions().parallelStream().forEach(member -> {
                // 计算相关度
                double corr = TextUtil.clacCorr(member, text);
                if (corr > minCorr) {
                    MongoQueryPromptItem item = new MongoQueryPromptItem();
                    item.setType((byte) 2);
                    item.setContent(member + "()");
                    item.setCorrelation(corr);
                    items.add(item);
                }
            }));
        }
        // 执行任务
        ThreadUtil.submit(tasks);
        // 根据相关度排序
        List<MongoQueryPromptItem> itemList = items.parallelStream().sorted(Comparator.comparingDouble(MongoQueryPromptItem::getCorrelation)).collect(Collectors.toList());
        // 反转列表
        return itemList.reversed();
    }
}
