#!/bin/sh
set -eu

DISK="${DISK:-/opt/easyshell/disk.qcow2}"
SEED="${SEED:-/opt/easyshell/seed.iso}"
MEMORY="${MEMORY:-2G}"
CPUS="${CPUS:-2}"

if [ ! -f "$DISK" ]; then
    echo "FreeBSD disk not found: $DISK" >&2
    exit 1
fi

set -- \
    -m "$MEMORY" \
    -smp "$CPUS" \
    -drive "file=$DISK,format=qcow2,if=virtio" \
    -netdev user,id=net0,hostfwd=tcp::22-:22 \
    -device virtio-net,netdev=net0 \
    -device virtio-rng-pci \
    -nographic

if [ -f "$SEED" ]; then
    set -- "$@" -drive "file=$SEED,media=cdrom,readonly=on"
fi

if [ -e /dev/kvm ]; then
    set -- "$@" -enable-kvm -cpu host
else
    set -- "$@" -cpu qemu64
fi

exec qemu-system-x86_64 "$@"
