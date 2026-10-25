## Entry Point

`cn.oyzh.easyshell.EasyShellBootstrap.main`

> **Note**: If you want to run the project, it is best to switch to the latest branch; otherwise, it may fail to start. The `master` branch code is merged periodically and may not be the latest.  
> **IDE**: IntelliJ IDEA Community Edition or Ultimate Edition is recommended.

---

## Dependencies

1. **base** project  
   https://gitee.com/oyzh1994/base
2. **fx-base** project  
   https://gitee.com/oyzh1994/fx-base
3. **JDK Version**:
   - Version 25 is required; version 27 is recommended. Object header compression in JDK 25 and later can significantly reduce memory usage.
   - On Linux, Zulu or Bellsoft JDK is recommended; other JDKs have higher glibc requirements.
   - On Windows ARM, Zulu or Bellsoft JDK is recommended; other JDKs do not support Windows on ARM.
   - On other platforms, OpenJDK is preferred.
   - OpenJDK: https://jdk.java.net/archive/
   - Zulu JDK: https://www.azul.com/downloads/#zulu
   - Bellsoft JDK: https://bell-sw.com/pages/downloads/
4. **JavaFX Version**:
    - Version 27 is required and recommended; features such as extended title bars are now stable.
    - It is recommended to use Maven to manage JavaFX libraries.
    - For binary libraries, use Zulu or Bellsoft FX. The standard FX libraries have higher glibc requirements and do not support Windows ARM.
5. **Windows on ARM Special Notes**
    - Step 1: Try to keep the JDK and JavaFX versions consistent.
    - Step 2: Complete the Maven installation of the `base` module.
    - Step 3: Complete the Maven installation of the `fx-base` module with the command `mvn package "-Djavafx.platform=win" "-DskipTests"`.
    - Step 4: Use the Bellsoft Full JDK together with the `base-pkg` module of `base` to handle the JavaFX dependencies. Entry point: `cn.oyzh.pkg.test.WoaPreHandler.run2`.
    - Step 5: Switch to the standard edition of Bellsoft JDK or Zulu JDK.

---

## Project Structure

```
.github    -> GitHub Actions configuration files
changes    -> Changelog for each version
docker     -> Docker configuration files
docs       -> Documentation resources
package    -> Packaging configuration
resource   -> Project resource files
src        -> Project source code
```

---

## Maven

### Build
```bash
mvn -X clean package -DskipTests
```
Using GitHub Actions for packaging is recommended.

### Notes
- Check whether the version number of `java -version` in cmd matches the project version number; otherwise, problems such as "invalid target release: 21" may occur.
- For users in China, the Tencent Cloud mirror is recommended; it can also download the JetBrains libraries, which may otherwise need to be handled separately.

---

## Application Packaging

### PNG Background Removal
https://www.iloveimg.com/remove-background

### Icon Conversion

**PNG to ICNS (Option 1)**  
https://anyconv.com/png-to-icns-converter/

**PNG to ICNS (Option 2)**  
https://www.aconvert.com/image/png-to-icns/

**PNG to ICO**  
https://www.freeconvert.com/png-to-ico

---

### Windows

**EXE / MSI Packaging Dependencies**  
https://github.com/wixtoolset/wix3/releases

**MSI Packaging (Recommended)**  
Config -> `package/win_msi.yaml`  
Entry -> `cn.oyzh.easyshell.test.Pack.win_msi`

**EXE Packaging**  
Config -> `package/win_exe.yaml`  
Entry -> `cn.oyzh.easyshell.test.Pack.win_exe`

**App Image Packaging**  
Config -> `package/win_image.yaml`  
Entry -> `cn.oyzh.easyshell.test.Pack.win_image`

> **Note**: EXE and MSI packaging require setting the `win-menu` and `win-shortcut` parameters to avoid the program icon not being displayed on the desktop.

---

### macOS

**PKG Packaging (Recommended)**  
Config -> `package/macos_pkg.yaml`  
Entry -> `cn.oyzh.easyshell.test.Pack.macos_pkg`

**DMG Packaging**  
Config -> `package/macos_dmg.yaml`  
Entry -> `cn.oyzh.easyshell.test.Pack.macos_dmg`

**App Image Packaging**  
Config -> `package/macos_image.yaml`  
Entry -> `cn.oyzh.easyshell.test.Pack.macos_image`

> **Note**: DMG and PKG packaging require setting the `mac-package-identifier` parameter to avoid the program icon not being displayed in Launchpad because of duplicate App names.

---

### Linux (UOS, Ubuntu, and CentOS as examples)

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
Config -> `package/linux_AppImage.yaml`  
Entry -> `cn.oyzh.easyshell.test.Pack.linux_AppImage`

**DEB Packaging**  
Config -> `package/linux_deb.yaml`  
Entry -> `cn.oyzh.easyshell.test.Pack.linux_deb`

**RPM Packaging**  
Config -> `package/linux_rpm.yaml`  
Entry -> `cn.oyzh.easyshell.test.Pack.linux_rpm`

**App Image Packaging**  
Config -> `package/linux_image.yaml`  
Entry -> `cn.oyzh.easyshell.test.Pack.linux_image`

---

## X11 / X-Server

### Windows
https://sourceforge.net/projects/vcxsrv/

### macOS
https://www.xquartz.org/

---

## Linux System

### DEB Installation Reports Failure

If the error details contain:
```
xdg-desktop-menu: No writable system menu directory found.
```
Perform the following operation, and then reinstall:
```bash
sudo mkdir /usr/share/desktop-directories/
```

### Double-Clicking the RPM Package Reports a Problem or Does Not Respond
```bash
rpm -ivh xx.rpm
```

### RPM Upgrade
```bash
rpm -U xx.rpm
```

### RPM Uninstall
```bash
rpm -e easyshell
```

### DEB Upgrade
```bash
dpkg -r easyshell
dpkg -i easyshell-xx.deb
```

### DEB Uninstall
```bash
dpkg -r easyshell
```

---

## macOS System

### Program Icon Not Displayed in Launchpad After Installing with DMG on Mac
```bash
defaults write com.apple.dock ResetLaunchPad -bool true && killall Dock
```

### Mac Cannot Launch — Solution 1
```bash
sudo chmod +x EasyShell.app
```

### Mac Cannot Launch — Solution 2
```bash
chmod -R 755 /path/to/EasyShell.app
```

### Mac Cannot Launch — Solution 3
When running the `.app` file on macOS, the message "The application EasyShell.app cannot be opened" appears:
1. Use a third-party extraction tool such as FastZip instead of the built-in Archive Utility.
2. Grant permissions in the terminal:
   ```bash
   chmod +x EasyShell.app/Contents/MacOS/*
   ```

### Mac Cannot Launch — Solution 4 ("Damaged" Warning)
When running the `.app` file on macOS, the message "damaged and cannot be opened" appears:
1. Allow apps downloaded from "Anywhere" to run:
   - Open **System Preferences** → **Security & Privacy** → **General**.
   - Select **Anywhere**.
   - If **Anywhere** is not visible, open the terminal and run:
     ```bash
     sudo spctl --master-disable
     ```
   - Enter your password and press Enter.
