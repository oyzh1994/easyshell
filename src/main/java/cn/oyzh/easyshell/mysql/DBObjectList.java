package cn.oyzh.easyshell.mysql;


import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.DBObjectStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2024/07/13
 */
public abstract class DBObjectList<S extends DBObjectStatus> extends ArrayList<S> {

    public static final byte TYPE_NORMAL = 0;

    public static final byte TYPE_DELETED = 1;

    public static final byte TYPE_CREATED = 2;

    public static final byte TYPE_CHANGED = 3;

    // protected void valueList(List<S> list) {
    //     if (CollUtil.isNotEmpty(list)) {
    //         this.clear();
    //         this.addAll(list);
    //     }
    // }

    public boolean isChanged() {
        for (S s : this) {
            if (StringUtil.isNotBlank(s.getStatus())) {
                return true;
            }
        }
        return false;
    }

    public List<S> createdList() {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        return this.stream().filter(DBObjectList::isCreated).collect(Collectors.toList());
    }

    public List<S> changedList() {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        return this.stream().filter(DBObjectList::isChanged).collect(Collectors.toList());
    }

    public List<S> deletedList() {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        return this.stream().filter(DBObjectList::isDeleted).collect(Collectors.toList());
    }

    public List<S> normalList() {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        return this.stream().filter(DBObjectList::isNormal).collect(Collectors.toList());
    }

    public List<S> filterList(byte... types) {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        if (types == null || types.length == 0) {
            return this;
        }
        List<S> list = new ArrayList<>();
        for (byte type : types) {
            List<S> list1 = null;
            if (type == TYPE_NORMAL) {
                list1 = this.normalList();
            } else if (type == TYPE_CHANGED) {
                list1 = this.changedList();
            } else if (type == TYPE_CREATED) {
                list1 = this.createdList();
            } else if (type == TYPE_DELETED) {
                list1 = this.deletedList();
            }
            if (CollectionUtil.isNotEmpty(list1)) {
                list.addAll(list1);
            }
        }
        return list;
    }

    @Override
    public boolean add(S s) {
        if (s != null) {
            return super.add(s);
        }
        return false;
    }

    public void remove(S s) {
        super.remove(s);
    }

    public boolean contains(S s) {
        return super.contains(s);
    }

    public boolean hasDeleted() {
        if (this.isEmpty()) {
            return false;
        }
        for (S s : this) {
            if (isDeleted(s)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCreated() {
        if (!this.isEmpty()) {
            for (S s : this) {
                if (isCreated(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasChanged() {
        if (!this.isEmpty()) {
            for (S s : this) {
                if (isChanged(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasNormal() {
        if (!this.isEmpty()) {
            for (S s : this) {
                if (isNormal(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isDeleted(DBObjectStatus status) {
        return status != null && status.isDeleted();
    }

    public static boolean isCreated(DBObjectStatus status) {
        return status != null && !status.isDeleted() && status.isCreated();
    }

    public static boolean isChanged(DBObjectStatus status) {
        return status != null && !status.isCreated() && !status.isDeleted() && status.isChanged();
    }

    public static boolean isNormal(DBObjectStatus status) {
        if (status == null) {
            return false;
        }
        return !status.isChanged() && !status.isDeleted() && !status.isCreated();
    }
}

