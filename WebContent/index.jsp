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
	var XMLHTTPREQUEST_MS_PROGIDS = new Array(
		"Msxml2.XMLHTTP.7.0",
		"Msxml2.XMLHTTP.6.0",
		"Msxml2.XMLHTTP.5.0",
		"Msxml2.XMLHTTP.4.0",
		"MSXML2.XMLHTTP.3.0",
		"MSXML2.XMLHTTP",
		"Microsoft.XMLHTTP"
	);

	function createRequestObject() {
	  var httpRequest = null;

	  // Create the appropriate HttpRequest object for the browser.
	  if (window.XMLHttpRequest != null)
	    httpRequest = new window.XMLHttpRequest();
	  else if (window.ActiveXObject != null)
	  {
	    // Must be IE, find the right ActiveXObject.
	    var success = false;
	    for (var req in XMLHTTPREQUEST_MS_PROGIDS)
	    {
	      try
	      {
	        httpRequest = new ActiveXObject(req);
	        break;
	      }
	      catch (ex)
	      {}
	    }
	  }

	  // Display an error if we couldn't create one.
	  if (httpRequest == null) {
	    alert("Error in HttpRequest():\n\n" + "Cannot create an XMLHttpRequest object.");
	  }

	  // Return it.
	  return httpRequest;
	}
	
    function upload_file() {
    	// TODO: Prevent re-uploading the file without reloading the page (so that UploadDescriptor will be re-initialized).
    	// Can be followed in a better way, but good enough at this stage.
    	// document.getElementById('uploadSubmitButton').disabled = true;
    	// document.getElementById('file').disabled = true;
    	update_progress();
    }

    function update_progress() {
    	var http = createRequestObject();    	
    	var request = 'UploadStateServlet';
    	
		http.open('get', request);		
		http.onreadystatechange = function() { display_progress(http); };
		http.send(null);
		return false; 
   	} 
    	
    function display_progress(http) {
		if (http.readyState == 4) {
			var again = false;
			
			if (http.status != 200) {
				document.getElementById('progress_bar').innerHTML = "Wrong response status received: " + http.status + "! Fix the server-side code.";
			} else {
				try {
		    		var resp = eval('(' + http.responseText + ')');				
		    		var status = resp['status'];
		    		
		    		if (status == 'DOING') {
		        		document.getElementById('progress_bar').innerHTML = "Uploaded: " + resp['progress'] + "%";
		        		again = true;
		    		} else if (status == 'DONE'){
		        		document.getElementById('progress_bar').innerHTML = 
		        			"Uploaded 100% (" + resp['totalBytes'] + " bytes)! Your file is &lt;a href=\"" + resp['url'] + "\"/&gt;" + "here" + "&lt;/a&gt;";
		    		} else if (status == 'ERROR') {
		    			document.getElementById('progress_bar').innerHTML = "Error while uploading!";
		    		} else {
		    			document.getElementById('progress_bar').innerHTML = "Unexpected state: " + status + "! Fix the server-side code.";
		    		}
				} catch (ex) {
					document.getElementById('progress_bar').innerHTML = "Wrong response received: " + resp + "! Fix the server-side code.";
				}
			}
			
			if (again) {
				setTimeout("update_progress()", 500);
			}
		}
	}  
        
    function set_usercomment() {
    	var http = createRequestObject();   
     	data = "usercomment=" + document.getElementById('usercomment').value;
    	var request = 'UsercommentServlet?' + data;
    	
		http.open('GET', request);
		// cool lambda calculus trick: the callback should be with zero arguments, but 'http' must be shared between 'set_usercomment()' and a 'display_comment()' callback
		http.onreadystatechange = function() { display_comment(http); }; 
		http.send(data);
		return false; 
    }
    
    function display_comment(http) {
		if (http.readyState == 4) {
			if (http.status != 200) {
				document.getElementById('comment_bar').innerHTML = "Wrong response status received: " + http.status + "! Fix the server-side code.";
			} else {
				try {
					var resp = eval('(' + http.responseText + ')');
					var comment = resp['userComment'];
					
					if (comment == undefined) {
						document.getElementById('comment_bar').innerHTML = "User comment not set (probably supplied empty)";
					} else if (comment) {
						document.getElementById('comment_bar').innerHTML = "User comment set (" + comment + ")";	
					} else {
						document.getElementById('comment_bar').innerHTML = "User comment set as empty";
					}
				} catch (ex) {
					document.getElementById('progress_bar').innerHTML = "Wrong response received: " + http.responseText + "! Fix the server-side code.";
				}
			}
		}
	}  
</script>

</head>
<body>

<jsp:scriptlet>
// first, replace the upload descriptor - currently there can be only one file at a time, any previous info is irrelevant
SessionResourceManager.createUploadDescriptor(request);
</jsp:scriptlet>

<!--
The forms management is a bit tricky, so here are the few useful things I've learned along the way:
Let's start from the second (comment submission form) as it is more straightforward:
- the 'onsubmit()' handler returns false, meaning no need to execute an 'action'
- the AJAX request is sent anyways by 'set_usercomment()' as it is triggered by pressing on the 'submit' button
- the data is (supposed to be) relatively small and can be transferred as a part of a regular 'GET' request

The file upload form is more cumbersome and I am almost sure about better ways to implement the functionality,
however, my purpose was to learn as much as I can, hence not used any kind of client-side libraries. Anyways:
- the 'onsubmit()' handler returns true, so that 'action' is being executed.
- the 'action' calls UploaderServlet URL and it's being triggered by 'upload_file()' which is called when submitting a form.
- the  'upload_file()' utilizes 'upload_progress()' and 'display_progress()' to display the upload progress
- the upload progress comes from the server side (!), means that (delayed by network latency)
  the percentage starts to increase on the server side after the bytes had been arrived to server.
  Some browsers (such as Chrome) count bytes that were sent (!) to the server and for big files
  those two percentages (client-side vs. server-side) can differ dramatically, which is Ok :)  
 -->

<!-- File upload form -->
<form enctype="multipart/form-data" id="upload_form" name="upload_form" method="POST" action="UploaderServlet" onsubmit="upload_file(); return true;" target="upload_target">
Choose a file <br /> 
<input name="file" size="27" type="file" id="file" /> <br/>  
<input type="submit" name="uploadSubmitButton" id="uploadSubmitButton" value="Upload" /><br /> 
<br />  
</form> 

<!-- Comment submission form -->
<form enctype="text/plain" id="usercomment_form" method="GET" action="" onsubmit="set_usercomment(); return false;" target="usercomment_target">
Type your comment here:<br/>
<input name="usercomment" id="usercomment" type="text" size="50"/>
<input type="submit" name="usercommentSubmitButton" id="usercommentSubmitButton" value="Set Comment" /><br />
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