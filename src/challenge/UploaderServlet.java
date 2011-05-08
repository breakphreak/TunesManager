package challenge;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.MultipartConfig;

/**
 * Processes the form, uploads the file and puts all the relevant upload-related info into the http session.
 */
@MultipartConfig(location = "c:\\tmp_labs")
// , fileSizeThreshold=1024*1024*10, maxFileSize=1024*1024*100,
// maxRequestSize=1024*1024*5*100)
public class UploaderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
			throw new ServletException("Supplied uploadDir " + uploadDirName + " is invalid (not a directory)");
		}
	}

	// @SuppressWarnings("unused")
	// TODO: I do check it! what's the better code to get rid of the warning?
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Set<String> upload_filenames = new HashSet<String>(); // to be used later to validate that only single file is being submitted
		HttpSession session = request.getSession();
		
		// first, replace the upload descriptor - currently there can be only one file at a time, any previous info is irrelevant
		UploadDescriptor uploadDescriptor = new UploadDescriptor();
		session.setAttribute(Constants.UPLOAD_DESCRIPTOR_KEY, uploadDescriptor);
		
		uploadDescriptor.setTotalBytes(request.getContentLength()); // form size = file size + other input fields size

		Collection<Part> parts = request.getParts();
		uploadDescriptor.setTotalParts(parts.size());

		for (Part part : parts) {
			log("processing part N" + (uploadDescriptor.getPartsSoFar()+1) + " of " + uploadDescriptor.getTotalParts());

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
				handleFileName(filename, part, uploadDescriptor);
			} else {
				log("filename was empty, will do nothing");
			}
			uploadDescriptor.incrementPartsSoFar();
			log("upload descriptor: " + uploadDescriptor.toJsonString());
		}
	}

	private void handleFileName(String filename, Part part, UploadDescriptor uploadDescriptor) throws ServletException {
		// fetch the the uploaded files map or create it for the first time
		FileOutputStream out = null;
		
		try {
			File uploadFile;
			if (uploadDescriptor.getAbsolutePath() == null) {
				// new file (first time): register the upload descriptor into an http session
				
				uploadFile = File.createTempFile(filename, "tmp", uploadDir); // TODO: keep the original file extension
				uploadDescriptor.setAbsolutePath(uploadFile.getAbsolutePath());
				
				log("upload descriptor created: " + uploadDescriptor.toJsonString());
			} else {
				// known file (subsequent times): fetch the upload descriptor
								
				uploadFile = new File(uploadDescriptor.getAbsolutePath());
				
				log("file descriptor found: " + uploadDescriptor.toJsonString());
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
		} catch (IOException e) {
			log("Error while processing file upload: " + e.getMessage());
			if (uploadDescriptor != null) {
				uploadDescriptor.setErrorStatus();
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
