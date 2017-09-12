<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="refresh" content="15">
<title>Ping Service</title>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>


<link type="text/css" rel="stylesheet"
	href="../resources/static/css/bootstrap.min.css" />
<style type="text/css">
.true-value {
	font-size: 16px;
	color: green;
}

.false-value {
	font-size: 16px;
	color: red;
	td
	{
	padding
	:
	1em;
}

}
table {
	border: 1px solid lightgray;
}

table th {
	background-color: silver;
	text-align: center;
	padding: 10px;
}

table td {
	width: 200px;
	border: 2px dotted silver;
	word-break: break-all;
}
body { width: 100%; margin: 0; padding: 0; overflow: hidden; }

#container_1 { display: block; float: left; position: relative; right: -5%; }
</style>
</head>
<body>
<div id="container_1">
	<div >
		<div class="row">
			<div>
				<a href="/pingservice/home"><img
					src="../resources/static/images/logo.jpg"
					style="width: 80px;height: 60px;" /></a>
			</div>
		</div>
		<div class="page-header" id="banner">
			<div class="row">
				<div class="col-lg-8 col-md-7 col-sm-6">
					<h3>
						<a href="/pingservice/home">${fn:toUpperCase(application.applicationName)}</a>
					</h3>
				</div>
			</div>
		</div>
		<div >

			<table border="1">
				
				<c:if test="${ not empty application}">
					
					<table border="1">

						<tr>
							<th>Id</th>
							<th>Server Type</th>
							<th>Availability</th>
							<th>Status Code</th>
							<th>Description</th>
						</tr>

						<c:forEach items="${application.applicationUrl}"
							var="applicationurl" varStatus="loop">
							<tr>
								<td>${loop.index+1}.</td>
								<td>${fn:toUpperCase(applicationurl.serverType.name)}</td>
								<td><a href="${applicationurl.applicationUrl}" target="_ blank">${applicationurl.applicationUrl}</a></td>
								<td class="${applicationurl.status == true ? 'true-value valid':'false-value invalid'}">${applicationurl.statusCode}</td>
								<td class="${applicationurl.status == true ? 'true-value valid':'false-value invalid'}">${applicationurl.description}</td>
							</tr>
						</c:forEach>

					</table>
			</table>
			</c:if>
			<c:if test="${empty application}">
			            *************** No Record Found**********************
			             </c:if>
		</div>
	</div>
	<footer>
	<div>
		<p class="navbar-text pull-left">
			&copy;
			<script>
				var date = new Date();
				document.write(date.getFullYear() + " ")
			</script>
			<a href="https://www.cma-cgm.com" target="_blank">CMA CGM SSC IT
				Projects</a>
		</p>
	</div>
	</footer>
	</div>
</body>
</html>


