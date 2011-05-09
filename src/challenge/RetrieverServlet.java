package challenge;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Receives filename and streams the file to the browser.
 */
public class RetrieverServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private File uploadDir;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);

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
		log("will download from: " + uploadDir.getAbsolutePath());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String filename = request.getParameter("filename");
		File file = new File(uploadDir, filename);
				
		int length = 0;
		ServletOutputStream op = response.getOutputStream();
		ServletContext context = getServletConfig().getServletContext();
		String mimetype = context.getMimeType(filename);

		response.setContentType((mimetype != null) ? mimetype : "application/octet-stream");
		response.setContentLength((int) file.length());
		response.setHeader( "Content-Disposition", "attachment; filename=\"" + filename + "\"" );

		// stream to the requester
		byte[] bbuf = new byte[1024];
		DataInputStream in = new DataInputStream(new FileInputStream(file));

		while ((in != null) && ((length = in.read(bbuf)) != -1)) {
			op.write(bbuf, 0, length);
		}

		in.close();
		op.flush();
		op.close();
	}

}
