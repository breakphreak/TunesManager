package challenge;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This servlet will receive the user comment and insert it into the appropriate UploadDescriptor
 */
public class UsercommentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UsercommentServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false); // the session should be pre-created by JSP
		
		if (session == null) {
			throw new ServletException("Illegal application state: no session had been opened yet");
		}

		String userComment = request.getParameter("usercomment");
		if (userComment != null) {		
			Object upload_descriptor_obj = session.getAttribute(Constants.UPLOAD_DESCRIPTOR_KEY);
			UploadDescriptor upload_descriptor = UploadDescriptor.UNKNOWN;
			
			if (upload_descriptor_obj != null) {
				if (!(upload_descriptor_obj instanceof UploadDescriptor)) {
					throw new ServletException(
							"Illegal application state: wrong object type had been found in session under \"" + Constants.UPLOAD_DESCRIPTOR_KEY + "\" key: " +
							"Expected: " + UploadDescriptor.class.getName() + ", found: " + upload_descriptor_obj.getClass().getName() + ""
						);
				}
				
				upload_descriptor = (UploadDescriptor)upload_descriptor_obj;
				upload_descriptor.setUserComment(userComment);
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
