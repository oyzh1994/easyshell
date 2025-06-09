package cn.oyzh.easyshell.ssh.exec;

import cn.oyzh.common.util.NumberUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-18
 */
public class ShellExecParser {

    public static List<ShellDiskInfo> diskForLinux(String output) {
        try {
            String[] lines = output.split("\n");
            List<ShellDiskInfo> list = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                String[] cols = line.split("\\s+");
                ShellDiskInfo info = new ShellDiskInfo();
                info.setFileSystem(cols[0]);
                info.setSize(cols[1]);
                info.setUsed(cols[2]);
                info.setAvail(cols[3]);
                info.setUse(cols[4]);
                info.setMountedOn(cols[5]);
                list.add(info);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static List<ShellDiskInfo> diskForMacos(String output) {
        try {
            String[] lines = output.split("\n");
            List<ShellDiskInfo> list = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                String[] cols = line.split("\\s+");
                ShellDiskInfo info = new ShellDiskInfo();
                info.setFileSystem(cols[0]);
                info.setSize(cols[1]);
                info.setUsed(cols[2]);
                info.setAvail(cols[3]);
                info.setUse(cols[7]);
                info.setMountedOn(cols[8]);
                list.add(info);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static List<ShellDiskInfo> diskForWindows(String output) {
        try {
            String[] lines = output.split("\n");
            List<ShellDiskInfo> list = new ArrayList<>();
            for (int i = 1; i < lines.length - 1; i++) {
                String line = lines[i];
                String[] cols = line.split("\\s+", -1);
                ShellDiskInfo info = new ShellDiskInfo();
                long free = Long.parseLong(cols[0]);
                long size = Long.parseLong(cols[2]);
                long used = size - free;
                info.setFileSystem(cols[3]);
                info.setSize(NumberUtil.formatSize(size, 2));
                info.setUsed(NumberUtil.formatSize(used, 2));
                info.setAvail(NumberUtil.formatSize(free, 2));
                info.setMountedOn(cols[1]);
                info.setUse(NumberUtil.scale(100 * (used / 1D / size), 2) + "%");
                list.add(info);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }
}
