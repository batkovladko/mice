package bg.mice.services;

import static bg.mice.ApplicationContextListener.getConnectionString;
import static bg.mice.ApplicationContextListener.getPass;
import static bg.mice.ApplicationContextListener.getUsername;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bg.mice.data.model.Location;
import bg.mice.helpers.GsonHelper;

import com.google.gson.Gson;

/**
 * Servlet implementation class TestServlet
 */

// TODO - logging
public class SearchService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection cn = null;
	private Gson gson;
	Logger logger = LogManager.getLogger(SearchService.class);
	public static final String QUERY_PARAM = "query";

	/**
	 * Default constructor.
	 */
	public SearchService() {
	}

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			gson = GsonHelper.getGson();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String queryId = "";
		String filter = null;
		try {
			logger.error("Searching initiated...");
			response.setContentType("application/json;UTF-8");
			response.setCharacterEncoding("UTF-8");
			cn = getSQLConnection();
			
			queryId = UUID.randomUUID().toString();
			filter = getFilter(request, queryId);

			List<Location> result = filterData(filter, queryId);

			gson.toJson(result, response.getWriter());
			logger.error("DONE : " + result);

		} catch (Exception e) {
			logger.error(String.format("Error during executing query [%s] with filter [%s].", queryId, filter), e);
			throw new ServletException(e);
		} finally {
			if (cn != null) {
				try {
					cn.close();
				} catch (SQLException e) {
					logger.warn("Cannot close connection");
				}
			}
		}
	}

	private String getFilter(HttpServletRequest request, String queryId) throws UnsupportedEncodingException {
		Enumeration<String> parameterNames = request.getParameterNames();
		StringBuilder buf = new StringBuilder("");
		request.setCharacterEncoding("UTF-8");
		int i = 0;
		while (parameterNames.hasMoreElements()) {
			String pName = parameterNames.nextElement();
			if (QUERY_PARAM.equalsIgnoreCase(pName)) {
				continue;
			}
			String pValue = request.getParameter(pName);
			if (StringUtils.isEmpty(pValue)) {
				continue;
			}
			if (i != 0) {
				buf.append(",");
			}
			buf.append("('");
			buf.append(queryId).append("',");
			buf.append(pName).append(",'");
			buf.append(pValue).append("')");
			i++;
		}
		return buf.toString();
	}

	private List<Location> filterData(String filterQuery, String queryId) throws IOException, ServletException,
			SQLException {
		String jdbcQuery = String.format("call filter(\"%s\",\"%s\")", queryId, filterQuery);
		logger.error("Filtering by: " + jdbcQuery);

		CallableStatement procedure = null;
		try {
			procedure = cn.prepareCall(jdbcQuery);
			procedure.execute();
		} finally {
			if (procedure != null && !procedure.isClosed()) {
				procedure.close();
			}
		}
		return Location.getResult(queryId, Route.getEm());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
	}

	public static Connection getSQLConnection() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection(getConnectionString(), getUsername(), getPass());
	}
}
