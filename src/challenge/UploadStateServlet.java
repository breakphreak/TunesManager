package challenge;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Peeps into http session to fetch an info regarding the file being uploaded.
 */
public class UploadStateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UploadStateServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//response.setContentType("application/json");

		HttpSession session = request.getSession(false); // don't create session if wasn't created before
		UploadDescriptor upload_descriptor = UploadDescriptor.UNKNOWN;

		if (session != null) {
			Object upload_descriptor_obj = session.getAttribute(Constants.UPLOAD_DESCRIPTOR_KEY);

			if (upload_descriptor_obj != null) {
				if (!(upload_descriptor_obj instanceof UploadDescriptor)) {
					throw new ServletException("Illegal input: wrong object type had been found in session under \"" + Constants.UPLOAD_DESCRIPTOR_KEY + "\" key");
				}
				
				upload_descriptor = (UploadDescriptor)upload_descriptor_obj;
			}
		}

		PrintWriter pw = response.getWriter();
		pw.println(upload_descriptor.toJsonString());
	}
}