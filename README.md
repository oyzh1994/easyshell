# 项目
###### 项目说明
这是一个使用javafx编写的ssh客户端，支持基本的连接管理，分组管理、ssh连接、sftp文件管理、x11转发(需要安装第三方x-server)、服务监控等功能，还支持暗色主题、系统主题跟随等能力

###### 下载地址
https://gitee.com/oyzh1994/easyssh/releases

###### 启动入口
cn.oyzh.easyssh.EasySSHBootstrap.main  
注意，如果要运行项目，最好切换到最新分支，不然可能启动不了，主分支master代码是定期合并进去  
ide建议idea社区版或者专业版，另外需要安装lombok插件(后续考虑去掉)

###### 依赖说明
1. base工程  
 https://gitee.com/oyzh1994/base  
2. fx-base工程  
 https://gitee.com/oyzh1994/fx-base  
3. jdk版本要求23  
注意，如果是linux的arm平台，建议使用aws的jdk，其他jdk可能缺失hsdis类库，其他情况下优先使用openjdk
awsjdk https://docs.aws.amazon.com/corretto/latest/corretto-23-ug/downloads-list.html
openjdk https://jdk.java.net/archive/

###### 结构说明 
docker -> docker配置文件  
package -> 打包相关配置  
resource -> 项目相关资源文件  
src -> 项目相关代码

# Maven
###### 打包
mvn -X clean package -DskipTests

###### 注意
检查cmd里面java -version的版本号和项目版本号是否一致，否则可能出现无效的目标版本号23之类的问题  
另外建议国内使用阿里镜像加速地址  

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
配置 -> package -> win -> win_exe.json  
入口 -> cn.oyzh.easyssh.test.SSHPack.easyssh_win_exe  
###### msi打包 
配置 -> package -> win -> win_msi.json  
入口 -> cn.oyzh.easyssh.test.SSHPack.easyssh_win_msi  
###### app-image打包
配置 -> package -> win -> win_image.json  
入口 -> cn.oyzh.easyssh.test.SSHPack.easyssh_win_image  
###### 注意事项
exe、msi打包需要设置win-menu、win-shortcut参数，避免桌面不显示程序图标的问题

###### macos
###### (推荐)pkg打包
配置 -> package -> macos -> macos_pkg.json  
入口 -> cn.oyzh.easyssh.test.SSHPack.easyssh_macos_pkg
###### dmg打包 
配置 -> package -> macos -> macos_dmg.json  
入口 -> cn.oyzh.easyssh.test.SSHPack.easyssh_macos_dmg  
###### app-image打包
配置 -> package -> macos -> macos_image.json  
入口 -> cn.oyzh.easyssh.test.SSHPack.easyssh_macos_image  
###### 注意事项
dmg、pkg打包需要设置mac-package-identifier参数，避免因为app同名，启动台不显示程序图标的问题

###### linux(以uos为例，debian系列通用)
###### deb打包依赖
sudo apt install fakeroot
###### (推荐)deb打包
配置 -> package -> linux -> linux_deb.json  
入口 -> cn.oyzh.easyssh.test.SSHPack.easyssh_linux_deb
###### app-image打包
配置 -> package -> linux -> linux_image.json  
入口 -> cn.oyzh.easyssh.test.SSHPack.easyssh_linux_image

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

# Macos系统
###### mac执行dmg安装后，启动台不显示程序图标解决方案
defaults write com.apple.dock ResetLaunchPad -bool true && killall Dock
###### mac无法启动解决方案1
sudo chmod +x EasyZK.app
###### mac无法启动解决方案2
chmod -R 755 /路径/EasyZK.app(可拖入命令行窗口)
###### mac无法启动解决方案3
当在macOS上运行.app文件时提示“已损坏，无法打开”，你可以尝试以下几种解决方法：
1. 允许“任何来源”下载的App运行‌
   打开“系统偏好设置”->“安全性与隐私”->“通用”选项卡。
   检查是否已经启用了“任何来源”选项。如果没有启用，先点击左下角的小黄锁图标解锁，然后选中“任何来源”‌1。
   如果“任何来源”选项不可用，可以打开终端，输入命令sudo spctl --master-disable，然后按提示输入电脑的登录密码并回车，即可启用“任何来源”选项‌12。

# 程序相关截图
###### 截图1
![img1.png](resource/md/img1.png)
###### 截图2
![img2.png](resource/md/img2.png)
###### 截图3
![img3.png](resource/md/img3.png)
###### 截图4
![img4.png](resource/md/img4.png)
###### 截图5
![img5.png](resource/md/img5.png)
###### 截图6
![img6.png](resource/md/img6.png)
###### 截图7
![img7.png](resource/md/img7.png)
###### 截图8
![img8.png](resource/md/img8.png)
###### 截图9
![img9.png](resource/md/img9.png)
###### 截图10
![img10.png](resource/md/img10.png)
###### 截图11
![img11.png](resource/md/img11.png)
###### 截图12
![img12.png](resource/md/img12.png)
###### 截图13
![img13.png](resource/md/img13.png)
###### 截图14
![img14.png](resource/md/img14.png)
###### 截图15
![img15.png](resource/md/img15.png)
###### 截图16
![img16.png](resource/md/img16.png)
###### 截图17
![img17.png](resource/md/img17.png)
###### 截图18
![img18.png](resource/md/img18.png)
###### 截图19
![img19.png](resource/md/img19.png)
###### 截图20
![img20.png](resource/md/img20.png)
###### 截图21
![img21.png](resource/md/img21.png)
###### 截图22
![img22.png](resource/md/img22.png)
###### 截图23
![img23.png](resource/md/img23.png)
###### 截图24
![img24.png](resource/md/img24.png)
###### 截图25
![img25.png](resource/md/img25.png)
###### 截图26
![img26.png](resource/md/img26.png)
###### 截图27
![img27.png](resource/md/img27.png)
###### 截图28
![img28.png](resource/md/img28.png)
###### 截图29
![img29.png](resource/md/img29.png)
###### 截图30
![img30.png](resource/md/img30.png)
###### 截图31
![img31.png](resource/md/img31.png)
###### 截图32
![img32.png](resource/md/img32.png)