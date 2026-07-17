FROM ubuntu:20.04
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y rsh-server xinetd && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN echo 'service login \
{ \
    socket_type     = stream \
    protocol        = tcp \
    wait            = no \
    user            = root \
    server          = /usr/sbin/in.rlogind \
    server_args     = -L \
    disable         = no \
}' > /etc/xinetd.d/rlogin

# 允许所有来源（TCP Wrapper）
RUN echo 'ALL: ALL' > /etc/hosts.allow

# ===== 关键修复：添加 rlogin 信任文件 =====
RUN echo '+ +' > /etc/hosts.equiv
RUN mkdir -p /root && echo '+ +' > /root/.rhosts && chmod 600 /root/.rhosts

# --- 在这里配置账密 ---
# 1. 创建一个新用户，例如 "myuser"
#RUN useradd -m myuser
# 2. 使用 chpasswd 为该用户设置密码
RUN echo 'root:123456' | chpasswd

EXPOSE 513

CMD ["xinetd", "-dontfork"]