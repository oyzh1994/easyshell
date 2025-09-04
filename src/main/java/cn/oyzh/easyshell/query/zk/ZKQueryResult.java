package cn.oyzh.easyshell.query.zk;

import cn.oyzh.easyshell.dto.zk.ZKEnvNode;
import cn.oyzh.i18n.I18nHelper;
import org.apache.zookeeper.StatsTrack;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.ClientInfo;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * zk查询结果
 *
 * @author oyzh
 * @since 2025/01/20
 */
public class ZKQueryResult {

    /**
     * 耗时
     */
    private long cost;

    /**
     * 状态
     */
    private Stat stat;

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
        return String.format("%.2f " + I18nHelper.seconds(), this.cost / 1000.0);
    }

    public byte[] asData() {
        return (byte[]) this.result;
    }

    public Integer asCount() {
        return (Integer) this.result;
    }

    public List<ClientInfo> asClientInfo() {
        return (List<ClientInfo>) this.result;
    }

    public List<ZKEnvNode> asEnvInfo() {
        return (List<ZKEnvNode>) this.result;
    }

    public StatsTrack asQuota() {
        return (StatsTrack) this.result;
    }

    public List<ACL> asACL() {
        return (List<ACL>) this.result;
    }

    public List<String> asNode() {
        return (List<String>) this.result;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public Stat getStat() {
        return stat;
    }

    public void setStat(Stat stat) {
        this.stat = stat;
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
}
