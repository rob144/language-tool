<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
    <xsl:for-each select="matches/error">
        <div class="testError">
            <div class="errorRuleId">
                <span>Rule ID:   </span>
                <xsl:value-of select="@ruleId"/>
            </div>
            <div class="errorRuleSubId">
                 <span>Sub ID:  </span>
                 <xsl:value-of select="@subId"/>
            </div>
            <div class="testContext">
                <span>Context: </span>
                <xsl:value-of select="@context"/>
            </div>
        </div>
    </xsl:for-each>
</xsl:template>
</xsl:stylesheet>

