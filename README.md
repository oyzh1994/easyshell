# 项目
###### 项目说明
这是一个使用javafx编写的跨平台仿真客户端

###### 下载地址
https://gitee.com/oyzh1994/easyshell/releases

###### 启动入口
cn.oyzh.easyshell.EasyShellBootstrap.main  
注意，如果要运行项目，最好切换到最新分支，不然可能启动不了，主分支master代码是定期合并进去  
ide建议idea社区版或者专业版

###### 依赖说明
1. base工程，可选手动安装，也可使用中心仓库稳定版本  
 https://gitee.com/oyzh1994/base  
2. fx-base工程，可选手动安装，也可使用中心仓库稳定版本    
 https://gitee.com/oyzh1994/fx-base  
3. jdk版本要求21，推荐24  
注意，如果是linux的arm平台，建议使用aws的jdk，其他jdk可能缺失hsdis类库，其他情况下优先使用openjdk  
awsjdk21 https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html  
awsjdk24 https://docs.aws.amazon.com/corretto/latest/corretto-24-ug/downloads-list.html  
openjdk https://jdk.java.net/archive/

###### 结构说明 
docker -> docker配置文件  
docs -> 文档相关资源  
package -> 打包相关配置  
libs -> 相关依赖库，本地依赖  
resource -> 项目相关资源文件  
src -> 项目相关代码

# Maven
###### 打包
mvn -X clean package -DskipTests

###### 注意
检查cmd里面java -version的版本号和项目版本号是否一致，否则可能出现无效的目标版本号24之类的问题  
另外建议国内使用阿里镜像加速地址  
对于jediterm-ui、jediterm-core类库，如果发现下载失败，可以使用libs的最新jediterm相关版本，然后手动覆盖到m2本地仓库  

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
###### (推荐)exe打包 
配置 -> package -> win_exe.yaml  
入口 -> cn.oyzh.easyshell.test.Pack.win_exe  
###### msi打包 
配置 -> package -> win_msi.yaml  
入口 -> cn.oyzh.easyshell.test.Pack.win_msi  
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
###### deb打包依赖(deepin)
sudo apt install fakeroot
###### deb打包依赖(ubuntu)
sudo apt install fakeroot binutils
###### rpm打包依赖(centos)
sudo yum install rpm-build
###### (推荐)deb打包
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
###### 双击安装rpm包提示出现问题
rpm -ivh xx.rpm
###### rpm升级
rpm -U xx.rpm
###### rpm卸载
rpm -e easyshell
###### deb升级
dpkg -r easyshell
dpkg -i xx.deb
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

# 程序相关截图
###### 截图1
![1.png](resource/md/1.png)
###### 截图2
![2.png](resource/md/2.png)
###### 截图3
![3.png](resource/md/3.png)
###### 截图4
![4.png](resource/md/4.png)
###### 截图5
![5.png](resource/md/5.png)
###### 截图6
![6.png](resource/md/6.png)
###### 截图7
![7.png](resource/md/7.png)
###### 截图8
![8.png](resource/md/8.png)
###### 截图9
![9.png](resource/md/9.png)
###### 截图10
![10.png](resource/md/10.png)
###### 截图11
![11.png](resource/md/11.png)
###### 截图12
![12.png](resource/md/12.png)
###### 截图13
![13.png](resource/md/13.png)
###### 截图14
![14.png](resource/md/14.png)
###### 截图15
![15.png](resource/md/15.png)
###### 截图16
![16.png](resource/md/16.png)
###### 截图17
![17.png](resource/md/17.png)
###### 截图18
![18.png](resource/md/18.png)
###### 截图19
![19.png](resource/md/19.png)
###### 截图20
![20.png](resource/md/20.png)
###### 截图21
![21.png](resource/md/21.png)
###### 截图22
![22.png](resource/md/22.png)
###### 截图23
![23.png](resource/md/23.png)
###### 截图24
![24.png](resource/md/24.png)
###### 截图25
![25.png](resource/md/25.png)
###### 截图26
![26.png](resource/md/26.png)
###### 截图27
![27.png](resource/md/27.png)
###### 截图28
![28.png](resource/md/28.png)
###### 截图29
![29.png](resource/md/29.png)
###### 截图30
![30.png](resource/md/30.png)
###### 截图31
![31.png](resource/md/31.png)
###### 截图32
![32.png](resource/md/32.png)
###### 截图33
![33.png](resource/md/33.png)
###### 截图34
![34.png](resource/md/34.png)
###### 截图35
![35.png](resource/md/35.png)
###### 截图36
![36.png](resource/md/36.png)
###### 截图37
![37.png](resource/md/37.png)
###### 截图38
![38.png](resource/md/38.png)
###### 截图39
![39.png](resource/md/39.png)
###### 截图40
![40.png](resource/md/40.png)
###### 截图41
![41.png](resource/md/41.png)
###### 截图42
![42.png](resource/md/42.png)
###### 截图43
![43.png](resource/md/43.png)
###### 截图44
![44.png](resource/md/44.png)
###### 截图45
![45.png](resource/md/45.png)
###### 截图46
![46.png](resource/md/46.png)
###### 截图47
![47.png](resource/md/47.png)
