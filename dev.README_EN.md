###### Entry Point
`cn.oyzh.easyshell.EasyShellBootstrap.main`

> **Note**: To run the project, it is recommended to switch to the latest branch. The `master` branch code is merged periodically and may not be up to date.  
> **IDE**: IntelliJ IDEA Community or Ultimate Edition is recommended.

###### Dependencies
1. **base** project  
   https://gitee.com/oyzh1994/base
2. **fx-base** project  
   https://gitee.com/oyzh1994/fx-base
3. **JDK Version**: 25 is required (recommended). JDK 25 reduces memory usage significantly via object header compression.  
   - For Linux ARM platforms, AWS Corretto JDK is recommended as other JDKs may lack the `hsdis` library.
   - For other platforms, OpenJDK is preferred.  
   - AWS Corretto JDK 25: https://docs.aws.amazon.com/corretto/latest/corretto-25-ug/downloads-list.html  
   - OpenJDK: https://jdk.java.net/archive/

###### Project Structure
```
.github    -> GitHub Actions configuration files
changes    -> Version changelogs
docker     -> Docker configuration files
docs       -> Documentation resources
package    -> Packaging configuration
resource   -> Project resource files
src        -> Project source code
```

# Maven

###### Build
```bash
mvn -X clean package -DskipTests
```
Using GitHub Actions for packaging is recommended.

###### Notes
- Ensure that `java -version` in your terminal matches the project's JDK version. Mismatches cause errors like "invalid target release: 21".
- For users in China, the Tencent Cloud Maven mirror is recommended as it can download dependencies from JetBrains repositories as well.

# Packaging

###### PNG Background Removal
https://www.iloveimg.com/remove-background

###### Icon Conversion

**PNG to ICNS (Option 1)**  
https://anyconv.com/png-to-icns-converter/

**PNG to ICNS (Option 2)**  
https://www.aconvert.com/image/png-to-icns/

**PNG to ICO**  
https://www.freeconvert.com/png-to-ico

---

###### Windows

**EXE / MSI packaging dependency**  
https://github.com/wixtoolset/wix3/releases

**MSI Packaging (Recommended)**  
Config: `package/win_msi.yaml`  
Entry: `cn.oyzh.easyshell.test.Pack.win_msi`

**EXE Packaging**  
Config: `package/win_exe.yaml`  
Entry: `cn.oyzh.easyshell.test.Pack.win_exe`

**App Image Packaging**  
Config: `package/win_image.yaml`  
Entry: `cn.oyzh.easyshell.test.Pack.win_image`

> **Note**: EXE and MSI packaging must set `win-menu` and `win-shortcut` parameters to avoid missing desktop icons.

---

###### macOS

**PKG Packaging (Recommended)**  
Config: `package/macos_pkg.yaml`  
Entry: `cn.oyzh.easyshell.test.Pack.macos_pkg`

**DMG Packaging**  
Config: `package/macos_dmg.yaml`  
Entry: `cn.oyzh.easyshell.test.Pack.macos_dmg`

**App Image Packaging**  
Config: `package/macos_image.yaml`  
Entry: `cn.oyzh.easyshell.test.Pack.macos_image`

> **Note**: DMG and PKG packaging must set `mac-package-identifier` to avoid missing app icons in Launchpad due to duplicate app names.

---

###### Linux (UOS, Ubuntu, CentOS examples)

**DEB Packaging Dependencies (Ubuntu, Deepin)**
```bash
sudo apt install fakeroot binutils
```

**RPM Packaging Dependencies (CentOS)**
```bash
sudo yum install rpm-build
# or
sudo yum install rpmrebuild
```

**AppImage Packaging Dependencies**

x86_64:
```bash
wget -O appimagetool https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage
chmod +x appimagetool
sudo mv appimagetool /usr/local/bin/appimagetool
```

ARM64:
```bash
wget -O appimagetool https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-aarch64.AppImage
chmod +x appimagetool
sudo mv appimagetool /usr/local/bin/appimagetool
```

**AppImage Packaging (Recommended)**  
Config: `package/linux_AppImage.yaml`  
Entry: `cn.oyzh.easyshell.test.Pack.linux_AppImage`

**DEB Packaging**  
Config: `package/linux_deb.yaml`  
Entry: `cn.oyzh.easyshell.test.Pack.linux_deb`

**RPM Packaging**  
Config: `package/linux_rpm.yaml`  
Entry: `cn.oyzh.easyshell.test.Pack.linux_rpm`

**App Image Packaging**  
Config: `package/linux_image.yaml`  
Entry: `cn.oyzh.easyshell.test.Pack.linux_image`

# X11 / X-Server

###### Windows
https://sourceforge.net/projects/vcxsrv/

###### macOS
https://www.xquartz.org/

# Linux System

###### DEB Installation Failure
If the error contains `xdg-desktop-menu: No writable system menu directory found.`, run:
```bash
sudo mkdir /usr/share/desktop-directories/
```
Then reinstall.

###### RPM Package Double-Click Failure or No Response
```bash
rpm -ivh xx.rpm
```

###### RPM Upgrade
```bash
rpm -U xx.rpm
```

###### RPM Uninstall
```bash
rpm -e easyshell
```

###### DEB Upgrade
```bash
dpkg -r easyshell
dpkg -i easyshell-xx.deb
```

###### DEB Uninstall
```bash
dpkg -r easyshell
```

# macOS System

###### App Icon Not Showing in Launchpad After DMG Install
```bash
defaults write com.apple.dock ResetLaunchPad -bool true && killall Dock
```

###### App Cannot Launch — Solution 1
```bash
sudo chmod +x EasyShell.app
```

###### App Cannot Launch — Solution 2
```bash
chmod -R 755 /path/to/EasyShell.app
```

###### App Cannot Launch — Solution 3
If macOS reports "EasyShell.app cannot be opened" when running the `.app` file:
1. Use a third-party unarchiver like FastZip instead of the built-in Archive Utility.
2. Grant execute permissions in the terminal:
   ```bash
   chmod +x EasyShell.app/Contents/MacOS/*
   ```

###### App Cannot Launch — Solution 4 ("Damaged" Warning)
If macOS reports the app is "damaged and cannot be opened":
1. Allow apps from "Anywhere":
   - Open **System Preferences** → **Security & Privacy** → **General**.
   - Click the lock icon and select **Anywhere**.
   - If "Anywhere" is not available, open the terminal and run:
     ```bash
     sudo spctl --master-disable
     ```
   - Enter your password when prompted.
