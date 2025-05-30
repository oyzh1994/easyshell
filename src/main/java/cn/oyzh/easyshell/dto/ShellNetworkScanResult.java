package cn.oyzh.easyshell.dto;

import cn.oyzh.fx.gui.svg.glyph.CancelSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SubmitSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;

/**
 * 端口扫描结果
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
     * zookeeper是否可用
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isRdpAvailable() {
        return rdpAvailable;
    }

    public void setRdpAvailable(boolean rdpAvailable) {
        this.rdpAvailable = rdpAvailable;
    }

    public boolean isVncAvailable() {
        return vncAvailable;
    }

    public void setVncAvailable(boolean vncAvailable) {
        this.vncAvailable = vncAvailable;
    }

    public boolean isFtpAvailable() {
        return ftpAvailable;
    }

    public void setFtpAvailable(boolean ftpAvailable) {
        this.ftpAvailable = ftpAvailable;
    }

    public boolean isSshAvailable() {
        return sshAvailable;
    }

    public void setSshAvailable(boolean sshAvailable) {
        this.sshAvailable = sshAvailable;
    }

    public boolean isTelnetAvailable() {
        return telnetAvailable;
    }

    public void setTelnetAvailable(boolean telnetAvailable) {
        this.telnetAvailable = telnetAvailable;
    }

    public boolean isRloginAvailable() {
        return rloginAvailable;
    }

    public void setRloginAvailable(boolean rloginAvailable) {
        this.rloginAvailable = rloginAvailable;
    }

    public boolean isHttpAvailable() {
        return httpAvailable;
    }

    public void setHttpAvailable(boolean httpAvailable) {
        this.httpAvailable = httpAvailable;
    }

    public boolean isHttpsAvailable() {
        return httpsAvailable;
    }

    public void setHttpsAvailable(boolean httpsAvailable) {
        this.httpsAvailable = httpsAvailable;
    }

    public boolean isMysqlAvailable() {
        return mysqlAvailable;
    }

    public void setMysqlAvailable(boolean mysqlAvailable) {
        this.mysqlAvailable = mysqlAvailable;
    }

    public boolean isRedisAvailable() {
        return redisAvailable;
    }

    public void setRedisAvailable(boolean redisAvailable) {
        this.redisAvailable = redisAvailable;
    }

    public boolean isZookeeperAvailable() {
        return zookeeperAvailable;
    }

    public void setZookeeperAvailable(boolean zookeeperAvailable) {
        this.zookeeperAvailable = zookeeperAvailable;
    }

    public boolean isOracleAvailable() {
        return oracleAvailable;
    }

    public void setOracleAvailable(boolean oracleAvailable) {
        this.oracleAvailable = oracleAvailable;
    }

    public boolean isMemcachedAvailable() {
        return memcachedAvailable;
    }

    public void setMemcachedAvailable(boolean memcachedAvailable) {
        this.memcachedAvailable = memcachedAvailable;
    }

    public boolean isElasticsearchAvailable() {
        return elasticsearchAvailable;
    }

    public void setElasticsearchAvailable(boolean elasticsearchAvailable) {
        this.elasticsearchAvailable = elasticsearchAvailable;
    }

    public boolean isSqlServerAvailable() {
        return sqlServerAvailable;
    }

    public boolean isRtspAvailable() {
        return rtspAvailable;
    }

    public void setRtspAvailable(boolean rtspAvailable) {
        this.rtspAvailable = rtspAvailable;
    }

    public void setSqlServerAvailable(boolean sqlServerAvailable) {
        this.sqlServerAvailable = sqlServerAvailable;
    }

    public boolean isMongoDBAvailable() {
        return mongoDBAvailable;
    }

    public void setMongoDBAvailable(boolean mongoDBAvailable) {
        this.mongoDBAvailable = mongoDBAvailable;
    }

    public boolean isPostgreSQLAvailable() {
        return postgreSQLAvailable;
    }

    public void setPostgreSQLAvailable(boolean postgreSQLAvailable) {
        this.postgreSQLAvailable = postgreSQLAvailable;
    }

    public SVGGlyph getSshStatus() {
        return this.createSVG(this.isSshAvailable());
    }

    public SVGGlyph getRdpStatus() {
        return this.createSVG(this.isRdpAvailable());
    }

    public SVGGlyph getVncStatus(){
        return this.createSVG(this.isVncAvailable());
    }

    public SVGGlyph getFtpStatus(){
        return this.createSVG(this.isFtpAvailable());
    }

    public SVGGlyph getHttpStatus(){
        return this.createSVG(this.isHttpAvailable());
    }

    public SVGGlyph getTelnetStatus(){
        return this.createSVG(this.isTelnetAvailable());
    }

    public SVGGlyph getHttpsStatus(){
        return this.createSVG(this.isHttpsAvailable());
    }

    public SVGGlyph getRloginStatus(){
        return this.createSVG(this.isRloginAvailable());
    }

    public SVGGlyph getMysqlStatus(){
        return this.createSVG(this.isMysqlAvailable());
    }

    public SVGGlyph getRedisStatus(){
        return this.createSVG(this.isRedisAvailable());
    }

    public SVGGlyph getZookeeperStatus(){
        return this.createSVG(this.isZookeeperAvailable());
    }

    public SVGGlyph getOracleStatus(){
        return this.createSVG(this.isOracleAvailable());
    }

    public SVGGlyph getMongoDBStatus(){
        return this.createSVG(this.isMongoDBAvailable());
    }

    public SVGGlyph getPostgreSQLStatus(){
        return this.createSVG(this.isPostgreSQLAvailable());
    }

    public SVGGlyph getSqlServerStatus(){
        return this.createSVG(this.isSqlServerAvailable());
    }

    public SVGGlyph getRtspStatus(){
        return this.createSVG(this.isRtspAvailable());
    }


    public SVGGlyph getMemcachedStatus(){
        return this.createSVG(this.isMemcachedAvailable());
    }

    public SVGGlyph getElasticsearchStatus(){
        return this.createSVG(this.isElasticsearchAvailable());
    }

    private SVGGlyph createSVG(boolean success){
        if (success) {
            SubmitSVGGlyph glyph = new SubmitSVGGlyph("12");
            glyph.setColor(Color.GREEN);
            glyph.setCursor(Cursor.DEFAULT);
            return glyph;
        }
        CancelSVGGlyph glyph = new CancelSVGGlyph("12");
        glyph.setColor(Color.RED);
        glyph.setCursor(Cursor.DEFAULT);
        return glyph;
    }
}
