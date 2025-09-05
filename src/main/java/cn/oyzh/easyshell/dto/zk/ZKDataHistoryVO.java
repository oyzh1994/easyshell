package cn.oyzh.easyshell.dto.zk;

import cn.oyzh.common.Index;
import cn.oyzh.common.date.DateUtil;
import cn.oyzh.easyshell.domain.zk.ShellZKDataHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * zk数据历史vo信息
 *
 * @author oyzh
 * @since 2024/04/24
 */
public class ZKDataHistoryVO extends ShellZKDataHistory implements Index {

    /**
     * 索引
     */
    private int index;

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 复制
     *
     * @param filter zk数据历史
     * @param index  索引
     * @return zk数据历史vo
     */
    public static ZKDataHistoryVO convert(ShellZKDataHistory filter, int index) {
        ZKDataHistoryVO vo = new ZKDataHistoryVO();
        vo.copy(filter);
        vo.setIid(filter.getIid());
        vo.setIndex(index);
        return vo;
    }

    /**
     * 转换
     *
     * @param list zk数据历史
     * @return zk数据历史列表
     */
    public static List<ZKDataHistoryVO> convert(List<ShellZKDataHistory> list) {
        List<ZKDataHistoryVO> voList = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            voList.add(convert(list.get(i), i + 1));
        }
        return voList;
    }

    /**
     * 获取格式化的保存时间
     *
     * @return 结果
     */
    public String getSaveTimeFormated() {
        return DateUtil.format("yy-MM-dd HH:mm:ss", this.getSaveTime());
    }
}
