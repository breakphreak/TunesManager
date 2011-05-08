<?xml version="1.0" encoding="ISO-8859-1" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0">
    <jsp:directive.page language="java"
        contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" />
    <jsp:directive.page isErrorPage="true" import="java.io.*"/>
    <jsp:text>
        <![CDATA[ <?xml version="1.0" encoding="ISO-8859-1" ?> ]]>
    </jsp:text>
    <jsp:text>
        <![CDATA[ <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
    </jsp:text>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Application error</title>
</head>
<body>
When it rains it pours :)<br/></br/>
Got exception: <jsp:expression>exception.getMessage()</jsp:expression>.<br/>
The detailed stack trace is: 

<jsp:scriptlet>
	Writer stringWriter = new StringWriter();
	exception.printStackTrace(new PrintWriter(stringWriter));
	String stackTraceString = stringWriter.toString();
</jsp:scriptlet>

<pre>
<jsp:expression>stackTraceString</jsp:expression>
</pre><br/>
</body>
</html>
</jsp:root>