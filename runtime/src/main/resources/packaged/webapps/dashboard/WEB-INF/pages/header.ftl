<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

	<title> <#if appName?? > ${appName} <#else> Flux </#if> Admin </title>

	<!-- all the css -->
	<link rel="stylesheet" href="/admin/resources/styles/main.css" type="text/css"></link>
	<link rel="stylesheet" href="/admin/resources/styles/colors.css" type="text/css"></link>
	<link rel="stylesheet" href="/admin/resources/styles/local.css" type="text/css"></link>
	<link rel="stylesheet" href="/admin/resources/styles/print.css" type="text/css" media="print"></link>

	<!-- jquery -->
	<script src="/admin/resources/js/jquery-1.4.2.min.js" type="text/javascript"></script>
	<script src="/admin/resources/js/jquery.validate-1.7.0.min.js" type="text/javascript"></script>

	<!-- codemirror includes -->
	<link rel="stylesheet" href="/admin/resources/codemirror/lib/codemirror.css">
	<script src="/admin/resources/codemirror//lib/codemirror.js"></script>
	<script src="/admin/resources/codemirror//mode/xml/xml.js"></script>
	<link rel="stylesheet" href="/admin/resources/codemirror/theme/neat.css">

	<!-- codemirror extra css -->
	<style type="text/css">
      .CodeMirror {border-top: 1px solid black; border-bottom: 1px solid black;}
      .activeline {background: #e8f2ff !important;}
    </style>

</head>

<body class="main">
	<div id="page">
		<div id="header">
			<div id="name-and-company">
				<div id='site-name'>
                    <#if appName?? >
                        <a href="/admin" title= "Site Name" rel="home"> ${appName} </a>
                    <#else>
					    <a href="/admin" title= "Site Name" rel="home"> Flux </a>
                    </#if>
				</div>
			</div>
			<!-- /name-and-company -->
		</div>
		<!-- /header -->
		<div id="primary-navigation">
			<div id="primary-left">
				<ul>
					<li><a href="/admin/dashboard">Dashboard</a></li>
					<li><a href="/admin/configuration">Configuration</a></li>
				</ul>
			</div>
		</div>
		<!-- /primary-navigation -->
		<div id="container">
			<div id="content" class="no-side-nav">
				<div id="body">