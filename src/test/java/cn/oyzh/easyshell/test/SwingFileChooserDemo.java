package cn.oyzh.easyshell.test;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class SwingFileChooserDemo {

    public static void main(String[] args) {
        // 在事件分发线程中运行 GUI 代码
        SwingUtilities.invokeLater(() -> {
            List<File> selectedFiles = showFileChooser();
            if (selectedFiles != null) {
                System.out.println("已选择文件:");
                selectedFiles.forEach(file -> System.out.println(file.getAbsolutePath()));
            } else {
                System.out.println("用户取消选择");
            }
        });
    }

    /**
     * 显示文件选择对话框并返回用户选择的文件列表
     */
    public static List<File> showFileChooser() {
        JFileChooser fileChooser = new JFileChooser();

        // 1. 启用多选模式
        fileChooser.setMultiSelectionEnabled(true);

        // 2. 可选：设置文件过滤器（例如仅显示图片文件）
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName().toLowerCase();
                return file.isDirectory() || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "图片文件 (*.jpg, *.png)";
            }
        });

        // 3. 显示对话框
        int result = fileChooser.showOpenDialog(null);

        // 4. 处理结果
        if (result == JFileChooser.APPROVE_OPTION) {
            return List.of(fileChooser.getSelectedFiles());
        } else {
            return null; // 用户取消选择
        }
    }
}
