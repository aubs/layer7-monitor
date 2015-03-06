<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns='http://www.w3.org/1999/xhtml' xmlns:L7p="http://www.layer7tech.com/ws/policy"
	xmlns:wsp="http://schemas.xmlsoap.org/ws/2002/12/policy" xmlns:tw="http://www.denhaag.nl/xslt/extensions/tw"
	exclude-result-prefixes="L7p wsp xsi tw">

	<xsl:output method="html" indent="yes" version="4.0"
		encoding="UTF-8" />
	<xsl:template match="/wsp:Policy">
		<html>
			<head>
				<style type="text/css">
					body {
					font-family: verdana, geneva, lucida, 'lucida grande', arial, helvetica,
					sans-serif;
					font-size: 11px;
					}
					.comment {
					color: gray;
					}
					.left-comment {
					color: gray;
					padding-left: 150px;
					}
					.right-comment {
					color: gray;
					display: block;
					padding-left: 150px;
					}
					.variable {

					}
					.routing {
					font-weight: bold;
					display: block;
					}
					.not-supported {
					color: red;
					}
					.disabled {
					font-weight: bold;
					color: gray;
					}					
				</style>
			</head>
			<body>
				<h2 class="blockHeader">
					<xsl:text>Policy</xsl:text>
				</h2>
				<ul>
					<xsl:apply-templates />
				</ul>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="wsp:All">
		<li>
			<xsl:call-template name="disabled" />		
			<span class="logical-and">
				<xsl:text>All assertions should be true</xsl:text>
			</span>
			<ul>
				<xsl:apply-templates />
			</ul>
		</li>
	</xsl:template>
	<xsl:template match="wsp:OneOrMore">
		<li>
			<xsl:call-template name="disabled" />
			<span class="logical-or">
				<xsl:text>At least one assertion should be true</xsl:text>
			</span>
			<ul>
				<xsl:apply-templates />
			</ul>
		</li>
	</xsl:template>
	<xsl:template match="L7p:CommentAssertion">
		<li>
			<span class="comment">
				<xsl:text>Comment: </xsl:text>
				<xsl:value-of select="./L7p:Comment/@stringValue" />
			</span>
		</li>
	</xsl:template>
	<xsl:template match="L7p:SetVariable">
		<li>
			<xsl:call-template name="leftComment" />
			<xsl:call-template name="disabled" />			
			<span class="variable">
				<xsl:value-of select="./L7p:VariableToSet/@stringValue" />
				<xsl:text>=</xsl:text>
				<xsl:value-of
					select="tw:base64Extension(./L7p:Base64Expression/@stringValue)" />
			</span>
			<xsl:call-template name="rightComment" />
		</li>
	</xsl:template>
	<xsl:template match="L7p:HttpRoutingAssertion">
		<li>
			<xsl:call-template name="leftComment" />
			<xsl:call-template name="disabled" />			
			<span class="routing">
				<xsl:text>HTTP routing: </xsl:text>
			</span>
			<xsl:call-template name="rightComment" />
			<ul>
				<xsl:apply-templates />
			</ul>
		</li>
	</xsl:template>
	<xsl:template match="L7p:ProtectedServiceUrl">
		<li>
				<xsl:text>Endpoint: </xsl:text> <xsl:value-of select="./@stringValue" />
		</li>
	</xsl:template>	
	<xsl:template match="L7p:Include">
		<li>
			<xsl:call-template name="leftComment" />
			<xsl:call-template name="disabled" />
			<span class="include">
				<xsl:text>Policy include uuid: </xsl:text>
				<xsl:value-of select="./L7p:PolicyGuid/@stringValue" />
			</span>
			<xsl:call-template name="rightComment" />
		</li>
	</xsl:template>
	<xsl:template match="L7p:RemoteIpAddressRange">
		<li>
			<xsl:call-template name="leftComment" />
			<xsl:call-template name="disabled" />
			<span class="iprange">
				
				<xsl:choose>
					<xsl:when test="./L7p:AllowRange/@booleanValue='false'">
						<xsl:text>Forbid access to IP Address Range: </xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>Allow access to IP Address Range: </xsl:text>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:value-of select="./L7p:StartIp/@stringValue" />
			</span>
			<xsl:call-template name="rightComment" />
		
		</li>
	</xsl:template>
	<xsl:template match="L7p:assertionComment">
		<li>
			<xsl:if
				test="child::node()/L7p:Properties/L7p:entry/L7p:key/@stringValue='LEFT.COMMENT'">
				<span class="left-comment">
					<xsl:value-of
								select="child::node()/L7p:Properties/L7p:entry/L7p:value" />
				</span>
			</xsl:if>
		</li>
	</xsl:template>	
	<xsl:template name="leftComment">
		<xsl:if
			test="child::node()/L7p:Properties/L7p:entry/L7p:key/@stringValue='LEFT.COMMENT'">
			<span class="left-comment">
				<xsl:choose>
					<xsl:when
						test="child::node()/L7p:Properties/L7p:entry[./L7p:key/@stringValue='LEFT.COMMENT']/L7p:value/@stringValue">
						<xsl:value-of
							select="child::node()/L7p:Properties/L7p:entry[./L7p:key/@stringValue='LEFT.COMMENT']/L7p:value/@stringValue" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of
							select="child::node()/L7p:Properties/L7p:entry[./L7p:key/@stringValue='LEFT.COMMENT']/L7p:value" />
					</xsl:otherwise>
				</xsl:choose>
			</span>
		</xsl:if>
	</xsl:template>
	<xsl:template name="rightComment">
		<xsl:if
			test="child::node()/L7p:Properties/L7p:entry/L7p:key/@stringValue='RIGHT.COMMENT'">
			<span class="right-comment">
				<xsl:choose>
					<xsl:when
						test="child::node()/L7p:Properties/L7p:entry[./L7p:key/@stringValue='RIGHT.COMMENT']/L7p:value/@stringValue">
						<xsl:value-of
							select="child::node()/L7p:Properties/L7p:entry[./L7p:key/@stringValue='RIGHT.COMMENT']/L7p:value/@stringValue" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of
							select="child::node()/L7p:Properties/L7p:entry[./L7p:key/@stringValue='RIGHT.COMMENT']/L7p:value" />
					</xsl:otherwise>
				</xsl:choose>
			</span>
		</xsl:if>
	</xsl:template>
	<xsl:template name="disabled">
		<xsl:if
			test="./@L7p:Enabled='false' or ./L7p:Enabled/@booleanValue='false'">
			<span class="disabled"><xsl:text>DISABLED: </xsl:text></span>
		</xsl:if>
	</xsl:template>	
	<xsl:template match="L7p:CurrentSecurityHeaderHandling">
		<li><xsl:choose>
			<xsl:when test="./@intValue = 3"><xsl:text>Don't modify the request Security header</xsl:text></xsl:when>
			<xsl:otherwise><xsl:text>Remove processed Seurity header from request before routing</xsl:text></xsl:otherwise>
		</xsl:choose>
		</li>
	</xsl:template>	
	<xsl:template match="@L7p:Enabled">
	</xsl:template>		
	<xsl:template match="*">
		<li>
			<span class="not-supported">
				NOT SUPPORTED:
				<xsl:value-of select="name(.)" />
			</span>
		</li>
	</xsl:template>
</xsl:stylesheet>
