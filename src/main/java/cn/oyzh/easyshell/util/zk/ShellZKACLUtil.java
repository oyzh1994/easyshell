package cn.oyzh.easyshell.util.zk;

import cn.oyzh.common.dto.FriendlyInfo;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dto.zk.ZKACL;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nManager;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * acl工具类
 *
 * @author oyzh
 * @since 2022/6/2
 */

public class ShellZKACLUtil {

    /**
     * 开放的acl权限
     */
    public static ACL OPEN_ACL = ZooDefs.Ids.OPEN_ACL_UNSAFE.getFirst();

    /**
     * 是否开放的acl权限
     *
     * @param acl acl
     * @return 结果
     */
    public static boolean isOpenACL(ACL acl) {
        if (acl == null) {
            return false;
        }
        if (OPEN_ACL.getPerms() != acl.getPerms() || acl.getId() == null) {
            return false;
        }
        if (OPEN_ACL.getId() == acl.getId()) {
            return true;
        }
        return Objects.equals(OPEN_ACL.getId().getId(), acl.getId().getId()) && Objects.equals(OPEN_ACL.getId().getScheme(), acl.getId().getScheme());
    }

    /**
     * 解析id
     *
     * @param idVal id值
     * @return id信息
     */
    public static FriendlyInfo<ACL> parseId( Id idVal) {
        FriendlyInfo<ACL> idInfo = new FriendlyInfo<>();
        idInfo.name("id");
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE) {
            idInfo.friendlyName("标识");
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE) {
            idInfo.friendlyName("標識");
        } else {
            idInfo.friendlyName("id");
        }
        idInfo.value(idVal.getId());
        if (idVal.getScheme().equalsIgnoreCase("WORLD")) {
            if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE) {
                idInfo.friendlyValue("任何人");
            } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE) {
                idInfo.friendlyValue("任何人");
            } else {
                idInfo.friendlyValue("anyone");
            }
        } else {
            idInfo.friendlyValue(idVal.getId());
        }
        return idInfo;
    }

    /**
     * 解析认证方式
     *
     * @param schemeStr 认证方式字符
     * @return 认证方式信息
     */
    public static FriendlyInfo<ACL> parseScheme( String schemeStr) {
        FriendlyInfo<ACL> scheme = new FriendlyInfo<>();
        scheme.name("scheme");
        scheme.value(schemeStr);
        scheme.friendlyName(I18nHelper.schema());
        switch (schemeStr.toUpperCase()) {
            case "IP" -> scheme.friendlyValue("IP");
            case "WORLD" -> {
                if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE) {
                    scheme.friendlyValue("开放认证");
                } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE) {
                    scheme.friendlyValue("開放認證");
                } else {
                    scheme.friendlyValue("world");
                }
            }
            case "DIGEST" -> scheme.friendlyValue(I18nHelper.digest());
            default -> scheme.friendlyValue(I18nHelper.unknown());
        }
        return scheme;
    }

    /**
     * 解析权限
     *
     * @param permsVal 权限值
     * @return 权限信息
     */
    public static FriendlyInfo<ACL> parsePerms(int permsVal) {
        String permsString = toPermStr(permsVal);
        StringBuilder valueBuilder = new StringBuilder();
        StringBuilder friendlyValueBuilder = new StringBuilder();
        if (permsString.contains("a")) {
            valueBuilder.append("a");
            friendlyValueBuilder.append(",").append(I18nHelper.admin());
        }
        if (permsString.contains("w")) {
            valueBuilder.append("w");
            friendlyValueBuilder.append(",").append(I18nHelper.write());
        }
        if (permsString.contains("r")) {
            valueBuilder.append("r");
            friendlyValueBuilder.append(",").append(I18nHelper.read());
        }
        if (permsString.contains("d")) {
            valueBuilder.append("d");
            friendlyValueBuilder.append(",").append(I18nHelper.delete());
        }
        if (permsString.contains("c")) {
            valueBuilder.append("c");
            friendlyValueBuilder.append(",").append(ShellI18nHelper.zkAclC());
        }
        FriendlyInfo<ACL> perms = new FriendlyInfo<>();
        perms.name("perms");
        perms.friendlyName(I18nHelper.perms());
        perms.value(valueBuilder.toString());
        if (friendlyValueBuilder.toString().startsWith(",")) {
            perms.friendlyValue(friendlyValueBuilder.substring(1));
        } else {
            perms.friendlyValue("");
        }
        return perms;
    }

    /**
     * 解析权限
     *
     * @param acl 权限内容
     * @return 权限列表
     */
    public static List<ACL> parseAcl(String acl) {
        if (acl == null || acl.isEmpty()) {
            return ZooDefs.Ids.OPEN_ACL_UNSAFE;
        }
        List<ACL> aclList = new ArrayList<>(12);
        for (String str : acl.split(",")) {
            if (str.isEmpty()) {
                continue;
            }
            String[] strings = str.split(":");
            Id id = new Id();
            ACL a = new ACL();
            if (strings.length > 0) {
                id.setScheme(strings[0]);
            }
            if (id.getScheme().equalsIgnoreCase("digest")) {
                if (strings.length > 2) {
                    id.setId(strings[1] + ":" + strings[2]);
                }
                if (strings.length > 3) {
                    a.setPerms(toPermInt(strings[3]));
                }
            } else {
                if (strings.length > 1) {
                    id.setId(strings[1]);
                }
                if (strings.length > 2) {
                    a.setPerms(toPermInt(strings[2]));
                }
            }
            a.setId(id);
            aclList.add(a);
        }
        return aclList;
    }

    /**
     * 解析权限为int
     *
     * @param perms 权限
     * @return int值
     */
    public static int toPermInt( String perms) {
        perms = perms.trim().toLowerCase();
        if (perms.isEmpty() || perms.length() > 5) {
            throw new ShellException(I18nHelper.invalidPerms());
        }
        int permsInt = 0;
        for (char c : perms.toCharArray()) {
            if (c == 'a') {
                permsInt += ZooDefs.Perms.ADMIN;
            } else if (c == 'r') {
                permsInt += ZooDefs.Perms.READ;
            } else if (c == 'w') {
                permsInt += ZooDefs.Perms.WRITE;
            } else if (c == 'c') {
                permsInt += ZooDefs.Perms.CREATE;
            } else if (c == 'd') {
                permsInt += ZooDefs.Perms.DELETE;
            } else {
                throw new ShellException("[" + c + "]" + I18nHelper.invalid());
            }
        }
        return permsInt;
    }

    /**
     * 解析权限为字符串
     *
     * @param permInt int值权限
     * @return 字符串权限
     */
    public static String toPermStr(int permInt) {
        return toPermStr(permInt, null);
    }

    /**
     * 解析权限为字符串
     *
     * @param permInt int值权限
     * @param joinStr 拼接字符串
     * @return 字符串权限
     */
    public static String toPermStr(int permInt, String joinStr) {
        StringBuilder permStr = new StringBuilder(NumberUtil.getBinaryStr(permInt));
        int i = 5 - permStr.length();
        if (i > 5 || i < 0) {
            throw new ShellException(I18nHelper.invalidPerms());
        }
        while (i-- > 0) {
            permStr.insert(0, "0");
        }
        if (joinStr == null) {
            joinStr = "";
        }
        StringBuilder builder = new StringBuilder();
        if (permStr.charAt(0) == '1') {
            builder.append(joinStr).append("a");
        }
        if (permStr.charAt(1) == '1') {
            builder.append(joinStr).append("d");
        }
        if (permStr.charAt(2) == '1') {
            builder.append(joinStr).append("c");
        }
        if (permStr.charAt(3) == '1') {
            builder.append(joinStr).append("w");
        }
        if (permStr.charAt(4) == '1') {
            builder.append(joinStr).append("r");
        }
        if (StringUtil.isNotBlank(joinStr) && !builder.isEmpty()) {
            return builder.substring(1);
        }
        return builder.toString();
    }

    /**
     * 检查ip
     *
     * @param ip ip数据
     */
    public static void checkIP( String ip) {
        String[] segments = ip.split("/");
        if (!RegexUtil.isIPV4(segments[0])) {
            throw new ShellException(segments[0] + I18nResourceBundle.i18nString("shell.zk.aclTip2"));
        }
        if (segments.length == 2 && !segments[0].endsWith("0")) {
            throw new ShellException(segments[0] + I18nResourceBundle.i18nString("shell.zk.aclTip3"));
        }
    }

    /**
     * 是否存在摘要权限
     *
     * @param aclList 权限列表
     * @param user    摘要权限用户名
     */
    public static boolean existDigest(List<ZKACL> aclList, String user) {
        if (CollectionUtil.isNotEmpty(aclList) && StringUtil.isNotBlank(user)) {
            for (ZKACL acl : aclList) {
                if (acl.isDigestACL() && StringUtil.equalsIgnoreCase(user, acl.digestUser())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 解析权限控制为字符串
     *
     * @param aclList 权限列表
     * @return 权限控制字符串
     */
    public static String toAclStr(List<? extends ACL> aclList) {
        StringBuilder builder = new StringBuilder();
        for (ACL acl : aclList) {
            builder.append(",").append(acl.getId().getScheme())
                    .append(":").append(acl.getId().getId())
                    .append(":").append(toPermStr(acl.getPerms()));
        }
        if (!builder.isEmpty()) {
            return builder.substring(1);
        }
        return builder.toString();
    }
}
