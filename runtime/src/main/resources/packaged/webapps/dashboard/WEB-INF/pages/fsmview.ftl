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
                <input class="form-control" type="text" placeholder="FSM Id" id="fsm-id"/>
            </div>
            <div class="Cell"><button class="btn btn-sm btn-primary" onclick="getFSMData()">Show FSM</button></div>
        </div>
    </div>
    <div>&nbsp;</div>
    <div>&nbsp;</div>
    <div id="alert-msg" class="alert alert-danger fade in" style="margin-left: 130px;"></div>
    <div>&nbsp;</div>
    <div id="graph-div">
        <div class="paper" id="fsmcanvas" style="width: 1200px; height: 600px; overflow: auto;"></div>
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

        // Build the DAG from the specified adjacency list
        function buildGraphFromAdjacencyList(adjacencyList) {
            var elements = [];
            var links = [];
            _.each(adjacencyList, function(edges, parentElementLabel) {
                elements.push(makeState(parentElementLabel));
                _.each(edges, function(childElementLabel) {
                    links.push(makeEdge(parentElementLabel, childElementLabel));
                });
            });
            // Links must be added after all the elements. This is because when the links
            // are added to the graph, link source/target
            // elements must be in the graph already.
            return elements.concat(links);
        }

        // Draw a State in the FSM
        function makeState(label) {
            var maxLineLength = _.max(label.split(' '), function(l) {
                return l.length;
            }).length;
            // Compute width/height of the rectangle based on the number
            // of lines in the label and the letter size. 0.6 * letterSize is
            // an approximation of the monospace font letter width.
            var letterSize = 10;
            var width = 1.0 * (letterSize * (0.6 * maxLineLength + 1));
            var height = 1.0 * ((label.split(' ').length + 1) * letterSize);
            return new joint.shapes.basic.Circle({
                id: label,
                size: { width: width, height: width },
                attrs: {
                    text: { text: label.replace(' ', '\n'), 'font-size': letterSize, 'font-family': 'monospace', fill: 'white' },
                    circle: {
                        fill: '#FE854F',
                        width: width,
                        height: height,
                        stroke: 'none'
                    }
                }
            });
        }

        // Draw an Edge in the DAG
        function makeEdge(parentElementLabel, childElementLabel) {
            // Edge details is of the form TargetNode<colon>EdgeLabel
            var edgeDetails = childElementLabel.split(":");
            return new joint.shapes.fsa.Arrow({
                source : { id : parentElementLabel },
                target : { id : edgeDetails[0] },
                labels : [ {
                    position : 0.5,
                    attrs : {
                        text : { text : edgeDetails[1].replace(' ', '\n'), 'font-size': 8,}
                    }
                } ],
                attrs: {
                    '.marker-target': { d: 'M 4 0 L 0 2 L 4 4 z' }
                },
                smooth: true
            });
        }

        // Function to draw the DAG
        function layout(adjacencyList) {
            var cells = buildGraphFromAdjacencyList(adjacencyList);
            graph.resetCells(cells);
            joint.layout.DirectedGraph.layout(graph, {
                nodeSep: 100,
                edgeSep: 20,
                rankDir: "LR"
            });
        }

        function getFSMData() {
            $.ajax({
                url: 'http://localhost:9998/api/machines/'+document.getElementById("fsm-id").value+'/fsmdata',
                type: 'GET',
                success: function(data, status, jqXHR) {
                    var json = JSON.parse(data);
                    if(json != null && Object.keys(json).length > 0) {
                        document.getElementById("graph-div").style.display = 'block';
                        document.getElementById("alert-msg").style.display = 'none';
                        layout(json);
                    } else {
                        document.getElementById("alert-msg").innerHTML = "State machine with Id: "+document.getElementById("fsm-id").value+" not found.";
                        document.getElementById("graph-div").style.display = 'none';
                        document.getElementById("alert-msg").style.display = 'inline';
                    }
                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    alert("Status: " + textStatus); alert("Error: " + errorThrown);
                }
            });
        }

        document.getElementById("graph-div").style.display = 'none';
        document.getElementById("alert-msg").style.display = 'none';

    </script>

<#include "./../footer.ftl">