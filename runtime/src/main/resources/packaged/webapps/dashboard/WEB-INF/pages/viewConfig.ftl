<#include "./../header.ftl"> 

<H1>Handler: ${handlerName}</H1>

<textarea id="XMLFileContents">
	${XMLFileContents}
</textarea>

<script>
	var editor = CodeMirror.fromTextArea(document.getElementById("XMLFileContents"), {
		mode: "application/xml",
		lineNumbers: true,
		lineWrapping: true,
		readOnly: true
	});
	var hlLine = editor.addLineClass(0, "background", "activeline");
	editor.on("cursorActivity", function() {
		var cur = editor.getLineHandle(editor.getCursor().line);
		if (cur != hlLine) {
			editor.removeLineClass(hlLine, "background", "activeline");
			hlLine = editor.addLineClass(cur, "background", "activeline");
		}
	});
</script>

<#include "./../footer.ftl">
