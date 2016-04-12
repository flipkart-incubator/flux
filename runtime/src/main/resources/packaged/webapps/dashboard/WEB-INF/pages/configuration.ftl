<#include "./../header.ftl"> 
<#import "/spring.ftl" as spring />

<div id="configuration">

	<h1>Service Proxy Configuration</h1>
	<span style="color:red "><#if RequestParameters.Error??>${RequestParameters.Error}</#if></span>

	<#if (networkServers?? && networkServers?size!=0) || (UDSServers?? && UDSServers?size!=0) >
		<h2>Servers</h2>
		<table id = "sp-conf-table-servers" class="bordered-table">
			<tr>
				<th>ServerType</th>
				<th>Endpoint</th>
			</tr>
			<#if networkServers?? && networkServers?size!=0>
				<#list networkServers as nServer>
					<tr>
						<td>${nServer.getServerType()}</td>
						<td>${nServer.getServerEndpoint()}</td>
					</tr>
				</#list>
			</#if>
		</table>
	</#if>
	
	
	<#if handlers?? && handlers?size!=0>
		<h2>Handlers</h2>
		<table id = "sp-conf-table" class="bordered-table">
			<tr>
				<th>Handler Name</th>
				<th>Handler Type</th>
				<th>Details</th>
				<th>View Config</th>
				<th>Edit Config</th>
				<th>Reinitialize</th>
			</tr>
			<#list handlers as handler>
				<tr>
					<#assign view_config_url><@spring.url relativeUrl="/viewConfig/handler/${handler.getName()}"/></#assign>
					<#assign edit_config_url><@spring.url relativeUrl="/modifyConfig/handler/${handler.getName()}"/></#assign>
					<#assign reinit_url><@spring.url relativeUrl="/reInit/handler/${handler.getName()}"/></#assign>
					<td><a href="${view_config_url}">${handler.getName()}</a></td>
					<td>${handler.getType()}</td>
					<td style="white-space: pre-wrap">${handler.getDetails()}</td>
					<td><a href="${view_config_url}">View Config</a></td>
					<td><a href="${edit_config_url}">Edit Config</a></td>
					<td><a href="${reinit_url}" onClick="return confirm('Are you sure you want to reinitialize this handler? This means the handler will not be able to serve requests when it is initializing.');">Reinitialize</a></td>
				</tr>
			</#list>
		</table>
	</#if>

</div>

<#include "./../footer.ftl"> 

