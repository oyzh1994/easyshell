//package cn.oyzh.easyshell.terminal;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyshell.file.ShellFileUtil;
//import cn.oyzh.easyshell.ssh.ShellSSHClient;
//import cn.oyzh.fx.plus.chooser.FXChooser;
//import cn.oyzh.fx.plus.chooser.FileChooserHelper;
//import cn.oyzh.fx.plus.information.MessageBox;
//import cn.oyzh.fx.plus.window.StageManager;
//import cn.oyzh.ssh.util.SSHUtil;
//
//import java.io.File;
//import java.util.List;
//
//public class ShellZModemProcessor {
//
//    private String read;
//
//    private StringBuilder write;
//
//    public void clearWrite() {
//        if (this.write != null && StringUtil.endWithAny(this.write.toString(), "\r", "\n")) {
//            this.write.setLength(0);
//        }
//    }
//
//    public void appendWrite(String str) {
//        if (this.write == null) {
//            this.write = new StringBuilder();
//        }
//        this.write.append(str);
//        System.out.println("write:" + this.write.toString());
//    }
//
//    public void setRead(String read) {
//        this.read = read;
//        System.out.println("read:" + this.read.toString());
//    }
//
//    public boolean isRZ(String str) {
//        this.appendWrite(str);
//        boolean clear = true;
//        try {
//            String read = this.read;
//            if (StringUtil.contains(read, "rz")) {
//                read = SSHUtil.removeAnsi(read);
//                read = SSHUtil.removeControl(read);
//                read = read.split(" ")[0];
//                if (StringUtil.endWithAny(str, "\r", "\n") && StringUtil.equals(read, "rz")) {
//                    return true;
//                }
//            }
//            String write = this.write.toString();
//            if (StringUtil.contains(write, "rz")) {
//                write = SSHUtil.removeAnsi(write);
//                write = SSHUtil.removeControl(write);
//                write = write.split(" ")[0];
//                if (StringUtil.endWithAny(str, "\r", "\n") && StringUtil.equals(write, "rz")) {
//                    clear = false;
//                    return true;
//                }
//            }
//        } finally {
//            if (clear) {
//                this.clearWrite();
//            }
//        }
//        return false;
//    }
//
//    public void doSend(ShellSSHClient client, Runnable callback) {
//        String wordDir = client.workDirProperty().get();
//        List<File> files = FileChooserHelper.chooseMultiple("请选择文件", FXChooser.allExtensionFilter());
//        if (CollectionUtil.isEmpty(files)) {
//            callback.run();
//        }
//        StageManager.showMask(() -> {
//            try {
//                for (File file : files) {
//                    String fileName = file.getName();
//                    String remotePath = ShellFileUtil.concat(wordDir, fileName);
//                    client.sftpClient().put(file.getPath(), remotePath);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                MessageBox.exception(e);
//            }finally {
//                callback.run();
//            }
//        });
//    }
//}
