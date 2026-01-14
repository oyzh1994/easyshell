###### 启动入口
cn.oyzh.easyshell.EasyShellBootstrap.main  
注意，如果要运行项目，最好切换到最新分支，不然可能启动不了，主分支master代码是定期合并进去  
ide建议idea社区版或者专业版

###### 依赖说明
1. base工程  
   https://gitee.com/oyzh1994/base
2. fx-base工程  
   https://gitee.com/oyzh1994/fx-base
3. jdk版本要求21，推荐25，jdk25对象头压缩大幅减少内存占用  
   注意，如果是linux的arm平台，建议使用aws的jdk，其他jdk可能缺失hsdis类库，其他情况下优先使用openjdk  
   awsjdk25 https://docs.aws.amazon.com/corretto/latest/corretto-25-ug/downloads-list.html  
   openjdk https://jdk.java.net/archive/

###### 结构说明
.github -> github actions配置文件  
changes -> 每个版本的更新清单  
docker -> docker配置文件  
docs -> 文档相关资源  
package -> 打包相关配置  
resource -> 项目相关资源文件  
src -> 项目相关代码

# Maven
###### 打包
mvn -X clean package -DskipTests
推荐使用github actions打包

###### 注意
检查cmd里面java -version的版本号和项目版本号是否一致，否则可能出现无效的目标版本号21之类的问题  
另外建议国内使用腾讯镜像加速地址，这个地址能把jetbrains的库一起下载，否则可能要单独处理  

# 程序打包
###### png去背景
https://www.iloveimg.com/zh-cn/remove-background
###### 图标转换
###### png转icns(地址1)
https://anyconv.com/png-to-icns-converter/
###### png转icns(地址2)
https://www.aconvert.com/cn/image/png-to-icns/
###### png转ico
https://www.freeconvert.com/zh/png-to-ico

###### windows
###### exe、msi打包依赖
https://github.com/wixtoolset/wix3/releases
###### (推荐)msi打包
配置 -> package -> win_msi.yaml  
入口 -> cn.oyzh.easyshell.test.Pack.win_msi
###### exe打包
配置 -> package -> win_exe.yaml  
入口 -> cn.oyzh.easyshell.test.Pack.win_exe
###### app-image打包
配置 -> package -> win_image.yaml  
入口 -> cn.oyzh.easyshell.test.Pack.win_image
###### 注意事项
exe、msi打包需要设置win-menu、win-shortcut参数，避免桌面不显示程序图标的问题

###### macos
###### (推荐)pkg打包
配置 -> package -> macos_pkg.yaml  
入口 -> cn.oyzh.easyshell.test.Pack.macos_pkg
###### dmg打包
配置 -> package -> macos_dmg.yaml  
入口 -> cn.oyzh.easyshell.test.Pack.macos_dmg
###### app-image打包
配置 -> package -> macos_image.yaml  
入口 -> cn.oyzh.easyshell.test.Pack.macos_image
###### 注意事项
dmg、pkg打包需要设置mac-package-identifier参数，避免因为app同名，启动台不显示程序图标的问题

###### linux(以uos、ubuntu、centos为例)
###### deb打包依赖(ubuntu、deepin)
sudo apt install fakeroot binutils
###### rpm打包依赖(centos)
sudo yum install rpm-build
或者
sudo yum install rpmrebuild
###### AppImage打包依赖
x64
wget -O appimagetool https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage
arm64
wget -O appimagetool https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-aarch64.AppImage
chmod +x appimagetool
mv appimagetool /usr/local/bin/appimagetool
###### (推荐)AppImage打包
配置 -> package -> linux_AppImage.yaml  
入口 -> cn.oyzh.easyshell.test.Pack.linux_AppImage
###### deb打包
配置 -> package -> linux_deb.yaml  
入口 -> cn.oyzh.easyshell.test.Pack.linux_deb
###### rpm打包
配置 -> package -> linux_rpm.yaml  
入口 -> cn.oyzh.easyshell.test.Pack.linux_rpm
###### app-image打包
配置 -> package -> linux_image.yaml  
入口 -> cn.oyzh.easyshell.test.Pack.linux_image

# X11、X-Server
###### windows
https://sourceforge.net/projects/vcxsrv/
###### macos
https://www.xquartz.org/

# Linux系统
###### 执行deb安装提示安装失败
如果错误详情发现这个错误
xdg-desktop-menu: No writable system menu directory found.  
执行以下操作，然后重新执行安装  
sudo mkdir /usr/share/desktop-directories/
###### 双击安装rpm包提示出现问题或者无响应
rpm -ivh xx.rpm
###### rpm升级
rpm -U xx.rpm
###### rpm卸载
rpm -e easyshell
###### deb升级
dpkg -r easyshell
dpkg -i easyshell-xx.deb
###### deb卸载
dpkg -r easyshell

# Macos系统
###### mac执行dmg安装后，启动台不显示程序图标解决方案
defaults write com.apple.dock ResetLaunchPad -bool true && killall Dock
###### mac无法启动解决方案1
sudo chmod +x EasyShell.app
###### mac无法启动解决方案2
chmod -R 755 /路径/EasyShell.app(可拖入命令行窗口)
###### mac无法启动解决方案3
当在macOS上运行.app文件时提示“已损坏，无法打开”，你可以尝试以下几种解决方法：
1. 允许“任何来源”下载的App运行‌
   打开“系统偏好设置”->“安全性与隐私”->“通用”选项卡。
   检查是否已经启用了“任何来源”选项。如果没有启用，先点击左下角的小黄锁图标解锁，然后选中“任何来源”‌1。
   如果“任何来源”选项不可用，可以打开终端，输入命令sudo spctl --master-disable，然后按提示输入电脑的登录密码并回车，即可启用“任何来源”选项‌12。
