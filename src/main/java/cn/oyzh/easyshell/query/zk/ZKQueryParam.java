package cn.oyzh.easyshell.query.zk;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.util.zk.ZKACLUtil;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;

import java.util.ArrayList;
import java.util.List;

/**
 * zk查询参数
 *
 * @author oyzh
 * @since 2025/01/20
 */
public class ZKQueryParam {

    /**
     * 内容
     */
    private String content;

    public String getContent() {
        return content;
    }

    /**
     * 参数
     */
    private List<String> params;

    /**
     * 设置内容
     *
     * @param content 内容
     */
    public void setContent(String content) {
        this.content = content;
        String[] arr = this.content.trim().split(" ");
        this.params = new ArrayList<>();
        for (String s : arr) {
            if (!s.isBlank()) {
                this.params.add(s);
            }
        }
    }

    public boolean isLs() {
        return "ls".equalsIgnoreCase(this.getCommand());
    }

    public boolean isGetEphemerals() {
        return "getEphemerals".equalsIgnoreCase(this.getCommand());
    }

    public boolean isGetAllChildrenNumber() {
        return "getAllChildrenNumber".equalsIgnoreCase(this.getCommand());
    }

    public boolean isWhoami() {
        return "whoami".equalsIgnoreCase(this.getCommand());
    }

    public boolean isSrvr() {
        return "srvr".equalsIgnoreCase(this.getCommand());
    }

    public boolean isMntr() {
        return "mntr".equalsIgnoreCase(this.getCommand());
    }

    public boolean isEnvi() {
        return "envi".equalsIgnoreCase(this.getCommand());
    }

    public boolean isConf() {
        return "conf".equalsIgnoreCase(this.getCommand());
    }

    public boolean isCons() {
        return "cons".equalsIgnoreCase(this.getCommand());
    }

    public boolean isRuok() {
        return "ruok".equalsIgnoreCase(this.getCommand());
    }

    public boolean isCrst() {
        return "crst".equalsIgnoreCase(this.getCommand());
    }

    public boolean isSrst() {
        return "srst".equalsIgnoreCase(this.getCommand());
    }

    public boolean isStat4() {
        return "stat4".equalsIgnoreCase(this.getCommand());
    }

    public boolean isWchc() {
        return "wchc".equalsIgnoreCase(this.getCommand());
    }

    public boolean isWchs() {
        return "wchs".equalsIgnoreCase(this.getCommand());
    }

    public boolean isWchp() {
        return "wchp".equalsIgnoreCase(this.getCommand());
    }

    public boolean isDump() {
        return "dump".equalsIgnoreCase(this.getCommand());
    }

    public boolean isReqs() {
        return "reqs".equalsIgnoreCase(this.getCommand());
    }

    public boolean isDirs() {
        return "dirs".equalsIgnoreCase(this.getCommand());
    }

    public boolean isLs2() {
        return "ls2".equalsIgnoreCase(this.getCommand());
    }

    public boolean isGet() {
        return "get".equalsIgnoreCase(this.getCommand());
    }

    public boolean isSet() {
        return "set".equalsIgnoreCase(this.getCommand());
    }

    public boolean isCreate() {
        return "create".equalsIgnoreCase(this.getCommand());
    }

    public boolean isSync() {
        return "sync".equalsIgnoreCase(this.getCommand());
    }

    public boolean isGetACL() {
        return "getAcl".equalsIgnoreCase(this.getCommand());
    }

    public boolean isStat() {
        return "stat".equalsIgnoreCase(this.getCommand());
    }

    public boolean isSetQuota() {
        return "setquota".equalsIgnoreCase(this.getCommand());
    }

    public boolean isListquota() {
        return "listquota".equalsIgnoreCase(this.getCommand());
    }

    public boolean isRmr() {
        return "rmr".equalsIgnoreCase(this.getCommand());
    }

    public boolean isDeleteall() {
        return "deleteall".equalsIgnoreCase(this.getCommand());
    }

    public boolean isDelete() {
        return "delete".equalsIgnoreCase(this.getCommand());
    }

    public boolean isSetACL() {
        return "setAcl".equalsIgnoreCase(this.getCommand());
    }

    public String getPath() {
        try {
            if (this.isGetEphemerals()) {
                if (this.params.size() == 2) {
                    return this.params.get(1);
                }
            }
            if (this.isSync() || this.isLs2() || this.isGetAllChildrenNumber() || this.isStat() || this.isListquota()
                    || this.isRmr() || this.isDeleteall() || this.isDelete()) {
                return this.params.get(1);
            }
            if (this.isLs()) {
                if (this.hasParamStat()) {
                    return this.params.get(2);
                }
                return this.params.get(1);
            }
            if (this.isGet()) {
                if (this.hasParamStat()) {
                    return this.params.get(2);
                }
                return this.params.get(1);
            }
            if (this.isSet()) {
                if (this.hasParamStat()) {
                    return this.params.get(2);
                }
                return this.params.get(1);
            }
            if (this.isGetACL()) {
                if (this.hasParamStat()) {
                    return this.params.get(2);
                }
                return this.params.get(1);
            }
            if (this.isCreate()) {
                int index = 0;
                for (String param : this.params) {
                    if (index == 0 || param.equals("-s")
                            || param.equals("-c")
                            || param.equals("-e")) {
                        index++;
                        continue;
                    }
                    return param;
                }
            }
            if (this.isSetACL()) {
                int index = 0;
                for (String param : this.params) {
                    if (index == 0 || param.equals("-s")) {
                        index++;
                        continue;
                    }
                    return param;
                }
            }
            if (this.isSetQuota()) {
                int index = 0;
                boolean isParam = false;
                for (String param : this.params) {
                    if (index == 0) {
                        index++;
                        continue;
                    }
                    if (param.startsWith("-")) {
                        index++;
                        isParam = true;
                        continue;
                    }
                    if (isParam) {
                        isParam = false;
                        continue;
                    }
                    return param;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String getData() {
        if (this.isSet()) {
            if (this.hasParamStat()) {
                return this.params.get(3);
            }
            return this.params.get(2);
        }
        if (this.isCreate()) {
            int index = 0;
            for (String param : this.params) {
                if (index == 0 || param.equals("-s")
                        || param.equals("-c")
                        || param.equals("-e")
                        || param.startsWith("/")) {
                    index++;
                    continue;
                }
                return param;
            }
        }
        return null;
    }

    public long getParamB() {
        boolean isParam = false;
        for (String param : this.params) {
            if (param.equals("-b")) {
                isParam = true;
                continue;
            }
            if (isParam) {
                return Long.parseLong(param);
            }
        }
        return -1;
    }

    public int getParamN() {
        boolean isParam = false;
        for (String param : this.params) {
            if (param.equals("-n")) {
                isParam = true;
                continue;
            }
            if (isParam) {
                return Integer.parseInt(param);
            }
        }
        return -1;
    }

    public int getParamV() {
        boolean isParam = false;
        for (String param : this.params) {
            if (param.equals("-v")) {
                isParam = true;
                continue;
            }
            if (isParam) {
                return Integer.parseInt(param);
            }
        }
        return -1;
    }

    public CreateMode getCreateMode() {
        boolean ephemeral = false;
        boolean container = false;
        boolean sequential = false;
        for (String param : params) {
            if (param.equals("-e")) {
                ephemeral = true;
            } else if (param.equals("-c")) {
                container = true;
            } else if (param.equals("-s")) {
                sequential = true;
            }
        }
        if (container) {
            return CreateMode.CONTAINER;
        }
        if (sequential && ephemeral) {
            return CreateMode.EPHEMERAL_SEQUENTIAL;
        }
        if (ephemeral) {
            return CreateMode.EPHEMERAL;
        }
        if (sequential) {
            return CreateMode.PERSISTENT_SEQUENTIAL;
        }
        return CreateMode.PERSISTENT;
    }

    public List<ACL> getACL() {
        List<ACL> aclList = List.of(ZKACLUtil.OPEN_ACL);
        if (this.isCreate()) {
            int index = 0;
            for (String param : this.params) {
                if (index == 0 || param.equals("-s")
                        || param.equals("-c")
                        || param.equals("-e")
                        || param.startsWith("/")
                        || !param.contains(":")) {
                    index++;
                    continue;
                }
                aclList = ZKACLUtil.parseAcl(param);
            }
        } else if (this.isSetACL()) {
            int index = 0;
            for (String param : this.params) {
                if (index == 0 || param.equals("-s")
                        || param.equals("-v")
                        || param.startsWith("/")
                        || !param.contains(":")) {
                    index++;
                    continue;
                }
                aclList = ZKACLUtil.parseAcl(param);
            }
        }
        return aclList;
    }

    public boolean hasParamStat() {
        if (this.isCreate()) {
            return false;
        }
        if (this.isLs2() || this.isStat()) {
            return true;
        }
        for (String param : this.params) {
            if ("-s".equals(param)) {
                return true;
            }
        }
        return false;
    }

    public String getCommand() {
        return CollectionUtil.getFirst(this.params);
    }
}
