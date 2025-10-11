package cn.oyzh.easyshell.sync;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.dto.ShellDataExport;
import cn.oyzh.easyshell.store.ShellSettingStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author oyzh
 * @since 2025-10-11
 */
public class ShellGiteeSyncer implements ShellSyncer {

    @Override
    public void sync(String snippetName) throws Exception {
        ShellSetting sync = ShellSettingStore.SETTING;
        ShellGistOperator operator = new ShellGistOperator(sync.getSyncToken());
        try {
            List<Map<String, Object>> map = operator.listGists();
            String snippetId = null;
            for (Map<String, Object> data : map) {
                if (snippetName.equals(data.get("description"))) {
                    snippetId = data.get("id").toString();
                    break;
                }
            }
            if (snippetId == null) {
                this.doReplace(operator, null, snippetName);
            } else if (StringUtil.equals(snippetId, sync.getGiteeId())) {
                this.doReplace(operator, snippetId, snippetName);
            } else {
                Map<String, String> files = operator.getFileContent(snippetId);
                String data = files.get("data");
                if (StringUtil.isEmpty(data)) {
                    this.doReplace(operator, snippetId, snippetName);
                } else {
                    ShellDataExport export = ShellSyncManager.decodeSyncData(data);
                    ShellSyncManager.saveSyncData(export,
                            sync.isSyncKey(),
                            sync.isSyncGroup(),
                            sync.isSyncSnippet(),
                            sync.isSyncConnect()
                    );
                    this.doReplace(operator, snippetId, snippetName);
                }
            }
        } finally {
            IOUtil.close(operator);
        }
    }

    private void doReplace(ShellGistOperator operator, String snippetId, String snippetName) throws Exception {
        ShellSetting sync = ShellSettingStore.SETTING;
        ShellDataExport export = ShellSyncManager.getSyncData(sync.isSyncKey(),
                sync.isSyncGroup(),
                sync.isSyncSnippet(),
                sync.isSyncConnect());
        String snippetData = ShellSyncManager.encodeSyncData(export);
        Map<String, String> files = new HashMap<>();
        files.put("data", snippetData);
        if (snippetId == null) {
            operator.createGist(snippetName, files, false);
        } else {
            operator.updateGist(snippetId, snippetName, files);
        }
    }
}
