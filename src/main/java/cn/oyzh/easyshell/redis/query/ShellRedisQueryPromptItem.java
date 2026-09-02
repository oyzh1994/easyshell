package cn.oyzh.easyshell.redis.query;


import cn.oyzh.fx.db.query.DBQueryPromptItem;

/**
 * redis查询提示内容
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellRedisQueryPromptItem extends DBQueryPromptItem {

    /**
     * 是否关键字类型
     *
     * @return 结果
     */
    public boolean isKeywordType() {
        return 1 == this.getType();
    }

    /**
     * 是否参数类型
     *
     * @return 结果
     */
    public boolean isParamType() {
        return 2 == this.getType();
    }

    /**
     * 是否键类型
     *
     * @return 结果
     */
    public boolean isKeyType() {
        return 3 == this.getType();
    }
}
