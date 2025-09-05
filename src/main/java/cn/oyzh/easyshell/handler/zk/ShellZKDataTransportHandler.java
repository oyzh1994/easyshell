package cn.oyzh.easyshell.handler.zk;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.domain.zk.ZKFilter;
import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.easyshell.zk.ZKClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.Charset;
import java.util.List;

/**
 * zk数据传输业务
 *
 * @author oyzh
 * @since 2024/10/15
 */
public class ShellZKDataTransportHandler extends ShellZKDataHandler {

    /**
     * 来源客户端
     */
    protected ZKClient sourceClient;

    /**
     * 目标客户端
     */
    protected ZKClient targetClient;

    /**
     * 节点存在时处理策略
     * 0 跳过
     * 1 更新
     */
    private String existsPolicy;

    /**
     * 过滤内容列表
     */
    private List<ZKFilter> filters;

    /**
     * 来源字符集
     */
    private Charset sourceCharset;

    /**
     * 目标字符集
     */
    private Charset targetCharset;

    /**
     * 执行传输
     */
    public void doTransport() throws Exception {
        this.message("Transport Starting");
        this.doTransport("/");
        this.message("Transport Finished");
    }

    /**
     * 执行传输
     *
     * @param path 节点路径
     * @throws InterruptedException 异常
     */
    private void doTransport(String path) throws InterruptedException {
        String decodePath = ShellZKNodeUtil.decodePath(path);
        try {
            // 检查中断
            this.checkInterrupt();
            // 获取节点
            Stat stat = this.sourceClient.checkExists(path);
            byte[] bytes = this.sourceClient.getData(path);

            // 节点查询失败
            if (stat == null || bytes == null) {
                this.message("node[" + decodePath + "] does not exist");
                this.processedDecr();
                return;
            }

            // 临时节点跳过
            if (stat.getEphemeralOwner() > 0) {
                this.message("node[" + decodePath + "] is ephemeral, skip it");
                this.processedSkip();
                return;
            }

            // 过滤处理
            if (ShellZKNodeUtil.isFiltered(decodePath, this.filters)) {
                this.message("node[" + decodePath + "] is filtered, skip it");
                this.processedSkip();
                return;
            }

            // 节点存在
            if (this.targetClient.exists(path)) {
                // 跳过
                if (StringUtil.equals(this.existsPolicy, "0")) {
                    this.message("node[" + decodePath + "] is exist, skip it");
                    this.processedSkip();
                } else if (StringUtil.equals(this.existsPolicy, "1")) { // 更新
                    bytes = TextUtil.changeCharset(bytes, this.sourceCharset, this.targetCharset);
                    this.targetClient.setData(path, bytes);
                    this.message("node[" + decodePath + "] is exist, update it");
                    this.processedIncr();
                }
            } else {// 创建
                bytes = TextUtil.changeCharset(bytes, this.sourceCharset, this.targetCharset);
                this.targetClient.createIncludeParents(path, bytes, CreateMode.PERSISTENT);
                this.message("node[" + decodePath + "] not exist, create it");
                this.processedIncr();
            }
            // 获取子节点
            List<String> subs = this.sourceClient.getChildren(path);
            // 递归传输节点
            for (String sub : subs) {
                this.checkInterrupt();
                this.doTransport(ShellZKNodeUtil.concatPath(path, sub));
            }
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            this.message("node[" + decodePath + "] transport fail, error[" + ex.getMessage() + "]");
            this.processedDecr();
        }
    }

    public ZKClient getSourceClient() {
        return sourceClient;
    }

    public void setSourceClient(ZKClient sourceClient) {
        this.sourceClient = sourceClient;
    }

    public ZKClient getTargetClient() {
        return targetClient;
    }

    public void setTargetClient(ZKClient targetClient) {
        this.targetClient = targetClient;
    }

    public String getExistsPolicy() {
        return existsPolicy;
    }

    public void setExistsPolicy(String existsPolicy) {
        this.existsPolicy = existsPolicy;
    }

    public List<ZKFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<ZKFilter> filters) {
        this.filters = filters;
    }

    public Charset getSourceCharset() {
        return sourceCharset;
    }

    public void setSourceCharset(Charset sourceCharset) {
        this.sourceCharset = sourceCharset;
    }

    public Charset getTargetCharset() {
        return targetCharset;
    }

    public void setTargetCharset(Charset targetCharset) {
        this.targetCharset = targetCharset;
    }
}

