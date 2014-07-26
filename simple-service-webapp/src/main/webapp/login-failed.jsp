<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="images/mice_favicon.png" rel="icon" type="image/x-icon" />
<link href="images/mice_favicon.png" rel="shortcut icon" type="image/x-icon" />
</head>
<body>
<%request.setAttribute("error", "yes"); request.getRequestDispatcher("login.jsp").forward(request, response); %>
</body>
</html>