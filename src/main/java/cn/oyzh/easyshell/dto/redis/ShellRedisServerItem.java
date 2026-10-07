package cn.oyzh.easyshell.dto.redis;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.SimpleStringProperty;

/**
 * redis服务信息
 *
 * @author oyzh
 * @since 2023/08/01
 */
public class ShellRedisServerItem {

    /**
     * 服务角色
     */
    private String role;

    /** 获取服务版本 */
    public String getServerVersion() {
        return serverVersion;
    }

    /** 设置服务版本 */
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    /** 获取服务角色 */
    public String getRole() {
        return role;
    }

    /**
     * 服务版本
     */
    private String serverVersion;

    /**
     * 正常运行时间
     */
    private SimpleStringProperty uptimeProperty;

    /**
     * 命中率
     */
    private SimpleStringProperty hitRateProperty;

    /**
     * 键数量
     */
    private SimpleStringProperty keyCountProperty;

    /**
     * 已使用内存
     */
    private SimpleStringProperty usedMemoryProperty;

    /**
     * 已连接客户端
     */
    private SimpleStringProperty connectedClientsProperty;

    /**
     * 已处理命令
     */
    private SimpleStringProperty totalCommandsProcessedProperty;

    /**
     * 初始化服务信息
     *
     * @param prop redis信息属性
     */
    public void init(ShellRedisInfoProp prop) {
        this.update(prop.getUptimeInDays(), prop.getUsedMemoryHuman(), prop.getTotalCommandsProcessed(), prop.getKeyspaceHits(), prop.getKeyspaceMisses(), prop.keyCount(), prop.getConnectedClients());
    }

    /**
     * 更新服务信息
     *
     * @param uptime                 正常运行时间
     * @param useMemory              已使用内存
     * @param totalCommandsProcessed 已处理命令数
     * @param hits                   键命中数量
     * @param misses                 键未命中数量
     * @param keyCount               键数量
     * @param connectedClients       已连接客户端数
     */
    public void update(long uptime, String useMemory, long totalCommandsProcessed, long hits, long misses, Long keyCount, int connectedClients) {
        String hitRate = null;
        if (hits == 0 && misses == 0) {
            hitRate = "N/A";
        } else if (hits != -1 && misses != -1) {
            double d = 100.0d * hits / (hits + misses);
            double decimal = NumberUtil.round(d, 4);
            hitRate = decimal + "%";
        }
        this.setHitRate(hitRate);
        this.setUptime(uptime + I18nHelper.days());
        this.setUsedMemory(useMemory == null ? "N/A" : useMemory);
        this.setConnectedClients(String.valueOf(connectedClients));
        this.setKeyCount(keyCount == null ? "N/A" : String.valueOf(keyCount));
        this.setTotalCommandsProcessed(String.valueOf(totalCommandsProcessed));
    }

    /** 获取正常运行时间属性 */
    public SimpleStringProperty uptimeProperty() {
        if (this.uptimeProperty == null) {
            this.uptimeProperty = new SimpleStringProperty();
        }
        return uptimeProperty;
    }

    /** 获取命中率属性 */
    public SimpleStringProperty hitRateProperty() {
        if (this.hitRateProperty == null) {
            this.hitRateProperty = new SimpleStringProperty();
        }
        return hitRateProperty;
    }

    /** 获取已使用内存属性 */
    public SimpleStringProperty usedMemoryProperty() {
        if (this.usedMemoryProperty == null) {
            this.usedMemoryProperty = new SimpleStringProperty();
        }
        return usedMemoryProperty;
    }

    /** 获取键数量属性 */
    public SimpleStringProperty keyCountProperty() {
        if (this.keyCountProperty == null) {
            this.keyCountProperty = new SimpleStringProperty();
        }
        return keyCountProperty;
    }

    /** 获取已处理命令属性 */
    public SimpleStringProperty totalCommandsProcessedProperty() {
        if (this.totalCommandsProcessedProperty == null) {
            this.totalCommandsProcessedProperty = new SimpleStringProperty();
        }
        return totalCommandsProcessedProperty;
    }

    /** 获取已连接客户端属性 */
    public SimpleStringProperty connectedClientsProperty() {
        if (this.connectedClientsProperty == null) {
            this.connectedClientsProperty = new SimpleStringProperty();
        }
        return connectedClientsProperty;
    }

    /** 设置正常运行时间 */
    public void setUptime(String uptime) {
        this.uptimeProperty().setValue(uptime);
    }

    /** 获取正常运行时间 */
    public String getUptime() {
        return this.uptimeProperty == null ? "N/A" : this.uptimeProperty().get();
    }

    /** 设置已使用内存 */
    public void setUsedMemory(String usedMemory) {
        this.usedMemoryProperty().setValue(usedMemory);
    }

    /** 获取已使用内存 */
    public String getUsedMemory() {
        return this.usedMemoryProperty == null ? "N/A" : this.usedMemoryProperty().get();
    }

    /** 设置命中率 */
    public void setHitRate(String value) {
        this.hitRateProperty().setValue(value);
    }

    /** 获取命中率 */
    public String getHitRate() {
        return this.hitRateProperty == null ? "N/A" : this.hitRateProperty().get();
    }

    /** 设置键数量 */
    public void setKeyCount(String keyCount) {
        this.keyCountProperty().setValue(keyCount);
    }

    /** 获取键数量 */
    public String getKeyCount() {
        return this.keyCountProperty == null ? "N/A" : this.keyCountProperty().get();
    }

    /** 设置已连接客户端数 */
    public void setConnectedClients(String connectedClients) {
        this.connectedClientsProperty().setValue(connectedClients);
    }

    /** 获取已连接客户端数 */
    public String getConnectedClients() {
        return this.connectedClientsProperty == null ? "N/A" : this.connectedClientsProperty().get();
    }

    /** 设置已处理命令数 */
    public void setTotalCommandsProcessed(String totalCommandsProcessed) {
        this.totalCommandsProcessedProperty().setValue(totalCommandsProcessed);
    }

    /** 获取已处理命令数 */
    public String getTotalCommandsProcessed() {
        return this.totalCommandsProcessedProperty == null ? "N/A" : this.totalCommandsProcessedProperty().get();
    }

    /** 设置服务角色 */
    public void setRole(String role) {
        if (StringUtil.equalsIgnoreCase("master", role)) {
            this.role = I18nHelper.master();
            // this.role = "主节点";
        } else if (StringUtil.equalsIgnoreCase("slave", role)) {
            this.role = I18nHelper.slave();
            // this.role = "从节点";
        } else if (StringUtil.equalsIgnoreCase("sentinel", role)) {
            this.role = I18nHelper.sentinel();
            // this.role = "哨兵";
        } else {
            this.role = "N/A";
        }
    }
}
