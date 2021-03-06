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
		response.setContentType("application/json");

		HttpSession session = request.getSession(false); // don't create session if it wasn't created before
		
		if (session == null) {
			throw new ServletException("Illegal application state: no session had been opened yet");
		}

		UploadDescriptor upload_descriptor = SessionResourceManager.getUploadDescriptor(request);
				
		PrintWriter pw = response.getWriter();
		pw.println(upload_descriptor.toJsonString());
	}
}
