package cn.oyzh.easyshell.util.zk;

import cn.oyzh.easyshell.event.ShellEventUtil;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-01-02
 */

public class ShellZKClientActionUtil {

    // public static void forAction(Record record) {
    //     String connectName = ThreadLocalUtil.getVal("connectName");
    //     if (connectName != null) {
    //         if (record instanceof SetDataRequest request) {
    //             ShellZKClientActionArgument arg1 = ShellZKClientActionArgument.ofArgument("-v", request.getVersion());
    //             ShellZKClientActionArgument arg2 = ShellZKClientActionArgument.ofArgument(request.getPath());
    //             ShellZKClientActionArgument arg3 = ShellZKClientActionArgument.ofArgument(new String(request.getData()));
    //             ShellEventUtil.zkClientAction(connectName, "set", arg1, arg2, arg3);
    //         } else if (record instanceof GetDataRequest request) {
    //             if (request.getWatch()) {
    //                 ShellZKClientActionArgument arg1 = ShellZKClientActionArgument.ofArgument("-v", request.getWatch());
    //                 ShellZKClientActionArgument arg2 = ShellZKClientActionArgument.ofArgument(request.getPath());
    //                 ShellEventUtil.zkClientAction(connectName, "get", arg1, arg2);
    //             } else {
    //                 ShellZKClientActionArgument arg2 = ShellZKClientActionArgument.ofArgument(request.getPath());
    //                 ShellEventUtil.zkClientAction(connectName, "get", arg2);
    //             }
    //         } else if (record instanceof CreateRequest request) {
    //         }
    //     }
    // }

    public static void forAction(String connectName, String action) {
        ShellEventUtil.zkClientAction(connectName, action);
    }

    public static void forCreateAction(String connectName, String path, byte[] data, CreateMode createMode, List<? extends ACL> aclList, Long ttl) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(12);
        if (createMode.isSequential()) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-s"));
        }
        if (createMode.isEphemeral()) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-e"));
        }
        if (createMode.isContainer()) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-c"));
        }
        if (ttl != null) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-t", ttl));
        }
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        arguments.add(ShellZKClientActionArgument.ofArgument(new String(data)));
        arguments.add(ShellZKClientActionArgument.ofArgument(ShellZKACLUtil.toAclStr(aclList)));
        ShellEventUtil.zkClientAction(connectName, "create", arguments);
    }

    public static void forSetAction(String connectName, String path, byte[] data, Integer version, boolean stat) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(12);
        if (stat) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-s"));
        }
        if (version != null) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-v", version));
        }
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        arguments.add(ShellZKClientActionArgument.ofArgument(new String(data)));
        arguments.add(ShellZKClientActionArgument.ofArgument(new String(data)));
        ShellEventUtil.zkClientAction(connectName, "set", arguments);
    }

    public static void forLsAction(String connectName, String path, boolean stat, boolean watch, boolean recursion) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(12);
        if (stat) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-s"));
        }
        if (watch) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-w"));
        }
        if (recursion) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-R"));
        }
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        ShellEventUtil.zkClientAction(connectName, "ls", arguments);
    }

    public static void forGetAction(String connectName, String path, boolean stat, boolean watch) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(12);
        if (stat) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-s"));
        }
        if (watch) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-w"));
        }
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        ShellEventUtil.zkClientAction(connectName, "get", arguments);
    }

    public static void forSetAclAction(String connectName, String path, boolean stat, boolean recursion, Integer version, List<? extends ACL> aclList) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(12);
        if (stat) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-s"));
        }
        if (version != null) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-v", version));
        }
        if (recursion) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-R"));
        }
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        arguments.add(ShellZKClientActionArgument.ofArgument(ShellZKACLUtil.toAclStr(aclList)));
        ShellEventUtil.zkClientAction(connectName, "setAcl", arguments);
    }

    public static void forGetAclAction(String connectName, String path, boolean watch) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(12);
        if (watch) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-w"));
        }
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        ShellEventUtil.zkClientAction(connectName, "getAcl", arguments);
    }

    public static void forStatAction(String connectName, String path, boolean watch) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(12);
        if (watch) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-w"));
        }
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        ShellEventUtil.zkClientAction(connectName, "stat", arguments);
    }

    public static void forGetEphemeralsAction(String connectName, String path) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(12);
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        ShellEventUtil.zkClientAction(connectName, "getEphemerals", arguments);
    }

    public static void forGetAllChildrenNumberAction(String connectName, String path) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(4);
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        ShellEventUtil.zkClientAction(connectName, "getAllChildrenNumber", arguments);
    }

    public static void forSyncAction(String connectName, String path) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(4);
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        ShellEventUtil.zkClientAction(connectName, "sync", arguments);
    }

    public static void forDeleteAction(String connectName, String path, Integer version) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(4);
        if (version != null) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-v", version));
        }
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        ShellEventUtil.zkClientAction(connectName, "sync", arguments);
    }

    public static void forListQuotaAction(String connectName, String path) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(4);
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        ShellEventUtil.zkClientAction(connectName, "listquota", arguments);
    }

    public static void forDelQuotaAction(String connectName, String path, boolean bytes, boolean count) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(8);
        if (bytes) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-b"));
        }
        if (count) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-n"));
        }
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        ShellEventUtil.zkClientAction(connectName, "delquota", arguments);
    }

    public static void forSetQuotaAction(String connectName, String path, long bytes, long count) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(8);
        if (bytes >= 0) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-b", bytes));
        }
        if (count >= 0) {
            arguments.add(ShellZKClientActionArgument.ofArgument("-n", count));
        }
        arguments.add(ShellZKClientActionArgument.ofArgument(path));
        ShellEventUtil.zkClientAction(connectName, "setquota", arguments);
    }

    public static void forAddAuthAction(String connectName, String scheme, String auth) {
        List<ShellZKClientActionArgument> arguments = new ArrayList<>(4);
        arguments.add(ShellZKClientActionArgument.ofArgument(scheme));
        arguments.add(ShellZKClientActionArgument.ofArgument(auth));
        ShellEventUtil.zkClientAction(connectName, "addauth", arguments);
    }
}
