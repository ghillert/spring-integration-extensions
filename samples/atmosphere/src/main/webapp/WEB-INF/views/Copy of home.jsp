<%@ include file="/WEB-INF/views/includes/taglibs.jsp"%>

<!DOCTYPE HTML>
<html>
<head>

<title>Spring Integration Atmosphere Adapter Demo</title>

<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<link rel="shortcut icon" href="${ctx}/favicon.ico">
<link rel="apple-touch-icon" href="${ctx}/apple-touch-icon.png">

<link rel="stylesheet" href="<c:url value='/css/blueprint/screen.css'/>"
	type="text/css" media="screen, projection">
<link rel="stylesheet" href="<c:url value='/css/blueprint/print.css'/>"
	type="text/css" media="print">
<!--[if lt IE 8]>
						<link rel="stylesheet" href="/css/blueprint/ie.css" type="text/css" media="screen, projection">
				<![endif]-->

<link rel="stylesheet" href="<c:url value='/css/main.css'/>"
	type="text/css">

<!--[if IE]><script src="<c:url value='/js/excanvas.js'/>"></script><![endif]-->

<script src="<c:url value='/js/jquery.js'/>"></script>
<script src="<c:url value='/js/jquery.atmosphere.js'/>"></script>
<script src="<c:url value='/js/jquery-ui-1.8.14.custom.min.js'/>"></script>

<script src="<c:url value='/js/jquery.tmpl.min.js'/>"></script>

<script src="<c:url value='/js/raphael/raphael-min.js'/>"
	type="text/javascript" charset="utf-8"></script>
<script src="<c:url value='/js/raphael/g.raphael-min.js'/>"
	type="text/javascript" charset="utf-8"></script>
<script src="<c:url value='/js/raphael/g.pie-min.js'/>"
	type="text/javascript" charset="utf-8"></script>
<script src="<c:url value='/js/raphael/g.line-min.js'/>"
	type="text/javascript" charset="utf-8"></script>
<script src="<c:url value='/js/raphael/g.bar-min.js'/>"
	type="text/javascript" charset="utf-8"></script>

</head>
<body>
	<div class="container">
		<div id="header" class="prepend-1 span-22 append-1 last">
			<h1 class="loud" style="margin-top: 0.5em" class="span-20 last">
				Spring Integration AsyncHttp Adapter Demo</h1>
			<div class="span-17">
				<p>
					This example demonstrates the functionality of the <a
						href="http://www.springsource.org/spring-integration/">Spring
						Integration</a> AsyncHttp Adapter. This adapter enables Spring
					Integration messages to be "pushed" to subscribed browser clients
					using the <a href="http://atmosphere.java.net/">Atmosphere
						framework</a>.
				</p>
				<p>
					The source code for this example is available via GitHub: <a
						href="https://github.com/ghillert/spring-asynchttp-examples">
						https://github.com/ghillert/spring-asynchttp-examples </a>
				</p>
			</div>
			<div id="wallclock" class="span-5 last">
				<div id="header">AsyncClock</div>
				<div id="timebox">No time updates.</div>
			</div>
			<div class="span-20 last">
				<form id="publishMessagesForm">
					<label for="publishMessage">Message to Publish:</label> <input
						id="publishMessage" name="publishMessage" type="text" size="50" />
					<input id="publishButton" name="publishButton" type="submit"
						value="Publish" /> <input id="subscribedToTimeService"
						name="subscribedToTimeService" type="checkbox" /> Subscribed to
					Time Service?
					<input id="pollTwitter"
						name="pollTwitter" type="checkbox" /> Poll Twitter?
				</form>
			</div>
		</div>
		<div id="content" class="prepend-1 span-22 append-1 last">
			<div id="main22" class="span-14 prepend-top append-bottom">
				<ul id="twitterMessages">
					<li id="placeHolder">Searching...</li>
				</ul>
			</div>
			<div id="stats" class="span-8 prepend-top append-bottom last">
				<table id="asynchHttpStats">
					<caption>AsynchHttp Stats</caption>
					<thead>
						<tr>
							<th></th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>Type</td>
							<td id="transportType">N/A</td>
						</tr>
						<tr>
							<td># Users</td>
							<td id="numberOfSubscribers">0</td>
						</tr>
						<tr>
							<td>User Ids</td>
							<td id="userIds"></td>
						</tr>
					</tbody>
				</table>
				<table id="chartableStats">
					<caption>Stats</caption>
					<thead>
						<tr>
							<th scope="col"></th>
							<th scope="col"></th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<th scope="row" style="color: #1751A7"># of Callbacks</th>
							<td id="numberOfCallbackInvocations">0</td>
						</tr>
						<tr>
							<th scope="row" style="color: #8AA717"># Tweets</th>
							<td id="numberOfTweets">0</td>
						</tr>
						<tr>
							<th scope="row" style="color: #A74217"># Errors</th>
							<td id="numberOfErrors">0</td>
						</tr>
					</tbody>
				</table>
				<div id="holder" style="height: 180px;"></div>
			</div>
			<div id="content" class="span-14 prepend-top append-bottom last">
				This is content.
			</div>
		</div>
		<div id="footer" class="prepend-1 span-22 append-1 last">
			<a href="http://www.springsource.org/"><img alt="SpringSource"
				title="SpringSource"
				src="${ctx}/images/SpringSource_VMware_HOR_RGB.png"></a>
		</div>
	</div>

	<script id="template" type="text/x-jquery-tmpl">
<li><p id="img-wrapper" style="background-image: url(\${profileImageUrl})"><img alt='\${fromUser}' title='\${fromUser}' src='\${profileImageUrl}' width='48' height='48'/></p><div><c:out value='\${text}'/></div></li>
				</script>
	<script id="userIdTemplate" type="text/x-jquery-tmpl">
						<div>\${}</div>
				</script>

	<script type="text/javascript">

		$(function() {

			var content = $('#content');

			if (!window.console) {
				console = {log: function() {}};
			}

			var socket = $.atmosphere;
			var transport = 'websocket';
			var websocketUrl = "${fn:replace(r.requestURL, r.requestURI, '')}${r.contextPath}/websockets/";

			console.log('websocketUrl: ' + websocketUrl);

			var request = {
					url: websocketUrl,
					contentType : "application/json",
					logLevel : 'debug',
					transport : transport ,
					fallbackTransport: 'long-polling',
					onMessage: onMessage,
					onOpen: function(response) {
						console.log('Atmosphere onOpen: Atmosphere connected using ' + response.transport);
						transport = response.transport;
					},
					onReconnect: function (request, response) {
						console.log("Atmosphere onReconnect: Reconnecting");
					},
					onClose: function(response) {
						console.log('Atmosphere onClose executed');
					},

					onError: function(response) {
						console.log('Atmosphere onError: Sorry, but there is some problem with your '
							+ 'socket or the server is down');
					}
			};

			var subSocket = socket.subscribe(request);

			function onMessage(response) {
				var message = response.responseBody;
				console.log('message: ' + message);

				content.html(message);

/* 				var result;

				try {
					result =  $.parseJSON(message);
				} catch (e) {
					console.log("An error ocurred while parsing the JSON Data: " + message.data + "; Error: " + e);
					return;
				} */

			}

			$('#pollTwitter').change(function(){

				if($(this).is(':checked')){
					console.log('Start Polling Twitter');
					subSocket.push("startTwitter");
				} else {
					console.log('Stop Polling Twitter');
					subSocket.push("stopTwitter");
				}

			});

			$('#subscribedToTimeService').change(function(){

				if($(this).is(':checked')){
					console.log('Start Polling Twitter');
					subSocket.push("subscribeToTimeService");
				} else {
					console.log('Stop Polling Twitter');
					subSocket.push("unsubscribeFromTimeService");
				}

			});

		});
	</script>
	</body>
</html>
