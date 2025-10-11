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
import java.util.Map;

/**
 * gitee同步器
 *
 * @author oyzh
 * @since 2025-10-11
 */
public class ShellGiteeSyncer implements ShellSyncer {

    @Override
    public void sync(String snippetName) throws Exception {
        ShellSetting setting = ShellSettingStore.SETTING;
        ShellGistOperator operator = new ShellGistOperator(setting.getSyncToken());
        try {
            // List<Map<String, Object>> map = operator.listGists();
            String snippetId = setting.getSyncId();
            if (snippetId == null) {
                JulLog.warn("syncId is null");
                return;
            }
            // for (Map<String, Object> data : map) {
            //     if (snippetName.equals(data.get("description"))) {
            //         snippetId = data.get("id").toString();
            //         break;
            //     }
            // }
            JSONObject files = operator.getFileContent(snippetId);
            JSONObject data = files.getJSONObject("data");
            String content = data.getString("content");
            String syncTime = files.getString("syncTime");
            String syncTime1 = setting.getSyncTime() + "";
            if (setting.getSyncTime() == null) {
                this.doUpdate(operator, snippetId, snippetName);
            } else if (StringUtil.equals(syncTime, syncTime1)) {
                this.doUpdate(operator, snippetId, snippetName);
            } else {
                if (StringUtil.isEmpty(content)) {
                    this.doUpdate(operator, snippetId, snippetName);
                } else {
                    ShellDataExport export = ShellSyncManager.decodeSyncData(content);
                    ShellSyncManager.saveSyncData(export,
                            setting.isSyncKey(),
                            setting.isSyncGroup(),
                            setting.isSyncSnippet(),
                            setting.isSyncConnect()
                    );
                    this.doUpdate(operator, snippetId, snippetName);
                    ShellEventUtil.dataImported();
                }
            }
        } finally {
            IOUtil.close(operator);
        }
    }

    @Override
    public void clear(String snippetName) throws Exception {
        ShellSetting setting = ShellSettingStore.SETTING;

        ShellGistOperator operator = new ShellGistOperator(setting.getSyncToken());
        String snippetId = setting.getSyncId();
        if (snippetId == null) {
            JulLog.warn("syncId is null");
            return;
        }
        this.doClear(operator, snippetId, snippetName);
    }

    /**
     * 执行更新
     *
     * @param operator    操作器
     * @param snippetId   片段id
     * @param snippetName 片段名称
     * @throws Exception 异常
     */
    private void doUpdate(ShellGistOperator operator, String snippetId, String snippetName) throws Exception {
        ShellSetting setting = ShellSettingStore.SETTING;
        long syncTime = System.currentTimeMillis();
        ShellDataExport export = ShellSyncManager.getSyncData(setting.isSyncKey(),
                setting.isSyncGroup(),
                setting.isSyncSnippet(),
                setting.isSyncConnect());
        String snippetData = ShellSyncManager.encodeSyncData(export);
        Map<String, String> files = new HashMap<>();
        files.put("data", snippetData);
        files.put("syncTime", syncTime + "");
        // if (snippetId == null) {
        //     operator.createGist(snippetName, files, false);
        // } else {
        operator.updateGist(snippetId, snippetName, files);
        // }
        setting.setSyncTime(syncTime);
        ShellSettingStore.INSTANCE.replace(setting);
    }

    /**
     * 执行清除
     *
     * @param operator    操作器
     * @param snippetId   片段id
     * @param snippetName 片段名称
     * @throws Exception 异常
     */
    private void doClear(ShellGistOperator operator, String snippetId, String snippetName) throws Exception {
        ShellSetting setting = ShellSettingStore.SETTING;
        Map<String, String> files = new HashMap<>();
        files.put("data", "");
        operator.updateGist(snippetId, snippetName, files);
        setting.setSyncTime(null);
        ShellSettingStore.INSTANCE.replace(setting);
    }
}
