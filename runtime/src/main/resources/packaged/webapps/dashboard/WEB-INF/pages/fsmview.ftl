<#include "./../header.ftl">

    <!-- The JoinJS CSS -->
    <link rel="stylesheet" type="text/css" href="/admin/fsm-dashboard/css/joint.css" />

    <!-- Keeping the Hystrix global stylesheet for some useful CSS types -->
    <link rel="stylesheet" type="text/css" href="/admin/hystrix-dashboard/css/global.css" />

    <!-- JointJS and Diagre JS files and their dependencies -->
    <script type="text/javascript" src="/admin/fsm-dashboard/js/jquery.min.js"></script>
    <script type="text/javascript" src="/admin/fsm-dashboard/js/lodash.min.js"></script>
    <script type="text/javascript" src="/admin/fsm-dashboard/js/backbone-min.js"></script>
    <script type="text/javascript" src="/admin/fsm-dashboard/js/joint.js"></script>
    <script type="text/javascript" src="/admin/fsm-dashboard/js/graphlib.core.js"></script>
    <script type="text/javascript" src="/admin/fsm-dashboard/js/dagre.core.js"></script>
    <script type="text/javascript" src="/admin/fsm-dashboard/js/joint.layout.DirectedGraph.js"></script>

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
        <div>
            <div id="info-div">
                <table class="table" style="width: 10%">
                    <tr>
                        <th align="left" style="border-top: none; vertical-align: middle;">FSM Name:</th>
                        <td id="fsmName" align="left" style="border-top: none; vertical-align: middle; max-width: 100px; word-wrap: break-word;"></td>
                    </tr>
                    <tr>
                        <th align="left" style="border-top: none; vertical-align: middle;">FSM Id:</th>
                        <td id="fsmId" align="left" style="border-top: none; vertical-align: middle;"></td>
                    </tr>
                    <tr>
                        <th align="left" style="border-top: none; vertical-align: middle;">Correlation Id:</th>
                        <td id="correlationId" align="left" style="border-top: none; vertical-align: middle; max-width: 100px; word-wrap: break-word;"></td>
                    </tr>
                    <tr>
                        <th align="left" style="border-top: none; vertical-align: middle;">FSM Version:</th>
                        <td id="fsmVersion" align="left" style="border-top: none; vertical-align: middle;"></td>
                    </tr>
                </table>
            </div>
            <div id="legend">
                <table class="table" style="width: 10%;">
                    <tr><th style="border-top: none">Legend</th></tr>
                    <tr><td style="border-top: none; vertical-align: middle"><div class="initialized">&nbsp;</div></td><td style="border-top: none;">&nbsp;Initialized</td></tr>
                    <tr><td style="border-top: none; vertical-align: middle"><div class="running">&nbsp;</div> </td><td style="border-top: none;">&nbsp;Running </td></tr>
                    <tr><td style="border-top: none; vertical-align: middle"><div class="completed">&nbsp;</div> </td><td style="border-top: none;">&nbsp;Completed</td></tr>
                    <tr><td style="border-top: none; vertical-align: middle"><div class="cancelled">&nbsp;</div> </td><td style="border-top: none;">&nbsp;Cancelled</td></tr>
                    <tr><td style="border-top: none; vertical-align: middle"><div class="errored">&nbsp;</div> </td><td style="border-top: none;">&nbsp;Errored </td></tr>
                    <tr><td style="border-top: none; vertical-align: middle"><div class="sidelined">&nbsp;</div> </td><td style="border-top: none;">&nbsp;Sidelined</td></tr>
                </table>
            </div>
        </div>

        <div id="audit-div">
            <!-- audit table creation is done from java script -->
        </div>
    </div>

    <script type="text/javascript">

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
                td.appendChild(document.createTextNode(JSON.stringify(new Date(auditRecord.createdAt))));
                tr.appendChild(td);

                var rowColor = getRowColor(auditRecord.stateStatus);
                if(rowColor != null)
                    tr.className = rowColor;

                //todo: assign color code based on rollback status as well
            });

            auditDiv.appendChild(table);
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
            	if (edgeData != null) {
	            	switch(edgeData.status) {
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

        function displayFsmInfo(fsmId, correlationId, fsmVersion, fsmName) {
            document.getElementById("fsmId").innerHTML = fsmId;
            document.getElementById("correlationId").innerHTML = correlationId;
            document.getElementById("fsmVersion").innerHTML = fsmVersion;
            document.getElementById("fsmName").innerHTML = fsmName;
        }

        function getFSMData() {
            $.ajax({
                url: '${flux_api_url}/api/machines/'+document.getElementById("fsm-id").value+'/fsmdata',
                type: 'GET',
                success: function(data, status, jqXHR) {
                    document.getElementById("graph-div").style.display = 'block';
                    document.getElementById("alert-msg").style.display = 'none';
                    layout(data.fsmGraphData,data.initStateEdges);
                    createAuditTable(data.fsmGraphData,data.auditData);
                    displayFsmInfo(data.stateMachineId, data.correlationId, data.fsmVersion, data.fsmName);
                    document.getElementById("info-div").style.display = 'block';
                    document.getElementById("legend").style.display = 'block';
                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    alert("Status: " + XMLHttpRequest.status + " Response:" + XMLHttpRequest.responseText);
                }
            });
        }

        document.getElementById("graph-div").style.display = 'none';
        document.getElementById("alert-msg").style.display = 'none';
        document.getElementById("info-div").style.display = 'none';
        document.getElementById("legend").style.display = 'none';

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