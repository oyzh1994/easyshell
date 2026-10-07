package cn.oyzh.easyshell.dto;

import cn.oyzh.fx.gui.svg.glyph.CancelSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SubmitSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;

/**
 * 网络扫描结果
 *
 * @author oyzh
 * @since 2025-05-26
 */
public class ShellNetworkScanResult {

    /**
     * 地址
     */
    private String host;

    /**
     * rdp是否可用
     */
    private boolean rdpAvailable;

    /**
     * vnc是否可用
     */
    private boolean vncAvailable;

    /**
     * ftp是否可用
     */
    private boolean ftpAvailable;

    /**
     * ssh是否可用
     */
    private boolean sshAvailable;

    /**
     * telnet是否可用
     */
    private boolean telnetAvailable;

    /**
     * rtsp是否可用
     */
    private boolean rtspAvailable;

    /**
     * rlogin是否可用
     */
    private boolean rloginAvailable;

    /**
     * http是否可用
     */
    private boolean httpAvailable;

    /**
     * https是否可用
     */
    private boolean httpsAvailable;

    /**
     * mysql是否可用
     */
    private boolean mysqlAvailable;

    /**
     * redis是否可用
     */
    private boolean redisAvailable;

    /**
     * zookeeper是否可用
     */
    private boolean zookeeperAvailable;

    /**
     * oracle是否可用
     */
    private boolean oracleAvailable;

    /**
     * mongoDB是否可用
     */
    private boolean mongoDBAvailable;

    /**
     * postgreSQL是否可用
     */
    private boolean postgreSQLAvailable;

    /**
     * Memcached是否可用
     */
    private boolean memcachedAvailable;

    /**
     * Elasticsearch是否可用
     */
    private boolean elasticsearchAvailable;

    /**
     * SQL Server是否可用
     */
    private boolean sqlServerAvailable;

    /** 获取地址 */
    public String getHost() {
        return host;
    }

    /** 设置地址 */
    public void setHost(String host) {
        this.host = host;
    }

    /** 是否rdp可用 */
    public boolean isRdpAvailable() {
        return rdpAvailable;
    }

    /** 设置是否rdp可用 */
    public void setRdpAvailable(boolean rdpAvailable) {
        this.rdpAvailable = rdpAvailable;
    }

    /** 是否vnc可用 */
    public boolean isVncAvailable() {
        return vncAvailable;
    }

    /** 设置是否vnc可用 */
    public void setVncAvailable(boolean vncAvailable) {
        this.vncAvailable = vncAvailable;
    }

    /** 是否ftp可用 */
    public boolean isFtpAvailable() {
        return ftpAvailable;
    }

    /** 设置是否ftp可用 */
    public void setFtpAvailable(boolean ftpAvailable) {
        this.ftpAvailable = ftpAvailable;
    }

    /** 是否ssh可用 */
    public boolean isSshAvailable() {
        return sshAvailable;
    }

    /** 设置是否ssh可用 */
    public void setSshAvailable(boolean sshAvailable) {
        this.sshAvailable = sshAvailable;
    }

    /** 是否telnet可用 */
    public boolean isTelnetAvailable() {
        return telnetAvailable;
    }

    /** 设置是否telnet可用 */
    public void setTelnetAvailable(boolean telnetAvailable) {
        this.telnetAvailable = telnetAvailable;
    }

    /** 是否rlogin可用 */
    public boolean isRloginAvailable() {
        return rloginAvailable;
    }

    /** 设置是否rlogin可用 */
    public void setRloginAvailable(boolean rloginAvailable) {
        this.rloginAvailable = rloginAvailable;
    }

    /** 是否http可用 */
    public boolean isHttpAvailable() {
        return httpAvailable;
    }

    /** 设置是否http可用 */
    public void setHttpAvailable(boolean httpAvailable) {
        this.httpAvailable = httpAvailable;
    }

    /** 是否https可用 */
    public boolean isHttpsAvailable() {
        return httpsAvailable;
    }

    /** 设置是否https可用 */
    public void setHttpsAvailable(boolean httpsAvailable) {
        this.httpsAvailable = httpsAvailable;
    }

    /** 是否mysql可用 */
    public boolean isMysqlAvailable() {
        return mysqlAvailable;
    }

    /** 设置是否mysql可用 */
    public void setMysqlAvailable(boolean mysqlAvailable) {
        this.mysqlAvailable = mysqlAvailable;
    }

    /** 是否redis可用 */
    public boolean isRedisAvailable() {
        return redisAvailable;
    }

    /** 设置是否redis可用 */
    public void setRedisAvailable(boolean redisAvailable) {
        this.redisAvailable = redisAvailable;
    }

    /** 是否zookeeper可用 */
    public boolean isZookeeperAvailable() {
        return zookeeperAvailable;
    }

    /** 设置是否zookeeper可用 */
    public void setZookeeperAvailable(boolean zookeeperAvailable) {
        this.zookeeperAvailable = zookeeperAvailable;
    }

    /** 是否oracle可用 */
    public boolean isOracleAvailable() {
        return oracleAvailable;
    }

    /** 设置是否oracle可用 */
    public void setOracleAvailable(boolean oracleAvailable) {
        this.oracleAvailable = oracleAvailable;
    }

    /** 是否memcached可用 */
    public boolean isMemcachedAvailable() {
        return memcachedAvailable;
    }

    /** 设置是否memcached可用 */
    public void setMemcachedAvailable(boolean memcachedAvailable) {
        this.memcachedAvailable = memcachedAvailable;
    }

    /** 是否elasticsearch可用 */
    public boolean isElasticsearchAvailable() {
        return elasticsearchAvailable;
    }

    /** 设置是否elasticsearch可用 */
    public void setElasticsearchAvailable(boolean elasticsearchAvailable) {
        this.elasticsearchAvailable = elasticsearchAvailable;
    }

    /** 是否sqlServer可用 */
    public boolean isSqlServerAvailable() {
        return sqlServerAvailable;
    }

    /** 是否rtsp可用 */
    public boolean isRtspAvailable() {
        return rtspAvailable;
    }

    /** 设置是否rtsp可用 */
    public void setRtspAvailable(boolean rtspAvailable) {
        this.rtspAvailable = rtspAvailable;
    }

    /** 设置是否sqlServer可用 */
    public void setSqlServerAvailable(boolean sqlServerAvailable) {
        this.sqlServerAvailable = sqlServerAvailable;
    }

    /** 是否mongoDB可用 */
    public boolean isMongoDBAvailable() {
        return mongoDBAvailable;
    }

    /** 设置是否mongoDB可用 */
    public void setMongoDBAvailable(boolean mongoDBAvailable) {
        this.mongoDBAvailable = mongoDBAvailable;
    }

    /** 是否postgreSQL可用 */
    public boolean isPostgreSQLAvailable() {
        return postgreSQLAvailable;
    }

    /** 设置是否postgreSQL可用 */
    public void setPostgreSQLAvailable(boolean postgreSQLAvailable) {
        this.postgreSQLAvailable = postgreSQLAvailable;
    }

    /** 获取ssh状态图标 */
    public SVGGlyph getSshStatus() {
        return this.createSVG(this.isSshAvailable());
    }

    /** 获取rdp状态图标 */
    public SVGGlyph getRdpStatus() {
        return this.createSVG(this.isRdpAvailable());
    }

    /** 获取vnc状态图标 */
    public SVGGlyph getVncStatus(){
        return this.createSVG(this.isVncAvailable());
    }

    /** 获取ftp状态图标 */
    public SVGGlyph getFtpStatus(){
        return this.createSVG(this.isFtpAvailable());
    }

    /** 获取http状态图标 */
    public SVGGlyph getHttpStatus(){
        return this.createSVG(this.isHttpAvailable());
    }

    /** 获取telnet状态图标 */
    public SVGGlyph getTelnetStatus(){
        return this.createSVG(this.isTelnetAvailable());
    }

    /** 获取https状态图标 */
    public SVGGlyph getHttpsStatus(){
        return this.createSVG(this.isHttpsAvailable());
    }

    /** 获取rlogin状态图标 */
    public SVGGlyph getRloginStatus(){
        return this.createSVG(this.isRloginAvailable());
    }

    /** 获取mysql状态图标 */
    public SVGGlyph getMysqlStatus(){
        return this.createSVG(this.isMysqlAvailable());
    }

    /** 获取redis状态图标 */
    public SVGGlyph getRedisStatus(){
        return this.createSVG(this.isRedisAvailable());
    }

    /** 获取zookeeper状态图标 */
    public SVGGlyph getZookeeperStatus(){
        return this.createSVG(this.isZookeeperAvailable());
    }

    /** 获取oracle状态图标 */
    public SVGGlyph getOracleStatus(){
        return this.createSVG(this.isOracleAvailable());
    }

    /** 获取mongoDB状态图标 */
    public SVGGlyph getMongoDBStatus(){
        return this.createSVG(this.isMongoDBAvailable());
    }

    /** 获取postgreSQL状态图标 */
    public SVGGlyph getPostgreSQLStatus(){
        return this.createSVG(this.isPostgreSQLAvailable());
    }

    /** 获取sqlServer状态图标 */
    public SVGGlyph getSqlServerStatus(){
        return this.createSVG(this.isSqlServerAvailable());
    }

    /** 获取rtsp状态图标 */
    public SVGGlyph getRtspStatus(){
        return this.createSVG(this.isRtspAvailable());
    }

    /** 获取memcached状态图标 */
    public SVGGlyph getMemcachedStatus(){
        return this.createSVG(this.isMemcachedAvailable());
    }

    /** 获取elasticsearch状态图标 */
    public SVGGlyph getElasticsearchStatus(){
        return this.createSVG(this.isElasticsearchAvailable());
    }

    /**
     * 创建状态图标
     *
     * @param success 是否可用
     * @return 状态图标
     */
    private SVGGlyph createSVG(boolean success){
        if (success) {
            SubmitSVGGlyph glyph = new SubmitSVGGlyph();
            glyph.setColor(Color.GREEN);
            glyph.setCursor(Cursor.DEFAULT);
            return glyph;
        }
        CancelSVGGlyph glyph = new CancelSVGGlyph();
        glyph.setColor(Color.RED);
        glyph.setCursor(Cursor.DEFAULT);
        return glyph;
    }
}
