// package cn.oyzh.easyshell.sync;
//
// import cn.oyzh.common.util.IOUtil;
// import cn.oyzh.easyshell.domain.ShellSync;
// import cn.oyzh.easyshell.store.ShellSyncStore;
//
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
// /**
//  *
//  * @author oyzh
//  * @since 2025-10-11
//  */
// public class ShellGiteeSyncer {
//
//     public void sync(String snippetName, String snippetData) throws Exception {
//         ShellSync sync = ShellSyncStore.SYNC;
//         ShellGistOperator operator = new ShellGistOperator(sync.getGiteeToken());
//         try {
//             List<Map<String, Object>> map = operator.listGists();
//             String snippetId = null;
//             for (Map<String, Object> data : map) {
//                 if (snippetName.equals(data.get("description"))) {
//                     snippetId = data.get("id").toString();
//                     break;
//                 }
//             }
//             Map<String, String> files = new HashMap<String, String>();
//             files.put("data", snippetData);
//             if (snippetId == null) {
//                 operator.createGist(snippetName, files, false);
//             } else {
//                 operator.updateGist(snippetId, snippetName, files);
//             }
//         } finally {
//             IOUtil.close(operator);
//         }
//     }
// }
