<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html lang="bg" ng-app="filterApp" class="ng-scope">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="">
<meta name="author" content="">
<style type="text/css">
@charset "UTF-8";

[ng\:cloak],[ng-cloak],[data-ng-cloak],[x-ng-cloak],.ng-cloak,.x-ng-cloak,.ng-hide
	{
	display: none !important;
}

ng\:form {
	display: block;
}
</style>

<title>MICE</title>

<!-- Bootstrap core CSS -->
<link href="css/bootstrap.css" rel="stylesheet">
<link href="css/custom.css" rel="stylesheet">

</head>
<script type="text/javascript">
	function submitForm() {
		document.getElementById("error").innerHTML ="";
		$("#loginForm").submit();
	}

</script>
<body style>
	<div class="panel panel-default ng-scope" id="loginDiv">
		<div class="panel-heading">Вход</div>
		<form method="POST" action="j_security_check" id="loginForm">
			<div class="panel-body">
				<table>
					<%
						if (request.getAttribute("error") != null) {
					%>
					<tr>
						<td align="ceneter" colspan="2"><span id="error" style="color: red">Невалидно потребителско име
							или парола</span></td>
					</tr>

					<%
						}
					%>
					<%request.removeAttribute("error");%>
					<tr>
						<td align="right"><label>Потребителско име:</label></td>
						<td><input type="text" name="j_username"  required="required"/></td>
					</tr>

					<tr>
						<td align="right"><label>Парола:</label></td>
						<td><input type="password" name="j_password" required="required"></td>
					</tr>
					<tr>
						<td colspan="2" align="right"><input type="submit"
							value="Влез"
							onclick="submitForm()"></td>
					</tr>

				</table>
			</div>
		</form>
	</div>
</body>
</html>