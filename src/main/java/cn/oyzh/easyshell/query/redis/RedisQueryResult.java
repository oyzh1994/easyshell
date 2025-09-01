package cn.oyzh.easyshell.query.redis;

import cn.oyzh.i18n.I18nHelper;

/**
 * zk查询结果
 *
 * @author oyzh
 * @since 2025/01/20
 */
public class RedisQueryResult {

    /**
     * 耗时
     */
    private long cost;

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

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

    public String costSeconds() {
        return String.format("%.2f" + I18nHelper.seconds(), this.cost / 1000.0);
    }

    public boolean hasData() {
        return this.result != null;
    }
}
