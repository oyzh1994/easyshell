package cn.oyzh.easyssh.trees.docker;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.docker.DockerContainer;
import cn.oyzh.easyssh.docker.DockerParser;
import cn.oyzh.easyssh.event.SSHEventUtil;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.sftp.SftpUtil;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.util.SSHI18nHelper;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.tableview.TableViewMouseSelectHelper;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-12
 */
public class SSHContainerTableView extends FXTableView<DockerContainer> {

    private SSHClient client;

    public SSHClient getClient() {
        return client;
    }

    public void setClient(SSHClient client) {
        this.client = client;
    }

    private List<DockerContainer> containers;

    public void loadContainer() {
        String output = this.client.exec_docker_ps();
        DockerParser parser = new DockerParser();
        this.containers = parser.ps(output);
        this.setItem(this.containers);
    }

    private String filterText;

    public void setFilterText(String filterText) {
        if (!StringUtil.equals(this.filterText, filterText)) {
            this.filterText = filterText;
            this.refreshContainer();
        }
    }

    public void refreshContainer() {
        if (this.containers == null) {
            this.loadContainer();
        } else {
            this.setItem(this.doFilter(this.containers));
        }
    }

    private List<DockerContainer> doFilter(List<DockerContainer> files) {
        if (CollectionUtil.isNotEmpty(files)) {
            return files.stream()
                    .filter(f -> {
                        if (StringUtil.isEmpty(this.filterText)) {
                            return true;
                        }
                        return StringUtil.containsIgnoreCase(f.getContainerId(), this.filterText)
                                || StringUtil.containsIgnoreCase(f.getImage(), this.filterText)
                                || StringUtil.containsIgnoreCase(f.getNames(), this.filterText);
                    })
                    .collect(Collectors.toList());
        }
        return files;
    }

    public void deleteContainer(boolean force) {
        DockerContainer container = this.getSelectedItem();
        if (!MessageBox.confirm(I18nHelper.deleteContainer() + " " + container.getNames())) {
            return;
        }
        try {
            String output;
            if (force) {
                output = this.client.exec_docker_rm_f(container.getContainerId());
            } else {
                output = this.client.exec_docker_rm(container.getContainerId());
            }
            if (StringUtil.isNotBlank(output)) {
                this.containers.remove(container);
                this.refreshContainer();
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        DockerContainer container = this.getSelectedItem();
        if (container == null) {
            return Collections.emptyList();
        }
        List<FXMenuItem> menuItems = new ArrayList<>();
        FXMenuItem deleteContainer = MenuItemHelper.deleteContainer("12", () -> this.deleteContainer(false));
        FXMenuItem forceDeleteContainer = MenuItemHelper.forceDeleteContainer("12", () -> this.deleteContainer(true));
        menuItems.add(deleteContainer);
        menuItems.add(forceDeleteContainer);
        return menuItems;
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<? extends MenuItem> items = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(items)) {
                this.showContextMenu(items, e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
    }
}
