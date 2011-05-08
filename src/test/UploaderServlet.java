package test;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.MultipartConfig;

/**
 * Servlet implementation class UploaderServlet
 */
@MultipartConfig(location="c:\\tmp_labs") //, fileSizeThreshold=1024*1024*10, maxFileSize=1024*1024*100, maxRequestSize=1024*1024*5*100)
public class UploaderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String UPLOADED_FILES_KEY = "uploaded_files"; // TODO:
																		// move
																		// to
																		// project-wise
																		// constants
	private File uploadDir; 

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		// Read the uploadDir from the servlet parameters
		String uploadDirName = config.getInitParameter("uploadDir");
		
		if (uploadDirName == null) {
			throw new ServletException("Please supply uploadDir parameter");
		}
		
		log("uploadDir: " + uploadDirName);
		
		uploadDir = new File(uploadDirName);
		if (!uploadDir.isDirectory()) {
			throw new ServletException("Supplied uploadDir " + uploadDirName
					+ " is invalid (not a directory)");
		}
	}
	
	//@SuppressWarnings("unused")
	// TODO: I do check it! what's the better code to get rid of the warning?
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Collection<Part> parts = request.getParts();
		int partsCount = parts.size();
 
		log("content-length: " + request.getContentLength());	
		int partNumber = 0;

		for (Part part : parts) {
			++partNumber;
			log("processing part N" + partNumber + " of " + partsCount);
			
			String filename = getFilename(part);
			String upload_id = null;

			if (filename == null) {
				// Process regular form field (input
				// type="text|radio|checkbox|etc", select, etc).
				String fieldname = part.getName();
				String fieldvalue = getValue(part);
				
				log("processed input field: " + fieldname + " -> " + fieldvalue);

				if (fieldname.equals("upload_id")) {
					upload_id = fieldvalue;
				}
			} else if (!filename.isEmpty()) {
				// Process form file field (input type="file").
				String fieldname = part.getName();

				filename = filename.substring(filename.lastIndexOf('/') + 1)
						.substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
				
				log("processing file: " + filename + ", fieldname=" + fieldname);

				// TODO: validate that upload_id is initialized and handle the
				// internal error properly
				handleFileName(request, upload_id, filename, partsCount, part);
			}
		}

		// ...
	}

	@SuppressWarnings("unchecked")
	// TODO: I do check it! what's the better code to get rid of the warning?
	private void handleFileName(HttpServletRequest request, String upload_id,
			String filename, int partsCount, Part part) {
		// RESET HTTP SESSION! REMOVE THE FILE DESCRIPTOR FROM IT! 
		
		assert (upload_id != null);

		HttpSession session = request.getSession();

		// fetch the the uploaded files map or create it for the first time
		Object uploaded_files_obj = session.getAttribute(UPLOADED_FILES_KEY);
		Map<String, UploadDescriptor> uploaded_files;

		if (uploaded_files_obj == null) {
			uploaded_files = Collections
					.synchronizedMap(new HashMap<String, UploadDescriptor>()); // no
																				// simultaneous
																				// access
			session.setAttribute(UPLOADED_FILES_KEY, uploaded_files);
			log("uploaded files map created");
		} else {
			assert (uploaded_files_obj instanceof Map<?, ?>); // TODO: perform a
																// better check
																// and notify
			uploaded_files = (Map<String, UploadDescriptor>) uploaded_files_obj; // upload_id
																					// ->
																					// file
																					// descriptor
		}

		FileOutputStream out = null;
		UploadDescriptor upload_file_descriptor = null;
		try {
			File upload_file;
			if (!uploaded_files.containsKey(upload_id)) {

				// agile: single file per upload id - easy! :)
				// create the file if new and register into the map
				upload_file = File.createTempFile(filename, "tmp", uploadDir);
				upload_file_descriptor = new UploadDescriptor(
						upload_file.getAbsolutePath(), partsCount);
				uploaded_files.put(upload_id, upload_file_descriptor);
				log("file descriptor created: " + upload_file_descriptor.toJsonString());
			} else {
				upload_file_descriptor = uploaded_files.get(upload_id);
				assert (upload_file_descriptor != null);
				upload_file = new File(upload_file_descriptor.getAbsolutePath());
				log("file descriptor found: " + upload_file_descriptor.toJsonString());
			}

			out = new FileOutputStream(upload_file, true); // append
			InputStream filecontent = part.getInputStream();

			// append the part to the file, while updating the progress
			byte[] buffer = new byte[1024];
			int len = filecontent.read(buffer);
			while (len != -1) {
				upload_file_descriptor.increaseBytesSoFar(len);
				out.write(buffer, 0, len);
				len = filecontent.read(buffer);
			}
			upload_file_descriptor.incrementPartsSoFar();
			log("file descriptor updated: " + upload_file_descriptor.toJsonString());
		} catch (IOException e) {
			log("Error while processing file upload: " + e.getMessage());
			if (upload_file_descriptor != null) {
				upload_file_descriptor.setErrorStatus();
			}
			// TODO: internal error, notify and handle
			// TODO: what about NetworkExceptions (client disconnect etc)?
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close(); // TODO: does 'close()' imply 'flush()'?
				}
			} catch (IOException ignored) {
				// TODO: issue some log message as there is nothing else to do
			}
		}
	}

	private static String getFilename(Part part) {
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				return cd.substring(cd.indexOf('=') + 1).trim()
						.replace("\"", "");
			}
		}
		return null;
	}

	private static String getValue(Part part) throws IOException {
		String value = new BufferedReader(new InputStreamReader(
				part.getInputStream(), "UTF-8")).readLine();
		return (value != null) ? value : ""; // Must be empty String according
												// to HTTP specification.
	}
}
