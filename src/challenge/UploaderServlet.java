package challenge;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Processes the form, uploads the file and puts all the relevant upload-related info into the http session.
 */
public class UploaderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private File uploadDir;
	private String applicationPathPrefix;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		applicationPathPrefix = config.getServletContext().getContextPath();
		
		// Read the uploadDir from the servlet parameters
		String uploadDirName = config.getServletContext().getInitParameter(Constants.UPLOAD_DIR);

		if (uploadDirName == null) {
			throw new ServletException("Please supply uploadDir parameter");
		}

		log("uploadDir: " + uploadDirName);

		uploadDir = new File(uploadDirName);
		if (!uploadDir.exists()) {
			try {
				uploadDir.createNewFile();
			} catch (IOException e) {
				throw new ServletException("Failed to create upload dir", e);
			}
		} else {
			if (!uploadDir.isDirectory()) {
				throw new ServletException("Supplied uploadDir " + uploadDirName + " is invalid (not a directory)");
			}
		}
		log("uploading to: " + uploadDir.getAbsolutePath());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//response.setContentType("application/json");
		
		Set<String> upload_filenames = new HashSet<String>(); // to be used later to validate that only single file is being submitted
		HttpSession session = request.getSession(false); // the session should be pre-created by JSP
		
		if (session == null) {
			throw new ServletException("Illegal application state: no session had been opened yet");
		}
		
		UploadDescriptor uploadDescriptor = SessionResourceManager.getUploadDescriptor(request); // will throw exception if upload descriptor is not in the session
		
		try {
			uploadDescriptor.setTotalBytes(request.getContentLength()); // form size = file size + other input fields size	
			Collection<Part> parts = request.getParts();
				
			for (Part part : parts) {
				String filename = getFilename(part);
	
				if (filename == null) {
					// Process regular form field (input
					// type="text|radio|checkbox|etc", select, etc).
					String fieldname = part.getName();
					String fieldvalue = getValue(part);
	
					log("processed input field: " + fieldname + " -> " + fieldvalue); // actually, no fields are expected
				} else if (!filename.isEmpty()) {
					// Process form file field (input type="file").
					String fieldname = part.getName();
	
					filename = filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
					upload_filenames.add(filename);
					if (upload_filenames.size() > 1) {
						throw new ServletException("Illegal input: currently only one file at a time can be uploaded");
					}
	
					log("processing file: " + filename + ", fieldname=" + fieldname);
					handleFilePart(filename, part, uploadDescriptor);
				} else {
					log("filename was empty, will do nothing");
				}
			}

			uploadDescriptor.setDoneStatus();
			log("file uploaded completely");
		}
		catch (IOException e) {
			uploadDescriptor.setErrorStatus();
			log("Error while processing file upload: " + e.getMessage());
		} finally {
			PrintWriter pw = response.getWriter();
			pw.println(uploadDescriptor.toJsonString());			
			log("upload descriptor: " + uploadDescriptor.toJsonString());
		}
	}
		
	private void handleFilePart(String filename, Part part, UploadDescriptor uploadDescriptor) throws ServletException, IOException {
		// fetch the the uploaded files map or create it for the first time
		FileOutputStream out = null;
		
		try {
			File uploadFile;

			if (uploadDescriptor.getRetrieveUrl() == null) {
				// new file (first time): register the upload descriptor into an http session
				
				// keep the extension of the original file
				
				String name, extension;
				int delimiter = filename.lastIndexOf('.');
				
				if (delimiter == -1) {
					name = filename + "_";
					extension = "";
				} else {
					name = filename.substring(0, delimiter-1) + "_";
					extension = "." + filename.substring(delimiter+1);				
				}
				
				uploadFile = File.createTempFile(name, extension, uploadDir); // a random number will be appended to the file name, original extension will be kept
				uploadDescriptor.setRetrieveUrl(applicationPathPrefix + "/RetrieverServlet?filename=" + uploadFile.getName());
			} else {
				// known file (subsequent times): fetch the upload descriptor								
				uploadFile = new File(uploadDescriptor.getRetrieveUrl());
			}

			out = new FileOutputStream(uploadFile, true); // open stream for append
			InputStream filecontent = part.getInputStream();

			// append the part to the file, while updating the progress
			byte[] buffer = new byte[1024];
			int len = filecontent.read(buffer);
			while (len != -1) {
				uploadDescriptor.increaseBytesSoFar(len);
				out.write(buffer, 0, len);
				len = filecontent.read(buffer);
			}
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				log("Internal error: excepton while cleanup: " + e.getMessage());
			}
		}
	}

	private static String getFilename(Part part) {
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}

	private static String getValue(Part part) throws IOException {
		String value = new BufferedReader(new InputStreamReader(part.getInputStream(), "UTF-8")).readLine();
		return (value != null) ? value : ""; // Must be empty String according
												// to HTTP specification.
	}
}
