package services;

import static bg.filterapp.ApplicationContextListener.getConnectionString;
import static bg.filterapp.ApplicationContextListener.getPass;
import static bg.filterapp.ApplicationContextListener.getUsername;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.persistence.dto.LocationDTO;
import org.persistence.dto.LocationPropertiesDTO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Servlet implementation class TestServlet
 */

// TODO - logging
public class SearchService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection cn = null;
	private PreparedStatement getResultQuery = null;
	private Gson gson;
	Logger logger = LogManager.getLogger(SearchService.class);
	public static final String QUERY_PARAM = "query";

	static String SQL_GET_RESULTS = "select l.* from memory_table mt left join all_data l on l.l_id=mt.location_id  "
			+ "where mt.query_n=? order by l.l_id, l.pg_name";

	/**
	 * Default constructor.
	 */
	public SearchService() {
	}

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			gson = new GsonBuilder().create();
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
			getResultQuery = cn.prepareStatement(SQL_GET_RESULTS);

			queryId = UUID.randomUUID().toString();
			filter = getFilter(request, queryId);

			Collection<LocationDTO> result = filterData(filter, queryId);

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

	private Collection<LocationDTO> filterData(String filterQuery, String queryId) throws IOException,
			ServletException, SQLException {
		String jdbcQuery = String.format("call filter(\"%s\",\"%s\")", queryId, filterQuery);
		// String jdbcQuery = "call filter(\"?s\",\"?\")";

		logger.error("Filtering by: " + jdbcQuery);

		CallableStatement procedure = null;
		try {
			
			procedure = cn.prepareCall(jdbcQuery);
			// procedure.setString(1, queryId);
			//
			// procedure.setString(2, filterQuery);

			procedure.execute();
		} finally {
			if (procedure != null && !procedure.isClosed()) {
				procedure.close();
			}
		}
		return getResult(queryId);
	}

	private Collection<LocationDTO> getResult(String queryId) throws SQLException, IOException {
		getResultQuery.setString(1, queryId);
		ResultSet resultRs = getResultQuery.executeQuery();
		Map<Long, LocationDTO> result = new HashMap<Long, LocationDTO>();
		LocationDTO l = null;
		while (resultRs.next()) {
			long id = resultRs.getLong("l_id");
			l = result.get(id);
			if (l == null) {
				l = new LocationDTO();
				l.setId(id);
				l.setName(resultRs.getString("l_name"));
				l.setDescription(resultRs.getString("l_description"));
				
//				Date date = resultRs.getDate("l_created");
//				System.out.println("Date created:" + date);
//				l.setCreated(date);
			}
			LocationPropertiesDTO p = new LocationPropertiesDTO();

			p.setLocationId(id);
			p.setId(resultRs.getLong("lp_id"));
			p.setName(resultRs.getString("pg_name"));
			p.setValue(resultRs.getString("lp_value"));
			p.setPropertyGroupId(resultRs.getLong("pg_id"));

			l.getLocationProperties().add(p);
			result.put(l.getId(), l);
		}
		resultRs.close();
		// response.getWriter().println(result.values());
		return result.values();
	}

	public static Connection getSQLConnection() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection(getConnectionString(), getUsername(), getPass());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
	}
}
