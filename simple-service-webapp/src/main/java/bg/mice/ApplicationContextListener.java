package bg.mice;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Application Lifecycle Listener implementation class ServletContextListener
 * 
 */
public class ApplicationContextListener implements javax.servlet.ServletContextListener {
	private static String MODE = null;

	private static String PROD_PICTURES_FOLDER = null;
	private static String PROD_CONN_STR = "jdbc:mysql://50.31.138.79:3306/micehost_mice?useInformationSchema=true";
	private static String PROD_USER = "micehost_root";
	private static String PROD_PASS = "[SINUax*X6+K";
	private static String DEV_PICTURES_FOLDER = null;
	private static String DEV_CONN_STR = null;
	private static String DEV_USER = null;
	private static String DEV_PASS = null;

	/**
	 * Default constructor.
	 */
	public ApplicationContextListener() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see ApplicationContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent arg0) {
		ServletContext sc = arg0.getServletContext();
		DEV_PICTURES_FOLDER = sc.getInitParameter("DEV_PICTURES_FOLDER");
		DEV_CONN_STR = sc.getInitParameter("DEV_CONN_STR");
		DEV_USER = sc.getInitParameter("DEV_USER");
		DEV_PASS = sc.getInitParameter("DEV_PASS");

		MODE = sc.getInitParameter("MODE");

		PROD_PICTURES_FOLDER = sc.getInitParameter("PROD_PICTURES_FOLDER");
		PROD_CONN_STR = sc.getInitParameter("PROD_CONN_STR");
		PROD_USER = sc.getInitParameter("PROD_USER");
		PROD_PASS = sc.getInitParameter("PROD_PASS");
		// TODO Auto-generated method stub
	}

	/**
	 * @see ApplicationContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
	}

	public static String getMODE() {
		return MODE;
	}

	public static String getPicturesFolder() {
		if (isProductive()) {
			return PROD_PICTURES_FOLDER;
		}
		return DEV_PICTURES_FOLDER;
	}

	public static String getConnectionString() {
		if (isProductive()) {
			return PROD_CONN_STR;
		}
		return DEV_CONN_STR;
	}

	public static String getUsername() {
		if (isProductive()) {
			return PROD_USER;
		}
		return DEV_USER;
	}

	public static String getPass() {
		if (isProductive()) {
			return PROD_PASS;
		}
		return DEV_PASS;
	}

	public static boolean isProductive() {
		return "PROD".equalsIgnoreCase(MODE);
	}

}
