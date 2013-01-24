<%@ include file="/WEB-INF/views/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Spring Integration &middot; Atmosphere</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="">
<meta name="author" content="">

<!-- Le styles -->
<link href="<c:url value='/assets/css/bootstrap.css'/>" rel="stylesheet">
<style type="text/css">
body {
	padding-top: 20px;
	padding-bottom: 40px;
}

/* Custom container */
.container-narrow {
	margin: 0 auto;
	max-width: 700px;
}

.container-narrow>hr {
	margin: 30px 0;
}

/* Main marketing message and sign up button */
.jumbotron {
	margin: 60px 0;
	text-align: center;
}

.jumbotron h1 {
	font-size: 72px;
	line-height: 1;
}

.jumbotron .btn {
	font-size: 21px;
	padding: 14px 24px;
}

/* Supporting marketing content */
.marketing {
	margin: 60px 0;
}

.marketing p+h4 {
	margin-top: 28px;
}
</style>
<link href="<c:url value='/assets/css/bootstrap-responsive.css'/>" rel="stylesheet">

<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

<!-- Fav and touch icons -->
	<link rel="shortcut icon" href="<c:url value='/assets/ico/favicon.ico'/>">
	<link rel="apple-touch-icon-precomposed" sizes="144x144" href="<c:url value='/assets/ico/apple-touch-icon-144-precomposed.png'/>">
	<link rel="apple-touch-icon-precomposed" sizes="114x114" href="<c:url value='/assets/ico/apple-touch-icon-114-precomposed.png'/>">
	<link rel="apple-touch-icon-precomposed" sizes="72x72" href="<c:url value='/assets/ico/apple-touch-icon-72-precomposed.png'/>">
	<link rel="apple-touch-icon-precomposed" href="<c:url value='/assets/ico/apple-touch-icon-57-precomposed.png'/>">
</head>

<body>

	<div class="container">

		<div class="masthead">
			<ul class="nav nav-pills pull-right">
				<li class="active"><a href="#">Home</a></li>
				<li><a href="#">About</a></li>
				<li><a href="#">Contact</a></li>
			</ul>
			<h3 class="muted">Spring Integration Atmosphere</h3>
		</div>

		<hr>

			<div class="row">
				<div class="span10" id="tweets">&nbsp;</div>
				<div class="span2">
					<div class="row">
						<div class="span2">
						<div>
							Twitter Adapter
						</div>
						<button id="startTwitterAdapter" class="btn btn-success disabled"><i class="icon-play"></i></button>
						<button id="stopTwitterAdapter"  class="btn btn-warning disabled"><i class="icon-stop"></i></button>
						</div>
					</div>
					<div class="row">
						<div class="span2">
						<div>
							Time Service
						</div>
						<button id="startTimeService" class="btn btn-success disabled"><i class="icon-play"></i></button>
						<button id="stopTimeService"  class="btn btn-warning disabled"><i class="icon-stop"></i></button>
						</div>
					</div>
				</div>

			</div>

		<hr>

		<footer>
			<a class="brand" href="http://www.springsource.org/"><img alt="SpringSource"
				title="SpringSource" src="${ctx}/assets/img/spring/SpringSource-logo.png"></a>
		</footer>

	</div>
	<!-- /container -->

	<!-- Le javascript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script src="<c:url value='/assets/js/jquery/jquery.js'/>"></script>
	<script src="<c:url value='/assets/js/bootstrap.js'/>"></script>
	<script src="<c:url value='/assets/js/jquery/jquery.atmosphere.js'/>"></script>
	<script src="<c:url value='/assets/js/spin.js'/>"></script>
	<script src="<c:url value='/assets/js/handlebars.js'/>"></script>
	<script src="<c:url value='/assets/js/custom.js'/>"></script>

	<script type="text/javascript">

			$(document).ready(function() {

				var content = $('#tweets');

				if (!window.console) {
					console = {log: function() {}};
				}

				content.spin();

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

					var context = {
							tweets : $.parseJSON(message)
						};
					var html = tweetTemplate(context);

					$(html).hide().prependTo("#tweets").fadeIn("slow");
					$('#tweets').spin(false);

	/* 				var result;

					try {
						result =  $.parseJSON(message);
					} catch (e) {
						console.log("An error ocurred while parsing the JSON Data: " + message.data + "; Error: " + e);
						return;
					} */

				}

				$("#startTwitterAdapter").click(function (){

					console.log('Start Polling Twitter');
					subSocket.push("startTwitter");

					$("#startTwitterAdapter").addClass("disabled");
					$("#stopTwitterAdapter").addClass("disabled");
				});
				$("#stopTwitterAdapter").click(function (){

					console.log('Stop Polling Twitter');
					subSocket.push("stopTwitter");

					$("#startTwitterAdapter").addClass("disabled");
					$("#stopTwitterAdapter").addClass("disabled");
				});
				$("#startTimeService").click(function (){

					console.log('Start Time Service');
					subSocket.push("subscribeToTimeService");
					$("#startTimeService").addClass("disabled");
					$("#stopTimeService").addClass("disabled");
				});
				$("#stopTimeService").click(function (){

					console.log('Stop Time Service');
					subSocket.push("unsubscribeFromTimeService");
					$("#startTimeService").addClass("disabled");
					$("#stopTimeService").addClass("disabled");
				});
			});
	</script>

	<script id="tweet-template" type="text/x-handlebars-template">
			{{#each tweets}}
				{{#with this}}
					<div id="{{id}}" class="row" style="margin-bottom: 5px;">
						<div class="span1">
							<img alt="{{fromUser}}"
								title="{{fromUser}}"
								src="{{profileImageUrl}}"
								width="48" height="48"/>
						</div>
						<div class="span6">
							<p>{{text}}</p>
							<p><small>{{createdAt}}</small></p>
						</div>
						<div class="span1 delete">
							<button class="close">&times;</button>
						</div>
					</div>
				{{/with}}
			{{/each}}
	</script>
</body>
</html>
