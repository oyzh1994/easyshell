package cn.oyzh.easyssh;

import cn.oyzh.common.util.SystemUtil;
import cn.oyzh.easyfx.spring.FXApplication;
import cn.oyzh.easyfx.view.FXViewUtil;
import cn.oyzh.easyssh.controller.MainController;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;


/**
 * 程序主入口
 *
 * @author oyzh
 * @since 2023/08/16
 */
@SpringBootApplication(scanBasePackages = "cn.oyzh",
        exclude = {
                AopAutoConfiguration.class,
                CacheAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                MessageSourceAutoConfiguration.class,
                TaskExecutionAutoConfiguration.class,
                TaskSchedulingAutoConfiguration.class,
                SqlInitializationAutoConfiguration.class,
        }
)
@Slf4j
public class EasySSHApp extends FXApplication implements CommandLineRunner, DisposableBean {

    public static void main(String[] args) {
        launchSpring(EasySSHApp.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // 开启定期gc
            SystemUtil.gcInterval(60_000);
            // 开始执行业务
            super.start(primaryStage);
            // 显示主页面
            FXViewUtil.showView(MainController.class);
            // 设置stage全部关闭后不自动销毁进程
            Platform.setImplicitExit(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        log.info("EasySSHApp destroyed.");
    }

    @Override
    public void run(String... args) {
        log.info("EasySSHApp started.");
    }
}
