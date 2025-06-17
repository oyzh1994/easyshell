package cn.oyzh.easyshell.zmodem;

public interface ControlCharacters {
    /**
     * \a
     */
    char BEL = 0x07;

    /**
     * \b
     */
    char BS = 0x08;

    /**
     * https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h4-Single-character-functions:SI.BE1
     */
    char SI = '\u000F';

    /**
     * https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h4-Single-character-functions:SO.BE7
     */
    char SO = '\u000E';

    /**
     * \r
     */
    char CR = 0x0D;

    char ENQ = 0x05;

    /**
     * \f
     */
    char FF = 0x0C;

    /**
     * \n
     */
    char LF = 0x0A;

    /**
     * SPACE
     */
    char SP = ' ';

    /**
     * \v
     */
    char VT = 0x0B;

    /**
     * \t
     */
    char TAB = 0x09;

    /**
     * \
     */
    char ST = 0x9c;

    char ESC = 0x1B;
}