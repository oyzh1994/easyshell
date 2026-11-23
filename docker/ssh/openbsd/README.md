# OpenBSD SSH test VM

OpenBSD 7.9 running under QEMU through
[AnyVM](https://github.com/anyvm-org/docker). The container is used to test
OpenBSD `ksh` OSC directory reporting, SSH/PTY behavior, SFTP, and derived
file/process capabilities.

The VM data lives in the named volume `easyshell-openbsd-osc-data`; nothing is
mounted from the project. SSH is published only on `127.0.0.1:2226` and the
container is kept alive after AnyVM starts QEMU in the background.

## Start and connect

```sh
cd docker/ssh/openbsd
docker compose up -d
```

The first run downloads the OpenBSD qcow2 image (about 1.2 GB compressed) and
extracts it to the named volume. Under Docker Desktop the guest runs with TCG,
so the first boot can take several minutes. Follow progress with:

```sh
docker logs -f easyshell-openbsd-osc
```

Once the health check is green, connect with:

```sh
ssh -p 2226 root@127.0.0.1
```

OpenBSD/aarch64 can hang while attaching the default `virtio-gpu` device
(the serial console stops at `viogpu0`). The compose file therefore uses a
plain VGA device by default. Override it only if you have verified another
device with this guest:

```sh
OPENBSD_VGA=VGA docker compose up -d
```

The upstream image does not ship a usable root password. On first boot the
compose command waits for the guest, sets the root password to `openbsd`,
and raises the test VM's SSH
`MaxSessions` from the OpenBSD default of 10 to 64 for channel-concurrency
regression. The AnyVM data volume contains the generated host key and metadata.

## Reset

Recreating the container keeps the installed VM. To return to a clean VM and
force a fresh image extraction:

```sh
docker compose down -v
docker compose up -d
```

`docker compose down` alone keeps the named volume. If the guest filesystem
was damaged by a forced QEMU shutdown, the old volume will keep dropping into
the single-user `fsck` prompt even after the container is recreated. Use
`down -v` or remove the volume explicitly:

```sh
docker volume rm easyshell-openbsd-osc-data
```

The password and `MaxSessions` initialization runs again automatically after
the volume is recreated.

The default guest architecture follows the Docker host architecture. Override
other settings through `OPENBSD_RELEASE`, `OPENBSD_BUILDER`, `SSH_PORT`,
`MEMORY`, and `CPUS` environment variables.
