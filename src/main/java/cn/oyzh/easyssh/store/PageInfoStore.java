package cn.oyzh.easyssh.store;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.common.util.FileStore;
import cn.oyzh.easyssh.SSHConst;
import cn.oyzh.easyssh.domain.SSHPageInfo;
import com.alibaba.fastjson.JSON;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * 页面信息储存
 *
 * @author oyzh
 * @since 2023/06/17
 */
public class PageInfoStore extends FileStore<SSHPageInfo> {

    /**
     * 当前实例
     */
    public static final PageInfoStore INSTANCE = new PageInfoStore();

    /**
     * 当前设置
     */
    public static final SSHPageInfo PAGE_INFO = INSTANCE.loadOne();

    {
        this.filePath(SSHConst.STORE_PATH + "page_info.json");
        JulLog.info("PageInfoStore filePath:{} charset:{} init {}.", this.filePath(), this.charset(), super.init() ? "success" : "fail");
    }

    @Override
    public synchronized List<SSHPageInfo> load() {
        SSHPageInfo pageInfo = null;
        String text = FileUtil.readString(this.storeFile(), this.charset());
        if (StringUtil.isNotBlank(text)) {
            try {
                pageInfo = JSON.parseObject(text, SSHPageInfo.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (pageInfo == null) {
            pageInfo = new SSHPageInfo();
        }
        return List.of(pageInfo);
    }

    @Override
    public boolean add(@NonNull SSHPageInfo data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean update(@NonNull SSHPageInfo data) {
        return this.save(data);
    }

    @Override
    public boolean delete(@NonNull SSHPageInfo data) {
        throw new UnsupportedOperationException();
    }
}
