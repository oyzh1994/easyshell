package cn.oyzh.easyshell.query.redis;

import cn.oyzh.i18n.I18nHelper;

/**
 * redis查询结果
 *
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellRedisQueryResult {

    /**
     * 耗时
     */
    private long cost;

    /**
     * 结果
     */
    private Object result;

    /**
     * 消息
     */
    private String message;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 获取耗时
     *
     * @return 耗时
     */
    public long getCost() {
        return cost;
    }

    /**
     * 设置耗时
     *
     * @param cost 耗时
     */
    public void setCost(long cost) {
        this.cost = cost;
    }

    /**
     * 获取结果
     *
     * @return 结果
     */
    public Object getResult() {
        return result;
    }

    /**
     * 设置结果
     *
     * @param result 结果
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * 获取消息
     *
     * @return 消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置消息
     *
     * @param message 消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 是否成功
     *
     * @return 结果
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置是否成功
     *
     * @param success 是否成功
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取耗时秒数字符串
     *
     * @return 耗时秒数字符串
     */
    public String costSeconds() {
        return String.format("%.2f" + I18nHelper.seconds(), this.cost / 1000.0);
    }

    /**
     * 是否有数据
     *
     * @return 结果
     */
    public boolean hasData() {
        return this.result != null;
    }
}
