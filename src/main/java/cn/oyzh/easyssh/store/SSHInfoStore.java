package cn.oyzh.easyssh.store;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.FileStore;
import cn.oyzh.easyssh.SSHConst;
import cn.oyzh.easyssh.domain.SSHInfo;
import com.alibaba.fastjson.JSON;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ssh信息存储
 *
 * @author oyzh
 * @since 2023/6/23
 */
@Slf4j
public class SSHInfoStore extends FileStore<SSHInfo> {

    /**
     * 当前实例
     */
    public static final SSHInfoStore INSTANCE = new SSHInfoStore();

    /**
     * 已加载的ssh键
     */
    private final List<SSHInfo> sshInfos;

    {
        this.filePath(SSHConst.STORE_PATH + "ssh_info.json");
        log.info("SSHInfoStore filePath:{} charset:{} init {}.", this.filePath(), this.charset(), super.init() ? "success" : "fail");
        this.sshInfos = this.load();
        for (SSHInfo SSHInfo : this.sshInfos) {
            if (StrUtil.isBlank(SSHInfo.getId())) {
                SSHInfo.setId(UUID.fastUUID().toString(true));
                this.update(SSHInfo);
            }
        }
    }

    @Override
    public synchronized List<SSHInfo> load() {
        if (this.sshInfos == null) {
            String text = FileUtil.readString(this.storeFile(), this.charset());
            if (StrUtil.isBlank(text)) {
                return new ArrayList<>();
            }
            List<SSHInfo> sshInfos = JSON.parseArray(text, SSHInfo.class);
            if (CollUtil.isNotEmpty(sshInfos)) {
                sshInfos = sshInfos.parallelStream().sorted().collect(Collectors.toList());
            }
            return sshInfos;
        }
        return this.sshInfos;
    }

    @Override
    public synchronized boolean add(@NonNull SSHInfo sshInfo) {
        try {
            if (!this.sshInfos.contains(sshInfo)) {
                sshInfo.setId(UUID.fastUUID().toString(true));
                // 添加到集合
                this.sshInfos.add(sshInfo);
                // 更新数据
                return this.save(this.sshInfos);
            }
        } catch (Exception e) {
            log.warn("add error,err:{}", e.getMessage());
        }
        return false;
    }

    @Override
    public synchronized boolean update(@NonNull SSHInfo sshInfo) {
        try {
            // 更新数据
            if (this.sshInfos.contains(sshInfo)) {
                return this.save(this.sshInfos);
            }
        } catch (Exception e) {
            log.warn("update error,err:{}", e.getMessage());
        }
        return false;
    }

    @Override
    public synchronized boolean delete(@NonNull SSHInfo sshInfo) {
        try {
            // 删除数据
            if (this.sshInfos.remove(sshInfo)) {
                return this.save(this.sshInfos);
            }
        } catch (Exception e) {
            log.warn("delete error,err:{}", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Paging<SSHInfo> getPage(int limit, Map<String, Object> params) {
        // 加载数据
        List<SSHInfo> sshInfos = this.load();
        // 分页对象
        Paging<SSHInfo> paging = new Paging<>(sshInfos, limit);
        // 数据为空
        if (CollUtil.isNotEmpty(sshInfos)) {
            String searchKeyWord = params == null ? null : (String) params.get("searchKeyWord");
            // 过滤数据
            if (StrUtil.isNotBlank(searchKeyWord)) {
                final String kw = searchKeyWord.toLowerCase().trim();
                sshInfos = sshInfos.parallelStream().filter(z ->
                        z.getHost() != null && z.getHost().contains(kw)
                                || z.getName() != null && z.getName().toLowerCase().contains(kw)
                                || z.getRemark() != null && z.getRemark().toLowerCase().contains(kw)
                ).collect(Collectors.toList());
            }
            // 对数据排序
            sshInfos = sshInfos.parallelStream().sorted().collect(Collectors.toList());
            // 添加到分页数据
            paging.dataList(sshInfos);
        }
        return paging;
    }

    /**
     * 是否存在此ssh信息
     *
     * @param sshInfo ssh信息
     * @return 结果
     */
    public boolean exist(SSHInfo sshInfo) {
        if (sshInfo == null) {
            return false;
        }
        for (SSHInfo info : this.sshInfos) {
            if (info.compareTo(sshInfo) == 0 && info != sshInfo) {
                return true;
            }
        }
        return false;
    }
}
