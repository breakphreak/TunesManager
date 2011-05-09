package challenge;

import java.io.IOException;
import java.io.PrintWriter;

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
		response.setContentType("application/json");
		
		HttpSession session = request.getSession(false); // the session should be pre-created by JSP
		
		if (session == null) {
			throw new ServletException("Illegal application state: no session had been opened yet");
		} 
		
		UploadDescriptor uploadDescriptor = SessionResourceManager.getUploadDescriptor(request);
		String userComment = (String)request.getParameter("usercomment");
		
		uploadDescriptor.setUserComment(userComment);
		
		PrintWriter pw = response.getWriter();
		pw.println(uploadDescriptor.toJsonString());
	}
}
