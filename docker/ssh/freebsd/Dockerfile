FROM alpine:3.22 AS freebsd-disk

ARG FREEBSD_ARCHIVE_URL=https://mirrors.aliyun.com/freebsd/releases/VM-IMAGES/14.5-RELEASE/amd64/Latest/FreeBSD-14.5-RELEASE-amd64-BASIC-CLOUDINIT-ufs.qcow2.xz
ARG FREEBSD_ARCHIVE_SHA256=a2a8cd84cd59077225a0e75ead852e8a08fe26f9ab251a96d5a4fd4fe1db4454

RUN apk add --no-cache ca-certificates curl xz \
    && mkdir -p /out \
    && curl -fL --retry 5 --retry-delay 2 --connect-timeout 30 \
        -o /tmp/freebsd.qcow2.xz "$FREEBSD_ARCHIVE_URL" \
    && echo "$FREEBSD_ARCHIVE_SHA256  /tmp/freebsd.qcow2.xz" | sha256sum -c - \
    && xz -dc /tmp/freebsd.qcow2.xz > /out/disk.qcow2 \
    && rm -f /tmp/freebsd.qcow2.xz

FROM alpine:3.22 AS freebsd-seed

COPY cloud-init /tmp/cloud-init

RUN apk add --no-cache xorriso \
    && mkdir -p /out \
    && xorriso -as mkisofs -output /out/seed.iso -volid cidata -joliet -rock /tmp/cloud-init

FROM aygpdr/freebsd:14.3-RELEASE

COPY --from=freebsd-disk /out/disk.qcow2 /opt/easyshell/disk.qcow2
COPY --from=freebsd-seed /out/seed.iso /opt/easyshell/seed.iso

COPY start.sh /opt/easyshell/start.sh
RUN chmod +x /opt/easyshell/start.sh

EXPOSE 22
ENTRYPOINT ["/opt/easyshell/start.sh"]
