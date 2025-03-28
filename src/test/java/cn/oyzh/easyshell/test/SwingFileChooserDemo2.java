package cn.oyzh.easyshell.test;

import cn.oyzh.fx.plus.chooser.SwingFileChooser;

import javax.swing.*;
import java.io.File;
import java.util.List;

public class SwingFileChooserDemo2 {

    public static void main(String[] args) {
        // 在事件分发线程中运行 GUI 代码
        SwingUtilities.invokeLater(() -> {
            new SwingFileChooser().showFileChooser(f->{
                if (f != null) {
                    System.out.println("已选择文件:");
                  System.out.println(f[0].getAbsolutePath());
                } else {
                    System.out.println("用户取消选择");
                }
            });

        });
    }

}
