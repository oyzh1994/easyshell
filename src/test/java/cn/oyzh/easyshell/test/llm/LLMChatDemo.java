package cn.oyzh.easyshell.test.llm;

import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.easyshell.llm.LLMSessionTab;
import cn.oyzh.easyshell.fx.llm.LLMView;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class LLMChatDemo extends Application {

    public static void main(String[] args) {
        launch(LLMChatDemo.class, args);
    }

    @Override
    public void start(Stage stage) {
        ThemeManager.apply(Themes.PRIMER_LIGHT);

        LLMView view = new LLMView();
        double width = Double.parseDouble(System.getProperty("llm.width", "920"));
        double height = Double.parseDouble(System.getProperty("llm.height", "780"));
        double minWidth = Double.parseDouble(System.getProperty("llm.minWidth", "720"));
        Scene scene = new Scene(view, width, height);
        scene.getStylesheets().add("/fx-plus/css/fx-base.css");

        stage.setScene(scene);
        stage.setTitle("LLM Chat Preview");
        stage.setMinWidth(minWidth);
        stage.setMinHeight(560);

        this.fillDemo(view);
        view.setSendHandler(message -> this.simulateReply(view));
        stage.show();
        this.maybeSnapshot(scene);
    }

    private void fillDemo(LLMView view) {
        view.getSessionTabComboBox().setTabs(List.of(
                new LLMSessionTab("terminal-prod", "终端 · 生产服务器"),
                new LLMSessionTab("ssh-test", "SSH · 测试环境"),
                new LLMSessionTab("sftp-release", "SFTP · 发布目录")
        ));
        view.addAssistantMessage("你好，我是内嵌在 EasyShell 里的 AI 助手。我可以帮你分析终端输出、排查 SSH 与 Docker 问题，或者生成可直接执行的脚本。");
        view.addUserMessage("服务器上的 Docker 没有启动，帮我看看可能是什么原因。");
        view.addAssistantMessage("先确认服务状态和最近日志：\n\nsystemctl status docker --no-pager\njournalctl -u docker -n 80 --no-pager\n\n把输出贴给我，我继续判断是配置、磁盘还是权限问题。");
        view.addUserMessage("这是刚才的日志，顺便帮我生成一个重启脚本。", new File("docker-diagnose.log"));
        LLMMessage placeholder = view.addAssistantMessage("思考中...");
        view.replaceMessage(placeholder, "收到。我会先检查 docker.socket，再尝试启动服务；如果启动失败，会输出最近 50 行日志，方便继续定位。");
        view.addErrorMessage("连接模型服务失败：请求超时，请检查网络或 Base URL 配置。");
        view.setContextUsage(12_600, 64_000);
        view.setSelectedFile(new File("docker-diagnose.log"));
    }

    private void simulateReply(LLMView view) {
        view.setBusy(true);
        PauseTransition thinking = new PauseTransition(Duration.millis(420));
        thinking.setOnFinished(event -> {
            LLMMessage answer = view.addAssistantMessage("");
            String content = "可以。我会先检查 Docker 服务与 socket 状态：\n\n"
                    + "systemctl is-active docker\n"
                    + "systemctl status docker.socket --no-pager\n\n"
                    + "如果 socket 正常但服务未启动，再执行：\n\n"
                    + "systemctl start docker\n"
                    + "journalctl -u docker -n 50 --no-pager\n\n"
                    + "需要我把它整理成一个幂等重启脚本吗？";

            Timeline timeline = new Timeline();
            int step = 3;
            for (int i = 0; i < content.length(); i += step) {
                int end = Math.min(content.length(), i + step);
                String chunk = content.substring(i, end);
                int frameIndex = i / step + 1;
                timeline.getKeyFrames().add(new KeyFrame(Duration.millis(18L * frameIndex), e -> view.appendMessage(answer, chunk)));
            }
            timeline.setOnFinished(e -> {
                view.setBusy(false);
                view.setContextUsage(19_200, 64_000);
            });
            timeline.play();
        });
        thinking.play();
    }

    private void maybeSnapshot(Scene scene) {
        String path = System.getProperty("llm.snapshot");
        if (path == null || path.isBlank()) {
            return;
        }
        PauseTransition delay = new PauseTransition(Duration.millis(1500));
        delay.setOnFinished(event -> {
            try {
                this.saveSnapshot(scene, new File(path));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Platform.exit();
        });
        delay.play();
    }

    private void saveSnapshot(Scene scene, File file) throws Exception {
        WritableImage image = scene.snapshot(null);
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        buffered.setRGB(0, 0, width, height, pixels, 0, width);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        ImageIO.write(buffered, "png", file);
    }

    public static class LLMChatDemoStarter {

        public static void main(String[] args) {
            LLMChatDemo.main(args);
        }
    }
}
