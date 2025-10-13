package cn.oyzh.easyshell.sync;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.security.AESUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.domain.ShellSnippet;
import cn.oyzh.easyshell.dto.ShellDataExport;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.store.ShellGroupStore;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.ShellSnippetStore;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-10-11
 */
public class ShellSyncManager {

    /**
     * 加密密钥
     */
    private final static String AES_SECRET = "easy_shell_sync_aes_secret";

    /**
     * 片段名称
     */
    private final static String SNIPPET_NAME = "EasyShell_Config_Data";

    /**
     * 获取同步器
     *
     * @return 同步器
     */
    private static ShellSyncer getSyncer() {
        ShellSetting setting = ShellSettingStore.SETTING;
        ShellSyncer syncer = null;
        if (setting.isGiteeType()) {
            syncer = new ShellGiteeSyncer();
        } else if (setting.isGithubType()) {
            syncer = new ShellGithubSyncer();
        }
        return syncer;
    }

    /**
     * 执行更新
     *
     * @throws Exception 异常
     */
    public static void doSync() throws Exception {
        ShellSyncer syncer = getSyncer();
        if (syncer != null) {
            syncer.sync(SNIPPET_NAME);
        }
    }

    /**
     * 执行清除
     *
     * @throws Exception 异常
     */
    public static void clearSync() throws Exception {
        ShellSyncer syncer = getSyncer();
        if (syncer != null) {
            syncer.clear(SNIPPET_NAME);
        }
    }

    /**
     * 加密数据
     *
     * @param export 数据
     * @return 加密后的数据
     * @throws Exception 异常
     */
    public static String encodeSyncData(ShellDataExport export) throws Exception {
        String json = JSONUtil.toJson(export);
        return AESUtil.encrypt(json, AES_SECRET);
    }

    /**
     * 解密数据
     *
     * @param data 数据
     * @return 解密后的数据
     * @throws Exception 异常
     */
    public static ShellDataExport decodeSyncData(String data) throws Exception {
        String json = AESUtil.decrypt(data, AES_SECRET);
        return JSONUtil.toBean(json, ShellDataExport.class);
    }

    /**
     * 获取同步数据
     *
     * @param key     是否同步密钥
     * @param group   是否同步分组
     * @param snippet 是否同步片段
     * @param connect 是否同步连接
     * @return 同步数据
     * @throws Exception 异常
     */
    public static ShellDataExport getSyncData(boolean key, boolean group, boolean snippet, boolean connect) throws Exception {
        // 密钥存储
        ShellKeyStore keyStore = ShellKeyStore.INSTANCE;
        // 分组存储
        ShellGroupStore groupStore = ShellGroupStore.INSTANCE;
        // 片段存储
        ShellSnippetStore snippetStore = ShellSnippetStore.INSTANCE;
        // 连接存储
        ShellConnectStore connectStore = ShellConnectStore.INSTANCE;
        ShellDataExport export = ShellDataExport.of();
        // 密钥
        if (key) {
            export.setKeys(keyStore.selectList());
        }
        // 分组
        if (group) {
            export.setGroups(groupStore.load());
        }
        // 片段
        if (snippet) {
            export.setSnippets(snippetStore.selectList());
        }
        // 连接
        if (connect) {
            export.setConnects(connectStore.loadFull());
        }
        return export;
    }

    /**
     * 保存同步数据
     *
     * @param data    数据
     * @param key     是否同步密钥
     * @param group   是否同步分组
     * @param snippet 是否同步片段
     * @param connect 是否同步连接
     * @throws Exception 异常
     */
    public static void saveSyncData(ShellDataExport data, boolean key, boolean group, boolean snippet, boolean connect) throws Exception {
        // 密钥存储
        ShellKeyStore keyStore = ShellKeyStore.INSTANCE;
        // 分组存储
        ShellGroupStore groupStore = ShellGroupStore.INSTANCE;
        // 片段存储
        ShellSnippetStore snippetStore = ShellSnippetStore.INSTANCE;
        // 连接存储
        ShellConnectStore connectStore = ShellConnectStore.INSTANCE;
        List<ShellKey> keys = data.getKeys();
        if (key && CollectionUtil.isNotEmpty(keys)) {
            for (ShellKey shellKey : keys) {
                keyStore.replace(shellKey);
            }
        }
        List<ShellGroup> groups = data.getGroups();
        if (group && CollectionUtil.isNotEmpty(groups)) {
            for (ShellGroup shellGroup : groups) {
                groupStore.replace(shellGroup);
            }
        }
        List<ShellSnippet> snippets = data.getSnippets();
        if (snippet && CollectionUtil.isNotEmpty(snippets)) {
            for (ShellSnippet shellSnippet : snippets) {
                snippetStore.replace(shellSnippet);
            }
        }
        List<ShellConnect> connects = data.getConnects();
        if (connect && CollectionUtil.isNotEmpty(connects)) {
            for (ShellConnect shellConnect : connects) {
                connectStore.replace(shellConnect);
            }
        }
    }
}
