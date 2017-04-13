var executeToken = 'store://1FZJ7ALMu33wuj1FHYKb0K';
var selectToken =  'store://K8uTlT2JclQFNzEvxQm72H';
var updateToken = 'store://Wx9gUUrEPlOw2eb8lmmphL';
var globalBaseURI1 ='http://rdf.onekin.org/';
var globalBaseURI ='http://localhost:8080/ldw/';
var ldwURL=globalBaseURI+"ldw/registerwrapper";

var globalBaseURI1 ='http://rdf.onekin.org/';
var globalBaseURI ='http://localhost:8080/ldw/';

var url; 
var uri; 
var credentials;

function setNewData(example, credentiallist, wrapper){
try{
	credentials =credentiallist;
	url = wrapper;
	uri = example;
	var h = 'Old%20URI%20Example%3A%20%3Ca%20href%3D%22%23OLDURIEXAMPLE%23%22%20target%3D%22_blank%22%3E%23OLDURIEXAMPLE%23%3C%2Fa%3E%20%3Cbr%2F%3E%0ANew%20URI%20Example%3A%20%3Cinput%20id%3D%22URIEXAMPLE%22%20type%3D%22text%22%20value%3D%22%23OLDURIEXAMPLE%23%22%3E%0A%3Chr%2F%3E%0ACredentials%3A%20%20%0A%23LIST%23%0A%3Chr%2F%3E%0A%20%20%3Cinput%20type%3D%22submit%22%20value%3D%22Submit%22%20onclick%3D%22showming()%22%3E%0A%0A%0A';
	credentiallist = credentiallist.replace("[","");
	credentiallist = credentiallist.replace("]","");
	var input = undecode('%23NAME%23%3A%3Cinput%20id%3D%22%23NAME%23%22%20type%3D%22text%22%20name%3D%22%23NAME%23%22%20value%3D%22%23VALUE%23%22%3E%20%3Cbr%3E');
	credentials = credentiallist.split(",");
	var first = true;
	var lists = "";
	for (var i = 0; i<credentials.length; i++){
		var l = credentials[i];
		l= l.trim();
		if (first){
			var inp = input;
			inp = inp.replace (new RegExp("#NAME#", 'g'), l);
			first=false;
		}else{
			inp = inp.replace (new RegExp("#VALUE#", 'g'), l);
			lists += inp;
			first=true;
		}
	}
	var form = undecode(h);
	form = form.replace(new RegExp("#LIST#",'g'), lists);
	form = form.replace(new RegExp("#OLDURIEXAMPLE#",'g'), uri);
	modal_init();
	createModalContent('Set new credentials', form);
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

function sendLDW(){
  try{
  var url3 = ldwURL+"?file="+selectToken+"&env=&type=ODT";
 window.open(url3,'_four');
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}


///////////////////////////
/////MODAL window Manager  seccion
//////////////////////////
var cont = 0;
function infoit(txt){
   console.info (cont +'>> '+txt);
   cont ++;
}

function showming(){
	infoit(url);	
	callURL(url, showme);
}

function showme(xml){
	for (var i = 0; i<credentials.length; i=i+2){
		var l = credentials[i];
		var q = anchorget(l);
		var newvalue = q.value;
		var oldvalue = credentials[i+1];
		oldvalue = 'default="'+oldvalue.trim();
		newvalue = 'default="'+newvalue.trim();
		infoit (newvalue+'=>'+oldvalue);
		xml = xml.replace(new RegExp(oldvalue, 'g'), newvalue);
	}
	var q = anchorget('URIEXAMPLE');
	var newvalue = q.value;
	xml = xml.replace(new RegExp(uri, 'g'), newvalue);
	
	infoit(xml);
	savestorageWrapper(xml);
	sendLDW();
}

function anchorget (name){
  try{
  return document.getElementById(name);
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

function createModal (){
try{
var div1 = document.createElement('div');
div1.id='modal_wrapper';
div1.class='';
var div2 = document.createElement('div');
div2.id='modal_window';
div2.innerHTML='<h3><span id="modaltitle">Fill in the form: <span></h3><span id="modal_content" style="text-align: left;">... </span><button id="modal_close" >Cancel</button>';
div1.appendChild(div2);
document.body.appendChild(div1);
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

function createModalContent (name, html){
try{
anchorget ('modal_content').innerHTML = html;
anchorget ('modaltitle').innerHTML = name;
openModal();
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

var openModal = function(e) {
anchorget ('modal_wrapper').className = "overlay";
//e.preventDefault ? e.preventDefault() : e.returnValue = false;
};

var closeModal = function(e) {
anchorget ('modal_wrapper').className = "";
//e.preventDefault ? e.preventDefault() : e.returnValue = false;
};

var clickHandler = function(e) {
if (!e.target) e.target = e.srcElement;
if (e.target.tagName == "DIV") {
if (e.target.id != "modal_window") ;//closeModal(e);
}
};

var keyHandler = function(e) {
if (e.keyCode == 27) openModal(e);
};

var modal_init = function() {
try{
createModal ();
if (document.addEventListener) {
anchorget ("modal_close").addEventListener("click", closeModal, false);
document.addEventListener("click", clickHandler, false);
document.addEventListener("keydown", keyHandler, false);
} else {
anchorget ("modal_close").attachEvent("onclick", closeModal);
document.attachEvent("onclick", clickHandler);
document.attachEvent("onkeydown", keyHandler);
}
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

///////////

function undecode (coded){
  try{
var undecoded = decodeURI(coded);
var pattern = "%3A", re = new RegExp(pattern, "g"), value = ':';
undecoded=undecoded.replace (re, value);
pattern = "%2F", re = new RegExp(pattern, "g"), value = '/';
undecoded=undecoded.replace (re, value);
pattern = "%3D", re = new RegExp(pattern, "g"), value = '=';
undecoded=undecoded.replace (re, value);
pattern = "%40", re = new RegExp(pattern, "g"), value = '@';
undecoded=undecoded.replace (re, value);
pattern = "%23", re = new RegExp(pattern, "g"), value = '#';
undecoded=undecoded.replace (re, value);
pattern = "%3B", re = new RegExp(pattern, "g"), value = ';';
undecoded=undecoded.replace (re, value);
pattern = "%2C", re = new RegExp(pattern, "g"), value = ',';
undecoded=undecoded.replace (re, value);
pattern = "%3F", re = new RegExp(pattern, "g"), value = '?';
undecoded=undecoded.replace (re, value);
pattern = "%2B", re = new RegExp(pattern, "g"), value = '+';
undecoded=undecoded.replace (re, value);
pattern = "%26", re = new RegExp(pattern, "g"), value = '&';
undecoded=undecoded.replace (re, value);
return undecoded;
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}


//////////

function readStorageTokens(){
try{
var tokens = readData ('storagetokens');
//var tokens = null;
if (tokens == null){
newstorage();
}
return tokens;
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

function newed(response) {
try{
var inserted = response.query.results.inserted;
executeToken = inserted.execute;
selectToken = inserted.select;
updateToken = inserted.update;
writeData ('storagetokens', {'executeToken':executeToken, 'selectToken': selectToken, 'updateToken':updateToken});
consoleTokens();
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

function newstorage() {
try{
var url = "https://query.yahooapis.com/v1/public/yql?q=insert%20into%20yql.storage.admin%20(value)%20values%20('iker%20')&format=json&callback=";
callURLJSON(url, function (resp) {newed (resp);});
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

function savestorageWrapper(xml) {
try{
var value = xml;
var name = updateToken;
// If it gets too big it needs to be a post...
var url = "https://query.yahooapis.com/v1/public/yql?q=update%20yql.storage%20set%20value%3D%40value%20where%20name%3D%40name&format=json&diagnostics=false&callback=&value=" + encodeURIComponent(value) + "&name=" + encodeURIComponent(name);
callURLJSON(url, function (resp) { infoit (JSON.stringify(resp));});
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

function loadstorageWrapper(callback) {
try{
var tokens = readStorageTokens();
selectToken = tokens.selectToken;
var name = selectToken;
var url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yql.storage%20where%20name%3D%40name&format=json&diagnostics=false&callback=&name="+name;
callURLJSON(url, function (resp) {callback(resp.query.results.result.value)});
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

function createCoupledWrapper (){
try{
var tableTemplate= undecode('%3C%3Fxml%20version%3D%221.0%22%20encoding%3D%22UTF-8%22%3F%3E%0A%20%20%20%20%20%20%3Ctable%20%23NS%23%20xmlns%3D%22http%3A%2F%2Fquery.yahooapis.com%2Fv1%2Fschema%2Ftable.xsd%22%3E%0A%20%20%20%20%3Cmeta%3E%23METAS%23%0A%23LOWERING%23%0A%20%20%20%20%3C%2Fmeta%3E%0A%20%20%20%20%3Cbindings%3E%0A%20%20%20%20%20%20%20%20%3Cselect%20itemPath%3D%22results.*%22%20produces%3D%22XML%22%3E%0A%20%20%20%20%20%20%20%20%20%20%20%20%3Cinputs%3E%0A%23INPUTS%23%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%3C%2Finputs%3E%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%3Cexecute%3E%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%3C!%5BCDATA%5B%0A%20var%20loweringselect%20%3D%20%22env%20%27store%3A%2F%2Fdatatables.org%2Falltableswithkeys%27%3B%20%23LAUNCHEDQUERY%23%22%3B%0A%20var%20loweringparams%20%3D%7B%7D%3B%0A%20%23PARAMSPARAMS%23%0A%20var%20loweringquery%20%3D%20y.query%20(loweringselect%2Cloweringparams)%3B%20%0A%20response.object%20%3D%20%20loweringquery.results%3B%0A%20%5D%5D%3E%0A%20%20%20%20%20%20%20%20%20%20%20%20%3C%2Fexecute%3E%0A%20%20%20%20%20%20%3C%2Fselect%3E%0A%20%23LIFTING%23%0A%20%20%20%20%3C%2Fbindings%3E%20%0A%20%3C%2Ftable%3E%0A%0A');
if (globalSignaler[XMLREANNOTATION]){
tableTemplate=ldw.getXML ();
var begin = tableTemplate.indexOf('<function');
var end = tableTemplate.indexOf('</bindings');
tableTemplate = tableTemplate.substr(0,begin)+'#LIFTING#'+tableTemplate.substr(end);
}
annotateLowering();
tableTemplate = completeTableAnnotation (tableTemplate);

return tableTemplate;
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

///////////////////
////COMUNICATION   seccion
//////////////////

function callURL(url, callback){
try{
	infoit ('URLnormal calling: '+url);
var xmlhttp = createCrossDomainRequest();
xmlhttp.open('GET', url, true);
xmlhttp.onreadystatechange = function() {
if (xmlhttp.readyState == 4) {
if(xmlhttp.status == 200) {
callback(xmlhttp.responseText);
}else{
alert("Problem retrieving data. Status: "+xmlhttp.status + ' | '+xmlhttp.statusText);
}
}
};
xmlhttp.send(null);
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

function callURLJSON(url, callback){
try{
//url = url.trim();
var xmlhttp = createCrossDomainRequest();
xmlhttp.open('GET', url, true);
xmlhttp.onreadystatechange = function() {
if (xmlhttp.readyState == 4) {
if(xmlhttp.status == 200) {
var obj = JSON.parse(xmlhttp.responseText);
callback (obj);
}else{
alert("Problem retrieving data. Status: "+xmlhttp.status + ' | '+xmlhttp.statusText);
}
}
};
xmlhttp.send(null);
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

function createCrossDomainRequest(){
try{
var request;
if (window.XDomainRequest){   //IE8
request = new window.XDomainRequest();
} else if (window.XMLHttpRequest)
{// code for all new browsers
request=new XMLHttpRequest();
}
else if (window.ActiveXObject)
{// code for IE5 and IE6
request=new ActiveXObject("Microsoft.XMLHTTP");
}
if (request!=null)
{
return request;
}
else
{
alert("Your browser does not support XMLHTTP.");
}
}catch(err){infoit (err.lineNumber+' :: '+ err.message);}}

///////////////////////

