package cn.oyzh.easyshell.http;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpCollection;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.domain.http.ShellHttpSetting;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个 HTTP 连接下的全部集合、环境和设置，运行期聚合对象。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpWorkspace {

    /**
     * 所属连接id
     */
    private String iid;

    /**
     * 集合
     */
    private List<ShellHttpCollection> collections = new ArrayList<>();

    /**
     * 环境
     */
    private List<ShellHttpEnvironment> environments = new ArrayList<>();

    /**
     * 设置
     */
    private ShellHttpSetting settings = new ShellHttpSetting();

    /**
     * 当前环境标识
     */
    private String activeEnvironmentId;

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public List<ShellHttpCollection> getCollections() {
        if (collections == null) {
            collections = new ArrayList<>();
        }
        return collections;
    }

    public void setCollections(List<ShellHttpCollection> collections) {
        this.collections = collections;
    }

    public List<ShellHttpEnvironment> getEnvironments() {
        if (environments == null) {
            environments = new ArrayList<>();
        }
        return environments;
    }

    public void setEnvironments(List<ShellHttpEnvironment> environments) {
        this.environments = environments;
    }

    public ShellHttpSetting getSettings() {
        if (settings == null) {
            settings = new ShellHttpSetting();
        }
        if (StringUtil.isBlank(settings.getIid())) {
            settings.setIid(this.iid);
        }
        return settings;
    }

    public void setSettings(ShellHttpSetting settings) {
        this.settings = settings;
    }

    public String getActiveEnvironmentId() {
        return activeEnvironmentId;
    }

    public void setActiveEnvironmentId(String activeEnvironmentId) {
        this.activeEnvironmentId = activeEnvironmentId;
    }

    /**
     * 获取当前启用的环境。
     *
     * @return 环境，未选择时返回 null
     */
    public ShellHttpEnvironment activeEnvironment() {
        if (StringUtil.isBlank(this.activeEnvironmentId)) {
            return null;
        }
        for (ShellHttpEnvironment environment : this.getEnvironments()) {
            if (this.activeEnvironmentId.equals(environment.getId())) {
                return environment;
            }
        }
        return null;
    }

    /**
     * 激活指定环境。
     *
     * @param environment 环境
     */
    public void activeEnvironment(ShellHttpEnvironment environment) {
        this.setActiveEnvironmentId(environment == null ? null : environment.getId());
    }

    /**
     * 获取请求总数。
     *
     * @return 请求总数
     */
    public int requestCount() {
        int count = 0;
        for (ShellHttpCollection collection : this.getCollections()) {
            count += collection.requestCount();
        }
        return count;
    }

    /**
     * 查找请求。
     *
     * @param requestId 请求标识
     * @return 请求
     */
    public ShellHttpRequest findRequest(String requestId) {
        if (StringUtil.isBlank(requestId)) {
            return null;
        }
        for (ShellHttpCollection collection : this.getCollections()) {
            for (ShellHttpRequest request : collection.getRequests()) {
                if (requestId.equals(request.getId())) {
                    return request;
                }
            }
        }
        return null;
    }

    /**
     * 查找环境。
     *
     * @param environmentId 环境标识
     * @return 环境
     */
    public ShellHttpEnvironment findEnvironment(String environmentId) {
        if (StringUtil.isBlank(environmentId)) {
            return null;
        }
        for (ShellHttpEnvironment environment : this.getEnvironments()) {
            if (environmentId.equals(environment.getId())) {
                return environment;
            }
        }
        return null;
    }
}
