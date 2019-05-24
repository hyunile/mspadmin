<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

	<meta http-equiv="content-type" content="text/html; charset=iso-8859-1"/>
	<title>jQuery async treeview11</title>
	
	<link rel="stylesheet" href="jquery.treeview.css" />
    
	<script src="jquery.js" type="text/javascript"></script>
	<script src="jquery.treeview.js" type="text/javascript"></script>
	<script src="jquery.treeview.edit.js" type="text/javascript"></script>
	<script src="jquery.treeview.async.js" type="text/javascript"></script>
	
	<script type="text/javascript">
	function initTrees() {
		$("#black").treeview({
			url: "source.jsp"
		})
		
		$("#mixed").treeview({
			url: "source.jsp",
			// add some additional, dynamic data and request with POST
			ajax: {
				data: {
					"additional": function() {
						return "yeah: " + new Date;
					}
				},
				type: "post"
			}
		});
	}
	$(document).ready(function(){
		initTrees();
		/*
		$("#refresh").click(function() {
			$("#black").empty();
			$("#mixed").empty();
			initTrees();
		});
		*/
	});
	</script>
	
	</head>
	<body>
	
	<h1 id="banner"><a href="http://bassistance.de/jquery-plugins/jquery-plugin-treeview/">jQuery Treeview Plugin</a> Demo</h1>
	<div id="main">
	
	<ul>
		<li><a href=".">Main Demo</a></li>
		<li><a href="source.phps">Server component used</a></li>
	</ul>
		
	<h4>Lazy-loading tree</h4>
	
	<ul id="black">
	</ul>
	
	<h4>Mixed pre and lazy-loading</h4>
	
	<ul id="mixed">
		<li><span>Item 1</span>
			<ul>
				<li><span>Item 1.0</span>
					<ul>
						<li><span>Item 1.0.0</span></li>
					</ul>
				</li>
				<li><span>Item 1.1</span></li>
			</ul>
		</li>
		<li id="36" class="hasChildren">
			<span>Item 2</span>
			<ul>
				<li><span class="placeholder">&nbsp;</span></li>
			</ul>
		</li>
		<li>
			<span>Item 3</span>
		</li>
	</ul>
	
	<button id="refresh">Refresh both Trees</button>
		
</div>
 
</body></html>