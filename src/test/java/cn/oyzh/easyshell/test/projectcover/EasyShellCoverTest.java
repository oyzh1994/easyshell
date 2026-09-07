package cn.oyzh.easyshell.test.projectcover;

import cn.oyzh.i18n.I18nCoverChecker;
import org.junit.Test;

public class EasyShellCoverTest {

    @Test
    public void test() throws Exception {
        I18nCoverChecker i18nChecker1 = new I18nCoverChecker();
        i18nChecker1.setMainI18n("zh_CN");
        i18nChecker1.setPrefx("base_i18n_");
        i18nChecker1.setProjectPath(i18nChecker1.getClass().getResource("").toExternalForm());
        i18nChecker1.i18Check();

        I18nCoverChecker i18nChecker2 = new I18nCoverChecker();
        i18nChecker2.setMainI18n("zh_CN");
        i18nChecker2.setPrefx("i18n_");
        i18nChecker2.setProjectPath(this.getClass().getResource("").toExternalForm());
        i18nChecker2.i18Check();

        EasyShellCoverStarter.main(null);
    }

}
