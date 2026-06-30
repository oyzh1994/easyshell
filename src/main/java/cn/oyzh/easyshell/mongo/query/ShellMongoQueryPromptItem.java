package cn.oyzh.easyshell.mongo.query;


import cn.oyzh.easyshell.query.ShellQueryPromptItem;

/**
 * 查询提示内容
 *
 * @author oyzh
 * @since 2024/02/21
 */
public class ShellMongoQueryPromptItem extends ShellQueryPromptItem {

    /**
     * 是否函数类型
     *
     * @return 结果
     */
    public boolean isFunctionType() {
        return 2 == this.getType();
    }

    /**
     * 是否集合类型
     *
     * @return 结果
     */
    public boolean isCollectionType() {
        return 1 == this.getType();
    }

    /**
     * 是否关键字类型
     *
     * @return 结果
     */
    public boolean isKeywordType() {
        return 4 == this.getType();
    }

}
