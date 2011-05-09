<?xml version="1.0" encoding="ISO-8859-1" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0">
    <jsp:directive.page language="java" session="true"
        contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" />
    <jsp:directive.page import="challenge.*"/>
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
			if (http.status != 200) {
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
		http.open('post', request);
		http.onreadystatechange = display_progress;
		http.send(null);
		return false; 
   	} 
    
    function upload_file() {
    	var request = 'UploaderServlet';
		http.open('post', request);
		http.onreadystatechange = display_progress;
		http.send(null);
    	update_progress();
    }
    
    function set_usercomment() {
    	var request = 'UsercommentServlet';
		http.open('post', request);
		http.onreadystatechange = display_comment;
		http.send(null);
		return false; 
    }
    
    function display_comment() {
		if (http.readyState == 4) {
			if (http.status != 200) {
				document.getElementById('comment_bar').innerHTML = "Wrong response status received: " + http.status + "! Fix the server-side code.";
			} else {
				var resp = eval('(' + http.responseText + ')');
				var comment = resp['userComment'];
				
				if (comment == undefined) {
					document.getElementById('comment_bar').innerHTML = "User comment not set (probably supplied empty)";
				} else {
					document.getElementById('comment_bar').innerHTML = "User comment set";	
				}
			}
		}
	}  
</script>

</head>
<body>

<jsp:expression>session.getId()</jsp:expression>
<jsp:scriptlet>
// first, replace the upload descriptor - currently there can be only one file at a time, any previous info is irrelevant
SessionResourceManager.createUploadDescriptor(request);
</jsp:scriptlet>

<form enctype="multipart/form-data" id="upload_form" name="upload_form" method="POST" action="UploaderServlet" onsubmit="upload_file(); return false;" target="upload_target">
<input name="file" size="27" type="file" id="file" /> <br/> 
Choose a file <br />  
<input type="submit" name="uploadSubmitButton" value="Upload" /><br /> 
<br />  
</form> 

<form enctype="text/plain" id="usercomment_form" method="POST" action="UsercommentServlet" onsubmit="set_usercomment(); return false;" target="usercomment_target">
Type your comment here:<br/>
<input name="usercomment" type="text" size="50"/>
<input type="submit" name="usercommentSubmitButton" value="Set Comment" /><br />
<br/>
</form>

<div id="progress_bar">
Please choose a file to upload and press "Upload".
</div> <br/>

<div id="comment_bar">
The track description is empty.
</div> <br/>


<iframe id="upload_target" name="upload_target" src="#" style="width:0;height:0;border:0px solid #fff;"></iframe>
<iframe id="usercomment_target" name="usercomment_target" src="#" style="width:0;height:0;border:0px solid #fff;"></iframe>
</body>
</html>
</jsp:root>