package cn.oyzh.easyshell.domain;


import cn.oyzh.fx.plus.domain.AppGroup;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.Table;

/**
 * 分组(目录)
 *
 * @author oyzh
 * @since 2023-08-16
 */
@Table("t_group")
public class ShellGroup extends AppGroup {

    /**
     * Fixed tag ids, comma-separated.
     */
    @Column
    private String tags;

    /**
     * 构造函数
     */
    public ShellGroup() {
        super();
    }

    /**
     * 构造函数
     *
     * @param gid    分组id
     * @param name   名称
     * @param expand 是否展开
     */
    public ShellGroup(String gid, String name, boolean expand) {
        super(gid, name, expand);
    }

    /** 获取标签 */
    public String getTags() {
        return this.tags;
    }

    /** 设置标签 */
    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public void copy(Object obj) {
        super.copy(obj);
        if (obj instanceof ShellGroup group) {
            this.tags = group.tags;
        }
    }
}
