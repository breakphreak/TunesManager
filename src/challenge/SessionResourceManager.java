package challenge;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * 
 * UploadDescriptor is shared among servlets and JSPs. Similar validations that happen upon its creation and retrieval reside here.
 *
 */
public class SessionResourceManager {

	// the descriptor will be totally replaced if existed before
	public static UploadDescriptor createUploadDescriptor(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		UploadDescriptor uploadDescriptor = new UploadDescriptor();
		session.setAttribute(Constants.UPLOAD_DESCRIPTOR_KEY, uploadDescriptor);
		return uploadDescriptor;
	}
	
	public static UploadDescriptor getUploadDescriptor(HttpServletRequest request) throws ServletException {
		HttpSession session = request.getSession(false);
		
		Object uploadDescriptorObj = session.getAttribute(Constants.UPLOAD_DESCRIPTOR_KEY);
		UploadDescriptor uploadDescriptor;
		
		if (uploadDescriptorObj != null) {
			if (!(uploadDescriptorObj instanceof UploadDescriptor)) {
				throw new ServletException(
						"Illegal application state: wrong object type had been found in session under \"" + Constants.UPLOAD_DESCRIPTOR_KEY + "\" key: " +
						"Expected: " + UploadDescriptor.class.getName() + ", found: " + uploadDescriptorObj.getClass().getName() + ""
					);
			}
			
			uploadDescriptor = (UploadDescriptor)uploadDescriptorObj;
		} else {
			throw new ServletException("Illegal application state: upload descriptor is expected to be in the session");
		}
		
		return uploadDescriptor;
	}

}
