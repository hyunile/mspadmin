<%@ page contentType="text/html; charset=utf-8" isErrorPage="true"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%
	String rootPath = request.getContextPath();
	String errorCode = "500"; 
	if(request.getParameter("ERROR") != null){
    	errorCode = request.getParameter("ERROR");
	}
	
	String errorMsg = "";
	if(request.getAttribute("ERROR_MSG") != null){
    	errorMsg = (String)request.getAttribute("ERROR_MSG");
	}
	
	String LOCALE = pageContext.getResponse().getLocale().getLanguage();
	if(request.getAttribute("LOCALE") != null){
		LOCALE = (String)request.getAttribute("LOCALE");
	}

	response.setStatus(200);
	
%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta http-equiv="content-type" content="applicaton/xhtml+xml;charset=utf-8" />
<title><spring:message code="header.title"/></title>
<link rel="shortcut icon" href="<%=rootPath%>/resources/img/favicon_morphues.png" />
<link rel="stylesheet" href="<%=rootPath%>/resources/css/morpheus_common.css">
<link rel="stylesheet" href="<%=rootPath%>/resources/css/morpheus_style.css">
<script type="text/javascript">
if("<%= errorMsg %>" != ""){
	alert("<%= errorMsg %>");
}
</script>
</head>

<body style="background-image:none;background:none;">
<div id="header">
	<div class="header">
		<h1 style="padding-top:30px"><a href="<%=rootPath%>/"><img src="<%=rootPath%>/resources/images/common/logo_<spring:message code="login.logo.image"/>.png" alt="<spring:message code="header.title"/>" /></a></h1>
	</div>
</div>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td height="100"></td>
	</tr>
  <tr>
    <td align="center">
    	<table width="500" border="0" cellspacing="0" cellpadding="0" style="background-image: url('<%=rootPath%>/resources/images/errorText_<%=LOCALE %>.gif'); background-repeat: no-repeat">
      		<tr>
        		<td height="200" align="center" valign="top">
		        	<table width="400" border="0" cellspacing="0" cellpadding="0">
		          		<tr>
		            		<td height="160">&nbsp;</td>
		          		</tr>
		          		<tr>
		            		<td align="center"><spring:message code="menu.push.sendManage.alert.processError"/>! 
                            </td>
		          		</tr>
		          		<tr>
		            		<td align="center" height="14"></td>
		          		</tr>
		          		
		        	</table>
        		</td>
      		</tr>
    	</table>
    </td> 
  </tr>
  <tr>
	<td align="center">
		<a href="javascript:history.go(-1)" class="btn1" ><spring:message code="common.button.goBack"/></a> 
		<a href="<%=rootPath%>/" class="btn1" ><spring:message code="common.button.goMain"/></a> 
	</td> 
  </tr>
</table>
<div id="footer" style="position: absolute; bottom: 0px;width:100%;">
	<div class="footer">Copyright <spring:message code="login.bottom.corpname"/> All right reserved</div>
</div>
</body>
</html>
