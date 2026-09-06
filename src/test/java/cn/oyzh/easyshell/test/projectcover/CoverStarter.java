package cn.oyzh.easyshell.test.projectcover;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.ClassUtil;
import cn.oyzh.fx.gui.tabs.RichTab;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CoverStarter extends Application {

    public static void main(String[] args) throws URISyntaxException {
        launch(CoverStarter.class, args);
    }

    private List<Class<?>> getClasses() throws ClassNotFoundException {
        String pkName = "";
        String r = getClass().getResource("").toExternalForm();
        String f = r.split("test-classes")[0];
        if (f.startsWith("file:/")) {
            if (OSUtil.isWindows()) {
                f = f.substring(6);

            } else {
                f = f.substring(5);
            }
        }
        f += "classes";
        List<Class<?>> list = new ArrayList<>();
        ClassUtil.findClassesInDirectory(new File(f), pkName, list, new Predicate<Class<?>>() {
            @Override
            public boolean test(Class<?> aClass) {
                if (Modifier.isAbstract(aClass.getModifiers())) {
                    return false;
                }
                return true;
            }
        });


        return list;
    }

    @Test
    public void tab_check() throws ClassNotFoundException {
        System.out.println("tab check start");
        for (Class<?> aClass : this.getClasses()) {
            if (RichTab.class.isAssignableFrom(aClass)) {
                RichTab tab = (RichTab) ClassUtil.newInstance(aClass);
                if (tab == null) {
                    throw new RuntimeException("init tab:" + aClass + " fail");
                }
                System.out.println(aClass + "=" + tab);
            }
        }
        System.out.println("tab check finish");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.tab_check();
    }
}
