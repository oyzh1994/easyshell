FROM debian:stable

RUN apt-get update && apt-get install -y \
    openssh-server \
    mosh \
    locales \
    && rm -rf /var/lib/apt/lists/*

# 1. 在 /etc/locale.gen 中启用 en_US.UTF-8
RUN echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen

# 2. 生成所有已启用的 locale
RUN locale-gen

# 3. 验证（可选）
RUN locale -a | grep en_US

# 4. 设置环境变量
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# 其余设置（密码、sshd 配置等）
RUN echo 'root:123456' | chpasswd
RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

EXPOSE 22 6000-6100/udp
CMD ["/usr/sbin/sshd", "-D"]