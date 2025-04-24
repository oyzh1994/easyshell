package cn.oyzh.easyshell.util;

import cn.oyzh.easyshell.controller.connect.ShellAddConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddGuidController;
import cn.oyzh.easyshell.controller.connect.ShellAddSerialConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateSerialConnectController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellViewFactory {

    public static void addGuid(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddGuidController.class);
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void addConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddConnectController.class);
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void addSerialConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddSerialConnectController.class);
            adapter.setProp("group", group);
            adapter.display();
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void updateConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateConnectController.class);
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void updateSerialConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateSerialConnectController.class);
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
