package cn.oyzh.easyshell.sync;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.dto.ShellDataExport;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellSettingStore;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * gist同步器
 *
 * @author oyzh
 * @since 2025-10-13
 */
public abstract class ShellGistSyncer implements ShellSyncer {

    /**
     * 设置
     */
    protected final ShellSetting setting = ShellSettingStore.SETTING;

    @Override
    public void sync(String snippetName) throws Exception {
        ShellGistOperator operator = this.getOperator(this.setting.getSyncToken());
        try {
            String snippetId = this.setting.getSyncId();
            if (StringUtil.isEmpty(snippetId)) {
                List<JSONObject> map = operator.listGists();
                for (JSONObject data : map) {
                    if (snippetName.equals(data.getString("description"))) {
                        snippetId = data.getString("id");
                        break;
                    }
                }
            }
            // 创建
            if (StringUtil.isEmpty(snippetId)) {
                this.doCreate(operator, snippetName);
                return;
            }
            JSONObject files = operator.getFileContent(snippetId);
            JSONObject data = files.getJSONObject("data");
            // 更新
            if (data == null) {
                this.doUpdate(operator, snippetId, snippetName);
                return;
            }
            String content = data.getString("content");
            String syncTime = files.getString("syncTime");
            String syncTime1 = this.setting.getSyncTime() + "";
            // 更新
            if (StringUtil.isEmpty(content) || this.setting.getSyncTime() == null || StringUtil.equals(syncTime, syncTime1)) {
                this.doUpdate(operator, snippetId, snippetName);
            } else {// 合并更新
                ShellDataExport export = ShellSyncManager.decodeSyncData(content);
                ShellSyncManager.saveSyncData(export,
                        this.setting.isSyncKey(),
                        this.setting.isSyncGroup(),
                        this.setting.isSyncSnippet(),
                        this.setting.isSyncConnect()
                );
                this.doUpdate(operator, snippetId, snippetName);
                ShellEventUtil.dataImported();
            }
        } finally {
            IOUtil.close(operator);
        }
    }

    @Override
    public void clear(String snippetName) throws Exception {
        ShellGistOperator operator = this.getOperator(this.setting.getSyncToken());
        String snippetId = this.setting.getSyncId();
        if (StringUtil.isEmpty(snippetId)) {
            List<JSONObject> map = operator.listGists();
            for (JSONObject data : map) {
                if (snippetName.equals(data.getString("description"))) {
                    snippetId = data.getString("id");
                    break;
                }
            }
        }
        if (StringUtil.isEmpty(snippetId)) {
            JulLog.warn("syncId is empty");
            return;
        }
        // 执行清除
        this.doClear(operator, snippetId, snippetName);
    }

    /**
     * 执行新增
     *
     * @param operator    操作器
     * @param snippetName 片段名称
     * @throws Exception 异常
     */
    protected void doCreate(ShellGistOperator operator, String snippetName) throws Exception {
        long syncTime = System.currentTimeMillis();
        ShellDataExport export = ShellSyncManager.getSyncData(this.setting.isSyncKey(),
                this.setting.isSyncGroup(),
                this.setting.isSyncSnippet(),
                this.setting.isSyncConnect());
        String snippetData = ShellSyncManager.encodeSyncData(export);
        Map<String, String> files = new HashMap<>();
        files.put("data", snippetData);
        files.put("syncTime", syncTime + "");
        String snippetId = operator.createGist(snippetName, files, false);
        this.setting.setSyncId(snippetId);
        this.setting.setSyncTime(syncTime);
        ShellSettingStore.INSTANCE.replace(this.setting);
    }

    /**
     * 执行更新
     *
     * @param operator    操作器
     * @param snippetId   片段id
     * @param snippetName 片段名称
     * @throws Exception 异常
     */
    protected void doUpdate(ShellGistOperator operator, String snippetId, String snippetName) throws Exception {
        long syncTime = System.currentTimeMillis();
        ShellDataExport export = ShellSyncManager.getSyncData(this.setting.isSyncKey(),
                this.setting.isSyncGroup(),
                this.setting.isSyncSnippet(),
                this.setting.isSyncConnect());
        String snippetData = ShellSyncManager.encodeSyncData(export);
        Map<String, String> files = new HashMap<>();
        files.put("data", snippetData);
        files.put("syncTime", syncTime + "");
        operator.updateGist(snippetId, snippetName, files);
        this.setting.setSyncId(snippetId);
        this.setting.setSyncTime(syncTime);
        ShellSettingStore.INSTANCE.replace(this.setting);
    }

    /**
     * 执行清除
     *
     * @param operator    操作器
     * @param snippetId   片段id
     * @param snippetName 片段名称
     * @throws Exception 异常
     */
    protected void doClear(ShellGistOperator operator, String snippetId, String snippetName) throws Exception {
        Map<String, String> files = new HashMap<>();
        files.put("data", "");
        operator.updateGist(snippetId, snippetName, files);
        this.setting.setSyncTime(null);
        this.setting.setSyncId(snippetId);
        ShellSettingStore.INSTANCE.replace(this.setting);
    }

    /**
     * 获取操作器
     *
     * @param accessToken 令牌
     * @return gist操作器
     * @throws Exception 异常
     */
    protected abstract ShellGistOperator getOperator(String accessToken) throws Exception;
}
