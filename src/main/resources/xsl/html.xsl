<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:wsg="http://schemas.denhaag.nl/tw/layer7" xmlns:fn="http://www.w3.org/2005/xpath-functions" >
	<xsl:param name="dienst"></xsl:param>
	<xsl:output indent="yes" method="html"  />
	<xsl:template match="/">
		<xsl:variable name="up" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
		<xsl:variable name="lo" select="'abcdefghijklmnopqrstuvwxyz'" />
		<html>
			<head>
				<style type="text/css">
body {
	font-family: verdana, geneva, lucida, 'lucida grande', arial, helvetica,
		sans-serif;
	font-size: 12px;
}

table {
	border-collapse: collapse;
	font-size: 12px;
	margin-top:10px;
}
h1 {
	background-color: #F0F0F0;
	border: 1px solid #CCCCCC;
	font-weight: bold;
	outline: 0 none;
	padding: 3px 10px;
	margin: 10px 0px;
	text-decoration: none;
}
h2 {
	background-color: #F0F0F0;
	border: 1px solid #CCCCCC;
	font-weight: bold;
	outline: 0 none;
	padding: 3px 10px;
	margin: 10px 0px;
	text-decoration: none;
}
.version {
	font-size: 10px;
	color: #CCCCCC;
}
.root {
	font-weight: bold;
}
table, th,td {
	border: 1px solid #CCCCCC;
	padding:10px;
}	
th {
	background-color: #F0F0F0;
	text-align: left;
}  
</style>
			</head>
			<body>
				<h1>Index of <xsl:value-of select="/wsg:service/wsg:name" /></h1>
				<div class="content">
					<div class="version"> Generator <xsl:value-of select="/wsg:service/@generatorVersion" />, Generatie datum: <xsl:value-of select="/wsg:service/@generationDate"></xsl:value-of></div>
					<h2>Global information</h2>
					<div class="content">
						<table>
							
								<tr><th>ID:</th><td><xsl:value-of select="/wsg:service/wsg:id" /></td></tr>
								<tr><th>Service:</th><td><xsl:value-of select="/wsg:service/wsg:name" /></td></tr>
								<tr><th>Enabled:</th><td><xsl:value-of select="/wsg:service/wsg:enabled" /></td></tr>
								<tr><th>Policy manager path(folder):</th><td><xsl:value-of select="/wsg:service/wsg:policyManagerPath" /></td></tr>
								<tr><th>Resolution path:</th><td><xsl:value-of select="/wsg:service/wsg:resolutionPath" /></td></tr>
								<tr><th>Version:</th><td><xsl:value-of select="/wsg:service/wsg:version" /></td></tr>
								<tr><th>Policy version:</th><td><xsl:value-of select="/wsg:service/wsg:policyVersion" /></td></tr>
								<tr><th>WS-Security:</th><td><xsl:value-of select="/wsg:service/wsg:name" /></td></tr>		
								<tr><th>SOAP:</th><td><xsl:value-of select="/wsg:service/wsg:soap" /></td></tr>
								<xsl:if test="/wsg:service/wsg:soapVersion">
									<tr><th>SOAP version:</th><td><xsl:value-of select="/wsg:service/wsg:soapVersion" /></td></tr>
								</xsl:if>
								<tr><th>Internal:</th><td><xsl:value-of select="/wsg:service/wsg:internal" /></td></tr>
								<xsl:if test="/wsg:service/wsg:httpMethods">
								<tr><th>Allowed HTTP methods:</th><td>
								<xsl:for-each select="/wsg:service/wsg:httpMethods/wsg:httpMethod">
									<xsl:value-of select="./text()" /><xsl:text> </xsl:text>
								</xsl:for-each>
								</td>	
								</tr>	
								</xsl:if>																							
							
						</table>
			
					</div>
					<xsl:if test="/wsg:service/wsg:files">
					<h2>Files</h2>
					<div class="content">
						<table>
							<xsl:for-each select="/wsg:service/wsg:files/wsg:file">
							<xsl:variable name="filename" select="./text()"></xsl:variable>
								<xsl:choose>
									<xsl:when test="./@root">
										<tr class="root"><th><xsl:value-of select="./@type" /></th><td><a href="{$filename}"><xsl:value-of select="$filename" /></a></td></tr>
									</xsl:when>
									<xsl:otherwise>
										<tr><th><xsl:value-of select="./@type" /></th><td><a href="{$filename}"><xsl:value-of select="$filename" /></a></td></tr>
									</xsl:otherwise>
								</xsl:choose>
								
							</xsl:for-each>
																						
							
						</table>
			
					</div>
					</xsl:if>	
				</div>

			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>