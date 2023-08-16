package cn.oyzh.easyssh.store;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.common.util.FileStore;
import cn.oyzh.easyssh.SSHConst;
import cn.oyzh.easyssh.domain.SSHGroup;
import com.alibaba.fastjson.JSON;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ssh分组存储
 *
 * @author oyzh
 * @since 2023/6/22
 */
@Slf4j
public class SSHGroupStore extends FileStore<SSHGroup> {

    /**
     * 当前实例
     */
    public static final SSHGroupStore INSTANCE = new SSHGroupStore();

    /**
     * 已加载的ssh键
     */
    private final List<SSHGroup> SSHGroups;

    {
        this.filePath(SSHConst.STORE_PATH + "ssh_group.json");
        log.info("SSHGroupStore filePath:{} charset:{} init {}.", this.filePath(), this.charset(), super.init() ? "success" : "fail");
        this.SSHGroups = this.load();
    }

    @Override
    public synchronized List<SSHGroup> load() {
        if (this.SSHGroups == null) {
            String text = FileUtil.readString(this.storeFile(), this.charset());
            if (StrUtil.isBlank(text)) {
                return new ArrayList<>();
            }
            List<SSHGroup> SSHGroups = JSON.parseArray(text, SSHGroup.class);
            if (CollUtil.isNotEmpty(SSHGroups)) {
                SSHGroups = SSHGroups.parallelStream().sorted().collect(Collectors.toList());
            }
            return SSHGroups;
        }
        return this.SSHGroups;
    }

    /**
     * 添加分组
     *
     * @param groupName 分组名称
     * @return 结果
     */
    public synchronized SSHGroup add(@NonNull String groupName) {
        SSHGroup group = new SSHGroup(UUID.fastUUID().toString(true), groupName, false);
        if (this.add(group)) {
            return group;
        }
        return null;
    }

    @Override
    public synchronized boolean add(@NonNull SSHGroup SSHGroup) {
        try {
            if (!this.SSHGroups.contains(SSHGroup)) {
                // 添加到集合
                this.SSHGroups.add(SSHGroup);
                // 更新数据
                return this.save(this.SSHGroups);
            }
        } catch (Exception e) {
            log.warn("add error,err:{}", e.getMessage());
        }
        return false;
    }

    @Override
    public synchronized boolean update(@NonNull SSHGroup SSHGroup) {
        try {
            // 更新数据
            if (this.SSHGroups.contains(SSHGroup)) {
                return this.save(this.SSHGroups);
            }
        } catch (Exception e) {
            log.warn("update error,err:{}", e.getMessage());
        }
        return false;
    }

    @Override
    public synchronized boolean delete(@NonNull SSHGroup SSHGroup) {
        try {
            // 删除数据
            if (this.SSHGroups.remove(SSHGroup)) {
                return this.save(this.SSHGroups);
            }
        } catch (Exception e) {
            log.warn("delete error,err:{}", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 是否存在此分组信息
     *
     * @param SSHGroup 分组信息
     * @return 结果
     */
    public boolean exist(SSHGroup SSHGroup) {
        if (SSHGroup == null) {
            return false;
        }
        for (SSHGroup group : this.SSHGroups) {
            if (Objects.equals(group.getName(), SSHGroup.getName()) && group != SSHGroup) {
                return true;
            }
        }
        return false;
    }
}
