package challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class FileSizeServlet
 */
public class FileStateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String UPLOAD_ID = "upload_id";
	private static final String UPLOADED_FILES_KEY = "uploaded_files"; // TODO:
																		// move
																		// to
																		// project-wise
																		// constants

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FileStateServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	// I do check those!
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		String uploadId = request.getParameter(UPLOAD_ID);

		HttpSession session = request.getSession(false); // don't create session
															// if wasn't created
															// before
		UploadDescriptor file_descriptor = UploadDescriptor.UNKNOWN;

		if (session != null) {
			Object uploaded_files_obj = session.getAttribute(UPLOADED_FILES_KEY);
			Map<String, UploadDescriptor> uploaded_files;
	
			if (uploaded_files_obj != null) {
				assert (uploaded_files_obj instanceof Map<?, ?>); // TODO: perform a
																	// better check
																	// and notify
				uploaded_files = (Map<String, UploadDescriptor>) uploaded_files_obj; 
	
				Object file_descriptor_obj = uploaded_files.get(uploadId);
				if (file_descriptor_obj != null) {
					assert (file_descriptor_obj instanceof UploadDescriptor);
					file_descriptor = (UploadDescriptor) file_descriptor_obj;
				}
			}
		}
	
		PrintWriter pw = response.getWriter();
		pw.println(file_descriptor.toJsonString() /* session.getAttribute(UPLOADED_FILES_KEY)*/);
	}
}
