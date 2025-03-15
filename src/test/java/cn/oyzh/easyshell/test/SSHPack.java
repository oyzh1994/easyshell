package cn.oyzh.easyshell.test;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.pkg.Packer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oyzh
 * @since 2023/3/8
 */
public class SSHPack {

    private String getProjectPath() {
        String projectPath = getClass().getResource("").getPath();
        if (OSUtil.isWindows()) {
            projectPath = projectPath.substring(1, projectPath.indexOf("/target/"));
        } else {
            projectPath = projectPath.substring(0, projectPath.indexOf("/target/"));
        }
        return projectPath;
    }

    private String getPackagePath() {
        return this.getProjectPath() + "/package/";
    }

    @Test
    public void easyshell_win_exe() throws Exception {
        String packagePath = this.getPackagePath();
        String win_pack_config = packagePath + "/win/win_exe.json";
        String getProjectPath = this.getProjectPath();
        Map<String, Object> properties = new HashMap<>();
        properties.put("projectPath", getProjectPath);

        Packer packer = new Packer();
        packer.registerProjectHandler();
        packer.registerJdepsHandler();
        packer.pack(win_pack_config, properties);
    }

    @Test
    public void easyshell_win_msi() throws Exception {
        String packagePath = this.getPackagePath();
        String win_pack_config = packagePath + "/win/win_msi.json";
        String getProjectPath = this.getProjectPath();
        Map<String, Object> properties = new HashMap<>();
        properties.put("projectPath", getProjectPath);

        Packer packer = new Packer();
        packer.registerProjectHandler();
        packer.registerJdepsHandler();
        packer.pack(win_pack_config, properties);
    }

    @Test
    public void easyshell_win_image() throws Exception {
        String packagePath = this.getPackagePath();
        String win_pack_config = packagePath + "/win/win_image.json";
        String getProjectPath = this.getProjectPath();
        Map<String, Object> properties = new HashMap<>();
        properties.put("projectPath", getProjectPath);

        Packer packer = new Packer();
        packer.registerProjectHandler();
        packer.registerJdepsHandler();
        packer.pack(win_pack_config, properties);
    }

    @Test
    public void easyshell_linux_deb() throws Exception {
        String packagePath = this.getPackagePath();
        String linux_pack_config = packagePath + "/linux/linux_deb.json";
        String getProjectPath = this.getProjectPath();
        Map<String, Object> properties = new HashMap<>();
        properties.put("projectPath", getProjectPath);

        Packer packer = new Packer();
        packer.registerProjectHandler();
        packer.pack(linux_pack_config, properties);
    }

    @Test
    public void easyshell_linux_image() throws Exception {
        String packagePath = this.getPackagePath();
        String linux_pack_config = packagePath + "/linux/linux_image.json";
        String getProjectPath = this.getProjectPath();
        Map<String, Object> properties = new HashMap<>();
        properties.put("projectPath", getProjectPath);

        Packer packer = new Packer();
        packer.registerProjectHandler();
        packer.pack(linux_pack_config, properties);
    }

    @Test
    public void easyshell_macos_dmg() throws Exception {
        String packagePath = this.getPackagePath();
        String macos_arm64_pack_config = packagePath + "/macos/macos_dmg.json";
        String getProjectPath = this.getProjectPath();
        Map<String, Object> properties = new HashMap<>();
        properties.put("projectPath", getProjectPath);

        Packer packer = new Packer();
        packer.registerProjectHandler();
        packer.registerJdepsHandler();
        packer.pack(macos_arm64_pack_config, properties);
    }

    @Test
    public void easyshell_macos_pkg() throws Exception {
        String packagePath = this.getPackagePath();
        String macos_arm64_pack_config = packagePath + "/macos/macos_pkg.json";
        String getProjectPath = this.getProjectPath();
        Map<String, Object> properties = new HashMap<>();
        properties.put("projectPath", getProjectPath);

        Packer packer = new Packer();
        packer.registerProjectHandler();
        packer.registerJdepsHandler();
        packer.pack(macos_arm64_pack_config, properties);
    }

    @Test
    public void easyshell_macos_image() throws Exception {
        String packagePath = this.getPackagePath();
        String macos_arm64_pack_config = packagePath + "/macos/macos_image.json";
        String getProjectPath = this.getProjectPath();
        Map<String, Object> properties = new HashMap<>();
        properties.put("projectPath", getProjectPath);

        Packer packer = new Packer();
        packer.registerProjectHandler();
        packer.registerJdepsHandler();
        packer.pack(macos_arm64_pack_config, properties);
    }

}
