package cn.oyzh.easyshell.dto.zk;

import cn.oyzh.i18n.I18nManager;
import org.apache.zookeeper.server.quorum.QuorumPeer;

import java.util.Locale;

/**
 * zk集群节点
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKClusterNode {

    /**
     * id
     */
    private Long id;

    /**
     * 类型
     */
    private String type;

    /**
     * 交互地址
     */
    private String addr;

    /**
     * 权重
     */
    private Long weight;

    /**
     * 客户端连接地址
     */
    private String clientAddr;

    /**
     * 服务端选举地址
     */
    private String electionAddr;

    /**
     * 构造函数（从QuorumServer对象）
     *
     * @param server 集群服务器对象
     */
    public ShellZKClusterNode(QuorumPeer.QuorumServer server) {
        this.id = server.id;
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE) {
            this.type = server.type == QuorumPeer.LearnerType.PARTICIPANT ? "选举节点" : "观察节点";
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE) {
            this.type = server.type == QuorumPeer.LearnerType.PARTICIPANT ? "選舉節點" : "觀察節點";
        } else {
            this.type = server.type == QuorumPeer.LearnerType.PARTICIPANT ? "Participant" : "Observer";
        }
        this.addr = server.addr.toString();
        this.clientAddr = server.clientAddr.toString();
        this.electionAddr = server.electionAddr.toString();
    }

    /**
     * 构造函数（从服务器配置文本）
     *
     * @param serverTxt 服务器配置文本
     */
    public ShellZKClusterNode(String serverTxt) {
        String serverName = serverTxt.split(":")[0];
        serverName = serverName.substring(serverName.indexOf("=") + 1);
        this.weight = 1L;
        this.addr = serverName + ":" + serverTxt.split(":")[1];
        this.electionAddr = serverName + ":" + serverTxt.split(":")[2];
        this.id = Long.parseLong(serverTxt.substring(7, serverTxt.indexOf("=")));
        this.clientAddr = serverTxt.substring(serverTxt.indexOf(";") + 1);
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE) {
            this.type = serverTxt.toLowerCase().contains("participant") ? "选举节点" : "观察节点";
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE) {
            this.type = serverTxt.toLowerCase().contains("participant") ? "選舉節點" : "觀察節點";
        } else {
            this.type = serverTxt.toLowerCase().contains("participant") ? "Participant" : "Observer";
        }
    }

    /** 获取id */
    public Long getId() {
        return id;
    }

    /** 设置id */
    public void setId(Long id) {
        this.id = id;
    }

    /** 获取类型 */
    public String getType() {
        return type;
    }

    /** 设置类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** 获取交互地址 */
    public String getAddr() {
        return addr;
    }

    /** 设置交互地址 */
    public void setAddr(String addr) {
        this.addr = addr;
    }

    /** 获取权重 */
    public Long getWeight() {
        return weight;
    }

    /** 设置权重 */
    public void setWeight(Long weight) {
        this.weight = weight;
    }

    /** 获取客户端连接地址 */
    public String getClientAddr() {
        return clientAddr;
    }

    /** 设置客户端连接地址 */
    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }

    /** 获取服务端选举地址 */
    public String getElectionAddr() {
        return electionAddr;
    }

    /** 设置服务端选举地址 */
    public void setElectionAddr(String electionAddr) {
        this.electionAddr = electionAddr;
    }
}
