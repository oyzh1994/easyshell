package cn.oyzh.easyshell.data.zk.handler;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.db.data.handler.DataTransportHandler;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.Charset;
import java.util.List;

/**
 * zk数据传输处理器
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKDataTransportHandler extends DataTransportHandler {

    /**
     * 来源客户端
     */
    protected ShellZKClient sourceClient;

    /**
     * 目标客户端
     */
    protected ShellZKClient targetClient;

    /**
     * 来源节点路径，默认根节点
     */
    private String nodePath = "/";

    /**
     * 节点存在时处理策略
     * 0 跳过
     * 1 更新
     */
    private String existsPolicy;

    /**
     * 来源字符集
     */
    private Charset sourceCharset;

    /**
     * 目标字符集
     */
    private Charset targetCharset;

    @Override
    public void doTransport() throws Exception {
        this.message("Transport Starting");
        String path = StringUtil.isBlank(this.nodePath) ? "/" : this.nodePath;
        this.doTransport(path);
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
            // 获取节点。checkExists 必须先于 getData：节点不存在时 getData 会抛出
            // NoNodeException，若直接落到外层 catch 会被当成处理失败而递减计数。
            Stat stat = this.sourceClient.checkExists(path);
            if (stat == null) {
                this.message("node[" + decodePath + "] does not exist");
                this.processedSkip();
                return;
            }
            byte[] bytes;
            try {
                bytes = this.sourceClient.getData(path);
            } catch (KeeperException.NoNodeException ex) {
                this.message("node[" + decodePath + "] does not exist");
                this.processedSkip();
                return;
            }
            // 数据为空，按跳过处理（此处尚未计入处理数）
            if (bytes == null) {
                this.message("node[" + decodePath + "] has no data");
                this.processedSkip();
                return;
            }

            // 临时节点跳过
            if (stat.getEphemeralOwner() > 0) {
                this.message("node[" + decodePath + "] is ephemeral, skip it");
                this.processedSkip();
                return;
            }

            // // 过滤处理
            // if (ShellZKNodeUtil.isFiltered(decodePath, this.filters)) {
            //     this.message("node[" + decodePath + "] is filtered, skip it");
            //     this.processedSkip();
            //     return;
            // }

            // 节点存在
            if (this.targetClient.exists(path)) {
                // 跳过
                if (StringUtil.equals(this.existsPolicy, "0")) {
                    this.message("node[" + decodePath + "] is exist, skip it");
                    this.processedSkip();
                } else if (StringUtil.equals(this.existsPolicy, "1")) { // 更新
                    bytes = this.convert(bytes);
                    this.targetClient.setData(path, bytes);
                    this.message("node[" + decodePath + "] is exist, update it");
                    this.processedIncr();
                }
            } else {// 创建
                bytes = this.convert(bytes);
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

    /**
     * 按源/目标字符集转换节点数据。字符集相同时直接返回原字节，
     * 避免 TextUtil 用引用比较判断相同字符集时，对二进制数据做有损的解码再编码。
     *
     * @param bytes 原始字节
     * @return 转换后的字节
     */
    private byte[] convert(byte[] bytes) {
        if (this.sourceCharset != null && this.targetCharset != null
                && this.sourceCharset.name().equalsIgnoreCase(this.targetCharset.name())) {
            return bytes;
        }
        return TextUtil.changeCharset(bytes, this.sourceCharset, this.targetCharset);
    }

    /**
     * 获取来源节点路径
     *
     * @return 来源节点路径
     */
    public String getNodePath() {
        return nodePath;
    }

    /**
     * 设置来源节点路径
     *
     * @param nodePath 来源节点路径
     */
    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    /**
     * 获取来源客户端
     *
     * @return 来源客户端
     */
    public ShellZKClient getSourceClient() {
        return sourceClient;
    }

    /**
     * 设置来源客户端
     *
     * @param sourceClient 来源客户端
     */
    public void setSourceClient(ShellZKClient sourceClient) {
        this.sourceClient = sourceClient;
    }

    /**
     * 获取目标客户端
     *
     * @return 目标客户端
     */
    public ShellZKClient getTargetClient() {
        return targetClient;
    }

    /**
     * 设置目标客户端
     *
     * @param targetClient 目标客户端
     */
    public void setTargetClient(ShellZKClient targetClient) {
        this.targetClient = targetClient;
    }

    /**
     * 获取存在时处理策略
     *
     * @return 存在时处理策略
     */
    public String getExistsPolicy() {
        return existsPolicy;
    }

    /**
     * 设置存在时处理策略
     *
     * @param existsPolicy 存在时处理策略
     */
    public void setExistsPolicy(String existsPolicy) {
        this.existsPolicy = existsPolicy;
    }

    /**
     * 获取来源字符集
     *
     * @return 来源字符集
     */
    public Charset getSourceCharset() {
        return sourceCharset;
    }

    /**
     * 设置来源字符集
     *
     * @param sourceCharset 来源字符集
     */
    public void setSourceCharset(Charset sourceCharset) {
        this.sourceCharset = sourceCharset;
    }

    /**
     * 获取目标字符集
     *
     * @return 目标字符集
     */
    public Charset getTargetCharset() {
        return targetCharset;
    }

    /**
     * 设置目标字符集
     *
     * @param targetCharset 目标字符集
     */
    public void setTargetCharset(Charset targetCharset) {
        this.targetCharset = targetCharset;
    }
}

