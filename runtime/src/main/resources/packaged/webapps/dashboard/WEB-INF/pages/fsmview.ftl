<#include "header.ftl">

    <!-- The JoinJS CSS -->
    <link rel="stylesheet" type="text/css" href="/admin/resources/css/joint.css" />

    <!-- Keeping the Hystrix global stylesheet for some useful CSS types -->
    <link rel="stylesheet" type="text/css" href="/admin/resources/css/global.css" />
    <link rel="stylesheet" type="text/css" href="/admin/resources/css/bootstrap.min.css">
    <!-- JointJS and Diagre JS files and their dependencies -->
    <script type="text/javascript" src="/admin/resources/js/jquery.min.js"></script>
    <script type="text/javascript" src="/admin/resources/js/lodash.min.js"></script>
    <script type="text/javascript" src="/admin/resources/js/backbone-min.js"></script>
    <script type="text/javascript" src="/admin/resources/js/joint.js"></script>
    <script type="text/javascript" src="/admin/resources/js/graphlib.core.js"></script>
    <script type="text/javascript" src="/admin/resources/js/dagre.core.js"></script>
    <script type="text/javascript" src="/admin/resources/js/joint.layout.DirectedGraph.js"></script>
    <script type="text/javascript" src="/admin/resources/js/bootstrap.min.js"></script>

    <div class="Table">
        <div class="Row">
            <div class="Cell" style="margin-left: -5px; width: 110px; float: left;">
                <label class="label"> Enter FSM ID </label>
            </div>
            <div class="Cell">
                <input class="form-control" type="text" placeholder="FSM Id or Correlation Id" id="fsm-id"/>
            </div>
            <div class="Cell"><button class="btn btn-sm btn-primary" onclick="getFSMData()" id="get-fsm-data">Show FSM</button></div>
        </div>
    </div>
    <div>&nbsp;</div>
    <div>&nbsp;</div>
    <div id="alert-msg" class="alert alert-danger fade in" style="margin-left: 130px;"></div>
    <div>&nbsp;</div>
    <div>
        <div id="graph-div" style="float: left">
            <div class="paper" id="fsmcanvas" style="width: 1000px; height: 500px; overflow: auto;"></div>
        </div>
        <div id="fsm-legend-table">
            <table style="width: 10%">
                <tr>
                    <td>
                        <div class="Cell" style="width: 175px">
                            <button style="margin-left: -5px;margin-bottom: 5px; width: 160px; height: 35px;" class="btn btn-sm btn-primary" id="fsm-unsideline-btn" data-toggle="modal" onclick="unsidelineModal()" data-target="#unsideline-modal">Unsideline </button>
                        </div>
                        <div class="Cell" style="width: 175px">
                            <button style="margin-bottom: 5px; width: 175px; height: 35px;" class="btn btn-sm btn-primary" id="fsm-event-details-btn" data-toggle="modal" data-target="#event-details-modal" >Event Information </button>
                        </div>
                    </td>
                </tr>
                <tr><td>
                    <div class="panel-group" id="fsm-legend-collapse" style="width: 350px;">
                        <div class="panel panel-default" id="fsm-details">
                            <div class="panel-heading" data-toggle="collapse" data-parent="#fsm-legend-collapse" href="#info-div">
                                    FSM Details
                            </div>
                            <div id="info-div" class="panel-collapse collapse">
                                <div class="panel-body">
                                    <table class="table" style="width: 10%">
                                        <tr>
                                            <th align="left" style="border-top: none; vertical-align: middle;">FSM Name:</th>
                                            <td id="fsmName" align="left" style="border-top: none; vertical-align: middle; max-width: 200px; word-wrap: break-word;"></td>
                                        </tr>
                                        <tr>
                                            <th align="left" style="border-top: none; vertical-align: middle;">FSM Id:</th>
                                            <td id="fsmId" align="left" style="border-top: none; vertical-align: middle;"></td>
                                        </tr>
                                        <tr>
                                            <th align="left" style="border-top: none; vertical-align: middle;">FSM Version:</th>
                                            <td id="fsmVersion" align="left" style="border-top: none; vertical-align: middle;"></td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                        <div class="panel panel-default" id="legend-details">
                            <div class="panel-heading" data-toggle="collapse" data-parent="#fsm-legend-collapse" href="#legend">
                                Legend
                            </div>
                            <div id="legend" class="panel-collapse collapse">
                                <div class="panel-body">
                                    <table class="table" style="width: 10%;">
                                        <#--<tr><th style="border-top: none">Legend</th></tr>-->
                                        <tr><td style="border-top: none; vertical-align: middle"><div class="initialized">&nbsp;</div></td><td style="border-top: none;">&nbsp;Initialized</td></tr>
                                        <tr><td style="border-top: none; vertical-align: middle"><div class="running">&nbsp;</div> </td><td style="border-top: none;">&nbsp;Running </td></tr>
                                        <tr><td style="border-top: none; vertical-align: middle"><div class="completed">&nbsp;</div> </td><td style="border-top: none;">&nbsp;Completed</td></tr>
                                        <tr><td style="border-top: none; vertical-align: middle"><div class="cancelled">&nbsp;</div> </td><td style="border-top: none;">&nbsp;Cancelled</td></tr>
                                        <tr><td style="border-top: none; vertical-align: middle"><div class="errored">&nbsp;</div> </td><td style="border-top: none;">&nbsp;Errored </td></tr>
                                        <tr><td style="border-top: none; vertical-align: middle"><div class="sidelined">&nbsp;</div> </td><td style="border-top: none;">&nbsp;Sidelined</td></tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </td></tr>
            </table>
        </div>
        <div id="audit-div">
            <!-- audit table creation is done from java script -->
        </div>
    </div>

    <!-- This is Bootstarap modal for unSideline task -->
    <div id="unsideline-modal" class="modal fade" role="dialog">
        <div class="modal-dialog">
            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header" >
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">Unsideline</h4>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <select class="form-control" id="errored-state-list" ><#--all option will come here--></select>
                    </div>
                    <div id="unsideline-msg"><#-- success msg appear here on success --></div>
                </div>
                <div class="modal-footer" class="text-center">
                    <div class="text-center" id="unsideline-button-submit-ok-toggle"><#-- submit and ok button created alternatively here--></div>
                </div>
            </div>
        </div>
    </div>

    <!-- This is Bootstarap modal to see event details -->
    <div id="event-details-modal" class="modal fade" role="dialog">
        <div class="modal-dialog">
            <!-- Modal content-->
            <div class="modal-content" style="height: 570px;">
                <div class="modal-header" >
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">Event Information</h4>
                </div>
                <div class="modal-body">
                    <div class="container col-md-12">
                        <div class="form-group">
                            <select style="height:28px;width:550px" class="selectpicker" id="event-data-select" ><#--all option will come here--></select>
                        </div>
                        <div id="event-data">
                            <textarea readonly rows="15" cols="50" id="event-data-txt-box" style="height: 262px;width: 550px;max-height: 262px;overflow-y: auto;resize: none;" data-role="none"></textarea>
                        </div>
                        <div>
                          <div>
                            <span style="text-align: left">Execution Version:</span>
                          </div>
                          <div>
                            <span style="text-align: left;" id="event-execution-version-label"></span>
                          </div>
                          <br>
                            <div>
                                <span style="text-align: left">Status:</span>
                            </div>
                            <div>
                                <span style="text-align: left;" id="event-status-label"></span>
                            </div>
                            <br>
                            <div>
                                <span style="text-align: left">Updated At:</span>
                            </div>
                            <div>
                                <span style="text-align: left" id="event-updated-at-label"></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- This is Bootstarap modal to see event details for the given exec version-->
    <div id="event-details-modal-exec-version" class="modal" role="dialog">
      <div class="modal-dialog">
        <!-- Modal content-->
        <div class="modal-content" style="height: 570px;">
          <div class="modal-header" >
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h4 class="modal-title">Event Data</h4>
          </div>
          <div class="modal-body">
            <div class="container col-md-12">
              <div>
                <span style="text-align: left">Event Name:</span>
                <span style="text-align: left" id="event-name-exec-version"></span>
              </div>
              <br>
              <div>
                <span style="text-align: left">Event Data:</span>
              </div>
              <div id="event-data-exec-version-div">
                <textarea readonly rows="15" cols="50" id="event-data-txt-box-exec-version" style="height: 262px;width: 550px;max-height: 262px;overflow-y: auto;resize: none;" data-role="none"></textarea>
              </div>
              <div>
                <div>
                  <span style="text-align: left">Execution Version:</span>
                  <span style="text-align: left;" id="event-execution-version-label-exec-version"></span>
                </div>
                <br>
                <div>
                  <span style="text-align: left">Status:</span>
                  <span style="text-align: left;" id="event-status-label-exec-version"></span>
                </div>
                <br>
                <div>
                  <span style="text-align: left">Updated At:</span>
                  <span style="text-align: left" id="event-updated-at-label-exec-version"></span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <script type="text/javascript">

        var eventNameDataMap=new Object();
        var eventNames = [];
        var graph = new joint.dia.Graph;
        var paper = new joint.dia.Paper({
            el: $('#fsmcanvas'),
            width: 6000,
            height: 4000,
            gridSize: 1,
            model: graph,
	        interactive: function(cellView) {
	        	if (cellView.model.isLink()) { // we dont want links to be interactive
	        		return false;
	        	}
	        	return true;
	        }
        });

        //creates Audit records table and attaches it to audit-div
        function createAuditTable(adjacencyList, auditData) {

            var stateIdToNameMap = {};
            _.each(adjacencyList, function(edgeData,vertexIdentifier){
                var sourceVertex = vertexIdentifier.split(":");
                stateIdToNameMap[sourceVertex[0]] = sourceVertex[1]; //stateId as key and stateName as value
            });

            var auditDiv = document.getElementById("audit-div");

            auditDiv.innerHTML = "";

            var table = document.createElement("table");
            table.className = "table table-hover";
            table.setAttribute("id","audit-table");

            table.border = '1';

            var tableHead = document.createElement("thead");
            table.appendChild(tableHead);

            var tr = document.createElement("tr");
            tableHead.appendChild(tr);

            var stateId = document.createElement("th");
            stateId.appendChild(document.createTextNode("State Id"));
            tr.appendChild(stateId);

            var stateName = document.createElement("th");
            stateName.appendChild(document.createTextNode("State Name"));
            tr.appendChild(stateName);

            var taskExecutionVersion = document.createElement("th");
            taskExecutionVersion.appendChild(document.createTextNode("State Execution Version"));
            tr.appendChild(taskExecutionVersion);

            var eventDependencies = document.createElement("th");
            eventDependencies.appendChild(document.createTextNode(
              "Dependent Events {Name, ExecutionVersion}"));
            tr.appendChild(eventDependencies);

            var retryAttempt = document.createElement("th");
            retryAttempt.appendChild(document.createTextNode("Retry Attempt"));
            tr.appendChild(retryAttempt);

            var status = document.createElement("th");
            status.appendChild(document.createTextNode("Status"));
            tr.appendChild(status);

            var rollbackStatus = document.createElement("th");
            rollbackStatus.appendChild(document.createTextNode("Rollback Status"));
            tr.appendChild(rollbackStatus);

            var errors = document.createElement("th");
            errors.appendChild(document.createTextNode("Errors"));
            tr.appendChild(errors);

            var createdAt = document.createElement("th");
            createdAt.appendChild(document.createTextNode("Created At"));
            tr.appendChild(createdAt);

            var tableBody = document.createElement("tbody");
            table.appendChild(tableBody);

            _.each(auditData, function(auditRecord) {
                tr = document.createElement("tr");
                tableBody.appendChild(tr);

                var td = document.createElement("td");
                td.appendChild(document.createTextNode(auditRecord.stateId));
                tr.appendChild(td);

                td = document.createElement("td");
                td.appendChild(document.createTextNode(stateIdToNameMap[auditRecord.stateId]));
                tr.appendChild(td);

                var td = document.createElement("td");
                td.appendChild(document.createTextNode(auditRecord.taskExecutionVersion));
                tr.appendChild(td);

              td = document.createElement("td");
              // Create a hyperlink for each dependency in the event Dependencies
              var eventDependenciesArray = [];
              try {
                eventDependenciesArray = JSON.parse(auditRecord.eventDependencies);

                // move all of this to a function
                for (var index = 0; index < eventDependenciesArray.length; index++) {
                  // console.log("index: " + index + " value: " + eventDependenciesArray[index]);
                  var eventNameFull = Object.keys(eventDependenciesArray[index])[0];
                  var execVersion = Object.values(eventDependenciesArray[index])[0];
                  var eventName = eventNameFull.split(".")[eventNameFull.split(".").length - 1];
                  // console.log("eventName: " + eventName + " execVersion: " + execVersion);
                  // console.log(eventDependenciesArray[index]);

                  var link = document.createElement('a');
                  var linkText = document.createTextNode(execVersion);
                  link.appendChild(linkText);
                  link.title = "click to get the event data";
                  link.setAttribute('href','${flux_api_url}/api/machines/' + auditRecord.stateMachineInstanceId
                      + "/"+eventNameFull+"/"+execVersion+"/eventdata");
                  link.setAttribute('target', '_blank');

                  td.appendChild(document.createTextNode("{".concat(eventName).concat(":")));
                  td.appendChild(link);
                  td.appendChild(document.createTextNode("}"));
                }
                tr.appendChild(td);

              } catch (err) {
                console.log("unable to parse Object: '" + auditRecord.eventDependencies
                    + "'. Error message: " + err);
                td.appendChild(document.createTextNode(auditRecord.eventDependencies));
                tr.appendChild(td);
              }


                td = document.createElement("td");
                td.appendChild(document.createTextNode(auditRecord.retryAttempt));
                tr.appendChild(td);

                td = document.createElement("td");
                td.appendChild(document.createTextNode(auditRecord.stateStatus));
                tr.appendChild(td);

                td = document.createElement("td");
                td.appendChild(document.createTextNode(auditRecord.stateRollbackStatus));
                tr.appendChild(td);

                td = document.createElement("td");
                td.appendChild(document.createTextNode(auditRecord.errors));
                tr.appendChild(td);

                td = document.createElement("td");
                td.appendChild(document.createTextNode(getFormattedDate(new Date(auditRecord.createdAt))));
                tr.appendChild(td);

                var rowColor = getRowColor(auditRecord.stateStatus);
                if(rowColor != null)
                    tr.className = rowColor;

                //todo: assign color code based on rollback status as well
            });
          table.onclick = function (ev) {
            ev.preventDefault();
            // console.log(event.target);

            var attribute = event.target.getAttribute('href');
            // console.log(attribute);
            if (attribute != null){
              $.ajax({
                url: attribute,
                type: 'GET',
                success: function (data, status, jqXHR) {
                  document.getElementById("event-name-exec-version").innerText = data.name;
                  document.getElementById("event-data-txt-box-exec-version").innerText = data.eventData;
                  document.getElementById("event-execution-version-label-exec-version").innerText = data.executionVersion;
                  document.getElementById("event-status-label-exec-version").innerText = data.status;
                  document.getElementById("event-updated-at-label-exec-version").innerText = data.updatedAt;

                  $('#event-details-modal-exec-version').modal('show');
                  // console.log(data);
                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                  alert("Status: " + XMLHttpRequest.status + " Response:" + XMLHttpRequest.responseText);
                }
              });
            }

          };
            auditDiv.appendChild(table);
        }

        function getFormattedDate(date) {
            return date.toLocaleDateString() + " " + date.toLocaleTimeString() + "." + date.getMilliseconds();
        }

        // Returns row color for audit record based on status, color setting is done by specifying bootstrap class name for the element.
        function getRowColor(status) {
            var rowColor = null;
            switch (status) {
                case 'initialized':
                    break; //let it be default
                case 'running':
                    rowColor = 'active';
                    break;
                case 'completed':
                    rowColor = 'success';
                    break;
                case 'cancelled':
                    rowColor = 'warning';
                    break;
                case 'errored':
                    rowColor = 'danger';
                    break;
                case 'sidelined':
                    rowColor = 'danger';
                    break;
            }
            return rowColor;
        }

        // Build the DAG from the specified adjacency list
        function buildGraphFromAdjacencyList(adjacencyList,initStateEdges) {
            var elements = [];
            var links = [];
            var nodeIds = [];
            // Make the vertex nodes
            _.each(adjacencyList, function(edgeData,vertexIdentifier ) {
            	var stateColor = '#33ccff'; // default is 'initialized' color
            	if (vertexIdentifier != null) {
	            	switch(vertexIdentifier.split(":")[2]) {
	            	case 'initialized':
	            		stateColor = '#33ccff';
	            		break;
	            	case 'running':
	            		stateColor = '#cc99ff';
	            		break;
	            	case 'completed':
	            		stateColor = '#33cc33';
	            		break;
	            	case 'cancelled':
	            		stateColor = '#993333';
	            		break;
	            	case 'errored':
	            		stateColor = '#ff0000';
	            		break;
	            	case 'sidelined':
	            		stateColor = '#FE854F';
	            		break;
	            	}
            	}
                elements.push(makeState(vertexIdentifier,stateColor));
            });
            // attach edges
            _.each(adjacencyList, function(edgeData,vertexIdentifier) {
                if(edgeData != null) {
                    var sourceVertexId = vertexIdentifier.split(":")[0];
                    _.each(edgeData.incidentOn, function(targetVertexId) {
                    links.push(makeEdge(edgeData.label, searchNodeId(sourceVertexId,elements),searchNodeId(targetVertexId,elements)));
                    });
                }
            });
            var i = -1;
            _.each(initStateEdges,function(initEdgeData) {
                var edgeSource = initEdgeData.source;
                var edgeSourceVertex = searchVertexByLabel(edgeSource,elements);
                if(edgeSourceVertex == null) {
                    edgeSourceVertex = makeState(i+":"+edgeSource,'#7c68fc');
                    i = i - 1;
                    elements.push(edgeSourceVertex);
                }
                _.each(initEdgeData.incidentOn, function(targetVertexId) {
                    links.push(makeEdge(initEdgeData.label, edgeSourceVertex,searchNodeId(targetVertexId,elements)));
                });
            });
            // Links must be added after all the elements. This is because when the links
            // are added to the graph, link source/target
            // elements must be in the graph already.
            return elements.concat(links);
        }


        function searchVertexByLabel(labelToSearch, elementsArray) {
            return _.find(elementsArray,function(node) {
                if(node.attributes.attrs.text.text == labelToSearch) {

                }
            });
        }
        function searchNodeId(requiredNodeId,vertexArray) {
            return _.find(vertexArray,function(node) {
                if (node.id == requiredNodeId) {
                    return true;
                }
            });
        }

        // Draw a State in the FSM
        function makeState(vertexIdentifier,colorCode) {
            var label = vertexIdentifier.split(":")[1];
            var nodeId = vertexIdentifier.split(":")[0];
            var maxLineLength = _.max(label.split(' '), function(l) {
                return l.length;
            }).length;
            // Compute width/height of the rectangle based on the number
            // of lines in the label and the letter size. 0.6 * letterSize is
            // an approximation of the monospace font letter width.
            var letterSize = 10;
            var width = 1.2 * (letterSize * (0.6 * maxLineLength + 1));
            var height = 1.2 * ((label.split(' ').length + 1) * letterSize);
            return new joint.shapes.basic.Circle({
                id: nodeId,
                size: { width: Math.max(width,height), height:  Math.max(width,height) },
                attrs: {
                    text: { text: label.split(' ').join('\n'), 'font-size': letterSize, 'font-family': 'monospace', fill: 'white' },
                    circle: {
                        fill: colorCode,
                        width: width,
                        height: height,
                        stroke: 'none'
                    }
                }
            });
        }

        // Draw an Edge in the DAG
        function makeEdge(label,sourceNode,targetNode) {
            // Edge details is of the form TargetNode<colon>EdgeLabel
            return new joint.shapes.fsa.Arrow({
                source : { id : sourceNode.id },
                target : { id : targetNode.id },
                labels : [ {
                    position : 0.5,
                    attrs : {
                        text : { text : label.split(' ').join('\n'), 'font-size': 8,}
                    }
                } ],
                attrs: {
                    '.marker-target': { d: 'M 4 0 L 0 2 L 4 4 z' }
                },
                smooth: true
            });
        }

        // Function to draw the DAG
        function layout(adjacencyList,initStateEdges) {
            var cells = buildGraphFromAdjacencyList(adjacencyList,initStateEdges);
            graph.resetCells(cells);
            joint.layout.DirectedGraph.layout(graph, {
	        	nodeSep: 100,
	        	edgeSep: 20,
	        	rankSep: 100,
	            rankDir: "LR"
            });
        }

        function displayFsmInfo(fsmId, fsmVersion, fsmName) {
            document.getElementById("fsmId").innerHTML = fsmId;
            document.getElementById("fsmVersion").innerHTML = fsmVersion;
            document.getElementById("fsmName").innerHTML = fsmName;
        }

         function getFSMData() {
            var fsmId = document.getElementById("fsm-id").value;
            if(!fsmId) {
                fsmId = ('${fsm_id}' != 'null' ? '${fsm_id}' : null);
                document.getElementById("fsm-id").value = fsmId;
            };
            if(fsmId) {
                $.ajax({
                    url: '${flux_api_url}/api/machines/' + fsmId + '/fsmdata',
                    type: 'GET',
                    success: function (data, status, jqXHR) {
                        document.getElementById("graph-div").style.display = 'block';
                        document.getElementById("alert-msg").style.display = 'none';
                        layout(data.fsmGraphData, data.initStateEdges);
                        createAuditTable(data.fsmGraphData, data.auditData);
                        displayFsmInfo(data.stateMachineId, data.fsmVersion, data.fsmName);
                        document.getElementById("fsm-unsideline-btn").style.display = 'block';
                        document.getElementById("fsm-event-details-btn").style.display='block';
                        document.getElementById("fsm-details").style.display='block';
                        document.getElementById("legend-details").style.display='block';

                        populateErroredStatesList(data);
                        populateEventInformation(data);
                    },
                    error: function (XMLHttpRequest, textStatus, errorThrown) {
                        alert("Status: " + XMLHttpRequest.status + " Response:" + XMLHttpRequest.responseText);
                    }
                });
            }
        }

        //prepares list of errored states for unsideline modal
        function populateErroredStatesList(data) {
            $("#errored-state-list").empty();
            $('#errored-state-list').append('<option value="" disabled selected value>--select State Id--</option>');
            for(var i=0;i<data.erroredStateIds.length;i++){
                $('#errored-state-list').append('<option>'+ data.erroredStateIds[i]+'</option>');
            }
        }

        //does necessary operations to show event information on list box select
        function populateEventInformation(data) {
            $("#event-data-select").empty();
            $('#event-data-select').append('<option value="" disabled selected value>--select Event--</option>');
            var count=0;
            for(var i=0;i<data.initStateEdges.length;i++){
                eventNameDataMap[data.initStateEdges[i].label] = {
                  eventData: data.initStateEdges[i].eventData.split("#")[0],
                  executionVersion: data.initStateEdges[i].eventData.split("#")[1],
                  status: data.initStateEdges[i].status,
                  updatedAt: data.initStateEdges[i].updatedAt};
                eventNames[count] = data.initStateEdges[i].label;
                count++;
            }
            for(var stateIdentifier in data.fsmGraphData) {
                if(data.fsmGraphData[stateIdentifier].label != "") {
                    eventNameDataMap[data.fsmGraphData[stateIdentifier].label] = {
                      eventData: data.fsmGraphData[stateIdentifier]["eventData"].split("#")[0],
                      executionVersion: data.fsmGraphData[stateIdentifier]["eventData"].split("#")[1],
                      status: data.fsmGraphData[stateIdentifier]["status"],
                      updatedAt: data.fsmGraphData[stateIdentifier]["updatedAt"]
                    };

                    eventNames[count] = data.fsmGraphData[stateIdentifier].label;
                    count++;
                }
            }
            eventNames.sort();
            for(var i=0; i<eventNames.length; i++){
                if(eventNames[i] != null && eventNames[i] != "") {
                    $('#event-data-select').append('<option>' + eventNames[i] + '</option>');
                }
            }
        }

        //This function used to unsideline a state of FSM. Triggered upon click on submit button
        function unSideline(){
            $("#unsideline-button-submit-ok-toggle").empty();
            $("#unsideline-button-submit-ok-toggle").append('<button type="button" id="fsm-modal-ok" class="btn btn-sm btn-primary center-block" display="none" data-dismiss="modal">Ok</button>');
            $.ajax({
                url:'${flux_api_url}/api/machines/'+document.getElementById("fsmId").innerHTML+'/'+document.getElementById("errored-state-list").value+'/unsideline',
                type: 'PUT',
                success: function(data,status,jqXHR) {
                    $("#unsideline-msg").append('<p>Request to unsideline sate:  '+document.getElementById("errored-state-list").value+' submitted successfully');
                    document.getElementById("unsideline-msg").style.display='block';
                    document.getElementById("errored-state-list").disabled = true;
                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    $('#unsideline-modal').modal('hide');
                    alert("Status: " + XMLHttpRequest.status + " Response:" + XMLHttpRequest.responseText);
                }
            });
        }

        //remove success message when modal goes hidden and also re-enable the state list box
        $('.modal').on('hidden.bs.modal', function() {
            $("#unsideline-msg").empty();
            $("#unsideline-button-submit-ok-toggle").empty();
            clearEventInfoModalParams();
            document.getElementById("event-data-select").selectedIndex = 0;
            document.getElementById("errored-state-list").selectedIndex = 0;
            document.getElementById("errored-state-list").disabled = false;
        }) ;

        //This function is for unsideline modal . It brings latest errored or sidelined states on every click on unsideline button and also recreate the submit button.
        function unsidelineModal() {
            $("#unsideline-button-submit-ok-toggle").append('<button type="button" id="fsm-modal-unsideline" class="btn btn-sm btn-primary center-block" onclick="unSideline()" data-toogle="modal" >Submit</button>');
        }

        function clearEventInfoModalParams() {
            $("#event-data-txt-box").empty();
            $("#event-execution-version-label").empty();
            $("#event-status-label").empty();
            $("#event-updated-at-label").empty();
        }

        //function to get event data on select of event name
        $(function() {
            $('.selectpicker').on('change', function(){
                clearEventInfoModalParams();
                var selectedEventName = $(this).find("option:selected").val();
                $("#event-data-txt-box").append(eventNameDataMap[selectedEventName].eventData);
                $("#event-execution-version-label").append(eventNameDataMap[selectedEventName].executionVersion);
                $("#event-status-label").append(eventNameDataMap[selectedEventName].status);
                $("#event-updated-at-label").append(getFormattedDate(new Date(eventNameDataMap[selectedEventName].updatedAt)));
            });
        });

        document.getElementById("graph-div").style.display = 'none';
        document.getElementById("alert-msg").style.display = 'none';
        document.getElementById("fsm-unsideline-btn").style.display = 'none';
        document.getElementById("fsm-event-details-btn").style.display='none';
        document.getElementById("unsideline-msg").style.display='none';
        document.getElementById("fsm-details").style.display='none';
        document.getElementById("legend-details").style.display='none';

        //useful when fsm-id is passed as request param
        getFSMData();

        //on pressing Enter key while "fsm-id" text box is in focus, click "get-fsm-data" button
        document.getElementById("fsm-id")
                .addEventListener("keyup", function(event) {
                    event.preventDefault();
                    if (event.keyCode == 13) {
                        document.getElementById("get-fsm-data").click();
                    }
                });

    </script>

<#include "./../footer.ftl">