package cn.oyzh.easyshell.tabs.zk.node;

import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.easyshell.dto.zk.ZKACL;
import cn.oyzh.easyshell.fx.zk.ShellZKACLControl;
import cn.oyzh.easyshell.fx.zk.ShellZKACLTableView;
import cn.oyzh.easyshell.trees.zk.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import org.apache.zookeeper.data.Stat;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * zk节点acl组件
 *
 * @author oyzh
 * @since 2025/04/11
 */
public class ShellZKNodeACLTabController extends SubTabController {

//    /**
//     * 右侧acl分页组件
//     */
//    @FXML
//    private PageBox<ZKACL> aclPage;
//
//    /**
//     * 分页信息
//     */
//    private Paging<ZKACL> aclPaging;

    /**
     * 右侧zk权限视图切换按钮
     */
    @FXML
    private FXToggleSwitch aclViewSwitch;

    /**
     * acl表视图
     */
    @FXML
    private ShellZKACLTableView aclTableView;

    /**
     * 重新载入权限
     */
    @FXML
    public void reloadACL() {
        try {
            this.activeItem().refreshACL();
            this.initACL();
            MessageBox.okToast(I18nHelper.operationSuccess());
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 添加权限
     */
    @FXML
    private void addACL() {
//        ZKEventUtil.showAddACL(this.activeItem(), this.client());
        StageAdapter adapter = ShellViewFactory.zkAddACL(this.activeItem(), this.client());
        // 操作成功
        if (adapter != null && BooleanUtil.isTrue(adapter.getProp("result"))) {
            this.reloadACL();
        }
    }

    /**
     * 复制访问控制
     */
    @FXML
    private void copyACL() {
        ZKACL acl = this.aclTableView.getSelectedItem();
        if (acl == null) {
            MessageBox.warn(I18nHelper.acl() + " " + I18nHelper.isEmpty());
            return;
        }
        try {
            String builder = acl.idFriend().getName(this.aclViewSwitch.isSelected()) + " " + acl.idFriend().getValue(this.aclViewSwitch.isSelected()) + System.lineSeparator() + acl.schemeFriend().getName(this.aclViewSwitch.isSelected()) + " " + acl.schemeFriend().getValue(this.aclViewSwitch.isSelected()) + System.lineSeparator() + acl.permsFriend().getName(this.aclViewSwitch.isSelected()) + " " + acl.permsFriend().getValue(this.aclViewSwitch.isSelected());
            ClipboardUtil.setStringAndTip(builder);
            MessageBox.okToast(I18nHelper.operationSuccess());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 修改权限
     */
    @FXML
    private void updateACL() {
        ZKACL acl = this.aclTableView.getSelectedItem();
        if (acl == null) {
            return;
        }
        StageAdapter adapter = ShellViewFactory.zkUpdateACL(this.activeItem(), this.client(), acl);
        // 操作成功
        if (adapter != null && BooleanUtil.isTrue(adapter.getProp("result"))) {
            this.reloadACL();
        }
    }

    /**
     * 删除权限
     */
    @FXML
    private void deleteACL() {
        ZKACL acl = this.aclTableView.getSelectedItem();
        if (acl == null) {
            return;
        }
        if (this.activeItem().acl().size() == 1) {
            MessageBox.warn(this.i18nString("shell.zk.aclTip1"));
            return;
        }
        if (!MessageBox.confirm(I18nHelper.deleteACL() + " " + acl.idVal() + " ?")) {
            return;
        }
        try {
            Stat stat = this.activeItem().deleteACL(acl);
            if (stat != null) {
                // 删除权限
                this.aclTableView.removeItem(acl);
                this.activeItem().acl().remove(acl);
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

//    /**
//     * 权限列表，上一页
//     */
//    @FXML
//    private void aclPrevPage() {
//        if (this.aclPaging != null) {
//            this.renderACLView(this.aclPaging.prevPage());
//        }
//    }
//
//    /**
//     * 权限列表，下一页
//     */
//    @FXML
//    private void aclNextPage() {
//        if (this.aclPaging != null) {
//            this.renderACLView(this.aclPaging.nextPage());
//        }
//    }

//    /**
//     * 渲染权限控件
//     *
//     * @param pageNo 页码
//     */
//    private void renderACLView(long pageNo) {
//        // 获取zk权限分页数据
//        List<ZKACL> aclList = this.aclPaging.page(pageNo);
//        // 设置分页信息
//        this.aclPage.setPaging(this.aclPaging);
//        List<ShellZKACLControl> list = new ArrayList<>();
//        for (ZKACL zkacl : aclList) {
//            ShellZKACLControl control = new ShellZKACLControl();
//            control.setId(zkacl.getId());
//            control.setPerms(zkacl.getPerms());
//            control.setFriendly(this.aclViewSwitch.isSelected());
//            control.setAuthed(this.client().isDigestAuthed(zkacl.idVal()));
//            list.add(control);
//        }
//        this.aclTableView.setItem(list);
//    }

    /**
     * 渲染权限控件
     *
     * @param aclList acl列表
     */
    private void renderACLView(List<ZKACL> aclList) {
        List<ShellZKACLControl> list = new ArrayList<>();
        for (ZKACL zkacl : aclList) {
            ShellZKACLControl control = new ShellZKACLControl();
            control.setId(zkacl.getId());
            control.setPerms(zkacl.getPerms());
            control.setFriendly(this.aclViewSwitch.isSelected());
            control.setAuthed(this.client().isDigestAuthed(zkacl.idVal()));
            list.add(control);
        }
        this.aclTableView.setItem(list);
    }

    /**
     * 初始化权限
     */
    public void initACL() {
        if ( this.activeItem() == null) {
            return;
        }
        if (this.activeItem().aclEmpty()) {
//            this.aclPaging = null;
            this.aclViewSwitch.disable();
            this.aclTableView.clearItems();
        } else {
            this.aclViewSwitch.enable();
            List<ZKACL> aclList = this.activeItem().acl();
//            // 获取分页控件
//            this.aclPaging = new Paging<>(aclList, 10);
            // 渲染首页数据
//            this.renderACLView(0);
            this.renderACLView(aclList);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 切换显示监听
        this.aclViewSwitch.selectedChanged((t3, t2, t1) -> this.initACL());
    }

//    /**
//     * 节点访问控制已新增事件
//     *
//     * @param event 事件
//     */
//    @EventSubscribe
//    public void onNodeACLAdded(ZKNodeACLAddedEvent event) {
//        if (event.data() == this.client().zkConnect()) {
//            this.reloadACL();
////            if (this.aclPaging != null) {
////                this.renderACLView(this.aclPaging.lastPage());
////            }
//        }
//    }

//    /**
//     * 节点访问控制已变更事件
//     *
//     * @param event 事件
//     */
//    @EventSubscribe
//    public void onNodeACLUpdated(ZKNodeACLUpdatedEvent event) {
//        if (event.data() == this.client().zkConnect()) {
//            this.reloadACL();

    /// /            Long curPage = this.aclPaging == null ? null : this.aclPaging.currentPage();
    /// /            if (curPage != null) {
    /// /                this.renderACLView(curPage);
    /// /            }
//        }
//    }
//     private ZKConnectTreeItem treeItem() {
//         return this.parent().getTreeItem();
//     }

    private ShellZKNodeTreeItem activeItem() {
        return this.parent().getActiveItem();
    }

    private ShellZKClient client() {
        return this.parent().getClient();
    }

    @Override
    public ShellZKNodeTabController parent() {
        return (ShellZKNodeTabController) super.parent();
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
        this.aclTableView.setAddAction(this::addACL);
        this.aclTableView.setCopyAction(this::copyACL);
        this.aclTableView.setEditAction(this::updateACL);
        this.aclTableView.setDeleteAction(this::deleteACL);
    }
}