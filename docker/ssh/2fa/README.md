# EasyShell SSH 2FA Test Server

该目录提供一个本地 OpenSSH 2FA 容器，用于验证 EasyShell JSch 的
`keyboard-interactive` 认证。认证链路为：

1. Unix 账户密码（PAM `common-auth`）
2. 6 位 TOTP（`pam_google_authenticator.so`，30 秒周期）

服务端仅启用 `keyboard-interactive`，密码或公钥不能单独完成认证。
本地测试环境允许同一 TOTP 在有效窗口内重复使用，以便连续执行自动回归和手工复测；
生产环境建议开启 `DISALLOW_REUSE`。

## 启动

```bash
export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
docker compose -f docker/ssh/2fa/docker-compose.yml up -d --build
docker ps --filter name=easyshell-ssh-2fa
```

默认映射到宿主机 `2224` 端口。可通过 `SSH_2FA_PORT` 修改。

## 查看本地测试凭据

用户名、本地测试密码、TOTP secret 和 `otpauth` URI 只保存在容器内，不写入镜像、
仓库或启动日志：

```bash
docker exec easyshell-ssh-2fa cat /root/.easyshell-2fa-credentials
```

首次启动生成凭据，之后由命名卷 `easyshell-ssh-2fa-root` 持久化。手动 SSH 登录：

```bash
ssh -p 2224 root@127.0.0.1
```

凭据文件为明文：`password` 是账户密码（32 位十六进制），`totp_secret` 是
Base32 共享密钥而不是验证码，`otpauth_uri` 可直接导入认证器。登录时需在
`Verification code:` 提示后输入认证器生成的当前 6 位 TOTP，验证码每 30 秒变化。
不要把 `totp_secret`、`otpauth_uri` 或文件中的应急码直接当作当前验证码。

## 自动回归

先完成 Maven 测试编译，再执行：

```bash
bash docker/ssh/2fa/test-2fa.sh
```

脚本会构建并启动容器、读取容器内凭据、刷新本机 known_hosts 中的本地端点，
然后运行 `ShellJSchTwoFactorAuthRegression`。密码和 TOTP secret 仅通过环境变量
传入测试进程，不输出到终端。

回归覆盖以下场景：

- 正确密码 + 当前 TOTP：连接、Exec、SFTP 上传/下载均通过。
- 错误 TOTP：只提交一次验证码，随后取消认证并触发既有认证回调；
  不会等待 JSch 将同一个验证码重复提交到 `MaxAuthTries`。
- 未注入验证码供应器时，桌面端仍使用 `MessageBox` 提示输入验证码。

最近一次本地验证结果：正确认证约 0.53 秒，错误 TOTP 约 4.4 秒拒绝。
测试完成后容器和命名卷保持运行，便于手工复测。

## 重置

需要重新生成密码和 TOTP secret 时：

```bash
docker compose -f docker/ssh/2fa/docker-compose.yml down -v
docker compose -f docker/ssh/2fa/docker-compose.yml up -d --build
```

重建容器后 host key 会变化，重新运行 `test-2fa.sh` 或执行
`ssh-keygen -R '[127.0.0.1]:2224'` 后再手工连接。
