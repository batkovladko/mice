package bg.mice.services;

import static bg.mice.ApplicationContextListener.getConnectionString;
import static bg.mice.ApplicationContextListener.getPass;
import static bg.mice.ApplicationContextListener.getPicturesFolder;
import static bg.mice.ApplicationContextListener.getUsername;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bg.mice.ApplicationContextListener;
import bg.mice.data.model.Location;

/**
 * Servlet implementation class ImageService
 */
@WebServlet("/api/v1/images")
public class ImageService extends HttpServlet {
	private static final String IMAGES_NOPICTURE_PNG = "images/nopicture.png";
	private static final long serialVersionUID = 1L;
	Logger logger = LogManager.getLogger(ImageService.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ImageService() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String locationId = request.getParameter("imageId");
		String imageName = null;
		try {
			if (StringUtils.isEmpty(locationId)) {
				serveDefaultImage(response);
				return;
			}
			Location foundLocation = getLocation(locationId);
			// byte[] image = foundLocation.getImage();

			if (foundLocation != null) {
				imageName = foundLocation.getImageName();
				if (!StringUtils.isEmpty(imageName)) {
					serveNonDefaultImage(response, imageName);
				} else {
					serveDefaultImage(response);
				}
			}
		} catch (Exception e) {
			logger.error("Error during serving an image", e);
			AddNewLocationService.buildServerError(response, e.getMessage(), false);
		}
	}

	private void serveDefaultImage(HttpServletResponse response) throws IOException,
			MagicParseException, MagicMatchNotFoundException, MagicException {
		String realPath = getServletContext().getRealPath(File.separator);
		serveImage(response, new File(realPath, IMAGES_NOPICTURE_PNG));
	}

	private void serveNonDefaultImage(HttpServletResponse response, String pathToTheImage) throws IOException,
			MagicParseException, MagicMatchNotFoundException, MagicException {
		String realPath = getPicturesFolder() + pathToTheImage;
		serveImage(response, new File(realPath));
	}

	private void serveImage(HttpServletResponse response, File f) throws IOException,
			MagicParseException, MagicMatchNotFoundException, MagicException {
		BufferedImage imageAsStream;
		OutputStream out = null;
		if (f.exists()) {
			System.out.println(String.format("Serving image [%s]...", f.getAbsolutePath()));
			imageAsStream = ImageIO.read(f);
			String extension = Magic.getMagicMatch(f, false).getExtension();
			out = response.getOutputStream();
			ImageIO.write(imageAsStream, extension, out);
		} else {
			System.out.println(String.format("Serving image [%s]... fails. File does no exist. Using default image", f.getAbsolutePath()));
			serveDefaultImage(response);
		}
	}

	private Location getLocation(String locationId) {
		Long id = Long.parseLong(locationId);
		EntityManager em = Route.getEm();
		try {
			return em.find(Location.class, id);
		} finally {
			if (em.isOpen()) {
				em.close();
			}
		}
	}
	public static void main(String[] args) {
		Map<String, String> props = new HashMap<String, String>();
		props.put("javax.persistence.jdbc.url", "jdbc:mysql://web441.webfaction.com:32527/test?useInformationSchema=true");
		props.put("javax.persistence.jdbc.user", "root");
		props.put("javax.persistence.jdbc.password", "0zmtwwu1");
		
		EntityManager em = null;
		
		em = Persistence.createEntityManagerFactory("simple-service-webapp", props)
				.createEntityManager();
		
		Location find = em.find(Location.class, 1);
		System.out.println(find);
		em.close();
		System.out.println("end");
	}
}
