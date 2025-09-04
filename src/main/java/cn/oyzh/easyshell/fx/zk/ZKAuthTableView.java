package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ZKAuth;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.scene.control.SelectionMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-12-19
 */
public class ZKAuthTableView extends FXTableView<ZKAuth> {

    /**
     * 当前过滤列表
     */
    private List<ZKAuth> list;

    /**
     * 关键字
     */
    private String kw;

    public boolean hasData() {
        return list != null;
    }

    public void setAuths(List<ZKAuth> auths) {
        this.list = auths;
        this.initDataList();
    }

    public void setKw(String kw) {
        this.kw = kw;
        this.initDataList();
    }

    public List<ZKAuth> getAuths() {
        List<ZKAuth> list = new ArrayList<>(this.list.size());
        for (ZKAuth authVO : this.list) {
            if (authVO != null && StringUtil.isNotBlank(authVO.getUser()) && StringUtil.isNotBlank(authVO.getPassword())) {
                list.add(authVO);
            }
        }
        return list;
    }

    private void initDataList() {
        List<ZKAuth> list = new ArrayList<>(12);
        if (this.list != null) {
            for (ZKAuth authVO : this.list) {
                if (StringUtil.isBlank(this.kw) || StringUtil.containsIgnoreCase(authVO.getUser(), this.kw)
                        || StringUtil.containsIgnoreCase(authVO.getPassword(), this.kw)) {
                    list.add(authVO);
                }
            }
        }
        super.setItem(list);
    }

    public void addAuth(ZKAuth authVO) {
        if (this.list == null) {
            this.list = new ArrayList<>(12);
        }
        this.list.add(authVO);
        this.initDataList();
    }

    @Override
    public void removeItem(Object item) {
        if (this.list != null) {
            this.list.remove(item);
        }
        super.removeItem(item);
        this.initDataList();
    }

    @Override
    public void removeItem(List<?> item) {
        if (this.list != null) {
            this.list.removeAll(item);
        }
        super.removeItem(item);
        this.initDataList();
    }

    @Override
    public void initNode() {
        super.initNode();
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
}
