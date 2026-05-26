package cn.oyzh.easyshell.util.zk;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.easyshell.dto.zk.ShellZKACL;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.easyshell.zk.ShellZKNode;
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

public class ShellZKAuthUtil {

    // /**
    //  * 已认证信息列表
    //  */
    // private static final Map<ShellZKClient, Set<String>> AUTHED_INFOS = new ConcurrentHashMap<>();

    /**
     * 获取认证信息列表
     *
     * @param auths 认证信息
     * @return 认证信息列表
     */
    public static List<AuthInfo> toAuthInfo(List<? extends ShellZKAuth> auths) {
        if (CollectionUtil.isNotEmpty(auths)) {
            List<AuthInfo> authInfos = new ArrayList<>(auths.size());
            for (ShellZKAuth auth : auths) {
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
    public static int authNode(String user, String password, ShellZKClient client, ShellZKNode zkNode) {
        int result = 0;
        try {
            client.addAuth(user, password);
            ShellZKNode node = ShellZKNodeUtil.getNode(client, zkNode.nodePath());
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
                for (ShellZKACL acl : node.getDigestACLs()) {
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
}
