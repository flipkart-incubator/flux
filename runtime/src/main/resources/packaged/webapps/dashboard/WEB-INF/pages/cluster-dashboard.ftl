<#include "./../header.ftl">

<!-- Setup base for everything -->
<link rel="stylesheet" type="text/css" href="/admin/hystrix-dashboard/css/global.css" />

<!-- Our custom CSS -->
<link rel="stylesheet" type="text/css" href="/admin/hystrix-dashboard/monitor/monitor.css" />

<!-- d3 -->
<script type="text/javascript" src="/admin/hystrix-dashboard/js/d3.v2.min.js"></script>

<!-- Javascript to monitor and display -->
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" type="text/javascript"></script>
<script type="text/javascript" src="/admin/hystrix-dashboard/js/jquery.tinysort.min.js"></script>
<script type="text/javascript" src="/admin/hystrix-dashboard/js/tmpl.js"></script>

<!-- HystrixCommand -->
<script type="text/javascript" src="/admin/hystrix-dashboard/components/hystrixCommand/hystrixCommand.js?v=2"></script>
<link rel="stylesheet" type="text/css" href="/admin/hystrix-dashboard/components/hystrixCommand/hystrixCommand.css" />

<!-- HystrixThreadPool -->
<script type="text/javascript" src="/admin/hystrix-dashboard/components/hystrixThreadPool/hystrixThreadPool.js"></script>
<link rel="stylesheet" type="text/css" href="/admin/hystrix-dashboard/components/hystrixThreadPool/hystrixThreadPool.css" />

<div class="container">
    <div class="row">
        <div class="menubar">
            <div class="title">
                Circuit
            </div>
            <div class="menu_actions">
                Sort:
                <a href="javascript://" onclick="hystrixMonitor.sortByErrorThenVolume();">Error then Volume</a> |
                <a href="javascript://" onclick="hystrixMonitor.sortAlphabetically();">Alphabetical</a> |
                <a href="javascript://" onclick="hystrixMonitor.sortByVolume();">Volume</a> |
                <a href="javascript://" onclick="hystrixMonitor.sortByError();">Error</a> |
                <a href="javascript://" onclick="hystrixMonitor.sortByLatencyMean();">Mean</a> |
                <a href="javascript://" onclick="hystrixMonitor.sortByLatencyMedian();">Median</a> |
                <a href="javascript://" onclick="hystrixMonitor.sortByLatency90();">90</a> |
                <a href="javascript://" onclick="hystrixMonitor.sortByLatency99();">99</a> |
                <a href="javascript://" onclick="hystrixMonitor.sortByLatency995();">99.5</a>
            </div>
            <div class="menu_legend">
                <span class="success">Success</span> | <span class="latent">Latent</span> | <span class="shortCircuited">Short-Circuited</span> | <span class="timeout">Timeout</span> | <span class="rejected">Rejected</span> | <span class="failure">Failure</span> | <span class="errorPercentage">Error %</span>
            </div>
        </div>
    </div>
    <div id="dependencies" class="row dependencies"><span class="loading">Loading ...</span></div>

    <div class="spacer"></div>
    <div class="spacer"></div>

    <div class="row">
        <div class="menubar">
            <div class="title">
                Thread Pools
            </div>
            <div class="menu_actions">
                Sort: <a href="javascript://" onclick="dependencyThreadPoolMonitor.sortAlphabetically();">Alphabetical</a> |
                <a href="javascript://" onclick="dependencyThreadPoolMonitor.sortByVolume();">Volume</a> |
            </div>
        </div>
    </div>
    <div id="dependencyThreadPools" class="row dependencyThreadPools"><span class="loading">Loading ...</span></div>
</div>



<script>
    /**
     * Queue up the monitor to start once the page has finished loading.
     *
     * This is an inline script and expects to execute once on page load.
     */

    /**
     * Method to parse query string
     * For example... passing a name parameter of "name1" will return a value of "100", etc.
     * page.htm?name1=100&name2=101&name3=102
     * @param name 	Name of the parameter
     **/
    function getQueryStringNameValue(name) {
        var winURL = window.location.href;
        var queryStringArray = winURL.split("?");
        var nameValue = null;
        if (queryStringArray.length > 1) {
            var queryStringParamArray = queryStringArray[1].split("&");
            for (var i=0; i<queryStringParamArray.length; i++) {
                queryStringNameValueArray = queryStringParamArray[i].split("=");
                if (name == queryStringNameValueArray[0]) {
                    nameValue = queryStringNameValueArray[1];
                }
            }
        }
        return nameValue;
    }

    // filtered commands
    var commands = [];
    var commandsStr = getQueryStringNameValue("commands");
    if (commandsStr) commands = commandsStr.split(",");

    // handler for commands
    var hystrixMonitor = new HystrixCommandMonitor('dependencies', {includeDetailIcon:false}, commands);

    // command stream
    var commandStream =  "/admin/turbine.stream.command";

    $(window).load(function() { // within load with a setTimeout to prevent the infinite spinner
        setTimeout(function() {

            // sort by error+volume by default
            hystrixMonitor.sortByErrorThenVolume();

            // start the EventSource which will open a streaming connection to the server
            var source = new EventSource(commandStream);

            // add the listener that will process incoming events
            source.addEventListener('message', hystrixMonitor.eventSourceMessageListener, false);

            source.addEventListener('error', function(e) {
                if (e.eventPhase == EventSource.CLOSED) {
                    // Connection was closed.
                    console.log("Connection was closed on error: " + e);
                } else {
                    console.log("Error occurred while streaming: " + e);
                }
            }, false);

        },0);
    });

    // filtered thread pools
    var threadPools = [];
    var threadPoolsStr = getQueryStringNameValue("tp");
    if (threadPoolsStr) threadPools = threadPoolsStr.split(",");

    // handler for thread pools
    var dependencyThreadPoolMonitor = new HystrixThreadPoolMonitor('dependencyThreadPools', threadPools);

    // thread pool stream
    var poolStream = "/admin/turbine.stream.tp";

    $(window).load(function() { // within load with a setTimeout to prevent the infinite spinner
        setTimeout(function() {

            // sort by volume
            dependencyThreadPoolMonitor.sortByVolume();

            // start the EventSource which will open a streaming connection to the server
            var source = new EventSource(poolStream);

            // add the listener that will process incoming events
            source.addEventListener('message', dependencyThreadPoolMonitor.eventSourceMessageListener, false);

            // error handler
            source.addEventListener('error', function(e) {
                if (e.eventPhase == EventSource.CLOSED) {
                    // Connection was closed.
                    console.log("Connection was closed on error: " + e);
                } else {
                    console.log("Error occurred while streaming: " + e);
                }
            }, false);

        },0);
    });

    //Read a page's GET URL variables and return them as an associative array.
    // from: http://jquery-howto.blogspot.com/2009/09/get-url-parameters-values-with-jquery.html
    function getUrlVars()
    {
        var vars = [], hash;
        var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
        for(var i = 0; i < hashes.length; i++)
        {
            hash = hashes[i].split('=');
            vars.push(hash[0]);
            vars[hash[0]] = hash[1];
        }
        return vars;
    }

</script>


<#include "./../footer.ftl">
