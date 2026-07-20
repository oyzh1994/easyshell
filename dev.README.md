## 启动入口

`cn.oyzh.easyshell.EasyShellBootstrap.main`

> **注意**：如果要运行项目，最好切换到最新分支，不然可能启动不了。主分支 `master` 代码是定期合并进去，可能不是最新。  
> **IDE**：建议使用 IntelliJ IDEA 社区版或专业版。

---

## 依赖说明

1. **base** 工程  
   https://gitee.com/oyzh1994/base
2. **fx-base** 工程  
   https://gitee.com/oyzh1994/fx-base
3. **JDK 版本**：要求 25，推荐 25。JDK 25 对象头压缩可大幅减少内存占用。
   - Linux ARM 平台建议使用 AWS Corretto JDK，其他 JDK 可能缺失 `hsdis` 类库。
   - 其他平台优先使用 OpenJDK。
   - AWS Corretto JDK 25：https://docs.aws.amazon.com/corretto/latest/corretto-25-ug/downloads-list.html
   - OpenJDK：https://jdk.java.net/archive/

---

## 结构说明

```
.github    -> GitHub Actions 配置文件
changes    -> 每个版本的更新清单
docker     -> Docker 配置文件
docs       -> 文档相关资源
package    -> 打包相关配置
resource   -> 项目相关资源文件
src        -> 项目相关代码
```

---

## Maven

### 打包
```bash
mvn -X clean package -DskipTests
```
推荐使用 GitHub Actions 打包。

### 注意
- 检查 cmd 里面 `java -version` 的版本号和项目版本号是否一致，否则可能出现无效的目标版本号 21 之类的问题。
- 国内建议使用腾讯云镜像加速地址，该地址能把 JetBrains 的库一起下载，否则可能要单独处理。

---

## 程序打包

### PNG 去背景
https://www.iloveimg.com/zh-cn/remove-background

### 图标转换

**PNG 转 ICNS（地址 1）**  
https://anyconv.com/png-to-icns-converter/

**PNG 转 ICNS（地址 2）**  
https://www.aconvert.com/cn/image/png-to-icns/

**PNG 转 ICO**  
https://www.freeconvert.com/zh/png-to-ico

---

### Windows

**EXE / MSI 打包依赖**  
https://github.com/wixtoolset/wix3/releases

**MSI 打包（推荐）**  
配置 -> `package/win_msi.yaml`  
入口 -> `cn.oyzh.easyshell.test.Pack.win_msi`

**EXE 打包**  
配置 -> `package/win_exe.yaml`  
入口 -> `cn.oyzh.easyshell.test.Pack.win_exe`

**App Image 打包**  
配置 -> `package/win_image.yaml`  
入口 -> `cn.oyzh.easyshell.test.Pack.win_image`

> **注意**：EXE、MSI 打包需要设置 `win-menu`、`win-shortcut` 参数，避免桌面不显示程序图标的问题。

---

### macOS

**PKG 打包（推荐）**  
配置 -> `package/macos_pkg.yaml`  
入口 -> `cn.oyzh.easyshell.test.Pack.macos_pkg`

**DMG 打包**  
配置 -> `package/macos_dmg.yaml`  
入口 -> `cn.oyzh.easyshell.test.Pack.macos_dmg`

**App Image 打包**  
配置 -> `package/macos_image.yaml`  
入口 -> `cn.oyzh.easyshell.test.Pack.macos_image`

> **注意**：DMG、PKG 打包需要设置 `mac-package-identifier` 参数，避免因为 App 同名导致启动台不显示程序图标的问题。

---

### Linux（以 UOS、Ubuntu、CentOS 为例）

**DEB 打包依赖（Ubuntu、Deepin）**
```bash
sudo apt install fakeroot binutils
```

**RPM 打包依赖（CentOS）**
```bash
sudo yum install rpm-build
# 或者
sudo yum install rpmrebuild
```

**AppImage 打包依赖**

x86_64：
```bash
wget -O appimagetool https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage
chmod +x appimagetool
sudo mv appimagetool /usr/local/bin/appimagetool
```

ARM64：
```bash
wget -O appimagetool https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-aarch64.AppImage
chmod +x appimagetool
sudo mv appimagetool /usr/local/bin/appimagetool
```

**AppImage 打包（推荐）**  
配置 -> `package/linux_AppImage.yaml`  
入口 -> `cn.oyzh.easyshell.test.Pack.linux_AppImage`

**DEB 打包**  
配置 -> `package/linux_deb.yaml`  
入口 -> `cn.oyzh.easyshell.test.Pack.linux_deb`

**RPM 打包**  
配置 -> `package/linux_rpm.yaml`  
入口 -> `cn.oyzh.easyshell.test.Pack.linux_rpm`

**App Image 打包**  
配置 -> `package/linux_image.yaml`  
入口 -> `cn.oyzh.easyshell.test.Pack.linux_image`

---

## X11 / X-Server

### Windows
https://sourceforge.net/projects/vcxsrv/

### macOS
https://www.xquartz.org/

---

## Linux 系统

### 执行 DEB 安装提示安装失败

如果错误详情中发现：
```
xdg-desktop-menu: No writable system menu directory found.
```
执行以下操作，然后重新安装：
```bash
sudo mkdir /usr/share/desktop-directories/
```

### 双击安装 RPM 包提示出现问题或无响应
```bash
rpm -ivh xx.rpm
```

### RPM 升级
```bash
rpm -U xx.rpm
```

### RPM 卸载
```bash
rpm -e easyshell
```

### DEB 升级
```bash
dpkg -r easyshell
dpkg -i easyshell-xx.deb
```

### DEB 卸载
```bash
dpkg -r easyshell
```

---

## macOS 系统

### Mac 执行 DMG 安装后启动台不显示程序图标
```bash
defaults write com.apple.dock ResetLaunchPad -bool true && killall Dock
```

### Mac 无法启动 — 解决方案 1
```bash
sudo chmod +x EasyShell.app
```

### Mac 无法启动 — 解决方案 2
```bash
chmod -R 755 /路径/EasyShell.app
```

### Mac 无法启动 — 解决方案 3
当 macOS 上运行 `.app` 文件时提示"应用程序 EasyShell.app 无法打开"：
1. 不使用内建的归档实用程序，而是使用第三方解压工具如 FastZip。
2. 在终端赋予权限：
   ```bash
   chmod +x EasyShell.app/Contents/MacOS/*
   ```

### Mac 无法启动 — 解决方案 4（"已损坏" 提示）
当 macOS 上运行 `.app` 文件时提示"已损坏，无法打开"：
1. 允许"任何来源"下载的 App 运行：
   - 打开 **系统偏好设置** → **安全性与隐私** → **通用**。
   - 勾选 **任何来源**。
   - 如果"任何来源"不可见，打开终端执行：
     ```bash
     sudo spctl --master-disable
     ```
   - 输入密码后回车。
