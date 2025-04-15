package cn.oyzh.easyshell.fx.jump;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.key.ShellCopyIdKeyController;
import cn.oyzh.easyshell.controller.key.ShellUpdateKeyController;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.domain.ShellSSHConfig;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.store.ShellSSHConfigStore;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.chooser.DirChooserHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.tableview.TableViewMouseSelectHelper;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-04-03
 */
public class ShellJumpTableView extends FXTableView<ShellSSHConfig> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }

    /**
     * 密钥存储器
     */
    private final ShellSSHConfigStore configStore = ShellSSHConfigStore.INSTANCE;

    public void init(String iid) {
        List<ShellSSHConfig> configs = this.configStore.listByIid(iid);
        this.setItem(configs);
    }

}
