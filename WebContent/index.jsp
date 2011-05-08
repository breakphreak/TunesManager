<?xml version="1.0" encoding="ISO-8859-1" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0">
    <jsp:directive.page language="java"
        contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" />
    <jsp:text>
        <![CDATA[ <?xml version="1.0" encoding="ISO-8859-1" ?> ]]>
    </jsp:text>
    <jsp:text>
        <![CDATA[ <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
    </jsp:text>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Tunes Uploader</title>

<script type="text/javascript"> 
	const UPLOAD_ID = <jsp:expression>(int) (Math.random() * 10000)</jsp:expression>;

	var http = createRequestObject();
	function createRequestObject() {
		var objAjax;
		var browser = navigator.appName;
		if (browser == "Microsoft Internet Explorer") {
			objAjax = new ActiveXObject("Microsoft.XMLHTTP");
		} else {
			objAjax = new XMLHttpRequest();
		}
		return objAjax;
	}
	
    function display_progress() {
		if (http.readyState == 4) {
    		var resp = eval('(' + http.responseText + ')');
    		document.getElementById('progress_bar').innerHTML = "Status: " + resp.progress + "%";
    	/*
		var resp = eval('(' + o.responseText + ')'); 
		if(!resp['done']) {   
			var pct = parseInt(100*(resp['current']/resp['total'])); 
			document.getElementById('pbar').style.width = ''+pct+'%'; 
			document.getElementById('ppct').innerHTML = " "+pct+"%"; 
			setTimeout("update_progress()",500); 
		} else if(resp['cancel_upload']) { 
			txt="Cancelled after "+resp['current']+" bytes!";  
			document.getElementById('ptxt').innerHTML = txt; 
			setTimeout("progress_win.hide(); window.location.reload();",2000); 
			window.location = "<?php echo $redirect; ?>";
		} 
		*/
			setTimeout("update_progress()",500);
		}
	}  
    
    function update_progress() {  
    	var request = 'FileStateServlet?upload_id='+UPLOAD_ID;
    	console.debug('requesting: ' + request);
		http.open('get', request);
		http.onreadystatechange = display_progress;
		http.send(null);
		return false; 
   	} 
    
    function post_form() {
    	document.getElementById('upload_id').value = UPLOAD_ID; // TODO: move it to the page init?
    	update_progress();
    }
</script>

</head>
<body>

<form enctype="multipart/form-data" id="upload_form" name="upload_form" method="POST" action="UploaderServlet" onsubmit="post_form(); return false;" target="upload_target">
<input name="file" size="27" type="file" id="file" /> <br/> 
Choose a file <br />  
<input type="submit" name="actionButton" value="Upload" /><br />
<input type="hidden" name="upload_id" id="upload_id" value="VALUE_TO_BE_REPLACED_BY_JAVASCRIPT"/> 
<br />  
</form> 

<div id="progress_bar">
Please choose a file to upload 
</div> 

<iframe id="upload_target" name="upload_target" src="#" style="width:0;height:0;border:0px solid #fff;"></iframe>
</body>
</html>
</jsp:root>