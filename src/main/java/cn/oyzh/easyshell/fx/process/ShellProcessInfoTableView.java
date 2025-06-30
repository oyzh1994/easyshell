package cn.oyzh.easyshell.fx.process;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ssh2.process.ShellProcessExec;
import cn.oyzh.easyshell.ssh2.process.ShellProcessInfo;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-29
 */
public class ShellProcessInfoTableView extends FXTableView<ShellProcessInfo> {

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<? extends MenuItem> menuItems = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(menuItems)) {
                this.showContextMenu(menuItems, e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
        // 快捷键
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (KeyboardUtil.stop_keyCombination.match(event)) {// 杀死进程
                this.killProcess(this.getSelectedItem());
                event.consume();
            }
        });
    }

    /**
     * 当前用户
     */
    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
        this.refreshData();
    }

    /**
     * 过滤文本
     */
    private String filterText;

    public String getFilterText() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
        this.refreshData();
    }

    /**
     * 数据列表
     */
    private List<ShellProcessInfo> dataList;

    private ShellProcessExec exec;

    public ShellProcessExec getExec() {
        return exec;
    }

    public void setExec(ShellProcessExec exec) {
        this.exec = exec;
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        ShellProcessInfo info = this.getSelectedItem();
        if (info == null) {
            return Collections.emptyList();
        }
        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem killProcess = MenuItemHelper.killProcess("12", () -> this.killProcess(info));
        killProcess.setAccelerator(KeyboardUtil.stop_keyCombination);
        menuItems.add(killProcess);
        MenuItem forceKillProcess = MenuItemHelper.forceKillProcess("12", () -> this.forceKillProcess(info));
        menuItems.add(forceKillProcess);
        return menuItems;
    }

    /**
     * 刷新数据
     */
    public void refreshData() {
        // 更新数据
        this.setItem(this.doFilter(this.dataList));
        // 更新排序
        this.sort();
        // 更新表格
        this.refresh();
    }

    /**
     * 杀死进程
     *
     * @param info 进程信息
     */
    protected void killProcess(ShellProcessInfo info) {
        if (info == null) {
            return;
        }
        if (MessageBox.confirm("[" + info.getPid() + "] " + I18nHelper.killProcess() + "?")) {
            String output = this.exec.kill(info.getPid());
            if (StringUtil.isNotBlank(output)) {
                MessageBox.warn(output);
            } else {
                this.dataList.remove(info);
                this.refreshData();
            }
        }
    }

    /**
     * 强制杀死进程
     *
     * @param info 进程信息
     */
    protected void forceKillProcess(ShellProcessInfo info) {
        if (info == null) {
            return;
        }
        if (MessageBox.confirm("[" + info.getPid() + "] " + I18nHelper.forceKillProcess() + "?")) {
            String output = this.exec.forceKill(info.getPid());
            if (StringUtil.isNotBlank(output)) {
                MessageBox.warn(output);
            } else {
                this.dataList.remove(info);
                this.refreshData();
            }
        }
    }

    /**
     * 进行过滤
     *
     * @param infos 数据
     * @return 过滤后的树
     */
    protected List<ShellProcessInfo> doFilter(List<ShellProcessInfo> infos) {
        infos = infos.parallelStream()
                .filter(f -> {
                    // 用户
                    if (StringUtil.isNotBlank(this.user) && !StringUtil.equalsIgnoreCase(f.getUser(), this.user)) {
                        return false;
                    }
                    // 关键字
                    if (StringUtil.isNotBlank(this.filterText) && !(
                            StringUtil.containsIgnoreCase(f.getCommand(), this.filterText)
                                    || StringUtil.contains(f.getPid() + "", this.filterText)
                                    || StringUtil.contains(f.getStat(), this.filterText)
                    )) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return infos;
    }

    /**
     * 更新数据
     *
     * @param infos 数据列表
     */
    public void updateData(List<ShellProcessInfo> infos) {
        if (this.isItemEmpty()) {
            this.dataList = new CopyOnWriteArrayList<>(infos);
        } else {
            // 删除列表
            List<ShellProcessInfo> delList = new ArrayList<>();
            List<ShellProcessInfo> addList = new ArrayList<>();
            // 寻找新增进程
            for (ShellProcessInfo info : infos) {
                Optional<ShellProcessInfo> p = this.dataList.parallelStream()
                        .filter(f -> info.getPid() == f.getPid())
                        .findAny();
                if (p.isEmpty()) {
                    addList.add(info);
                }
            }
            // 寻找删除进程，更新已有进程
            for (ShellProcessInfo info : this.dataList) {
                Optional<ShellProcessInfo> p = infos.parallelStream()
                        .filter(f -> info.getPid() == f.getPid())
                        .findAny();
                if (p.isPresent()) {
                    info.copy(p.get());
                } else {
                    delList.add(info);
                }
            }
            // 删除
            this.dataList.removeAll(delList);
            // 新增
            this.dataList.addAll(addList);
        }
        // 更新数据
        this.setItem(this.doFilter(this.dataList));
        // 更新排序
        this.sort();
        // 更新表格
        this.refresh();
    }
}
