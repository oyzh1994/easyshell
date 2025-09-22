package cn.oyzh.easyshell.test;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackCost;
import cn.oyzh.fx.pkg.Packer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oyzh
 * @since 2023/3/8
 */
public class Pack {

    private boolean inGithub = false;

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

    private String getGithubPath() {
        return this.getProjectPath() + "/dist/";
    }

    private String getTargetDestPath() {
        return this.getProjectPath() + "/target/dest";
    }

    @Test
    public void win_exe() throws Exception {
        String packagePath = this.getPackagePath();
        String win_pack_config = packagePath + "/win_exe.yaml";
        this.pack(win_pack_config);
    }

    @Test
    public void win_msi() throws Exception {
        String packagePath = this.getPackagePath();
        String win_pack_config = packagePath + "/win_msi.yaml";
        this.pack(win_pack_config);
    }

    @Test
    public void win_image() throws Exception {
        String packagePath = this.getPackagePath();
        String win_pack_config = packagePath + "/win_image.yaml";
        this.pack(win_pack_config);
    }

    @Test
    public void linux_deb() throws Exception {
        String packagePath = this.getPackagePath();
        String linux_pack_config = packagePath + "/linux_deb.yaml";
        this.pack(linux_pack_config);
    }

    @Test
    public void linux_rpm() throws Exception {
        String packagePath = this.getPackagePath();
        String linux_pack_config = packagePath + "/linux_rpm.yaml";
        this.pack(linux_pack_config);
    }

    @Test
    public void linux_image() throws Exception {
        String packagePath = this.getPackagePath();
        String linux_pack_config = packagePath + "/linux_image.yaml";
        this.pack(linux_pack_config);
    }

    @Test
    public void macos_dmg() throws Exception {
        String packagePath = this.getPackagePath();
        String macos_arm64_pack_config = packagePath + "/macos_dmg.yaml";
        this.pack(macos_arm64_pack_config);
    }

    @Test
    public void macos_pkg() throws Exception {
        String packagePath = this.getPackagePath();
        String macos_arm64_pack_config = packagePath + "/macos_pkg.yaml";
        this.pack(macos_arm64_pack_config);
    }

    @Test
    public void macos_image() throws Exception {
        String packagePath = this.getPackagePath();
        String macos_arm64_pack_config = packagePath + "/macos_image.yaml";
        this.pack(macos_arm64_pack_config);
    }

    private void pack(String pack_config) throws Exception {
        Map<String, Object> properties = new HashMap<>();
        String projectPath = this.getProjectPath();
        properties.put(PackCost.PROJECT_PATH, projectPath);
        Packer packer = new Packer();
        if (this.inGithub) {
            // github处理
            String githubPath = this.getGithubPath();
            properties.put(PackCost.GITHUB_DIST, githubPath);
            packer.registerGitHubHandler();
            // 覆盖dest设置
            String targetDestPath = this.getTargetDestPath();
            properties.put(PackCost.DEST, targetDestPath);
        }
        packer.registerProjectHandler();
        packer.registerJdepsHandler();
        packer.pack(pack_config, properties);
    }

    public static void main(String[] args) throws Exception {
        String packType = null;
        if (args.length > 0) {
            packType = args[0];
        }
        Pack pack = new Pack();
        pack.inGithub = true;
        if (StringUtil.equalsIgnoreCase(packType, "macos_pkg")) {
            pack.macos_pkg();
        } else if (StringUtil.equalsIgnoreCase(packType, "macos_dmg")) {
            pack.macos_dmg();
        } else if (StringUtil.equalsIgnoreCase(packType, "macos_image")) {
            pack.macos_image();
        } else if (StringUtil.equalsIgnoreCase(packType, "linux_deb")) {
            pack.linux_deb();
        } else if (StringUtil.equalsIgnoreCase(packType, "linux_rpm")) {
            pack.linux_rpm();
        } else if (StringUtil.equalsIgnoreCase(packType, "linux_image")) {
            pack.linux_image();
        } else if (StringUtil.equalsIgnoreCase(packType, "windows_exe")) {
            pack.win_exe();
        } else if (StringUtil.equalsIgnoreCase(packType, "windows_msi")) {
            pack.win_msi();
        } else if (StringUtil.equalsIgnoreCase(packType, "windows_image")) {
            pack.win_image();
        }
    }
}
