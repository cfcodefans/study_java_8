<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>JWChat - Collecties</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <script src="switchStyle.js"></script>
    <script src="jsjac.js"></script>
		<script>

var ie5 = document.all&&document.getElementById;

function init() {
  var myTable = document.getElementById('myTable');
  
  if (parent.top.srcW.is.ie) {
    myTable.cellSpacing = 0;
    myTable.cellPadding = 0;
    myTable.border = 0;
    myTable.width = '100%';	
  }

  var rows = myTable.getElementsByTagName("TBODY").item(0).childNodes;
  for (var i=0; i<rows.length; i++) {
    rows[i].onmouseover = highlightRow;
    rows[i].onmouseout = unhighlightRow;
    rows[i].onclick = rowClicked;
    rows[i].title = "klik om een collectie te selecteren";
  }
}
function highlightRow(e) {
  var row = ie5 ? event.srcElement.parentNode : e.target.parentNode;
  row.className = 'highlighted';
}
function unhighlightRow(e) {
  var row = ie5 ? event.srcElement.parentNode : e.target.parentNode;
  if (row != selectedRow)
    row.className = '';
}

var selectedRow;
function rowClicked(e) {
  if (selectedRow)
    selectedRow.className = '';
  selectedRow = ie5 ? event.srcElement.parentNode : e.target.parentNode;
  selectedRow.className = 'highlighted';
  
  srcW = parent.srcW;

  // get collection
  var aIQ = new JSJaCIQ();
  aIQ.setType('get');
  aIQ.setTo(srcW.loghost);
  var aNode = 
	aIQ.appendNode(
	  'retrieve', 
	  {'xmlns': 'http://jabber.org/protocol/archive',
	   'with': parent.jid,
	   'start': selectedRow.getAttribute('start')});

  srcW.Debug.log(aIQ.xml(),2);
  
  target = parent;

  srcW.con.send(aIQ,target.handleCollGet);
}
		</script>

		<style type="text/css">
			body { background-color: white; }
			th { 
			font-size: 0.8em; 
			border-bottom: 1px solid black;
			padding: 2px;
			}
			td {
			border-bottom: 1px solid black;
			padding: 2px;
			cursor: default;
			}
			tr.highlighted {
			color: highlighttext;
			background-color: highlight;
			}
		</style>
  </head>
  <body>
  </body>
</html>
