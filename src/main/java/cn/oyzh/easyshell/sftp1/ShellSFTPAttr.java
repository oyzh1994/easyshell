//package cn.oyzh.easyshell.sshj.sftp;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author oyzh
// * @since 2025-03-05
// */
//public class ShellSFTPAttr {
//
//    private Map<Integer, String> owner;
//
//    private Map<Integer, String> group;
//
//    public String getOwner(int id) {
//        return this.owner == null ? null : this.owner.get(id);
//    }
//
//    public void putOwner(int id, String owner) {
//        if (owner == null) {
//            return;
//        }
//        if (this.owner == null) {
//            this.owner = new HashMap<>();
//        }
//        this.owner.put(id, owner);
//    }
//
//    public String getGroup(int id) {
//        return this.group == null ? null : this.group.get(id);
//    }
//
//    public void putGroup(int id, String group) {
//        if (group == null) {
//            return;
//        }
//        if (this.group == null) {
//            this.group = new HashMap<>();
//        }
//        this.group.put(id, group);
//    }
//
//    public void clear() {
//        if (this.owner != null) {
//            this.owner.clear();
//            this.owner = null;
//        }
//        if (this.group != null) {
//            this.group.clear();
//            this.group = null;
//        }
//    }
//}
