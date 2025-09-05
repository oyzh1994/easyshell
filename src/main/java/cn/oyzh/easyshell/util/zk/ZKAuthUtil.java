package cn.oyzh.easyshell.util.zk;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ZKAuth;
import cn.oyzh.easyshell.dto.zk.ZKACL;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.zk.ZKAuthStore;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.easyshell.zk.ZKNode;
import org.apache.curator.framework.AuthInfo;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * zk认证工具类
 *
 * @author oyzh
 * @since 2023/3/6
 */

public class ZKAuthUtil {

    // /**
    //  * 已认证信息列表
    //  */
    // private static final Map<ZKClient, Set<String>> AUTHED_INFOS = new ConcurrentHashMap<>();

    /**
     * 获取认证信息列表
     *
     * @param auths 认证信息
     * @return 认证信息列表
     */
    public static List<AuthInfo> toAuthInfo(List<? extends ZKAuth> auths) {
        if (CollectionUtil.isNotEmpty(auths)) {
            List<AuthInfo> authInfos = new ArrayList<>(auths.size());
            for (ZKAuth auth : auths) {
                authInfos.add(new AuthInfo("digest", (auth.getUser() + ":" + auth.getPassword()).getBytes()));
            }
            return authInfos;
        }
        return Collections.emptyList();
    }

    /**
     * 认证节点
     *
     * @param user     用户名
     * @param password 密码
     * @param client   客户端
     * @param zkNode   zk节点
     * @return 结果 0 失败 1 成功 2 异常
     */
    public static int authNode(String user, String password, ZKClient client, ZKNode zkNode) {
        int result = 0;
        try {
            client.addAuth(user, password);
            ZKNode node = ZKNodeUtil.getNode(client, zkNode.nodePath());
            if (zkNode.aclEmpty() && !node.aclEmpty()) {
                result = 1;
            } else if (!zkNode.hasDeletePerm() && node.hasDeletePerm()) {
                result = 1;
            } else if (!zkNode.hasCreatePerm() && node.hasCreatePerm()) {
                result = 1;
            } else if (!zkNode.hasReadPerm() && node.hasReadPerm()) {
                result = 1;
            } else if (!zkNode.hasWritePerm() && node.hasWritePerm()) {
                result = 1;
            } else {
                String digest = digest(user, password);
                for (ZKACL acl : node.getDigestACLs()) {
                    if (Objects.equals(acl.idVal(), digest)) {
                        result = 1;
                        break;
                    }
                }
            }
            if (result == 1) {
                zkNode.copy(node);
                // setAuthed(client, user, password);
                client.setAuthed(user, password);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            result = 2;
        }
        return result;
    }

    // /**
    //  * 加载已启用的认证信息
    //  *
    //  * @return 已启用的认证信息列表
    //  */
    // public static List<ZKAuth> loadEnableAuths() {
    //     List<ZKAuth> auths = ZKAuthJdbcStore.INSTANCE.load();
    //     auths = auths.stream().filter(ZKAuth::getEnable).toList();
    //     return auths;
    // }

    /**
     * 加载认证信息
     *
     * @param iid 连接id
     * @return 认证信息列表
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public static List<ZKAuth> loadAuths(String iid) {
        if (StringUtil.isNotBlank(iid)) {
            if (ShellSettingStore.SETTING.isAutoAuth()) {
                return ZKAuthStore.INSTANCE.loadEnable(iid);
            }
        }
        return Collections.emptyList();
    }

    // /**
    //  * 设置认证信息为已认证
    //  *
    //  * @param client zk客户端
    //  * @param auth   认证信息
    //  */
    // public static void setAuthed( ZKClient client,  ZKAuth auth) {
    //     setAuthed(client, auth.getUser(), auth.getPassword());
    // }

    // /**
    //  * 设置认证信息为已认证
    //  *
    //  * @param client zk客户端
    //  * @param auths  认证信息列表
    //  */
    // public static void setAuthed( ZKClient client, List<? extends ZKAuth> auths) {
    //     if (CollectionUtil.isNotEmpty(auths)) {
    //         auths.parallelStream().forEach(a -> setAuthed(client, a));
    //     }
    // }
    //
    // /**
    //  * 设置认证信息为已认证
    //  *
    //  * @param client   zk客户端
    //  * @param user     用户名
    //  * @param password 密码
    //  */
    // public static void setAuthed( ZKClient client, String user, String password) {
    //     if (user != null && password != null) {
    //         Set<String> set = AUTHED_INFOS.computeIfAbsent(client, k -> new CopyOnWriteArraySet<>());
    //         set.add(user + ":" + password);
    //     }
    // }

    // /**
    //  * 清除已认证信息
    //  */
    // public static void clearAuthed() {
    //     AUTHED_INFOS.clear();
    // }

    // /**
    //  * 移除已认证信息
    //  *
    //  * @param client zk客户端
    //  */
    // public static void removeAuthed( ZKClient client) {
    //     AUTHED_INFOS.remove(client);
    // }

    // /**
    //  * 获取已认证信息
    //  *
    //  * @param client zk客户端
    //  * @return 已认证的信息列表
    //  */
    // public static Set<String> getAuthed(ZKClient client) {
    //     return client == null ? Collections.emptySet() : AUTHED_INFOS.get(client);
    // }

    // /**
    //  * 是否已认证
    //  *
    //  * @param client zk客户端
    //  * @param auth   认证信息
    //  * @return 结果
    //  */
    // public static boolean isAuthed( ZKClient client, ZKAuth auth) {
    //     if (auth != null) {
    //         Set<String> set = getAuthed(client);
    //         if (set != null) {
    //             return set.contains(auth.getUser() + ":" + auth.getPassword());
    //         }
    //     }
    //     return false;
    // }

    // /**
    //  * 获取已认证的摘要信息
    //  *
    //  * @param client zk客户端
    //  * @return 已认证的摘要信息列表
    //  */
    // public static Set<String> getAuthedDigest(ZKClient client) {
    //     Set<String> set = getAuthed(client);
    //     if (set == null) {
    //         return Collections.emptySet();
    //     }
    //     Set<String> digestList = new HashSet<>();
    //     for (String s : set) {
    //         String[] arr = s.split(":");
    //         digestList.add(digest(arr[0], arr[1]));
    //     }
    //     return digestList;
    // }

    // /**
    //  * 获取已认证的摘要信息
    //  *
    //  * @param client zk客户端
    //  * @return 已认证的摘要信息列表
    //  */
    // public static boolean isDigestAuthed(ZKClient client, String digestVal) {
    //     Set<String> digests = ZKAuthUtil.getAuthedDigest(client);
    //     return CollectionUtil.isNotEmpty(digests) && digests.contains(digestVal);
    // }

    // /**
    //  * 是否任意权限已认证
    //  *
    //  * @param aclList 权限列表
    //  * @return 结果
    //  */
    // public static boolean isAnyAuthed( ZKClient client, List<ZKACL> aclList) {
    //     if (CollectionUtil.isNotEmpty(aclList)) {
    //         Set<String> digestList = getAuthedDigest(client);
    //         for (ZKACL zkacl : aclList) {
    //             if (digestList.contains(zkacl.idVal())) {
    //                 return true;
    //             }
    //         }
    //     }
    //     return false;
    // }

    /**
     * 生成摘要信息
     *
     * @param user     用户
     * @param password 密码
     * @return 摘要信息
     */
    public static String digest(String user, String password) {
        try {
            return DigestAuthenticationProvider.generateDigest(user + ":" + password);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // /**
    //  * 是否需要认证
    //  *
    //  * @param node   zk节点
    //  * @param client zk客户端
    //  * @return 结果
    //  */
    // public boolean isNeedAuth( ZKNode node,  ZKClient client) {
    //     if (node.aclEmpty() && node.lackPerm()) {
    //         return true;
    //     }
    //     if (node.hasWorldACL()) {
    //         return false;
    //     }
    //     if (node.hasIPACL()) {
    //         return false;
    //     }
    //     return !isAnyAuthed(client, node.acl());
    // }
}
