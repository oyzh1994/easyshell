package cn.oyzh.easyshell.store.http;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpCollection;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.domain.http.ShellHttpSetting;
import cn.oyzh.easyshell.http.ShellHttpWorkspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP 工作区聚合存储，负责从各实体存储器加载和清理数据。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpWorkspaceStore {

    public static final ShellHttpWorkspaceStore INSTANCE = new ShellHttpWorkspaceStore();

    /**
     * 加载工作区。
     *
     * @param iid 连接id
     * @return 工作区
     */
    public ShellHttpWorkspace load(String iid) {
        return this.load(iid, 30);
    }

    /**
     * 加载工作区。
     *
     * @param iid            连接id
     * @param defaultTimeout 默认超时秒数
     * @return 工作区
     */
    public ShellHttpWorkspace load(String iid, int defaultTimeout) {
        ShellHttpWorkspace workspace = new ShellHttpWorkspace();
        workspace.setIid(iid);
        List<ShellHttpCollection> collections = ShellHttpCollectionStore.INSTANCE.list(iid);
        for (ShellHttpCollection collection : collections) {
            collection.setRequests(ShellHttpRequestStore.INSTANCE.listByCollection(collection.getId()));
        }
        workspace.setCollections(new ArrayList<>(collections));
        workspace.setEnvironments(new ArrayList<>(ShellHttpEnvironmentStore.INSTANCE.list(iid)));
        ShellHttpSetting setting = ShellHttpSettingStore.INSTANCE.getByIid(iid);
        if (setting == null) {
            setting = new ShellHttpSetting();
            setting.setIid(iid);
            if (defaultTimeout > 0) {
                setting.setTimeoutSeconds(defaultTimeout);
            }
        }
        workspace.setSettings(setting);
        workspace.setActiveEnvironmentId(setting.getActiveEnvironmentId());
        if (workspace.activeEnvironment() == null) {
            workspace.setActiveEnvironmentId(null);
        }
        return workspace;
    }

    /**
     * 保存工作区设置及当前环境。
     *
     * @param workspace 工作区
     * @return 结果
     */
    public boolean saveSetting(ShellHttpWorkspace workspace) {
        if (workspace == null || StringUtil.isBlank(workspace.getIid())) {
            return false;
        }
        ShellHttpSetting setting = workspace.getSettings();
        setting.setIid(workspace.getIid());
        setting.setActiveEnvironmentId(workspace.getActiveEnvironmentId());
        return ShellHttpSettingStore.INSTANCE.replace(setting);
    }

    /**
     * 迁移旧版本保存在 t_connect.extras 中的 HTTP 工作区数据。
     *
     * @param iid  连接id
     * @param json 旧工作区 JSON
     */
    public void migrate(String iid, String json) {
        if (StringUtil.isBlank(iid) || StringUtil.isBlank(json)) {
            return;
        }
        try {
            JSONObject root = JSONUtil.parseObject(json);
            if (root == null) {
                return;
            }
            JSONArray collections = root.getJSONArray("collections");
            if (collections != null) {
                for (int i = 0; i < collections.size(); i++) {
                    JSONObject oldCollection = collections.getJSONObject(i);
                    ShellHttpCollection collection = new ShellHttpCollection();
                    collection.setIid(iid);
                    collection.setName(oldCollection.getString("name"));
                    ShellHttpCollectionStore.INSTANCE.insert(collection);
                    // 旧版本的目录拍平成集合下的请求
                    this.migrateRequests(iid, collection.getId(), oldCollection.getJSONArray("requests"));
                    JSONArray folders = oldCollection.getJSONArray("folders");
                    if (folders != null) {
                        for (int j = 0; j < folders.size(); j++) {
                            JSONObject folder = folders.getJSONObject(j);
                            this.migrateRequests(iid, collection.getId(), folder.getJSONArray("requests"));
                        }
                    }
                }
            }
            Map<String, String> environmentIdMap = new HashMap<>();
            JSONArray environments = root.getJSONArray("environments");
            if (environments != null) {
                for (int i = 0; i < environments.size(); i++) {
                    JSONObject oldEnvironment = environments.getJSONObject(i);
                    ShellHttpEnvironment environment = new ShellHttpEnvironment();
                    environment.setIid(iid);
                    environment.setName(oldEnvironment.getString("name"));
                    environment.setValuesJson(oldEnvironment.getString("values"));
                    ShellHttpEnvironmentStore.INSTANCE.insert(environment);
                    environmentIdMap.put(oldEnvironment.getString("id"), environment.getId());
                }
            }
            ShellHttpSetting setting = new ShellHttpSetting();
            setting.setIid(iid);
            JSONObject oldSetting = root.getJSONObject("settings");
            if (oldSetting != null) {
                int timeout = oldSetting.getIntValue("timeoutSeconds");
                if (timeout > 0) {
                    setting.setTimeoutSeconds(timeout);
                }
                if (oldSetting.containsKey("httpVersion")) {
                    setting.setHttpVersion(oldSetting.getString("httpVersion"));
                }
                if (oldSetting.containsKey("followRedirects")) {
                    setting.setFollowRedirects(oldSetting.getBooleanValue("followRedirects"));
                }
                if (oldSetting.containsKey("verifySsl")) {
                    setting.setVerifySsl(oldSetting.getBooleanValue("verifySsl"));
                }
                if (oldSetting.containsKey("prettyJson")) {
                    setting.setPrettyJson(oldSetting.getBooleanValue("prettyJson"));
                }
            }
            String legacyActiveId = root.getString("activeEnvironmentId");
            setting.setActiveEnvironmentId(legacyActiveId == null ? null : environmentIdMap.get(legacyActiveId));
            ShellHttpSettingStore.INSTANCE.replace(setting);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 迁移请求。
     *
     * @param iid          连接id
     * @param collectionId 集合id
     * @param requests     旧请求数组
     */
    private void migrateRequests(String iid, String collectionId, JSONArray requests) {
        if (requests == null) {
            return;
        }
        for (int i = 0; i < requests.size(); i++) {
            JSONObject oldRequest = requests.getJSONObject(i);
            ShellHttpRequest request = new ShellHttpRequest();
            request.setIid(iid);
            request.setCollectionId(collectionId);
            request.setName(oldRequest.getString("name"));
            request.setMethod(oldRequest.getString("method"));
            request.setUrl(oldRequest.getString("url"));
            request.setParamsJson(oldRequest.getString("params"));
            request.setHeadersJson(oldRequest.getString("headers"));
            request.setBodyType(oldRequest.getString("bodyType"));
            request.setBodyContentType(oldRequest.getString("bodyContentType"));
            request.setBody(oldRequest.getString("body"));
            request.setFormBodyJson(oldRequest.getString("formBody"));
            request.setAuthType(oldRequest.getString("authType"));
            request.setAuthUser(oldRequest.getString("authUser"));
            request.setAuthPassword(oldRequest.getString("authPassword"));
            request.setAuthToken(oldRequest.getString("authToken"));
            request.setAuthKeyName(oldRequest.getString("authKeyName"));
            request.setAuthKeyValue(oldRequest.getString("authKeyValue"));
            request.setAuthKeyPlacement(oldRequest.getString("authKeyPlacement"));
            request.setTimeoutSeconds(oldRequest.getInteger("timeoutSeconds"));
            ShellHttpRequestStore.INSTANCE.insert(request);
        }
    }

    /**
     * 删除连接下的全部 HTTP 数据。
     *
     * @param iid 连接id
     */
    public void deleteByIid(String iid) {
        if (StringUtil.isBlank(iid)) {
            return;
        }
        ShellHttpRequestScriptStore.INSTANCE.deleteByIid(iid);
        ShellHttpRequestStore.INSTANCE.deleteByIid(iid);
        ShellHttpCollectionStore.INSTANCE.deleteByIid(iid);
        ShellHttpEnvironmentStore.INSTANCE.deleteByIid(iid);
        ShellHttpSettingStore.INSTANCE.deleteByIid(iid);
    }
}
