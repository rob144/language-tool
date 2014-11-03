<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
    <table border="1">
        <tr bgcolor="#9acd32">
            <th style="text-align:left">Rule ID</th>
            <th style="text-align:left">Matches</th>
        </tr>
        <xsl:for-each select="matches/error">
            <tr>
                <td><xsl:value-of select="number(@fromy)+1"/></td>
                <td><xsl:value-of select="number(@fromx)+1"/></td>
            </tr>
        </xsl:for-each>
    </table>
</xsl:template>
</xsl:stylesheet>

