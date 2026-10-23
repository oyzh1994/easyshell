FROM alpine:3.20

RUN apk add --no-cache bash dante-server iproute2 tinyproxy

COPY entrypoint.sh /usr/local/bin/easyshell-proxy-entrypoint
RUN chmod +x /usr/local/bin/easyshell-proxy-entrypoint

ENTRYPOINT ["/usr/local/bin/easyshell-proxy-entrypoint"]
