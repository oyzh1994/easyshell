package cn.oyzh.easyshell.query;


import java.util.List;

/**
 * 查询token解析器
 *
 * @author oyzh
 * @since 2025/01/21
 */
public abstract class ShellQueryTokenAnalyzer<E extends ShellQueryPromptItem, T extends ShellQueryToken> {

    /**
     * 获取当前提示词
     *
     * @param input        内容
     * @param currentIndex 当前索引
     * @return 结果
     */
    public abstract T currentToken(String input, int currentIndex);

    /**
     * 初始化提示词
     *
     * @param token   提示词
     * @param minCorr 最低相关度
     * @return 结果
     */
    public abstract List<E> initPrompts(T token, float minCorr);
}
