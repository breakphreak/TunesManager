<?xml version="1.0" encoding="ISO-8859-1" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0">
    <jsp:directive.page language="java" session="true"
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
	var http = createRequestObject();
	function createRequestObject() {
		var objAjax;
		if (window.XMLHttpRequest)
		{// code for IE7+, Firefox, Chrome, Opera, Safari
			objAjax=new XMLHttpRequest();
		}
		else
		{// code for IE6, IE5
			objAjax=new ActiveXObject("Microsoft.XMLHTTP");
		}
		return objAjax;
	}
	
    function display_progress() {
		if (http.readyState == 4) {
			if (http.status !=200) {
				document.getElementById('progress_bar').innerHTML = "Wrong response status received: " + http.status + "! Fix the server-side code.";
				setTimeout("update_progress()", 500);
			} else {
				try {
		    		var resp = eval('(' + http.responseText + ')');				
		    		var status = resp['status'];
		    		
		    		if (status == 'UNKNOWN' || status == 'DOING') {
		        		document.getElementById('progress_bar').innerHTML = "Uploaded: " + resp['progress'] + "%";    			
		    		} else if (status == 'DONE'){
		        		document.getElementById('progress_bar').innerHTML = "Uploaded 100% (" + resp['totalBytes'] + " bytes)! Your file is here: " + resp['url'];
		    		} else if (status == 'ERROR') {
		    			document.getElementById('progress_bar').innerHTML = "Error while uploading!";
		    		} else {
		    			document.getElementById('progress_bar').innerHTML = "Unexpected state: " + status + "! Fix the server-side code.";
		    		}
				} catch (ex) {
					document.getElementById('progress_bar').innerHTML = "Wrong response received: " + resp + "! Fix the server-side code.";
				}
	    			
				setTimeout("update_progress()", 500);
			}
		}
	}  
    
    function update_progress() {  
    	var request = 'UploadStateServlet';
    	// console.debug('requesting: ' + request); // doesn't work on IE
		http.open('get', request);
		http.onreadystatechange = display_progress;
		http.send(null);
		return false; 
   	} 
    
    function post_form() {
    	update_progress();
    }
</script>

</head>
<body>

<jsp:expression>session.getId()</jsp:expression>

<form enctype="multipart/form-data" id="upload_form" name="upload_form" method="POST" action="UploaderServlet" onsubmit="post_form(); return false;" target="upload_target">
<input name="file" size="27" type="file" id="file" /> <br/> 
Choose a file <br />  
<input type="submit" name="uploadSubmitButton" value="Upload" /><br /> 
<br />  
</form> 

<form enctype="text/plain" id="usercomment_form" method="POST" action="UsercommentServlet" onsubmit="return false;" target="usercomment_target">
Type your comment here:<br/>
<input name="usercomment" type="text" size="50"/>
<input type="submit" name="usercommentSubmitButton" value="Set Comment" /><br />
<br/>
</form>

<div id="progress_bar">
Please choose a file to upload and press "Upload".
</div> 

<iframe id="upload_target" name="upload_target" src="#" style="width:0;height:0;border:0px solid #fff;"></iframe>
<iframe id="usercomment_target" name="usercomment_target" src="#" style="width:0;height:0;border:0px solid #fff;"></iframe>
</body>
</html>
</jsp:root>