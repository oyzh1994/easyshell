// package cn.oyzh.easyshell.sync;
//
// import cn.oyzh.common.util.CollectionUtil;
// import cn.oyzh.common.util.StringUtil;
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.domain.ShellGroup;
// import cn.oyzh.easyshell.domain.ShellKey;
// import cn.oyzh.easyshell.domain.ShellSnippet;
// import cn.oyzh.easyshell.dto.ShellConnectExport;
// import cn.oyzh.easyshell.store.ShellConnectStore;
// import cn.oyzh.easyshell.store.ShellGroupStore;
// import cn.oyzh.easyshell.store.ShellKeyStore;
// import cn.oyzh.easyshell.store.ShellSnippetStore;
//
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;
// import java.util.Optional;
//
// /**
//  *
//  * @author oyzh
//  * @since 2025-10-11
//  */
// public class ShellSyncManager {
//
//     public static void doSync() throws Exception {
//
//     }
//
//     public static String encodeSyncData(ShellConnectExport export) throws Exception {
//
//         return null;
//
//     }
//
//     public static ShellConnectExport decodeSyncData(String data) throws Exception {
//
//         return null;
//
//     }
//
//     public static ShellConnectExport getSyncData(boolean key, boolean group, boolean snippet, boolean connect) throws Exception {
//         // 密钥存储
//         ShellKeyStore keyStore = ShellKeyStore.INSTANCE;
//         // 分组存储
//         ShellGroupStore groupStore = ShellGroupStore.INSTANCE;
//         // 片段存储
//         ShellSnippetStore snippetStore = ShellSnippetStore.INSTANCE;
//         // 连接存储
//         ShellConnectStore connectStore = ShellConnectStore.INSTANCE;
//         ShellConnectExport export = ShellConnectExport.of();
//         // 密钥
//         if (key) {
//             export.setKeys(keyStore.selectList());
//         }
//         // 分组
//         if (group) {
//             export.setGroups(groupStore.load());
//         }
//         // 连接
//         if (connect) {
//             export.setConnects(connectStore.loadFull());
//         }
//         // 片段
//         if (snippet) {
//             export.setSnippets(snippetStore.selectList());
//         }
//         return export;
//     }
//
//     public static ShellConnectExport margeSyncData(ShellConnectExport local,
//                                                    ShellConnectExport remote,
//                                                    boolean key,
//                                                    boolean group,
//                                                    boolean snippet,
//                                                    boolean connect,
//                                                    int mode
//     ) throws Exception {
//
//         ShellConnectExport export = ShellConnectExport.of();
//         if (key) {
//             // 本地覆盖云端
//             if (mode == 0) {
//                 export.setKeys(local.getKeys());
//             } else if (mode == 1) { // 云端覆盖本地
//                 export.setKeys(local.getKeys());
//             } else if (mode == 2) { // 合并
//                 export.setKeys(local.getKeys());
//             }
//         }
//         return null;
//     }
//
//     public static List<ShellKey> margeKeys(List<ShellKey> local, List<ShellKey> remote) {
//         if (local == null && remote == null) {
//             return Collections.emptyList();
//         }
//         if (remote == null) {
//             return local;
//         }
//         if (local == null) {
//             return remote;
//         }
//         List<ShellKey> delList = new ArrayList<>();
//         for (ShellKey shellKey : local) {
//             Optional<ShellKey> optional = remote.parallelStream().filter(k -> StringUtil.equals(k.getId(), shellKey.getId())).findAny();
//             if (optional.isEmpty()) {
//                 delList.add(shellKey);
//             }
//         }
//         remote.removeAll(delList);
//         return remote;
//     }
//
//     public static void saveSyncData(ShellConnectExport export, boolean key, boolean group, boolean snippet, boolean connect) throws Exception {
//         // 密钥存储
//         ShellKeyStore keyStore = ShellKeyStore.INSTANCE;
//         // 分组存储
//         ShellGroupStore groupStore = ShellGroupStore.INSTANCE;
//         // 片段存储
//         ShellSnippetStore snippetStore = ShellSnippetStore.INSTANCE;
//         // 连接存储
//         ShellConnectStore connectStore = ShellConnectStore.INSTANCE;
//         List<ShellKey> keys = export.getKeys();
//         if (key && CollectionUtil.isNotEmpty(keys)) {
//             for (ShellKey shellKey : keys) {
//                 keyStore.replace(shellKey);
//             }
//         }
//         List<ShellGroup> groups = export.getGroups();
//         if (group && CollectionUtil.isNotEmpty(groups)) {
//             for (ShellGroup shellGroup : groups) {
//                 groupStore.replace(shellGroup);
//             }
//         }
//         List<ShellSnippet> snippets = export.getSnippets();
//         if (snippet && CollectionUtil.isNotEmpty(snippets)) {
//             for (ShellSnippet shellSnippet : snippets) {
//                 snippetStore.replace(shellSnippet);
//             }
//         }
//         List<ShellConnect> connects = export.getConnects();
//         if (connect && CollectionUtil.isNotEmpty(connects)) {
//             for (ShellConnect shellConnect : connects) {
//                 connectStore.replace(shellConnect);
//             }
//         }
//     }
// }
