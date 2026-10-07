package cn.oyzh.easyshell.dto.zk;

import cn.oyzh.common.util.StringUtil;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

/**
 * zk服务信息
 *
 * @author oyzh
 * @since 2023/08/01
 */
public class ShellZKServerInfo {

    /**
     * 服务id
     */
    private SimpleStringProperty zxidProperty;

    /**
     * 服务类型
     */
    private SimpleStringProperty modeProperty;

    /**
     * 服务版本
     */
    private SimpleStringProperty versionProperty;

    /**
     * 延迟信息，单位毫秒
     * 最小/平均/最大
     */
    private SimpleStringProperty latencyInfoProperty;

    /**
     * 节点数量
     */
    private SimpleStringProperty nodeCountProperty;

    /**
     * 已连接客户端
     */
    private SimpleStringProperty connectionsProperty;

    /**
     * 命令信息
     * 已接收/已发送/等待中
     */
    private SimpleStringProperty commandInfoProperty;

    /**
     * 更新服务信息
     *
     * @param envNodes 环境节点列表
     */
    public void update(List<ShellZKEnvNode> envNodes) {
        String sent = null;
        String received = null;
        String outstanding = null;
        for (ShellZKEnvNode envNode : envNodes) {
            if (StringUtil.equalsIgnoreCase(envNode.getName(), "Zxid")) {
                this.setZxid(envNode.getValue());
            } else if (StringUtil.equalsIgnoreCase(envNode.getName(), "Mode")) {
                this.setMode(envNode.getValue());
            } else if (StringUtil.containsIgnoreCase(envNode.getName(), "version")) {
                this.setVersion(envNode.getValue());
            } else if (StringUtil.equalsIgnoreCase(envNode.getName(), "Connections")) {
                this.setConnections(envNode.getValue());
            } else if (StringUtil.equalsIgnoreCase(envNode.getName(), "Node count")) {
                this.setNodeCount(envNode.getValue());
            } else if (StringUtil.containsIgnoreCase(envNode.getName(), "Latency")) {
                this.setLatencyInfo(envNode.getValue());
            } else if (StringUtil.equalsIgnoreCase(envNode.getName(), "Received")) {
                received = envNode.getValue();
            } else if (StringUtil.equalsIgnoreCase(envNode.getName(), "Sent")) {
                sent = envNode.getValue();
            } else if (StringUtil.equalsIgnoreCase(envNode.getName(), "Outstanding")) {
                outstanding = envNode.getValue();
            }
        }
        StringBuilder commandInfo = new StringBuilder();
        if (received != null) {
            commandInfo.append(received.trim()).append("/");
        } else {
            commandInfo.append("N/");
        }
        if (sent != null) {
            commandInfo.append(sent.trim()).append("/");
        } else {
            commandInfo.append("N/");
        }
        if (outstanding != null) {
            commandInfo.append(outstanding.trim());
        } else {
            commandInfo.append("N");
        }
        this.setCommandInfo(commandInfo.toString());
    }

    /**
     * 获取已接收命令数
     *
     * @return 已接收命令数
     */
    public int commandReceived() {
        int val;
        try {
            String commandInfo = this.getCommandInfo();
            if (StringUtil.isBlank(commandInfo) || StringUtil.equalsAnyIgnoreCase(commandInfo, "N/A", "N/N/N")) {
                val = 0;
            } else {
                val = Integer.parseInt(commandInfo.split("/")[0]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            val = 0;
        }
        return val;
    }

    /**
     * 获取已发送命令数
     *
     * @return 已发送命令数
     */
    public int commandSent() {
        int val;
        try {
            String commandInfo = this.getCommandInfo();
            if (StringUtil.isBlank(commandInfo) || StringUtil.equalsAnyIgnoreCase(commandInfo, "N/A", "N/N/N")) {
                val = 0;
            } else {
                val = Integer.parseInt(commandInfo.split("/")[1]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            val = 0;
        }
        return val;
    }

    /**
     * 获取等待中命令数
     *
     * @return 等待中命令数
     */
    public int commandOutstanding() {
        int val;
        try {
            String commandInfo = this.getCommandInfo();
            if (StringUtil.isBlank(commandInfo) || StringUtil.equalsAnyIgnoreCase(commandInfo, "N/A", "N/N/N")) {
                val = 0;
            } else {
                val = Integer.parseInt(commandInfo.split("/")[2]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            val = 0;
        }
        return val;
    }

    /** 获取命令信息属性 */
    public SimpleStringProperty commandInfoProperty() {
        if (this.commandInfoProperty == null) {
            this.commandInfoProperty = new SimpleStringProperty();
        }
        return commandInfoProperty;
    }

    /** 设置命令信息 */
    public void setCommandInfo(String commandInfo) {
        this.commandInfoProperty().set(commandInfo);
    }

    /** 获取命令信息 */
    public String getCommandInfo() {
        return commandInfoProperty == null ? "N/A" : commandInfoProperty.getValue();
    }

    /** 获取已连接客户端属性 */
    public SimpleStringProperty connectionsProperty() {
        if (this.connectionsProperty == null) {
            this.connectionsProperty = new SimpleStringProperty();
        }
        return connectionsProperty;
    }

    /** 设置已连接客户端 */
    public void setConnections(String connections) {
        if (connections != null) {
            this.connectionsProperty().set(connections.trim());
        }
    }

    /**
     * 获取已连接客户端数
     *
     * @return 已连接客户端数
     */
    public int connections() {
        int val;
        try {
            String connections = this.getConnections();
            if (StringUtil.isBlank(connections) || connections.equals("N/A")) {
                val = 0;
            } else {
                val = Integer.parseInt(connections);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            val = 0;
        }
        return val;
    }

    /** 获取已连接客户端 */
    public String getConnections() {
        return connectionsProperty == null ? "N/A" : connectionsProperty.getValue();
    }

    /**
     * 获取节点数量
     *
     * @return 节点数量
     */
    public int nodeCount() {
        int val;
        try {
            String nodeCount = this.getNodeCount();
            if (StringUtil.isBlank(nodeCount) || nodeCount.equals("N/A")) {
                val = 0;
            } else {
                val = Integer.parseInt(nodeCount);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            val = 0;
        }
        return val;
    }

    /** 获取节点数量属性 */
    public SimpleStringProperty nodeCountProperty() {
        if (this.nodeCountProperty == null) {
            this.nodeCountProperty = new SimpleStringProperty();
        }
        return nodeCountProperty;
    }

    /** 设置节点数量 */
    public void setNodeCount(String nodeCount) {
        if (nodeCount != null) {
            this.nodeCountProperty().set(nodeCount.trim());
        }
    }

    /** 获取节点数量 */
    public String getNodeCount() {
        return nodeCountProperty == null ? "N/A" : nodeCountProperty.getValue();
    }

    /** 获取延迟信息属性 */
    public SimpleStringProperty latencyInfoProperty() {
        if (this.latencyInfoProperty == null) {
            this.latencyInfoProperty = new SimpleStringProperty();
        }
        return latencyInfoProperty;
    }

    /** 设置延迟信息 */
    public void setLatencyInfo(String latencyInfo) {
        this.latencyInfoProperty().set(latencyInfo);
    }

    /**
     * 获取最小延迟
     *
     * @return 最小延迟
     */
    public double latencyMin() {
        double val;
        try {
            String latencyInfo = this.getLatencyInfo();
            if (StringUtil.isBlank(latencyInfo) || latencyInfo.equals("N/A")) {
                val = 0;
            } else {
                val = Double.parseDouble(latencyInfo.split("/")[0]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            val = 0;
        }
        return val;
    }

    /**
     * 获取平均延迟
     *
     * @return 平均延迟
     */
    public double latencyAvg() {
        double val;
        try {
            String latencyInfo = this.getLatencyInfo();
            if (StringUtil.isBlank(latencyInfo) || latencyInfo.equals("N/A")) {
                val = 0;
            } else {
                val = Double.parseDouble(latencyInfo.split("/")[1]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            val = 0;
        }
        return val;
    }

    /**
     * 获取最大延迟
     *
     * @return 最大延迟
     */
    public double latencyMax() {
        double val;
        try {
            String latencyInfo = this.getLatencyInfo();
            if (StringUtil.isBlank(latencyInfo) || latencyInfo.equals("N/A")) {
                val = 0;
            } else {
                val = Double.parseDouble(latencyInfo.split("/")[2]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            val = 0;
        }
        return val;
    }

    /** 获取延迟信息 */
    public String getLatencyInfo() {
        return latencyInfoProperty == null ? "N/A" : latencyInfoProperty.getValue();
    }

    /** 获取服务id属性 */
    public SimpleStringProperty zxidProperty() {
        if (this.zxidProperty == null) {
            this.zxidProperty = new SimpleStringProperty();
        }
        return zxidProperty;
    }

    /** 设置服务id */
    public void setZxid(String zxid) {
        this.zxidProperty().set(zxid);
    }

    /** 获取服务id */
    public String getZxid() {
        return zxidProperty == null ? "N/A" : zxidProperty.getValue();
    }

    /** 获取服务类型属性 */
    public SimpleStringProperty modeProperty() {
        if (this.modeProperty == null) {
            this.modeProperty = new SimpleStringProperty();
        }
        return modeProperty;
    }

    /** 设置服务类型 */
    public void setMode(String zxid) {
        this.modeProperty().set(zxid);
    }

    /** 获取服务类型 */
    public String getMode() {
        return modeProperty == null ? "N/A" : modeProperty.getValue();
    }

    /** 获取服务版本属性 */
    public SimpleStringProperty versionProperty() {
        if (this.versionProperty == null) {
            this.versionProperty = new SimpleStringProperty();
        }
        return versionProperty;
    }

    /** 设置服务版本 */
    public void setVersion(String version) {
        if (version != null) {
            version = version.split("-")[0];
            this.versionProperty().set(version);
        }
    }

    /** 获取服务版本 */
    public String getVersion() {
        return versionProperty == null ? "N/A" : versionProperty.getValue();
    }

}
