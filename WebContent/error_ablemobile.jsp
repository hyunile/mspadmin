<%@ page contentType="text/html; charset=utf-8" isErrorPage="true"%>
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
</head>
<body style="background-image:none;background:none;">
오류가 발생하였습니다.
</body>
</html>
