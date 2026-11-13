import fcntl
import os
import pty
import select
import signal
import struct
import sys
import termios


def set_size(fd, rows, cols):
    try:
        fcntl.ioctl(fd, termios.TIOCSWINSZ, struct.pack("HHHH", rows, cols, 0, 0))
    except OSError:
        pass


def write_all(fd, data):
    while data:
        try:
            n = os.write(fd, data)
        except InterruptedError:
            continue
        data = data[n:]


def main():
    rows = int(sys.argv[1]) if len(sys.argv) > 1 else 24
    cols = int(sys.argv[2]) if len(sys.argv) > 2 else 80
    term = sys.argv[3] if len(sys.argv) > 3 else "xterm"
    shell = sys.argv[4] if len(sys.argv) > 4 else (os.environ.get("SHELL") or "/bin/sh")

    pid, fd = pty.fork()
    if pid == 0:
        os.environ["TERM"] = term
        try:
            os.execvp(shell, [shell, "-i"])
        except OSError:
            pass
        os._exit(127)

    set_size(fd, rows, cols)
    write_all(1, bytes([27]) + b"]9999;rshpty;resize" + bytes([7]))

    prefix = bytes([0, 0]) + b"RSZ;"
    pending = b""
    try:
        while True:
            ready, _, _ = select.select([0, fd], [], [])
            if 0 in ready:
                data = os.read(0, 4096)
                if not data:
                    break
                pending += data
                while pending:
                    index = pending.find(prefix)
                    if index < 0:
                        keep = 0
                        limit = min(len(prefix) - 1, len(pending))
                        while limit > 0:
                            if pending.endswith(prefix[:limit]):
                                keep = limit
                                break
                            limit -= 1
                        if pending[:len(pending) - keep]:
                            write_all(fd, pending[:len(pending) - keep])
                        pending = pending[len(pending) - keep:]
                        break
                    if index > 0:
                        write_all(fd, pending[:index])
                        pending = pending[index:]
                        continue
                    end = pending.find(bytes([0]), len(prefix))
                    if end < 0:
                        break
                    message = pending[len(prefix):end].split(b";")
                    if len(message) >= 2:
                        try:
                            set_size(fd, int(message[0]), int(message[1]))
                        except ValueError:
                            pass
                    pending = pending[end + 1:]
            if fd in ready:
                try:
                    data = os.read(fd, 65536)
                except OSError:
                    break
                if not data:
                    break
                write_all(1, data)
    except (BrokenPipeError, OSError):
        pass
    finally:
        try:
            os.close(fd)
        except OSError:
            pass
        try:
            if os.waitpid(pid, os.WNOHANG)[0] == 0:
                os.kill(pid, signal.SIGKILL)
                os.waitpid(pid, 0)
        except OSError:
            pass


main()
