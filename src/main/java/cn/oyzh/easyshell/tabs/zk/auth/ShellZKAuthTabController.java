package cn.oyzh.easyshell.tabs.zk.auth;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.easyshell.fx.zk.ShellZKAuthTableView;
import cn.oyzh.easyshell.store.zk.ShellZKAuthStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.util.List;

/**
 * zk认证tab内容组件
 *
 * @author oyzh
 * @since 2025/09/06
 */
public class ShellZKAuthTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * 认证列表
     */
    @FXML
    private ShellZKAuthTableView authTable;

    /**
     * 认证搜索
     */
    @FXML
    private ClearableTextField authSearchKW;

    /**
     * 客户端
     */
    private ShellZKClient client;

    /**
     * zk认证配置储存
     */
    private final ShellZKAuthStore authStore = ShellZKAuthStore.INSTANCE;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void init(ShellZKClient client) {
        this.client = client;
    }

    /**
     * 获取zk客户端
     *
     * @return zk客户端
     */
    public ShellZKClient client() {
        return this.client;
    }

    /**
     * 获取zk信息
     *
     * @return zk信息
     */
    public ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }

    /**
     * 初始化认证
     */
    private void initAuthDataList() {
        if (!this.authTable.hasData()) {
            this.authTable.setAuths(this.shellConnect().getAuths());
        } else {
            this.authTable.setKw(this.authSearchKW.getText());
        }
    }

    /**
     * 添加认证
     */
    @FXML
    private void addAuth() {
        StageAdapter adapter = ShellViewFactory.zkAuthAdd(this.shellConnect());
        if (adapter == null) {
            return;
        }
        ShellZKAuth auth = adapter.getProp("auth");
        if (auth == null) {
            return;
        }
        this.authTable.addAuth(auth);
    }

    /**
     * 删除认证
     */
    @FXML
    private void deleteAuth() {
        try {
            List<ShellZKAuth> auths = this.authTable.getSelectedItems();
            if (CollectionUtil.isEmpty(auths)) {
                return;
            }
            if (MessageBox.confirm(I18nHelper.deleteData())) {
                for (ShellZKAuth auth : auths) {
                    if (auth != null) {
                        this.authStore.delete(auth.getUid());
                        this.client.removeAuth(auth);
                    }
                }
                this.authTable.removeItem(auths);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 复制认证
     */
    @FXML
    private void copyAuth() {
        ShellZKAuth auth = this.authTable.getSelectedItem();
        if (auth == null) {
            return;
        }
        String data = I18nHelper.userName() + " " + auth.getUser() + System.lineSeparator()
                + I18nHelper.password() + " " + auth.getPassword();
        ClipboardUtil.setStringAndTip(data);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 认证监听
        this.authSearchKW.addTextChangeListener((observableValue, s, t1) -> this.initAuthDataList());
    }

    /**
     * 初始化标志位
     */
    private boolean initFlag = false;

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        this.root.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !this.initFlag) {
                this.initFlag = true;
                this.initAuthDataList();
            }
        });
    }
}
