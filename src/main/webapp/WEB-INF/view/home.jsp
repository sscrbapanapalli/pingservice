<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="refresh" content="30">
<title>Ping Service</title>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>


<link type="text/css" rel="stylesheet"
	href="resources/static/css/bootstrap.min.css" />
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
	background-color: #217dbb;
	text-align: center;
	padding: 10px;
}

table td {
	width: 200px;
	border: 2px dotted silver;
	word-break: break-all;
}

body { width: 100%; margin: 0; padding: 0; overflow: scroll; }

#container_1 { display: block; float: left; position: relative; right: -5%; }
</style>
<style type="text/css">
	.TFtable{
		width:100%; 
		border-collapse:collapse; 
	}
	.TFtable td{ 
		padding:7px; border:#4e95f4 1px solid;
	}
	/* provide some minimal visual accomodation for IE8 and below */
	.TFtable tr{
		background: #b8d1f3;
	}
	/*  Define the background color for all the ODD background rows  */
	.TFtable tr:nth-child(odd){ 
		background: #b8d1f3;
	}
	/*  Define the background color for all the EVEN background rows  */
	.TFtable tr:nth-child(even){
		background: #dae5f4;
	}
</style>
</head>
<body>
<div id="container_1">
	<div>
		<div class="row">
			<div>
				<a href="/pingservice/home"><img
					src="resources/static/images/logo.jpg"
					style="width: 80px;height: 60px;" /></a>
			</div>
		</div>
		<div class="page-header" id="banner">
			<div class="row">
				<div class="col-lg-8 col-md-7 col-sm-6">
					<h1>List of Applications</h1>
				</div>
			</div>
		</div>
		<div>
			<c:if test="${ not empty applications}">

				<table border="1" class="TFtable" >

					<c:forEach items="${applications}" var="app" varStatus="loop">
						<h3>${loop.index+1}.<a href="/pingservice/getUrlStatus/${app.id}">${fn:toUpperCase(app.applicationName)}</a>
						</h3>
						<h3>Last Sync Time:
						<fmt:parseDate value="${fn:toUpperCase(app.lastSyncTime)}" pattern="yyyy-MM-dd HH:mm:ss" var="date"/>
                        <fmt:formatDate value="${date}" pattern="dd-MM-yyyy hh:mm:ss aa" />
						</h3>
						<table border="1" class="TFtable">
							<tr>
							    <th>Application Name</th>
								<th>Server Type</th>
								<th>Availability</th>
								<th>Status Code</th>
							</tr>
							<c:forEach items="${app.applicationUrl}" var="appurl">
								<tr>
								<td>${fn:toUpperCase(appurl.appName)}</td>						   
									<td>${fn:toUpperCase(appurl.serverType.name)}</td>
									<td
										class="${ (appurl.statusCode=='400' || appurl.statusCode=='200') ? 'true-value valid':'false-value invalid'}">${(appurl.statusCode=='400' || appurl.statusCode == '200')?'AVAILABLE':'NOT AVAILABLE'}</td>
									<td class="${(appurl.statusCode=='400' || appurl.statusCode=='200')? 'true-value valid':'false-value invalid'}">${appurl.statusCode}</td>
								</tr>
							</c:forEach>
						</table>
					</c:forEach>
				</table>
			</c:if>
			<c:if test="${empty applications}">
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


