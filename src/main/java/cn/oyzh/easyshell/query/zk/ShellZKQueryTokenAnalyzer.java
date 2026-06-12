package cn.oyzh.easyshell.query.zk;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.query.ShellQueryTokenAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * zk查询token解析器
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellZKQueryTokenAnalyzer extends ShellQueryTokenAnalyzer<ShellZKQueryPromptItem, ShellZKQueryToken> {

    public static final ShellZKQueryTokenAnalyzer INSTANCE = new ShellZKQueryTokenAnalyzer();

    @Override
    public ShellZKQueryToken currentToken(String input, int currentIndex) {
        try {
            if (StringUtil.isEmpty(input)) {
                return null;
            }
            if (currentIndex <= 0) {
                return null;
            }
            if (currentIndex > input.length()) {
                return null;
            }
            ShellZKQueryToken token = new ShellZKQueryToken();
            // 截取字符串
            String content = input.substring(0, currentIndex);
            // 当前位置
            int tokenIndex = 0;
            Character tokenType = null;
            if (content.contains(" ") || content.contains("-")) {
                char[] chars = ArrayUtil.reverse(content.toCharArray());
                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    // 遇到换行符则停止
                    if (c == '\n') {
                        return null;
                    }
                    // 寻找操作符1
                    if (c == ' ') {
                        tokenType = c;
                        tokenIndex = chars.length - i;
                        break;
                    }
                    // 寻找操作符2
                    if (c == '-') {
                        tokenType = c;
                        tokenIndex = chars.length - i - 1;
                        break;
                    }
                }
            }
            String tokenContent = content.substring(tokenIndex);
            token.setToken(tokenType);
            token.setEndIndex(currentIndex);
            token.setStartIndex(tokenIndex);
            token.setContent(tokenContent.trim());
            return token;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ShellZKQueryPromptItem> initPrompts(ShellZKQueryToken token, float minCorr) {
        if (token == null) {
            return Collections.emptyList();
        }
        // 当前提示词
        String text = token.getContent().toUpperCase();
        // 提示词列表
        final List<ShellZKQueryPromptItem> items = new CopyOnWriteArrayList<>();
        // 任务列表
        List<Runnable> tasks = new ArrayList<>();
        // 关键字
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> ShellZKQueryUtil.getKeywords().parallelStream().forEach(keyword -> {
                // 计算相关度
                double corr = TextUtil.clacCorr(keyword, text);
                if (corr > minCorr) {
                    ShellZKQueryPromptItem item = new ShellZKQueryPromptItem();
                    item.setType((byte) 1);
                    item.setContent(keyword);
                    item.setCorrelation(corr);
                    items.add(item);
                }
            }));
        }
        // 参数
        if (token.isPossibilityParam()) {
            tasks.add(() -> ShellZKQueryUtil.getParams().parallelStream().forEach(param -> {
                ShellZKQueryPromptItem item = new ShellZKQueryPromptItem();
                item.setType((byte) 2);
                item.setContent(param);
                item.setCorrelation(1);
                items.add(item);
            }));
        }
        // 节点
        if (token.isPossibilityNode()) {
            tasks.add(() -> ShellZKQueryUtil.getNodes().parallelStream().forEach(node -> {
                // 计算相关度
                double corr = TextUtil.clacCorr(node, text);
                if (corr > minCorr) {
                    ShellZKQueryPromptItem item = new ShellZKQueryPromptItem();
                    item.setType((byte) 3);
                    item.setContent(node);
                    item.setCorrelation(corr);
                    items.add(item);
                }
            }));
        }
        // 执行任务
        ThreadUtil.submit(tasks);
        // 根据相关度排序
        return items.parallelStream()
                .sorted(Comparator.comparingDouble(ShellZKQueryPromptItem::getCorrelation))
                .collect(Collectors.toList())
                .reversed();
    }
}
