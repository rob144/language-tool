<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
    <table border="1">
      <tr bgcolor="#9acd32">
        <th style="text-align:left">Line</th>
        <th style="text-align:left">Char</th>
        <th style="text-align:left">Category</th>
        <th style="text-align:left">RuleID</th>
        <th style="text-align:left">Message</th>
        <th style="text-align:left">Replacements</th>
      </tr>
      <xsl:for-each select="matches/error">
      <tr>
        <td><xsl:value-of select="number(@fromy)+1"/></td>
        <td><xsl:value-of select="number(@fromx)+1"/></td>
        <td><xsl:value-of select="@category"/></td>
        <td><xsl:value-of select="@ruleId"/></td>
        <td><xsl:value-of select="@msg"/></td>
        <td><xsl:value-of select="translate(@replacements,'#',' ')"/></td>
      </tr>
      </xsl:for-each>
    </table>
</xsl:template>
</xsl:stylesheet>

