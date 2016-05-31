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
            <div class="Cell">
                <label> Enter FSM Id </label>
            </div>
            <div class="Cell">
                <input type="text" placeholder="FSM Id" id="fsm-id"/>
            </div>
            <div class="Cell"><button onclick="getFSMData()">Get fsm visual</button></div>
        </div>
    </div>

    <script type="text/javascript">
        function getFSMData() {
            window.location.assign("/admin/fsmview/"+document.getElementById("fsm-id").value);
        }
    </script>

<#include "./../footer.ftl">