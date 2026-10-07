# EasyShell `fx.svg` 图标类代码审查文档（极简）

> 范围：`src/main/java/cn/oyzh/easyshell/fx/svg/` 递归全部类。
> 共 51 个类：50 个 `SVGGlyph` 图标类 + 1 个 `SVGPane` 面板类。
> 图标类职责极简，均为“加载指定 SVG 资源文件并渲染”，故每类仅一行职责。

## 通用结构（所有 `glyph` 包下的 `*SVGGlyph`）
- 职责：加载指定 SVG 资源并渲染的图标。
- 字段：无（资源路径在构造器中传给父类 `SVGGlyph`）。
- 方法：
  - `XxxSVGGlyph()`：`super("资源路径")` 加载默认尺寸。
  - `XxxSVGGlyph(String size)`（部分类提供）：先调无参构造再 `setSizeStr(size)` 指定尺寸。
- 调用链：`new XxxSVGGlyph() → SVGGlyph(url)`

> 说明：下表中“资源”列即构造器传入的 SVG 文件路径；“类”列加点号前缀表示子包。

### 包 `fx.svg.glyph`

| 类 | 职责（一句话） | 资源 |
|---|---|---|
| CollapseListSVGGlyph | 列表折叠状态图标 | `/font/arrow-up-double-line.svg` |
| CursorBarSVGGlyph | 光标竖线样式图标 | `/font/cursor-bar.svg` |
| CursorBlockSVGGlyph | 光标方块样式图标 | `/font/cursor-block.svg` |
| CursorUnderlineSVGGlyph | 光标下划线样式图标 | `/font/cursor-underline.svg` |
| ExpandListSVGGlyph | 列表展开状态图标 | `/font/arrow-down-double-line.svg` |
| PublishSVGGlyph | 发布图标 | `/font/publish.svg` |
| ReturnFolderSVGGlyph | 返回目录图标 | `/font/return-folder.svg` |
| SubscribeSVGGlyph | 订阅图标 | `/font/subscribe.svg` |

### 包 `fx.svg.glyph.os`

| 类 | 职责（一句话） | 资源 |
|---|---|---|
| AppleSVGGlyph | Apple（macOS）操作系统图标 | `/font/os/apple.svg` |
| ArchSVGGlyph | Arch Linux 操作系统图标 | `/font/os/arch.svg` |
| CentosSVGGlyph | CentOS 操作系统图标 | `/font/os/centos.svg` |
| DebianSVGGlyph | Debian 操作系统图标 | `/font/os/debian.svg` |
| DeepinSVGGlyph | Deepin 操作系统图标 | `/font/os/deepin.svg` |
| FedoraSVGGlyph | Fedora 操作系统图标 | `/font/os/fedora.svg` |
| FreebsdSVGGlyph | FreeBSD 操作系统图标 | `/font/os/freebsd.svg` |
| LinuxSVGGlyph | Linux 操作系统图标 | `/font/linux.svg` |
| MintSVGGlyph | Linux Mint 操作系统图标 | `/font/os/mint.svg` |
| RaspberrypiSVGGlyph | 树莓派图标 | `/font/os/raspberry-pi.svg` |
| RedhatSVGGlyph | 红帽图标 | `/font/os/redhat.svg` |
| UbuntuSVGGlyph | Ubuntu 图标 | `/font/os/ubuntu.svg` |
| WindowsSVGGlyph | Windows 图标 | `/font/os/windows.svg` |

### 包 `fx.svg.glyph.other`

| 类 | 职责（一句话） | 资源 |
|---|---|---|
| AlibabaCloudSVGGlyph | 阿里云图标 | `/font/other/alibaba_cloud.svg` |
| HuaweiCloudSVGGlyph | 华为云图标 | `/font/other/huawei_cloud.svg` |
| MinioSVGGlyph | MinIO 对象存储图标 | `/font/other/minio.svg` |
| TencentCloudSVGGlyph | 腾讯云图标 | `/font/other/tencent_cloud.svg` |

### 包 `fx.svg.glyph.protocol`

| 类 | 职责（一句话） | 资源 |
|---|---|---|
| FTPSVGGlyph | FTP 协议图标 | `/font/protocol/ftp.svg` |
| MoshSVGGlyph | Mosh 协议图标 | `/font/protocol/mosh.svg` |
| RDPSVGGlyph | RDP 协议图标 | `/font/protocol/rdp.svg` |
| RLoginSVGGlyph | RLogin 协议图标 | `/font/protocol/rlogin.svg` |
| S3SVGGlyph | S3 协议图标 | `/font/protocol/s3.svg` |
| SFTPSVGGlyph | SFTP 协议图标 | `/font/protocol/sftp.svg` |
| SMBSVGGlyph | SMB 协议图标 | `/font/protocol/smb.svg` |
| SSHSVGGlyph | SSH 协议图标 | `/font/ssh.svg` |
| SerialPortSVGGlyph | 串口协议图标 | `/font/protocol/serial_port.svg` |
| TelnetSVGGlyph | Telnet 协议图标 | `/font/protocol/telnet.svg` |
| VNCSVGGlyph | VNC 协议图标 | `/font/protocol/vnc.svg` |
| WebdavSVGGlyph | WebDAV 协议图标 | `/font/protocol/webdav.svg` |

### 包 `fx.svg.glyph.redis`

| 类 | 职责（一句话） | 资源 |
|---|---|---|
| HashSVGGlyph | Redis Hash 数据类型图标 | `/font/redis/hash.svg` |
| JsonSVGGlyph | Redis JSON 数据类型图标 | `/font/redis/json.svg` |
| KeysSVGGlyph | Redis 键图标 | `/font/redis/keys.svg` |
| ListSVGGlyph | Redis List 数据类型图标 | `/font/redis/list.svg` |
| RedisSVGGlyph | Redis 图标 | `/font/redis/redis.svg` |
| SetSVGGlyph | Redis Set 数据类型图标 | `/font/redis/set.svg` |
| StreamSVGGlyph | Redis Stream 数据类型图标 | `/font/redis/stream.svg` |
| StringSVGGlyph | Redis String 数据类型图标 | `/font/redis/string.svg` |
| ZSetSVGGlyph | Redis ZSet 数据类型图标 | `/font/redis/zset.svg` |

### 包 `fx.svg.glyph.zk`

| 类 | 职责（一句话） | 资源 |
|---|---|---|
| AuditSVGGlyph | ZooKeeper 审计节点图标 | `/font/zk/audit.svg` |
| NodeSVGGlyph | ZooKeeper 节点图标 | `/font/zk/file-text.svg` |
| TempSVGGlyph | ZooKeeper 临时节点图标 | `/font/zk/temp.svg` |
| ZookeeperSVGGlyph | ZooKeeper 图标 | `/font/zk/zookeeper.svg` |

---

## `fx.svg.pane`（非纯图标类）

### ExpandListSVGPane
- 职责：列表展开/折叠图标面板，按状态切换子图标（继承 `SVGPane`）。
- 字段：无（父类提供 `size`）。
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ExpandListSVGPane()` | 构造器 | 默认 `collapse()` |
  | `void expand()` | 展开 | `setChild(new ExpandListSVGGlyph(this.size))` |
  | `void collapse()` | 折叠 | `setChild(new CollapseListSVGGlyph(this.size))` |
  | `boolean isCollapse()` | 是否折叠 | 取首个子 `SVGGlyph.getUrl()` 是否含 `arrow-up-double-line.svg` |
  | `void setCollapse(boolean)` | 设置状态 | 依值调 `collapse()`/`expand()` |
- 调用链：`ExpandListSVGPane() → collapse → CollapseListSVGGlyph`；`setCollapse → expand/collapse`

---

## 附：图标类统计

| 包 | 类数 |
|---|---|
| fx.svg.glyph | 8 |
| fx.svg.glyph.os | 13 |
| fx.svg.glyph.other | 4 |
| fx.svg.glyph.protocol | 12 |
| fx.svg.glyph.redis | 9 |
| fx.svg.glyph.zk | 4 |
| fx.svg.pane | 1 |
| **合计** | **51** |

> 其中 `ExpandListSVGGlyph`、`CollapseListSVGGlyph`、`CursorBarSVGGlyph`、`CursorBlockSVGGlyph`、`CursorUnderlineSVGGlyph` 等支持带尺寸的构造器 `(String size)`，被 `ShellTermCursorStyleComboBox`、`ExpandListSVGPane` 等使用。
