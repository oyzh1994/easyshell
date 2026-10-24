# FreeBSD SSH test VM

FreeBSD 14.5 Cloud-Init VM running under QEMU inside Docker. It is used to test
interactive `sh`, `csh` and `tcsh` OSC directory reporting.

The FreeBSD Cloud-Init disk and seed ISO are generated during the Docker image
build and stored in the image. The container has no host mounts or project data
directory, and its SSH port is published only on `127.0.0.1`. The downloaded
archive is pinned to SHA256
`a2a8cd84cd59077225a0e75ead852e8a08fe26f9ab251a96d5a4fd4fe1db4454`.

## Start and connect

```sh
docker compose up -d --build
ssh -p 2225 root@127.0.0.1
```

Password: `123456`.

The Cloud-Init seed also creates two users with the same password:

```sh
ssh -p 2225 oscsh@127.0.0.1
ssh -p 2225 osccsh@127.0.0.1
```

`oscsh` uses `/bin/sh`; `osccsh` uses `/bin/csh`. This makes both FreeBSD shell
families available without changing the login shell of `root`.

The first build downloads and extracts the official FreeBSD VM image, so it can
take several minutes. Normal rebuilds reuse the Docker layer cache.

Container changes persist across restart. Recreating the container resets the
VM to the clean disk baked into the image:

```sh
docker compose down
docker compose up -d
```

Boot may take a few minutes on Apple Silicon because the x86_64 guest is
emulated by QEMU.
