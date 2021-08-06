<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

	<title>Flux</title>

	<!-- all the css -->
	<link rel="stylesheet" href="/admin/resources/css/main.css" type="text/css"></link>
	<link rel="stylesheet" href="/admin/resources/css/colors.css" type="text/css"></link>
	<link rel="stylesheet" href="/admin/resources/css/local.css" type="text/css"></link>
	<link rel="stylesheet" href="/admin/resources/css/print.css" type="text/css" media="print"></link>

	<!-- jquery -->
	<script src="/admin/resources/js/jquery-1.4.2.min.js" type="text/javascript"></script>
	<script src="/admin/resources/js/jquery.validate-1.7.0.min.js" type="text/javascript"></script>

	<!-- codemirror includes -->
	<link rel="stylesheet" href="/admin/resources/codemirror/lib/codemirror.css">
	<script src="/admin/resources/codemirror//lib/codemirror.js"></script>
	<script src="/admin/resources/codemirror/mode/xml/xml.js"></script>
	<link rel="stylesheet" href="/admin/resources/codemirror/theme/neat.css">

	<!-- codemirror extra css -->
	<style type="text/css">
      .CodeMirror {border-top: 1px solid black; border-bottom: 1px solid black;}
      .activeline {background: #e8f2ff !important;}
    </style>

</head>

<body class="main">
	<div id="page">
		<div id="header" class="Table">
            <div class="Row">
                <div class="Cell" style="width: 60px;">
                    <img src="/admin/resources/images/flux.png" alt="Flux" height="50" width="55"/>
                </div>
                <div class="Cell">
                    <div class="Row">
                    		<#if '${mode}' == 'ORCHESTRATION'>
							<div class="Cell"><a href="/admin/fsmview" style="text-decoration: none; color: white;"">FSM Visualization</a></div>
						<#elseif '${mode}' == 'EXECUTION'>
							<div class="Cell"><a href="/admin/dashboard" style="text-decoration: none; color: white;"">Dashboard</a></div>
						<#else>
							<div class="Cell"><a href="/admin/dashboard" style="text-decoration: none; color: white;"">Dashboard</a></div>
							<div class="Cell"><a href="/admin/fsmview" style="text-decoration: none; color: white;"">FSM Visualization</a></div>
						</#if>                        
                    </div>
                </div>
            </div>
        </div>
		<!-- /header table -->
		<div id="container">
			<div id="content" class="no-side-nav">
				<div id="body">