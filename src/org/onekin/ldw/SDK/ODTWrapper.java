package org.onekin.ldw.SDK;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.onekin.ldw.WrapperManager;
import org.onekin.ldw.util.Cons;
import org.onekin.ldw.util.Utils;

public class ODTWrapper implements Wrapper {
	public  String type ="ODT"; 
	private Map<String, URILookup> exampleURIs= new HashMap<String, URILookup>();
	public String wrapperSourceCode;
	
	
	private boolean LiftingExists= false;
	private String wrapperId;
	private String service;
	private JSONObject jsMetadata = new JSONObject(); //Metadata
	private ArrayList<String> propertyList = new ArrayList<String>(); //Properties
	private Map<String, String> interlinkList = new HashMap<String, String>(); //Interlinks: target, property
	private ArrayList<String> ontologyList = new ArrayList<String>(); //Ontologies
	private JSONObject compatibilityStatus = new JSONObject(); 
	private JSONObject exampleLowering = null;
	private JSONObject exampleLifting = null;
	private JSONObject qualityMetadata = new JSONObject();
	private HashMap qualityparams = new HashMap();

	private long[] times = { 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000};
	private int timesCont = -1;
	private Long latency = new Long(100000);
	private String typeclass; 
	private JSONParser parser = new JSONParser();

	/* json.put("void:inDataset", voiddesc);
		    	json.put("wrapperid",wrapperId);
		    	json.put("service",service);
		    	json.put("type",type);
		    	json.put("wrappername", name);
		    	json.put("username", this.userId);
	    	    json.put("uripattern", URIPattern);
		    	json.put("uriexample", examples);
		    	json.put("wrapperurl", examples);
		    	json.put("datahauburl", examples);
		    	json.put("description",  description);		    	
		    	//ODT specific
	    	    odturl  	    	    inputkeys
		    	json.put("documentationURL",documentationURL);
		    	json.put("apiKeyURL",apiKeyURL);
		    	json.put("apiURL",apiKeyURL);
				json.put("odtname", Cons.NOPROVIDED);
		    	json.put("author",author);  
		    	*/
	
	public ODTWrapper (String wrapperLocator){   //String userId, 
	if (wrapperLocator.startsWith("http")){
//		setMetadata("wrapperurl", wrapperLocator);
		//this.userId=userId;
		String wrapper = Utils.calling(wrapperLocator);
		if (!Utils.isVoidOrNull(wrapper)){
			jsMetadata = parseWrapperToJSON (wrapper);
			wrapperSourceCode= wrapper;
	//		setMetadata("wrapperurl", wrapperLocator);
	
			List<String> URIExamples = extractURIExamples(wrapper);
	    	for (String URIExample: URIExamples){
	    		URILookup exURI=null;
				try {
					exURI = newURILookup (new URI(URIExample));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
	    		exampleURIs.put(URIExample, exURI);
	    	}	
	    	String voidTXT = getVoidString();
	    	JSONObject voiddesc = new JSONObject();		
	    	try {
				voiddesc = (JSONObject)parser.parse(voidTXT);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
	    	setMetadata("void:inDataset", voiddesc);	    	
		}
	}else{
	   	String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yql.storage%20where%20name%3D'"+ wrapperLocator +"'&diagnostics=true&debug=true";
    	String wrapper= Utils.calling (url);
    	int begining=wrapper.indexOf("<value>");
    	int ending = wrapper.indexOf("</value>");
    	if (begining==-1 || ending==-1) return;
    	wrapper=wrapper.substring(begining+7, ending);
    	wrapper=wrapper.replaceAll("&lt;", "<");
    	wrapper=wrapper.replaceAll("&gt;", ">");
    	wrapper=wrapper.replaceAll("&amp;", "&");
    	//this.userId=userId;
		if (!Utils.isVoidOrNull(wrapper)){
			jsMetadata = parseWrapperToJSON (wrapper);
			wrapperSourceCode= wrapper;
		//	setMetadata("wrapperurl", url);
			List<String> URIExamples = extractURIExamples(wrapper);
	    	for (String URIExample: URIExamples){
	    		URILookup exURI=null;
				try {
					exURI = newURILookup (new URI(URIExample));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
	    		exampleURIs.put(URIExample, exURI);
	    	}
	    	String voidTXT = getVoidString();
	    	JSONObject voiddesc = new JSONObject();
	    	try {
				voiddesc = (JSONObject)parser.parse(voidTXT);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
	    	setMetadata("void:inDataset", voiddesc);
		}		
	}
	}
	
	
	@Override
	public String getSourceCode(){
		return wrapperSourceCode;		
	}
	
	

public List<URILookup> getURIExamples(){
	List<URILookup> list = new ArrayList<URILookup>();
	for (Entry ent : this.exampleURIs.entrySet()){
		list.add((URILookup)ent.getValue());
	}
	return list;
}

@Override
public boolean addURIExample(URI exampleURI) {
	URILookup ex = new URILookup (exampleURI, getMetadataString("uripattern"), getMetadataString("wrapperurl"));
	if (!Utils.isVoidOrNull(ex)){
		exampleURIs.put(exampleURI.toString(), ex);
		return true;
	}
	return false;
}

	@Override
	public JSONArray getHydraApiDocumentation (){
		return (JSONArray)ApiDocumentation();
	}
		
	/*	public JSONObject getHydraEntryPoint (){
		return (JSONObject)ApiDocumentation().get ("hydra:entrypoint");
	}
	public JSONObject getHydraOperation (){
		return (JSONObject)ApiDocumentation().get ("hydra:operation");
	}
	public JSONObject getHydraReturns (){
		return (JSONObject)ApiDocumentation().get ("hydra:returns");
	}
	public JSONObject getHydraExpects (){
		return (JSONObject)ApiDocumentation().get ("hydra:expects");
	}
*/
	///////
	
	private String getDescription (){
		return getVoID().get("dcterms:description").toString();
	}
	
	private String getService (){
		return getMetadataString("service");
	}

	//////////////////
	

private Boolean isVariable (String name){
	URILookup uexample = getURIExamples().get(0);
	if (Utils.isVoidOrNull(uexample)){return false;}
	Set<String> params = uexample.getVariables ();
	Iterator it = params.iterator();
	while (it.hasNext()){
		String n = it.next().toString();
		if (n.endsWith(name)) return true;
	}
	return false;
}

private JSONObject getLiftingMetadata (){
	JSONObject js = getMetadata("liftingmetadata");
	return js;
}

private JSONArray getSupportedProperties(JSONObject individual){
	JSONArray res = new JSONArray();
	JSONObject context = (JSONObject) individual.get("@context"); // new JSONObject();
	for (Object keyS : individual.keySet()){
		String key = (String) keyS;
		if (key.startsWith("@")) continue;
		JSONObject json = new JSONObject();
		json.put("@type", "hydra:SupportedProperty");
		 if (isLink(context, key)){
			 json.put("@type","hydra:Link");
		 }else{
			 json.put("@type","rdf:Property");
		 }
		 json.put("required", false);
		 json.put("readonly", false);
		 json.put("writeonly", false);
		 json.put("property", key);
		 json.put("hydra:title", key);
		 json.put("description", key);
		 res.add(json);
	}
	return res;
}

private Boolean isLink (JSONObject context, String property){
	 JSONObject j = (JSONObject)context.get(property);
	 if (j == null) return false;
	 String t =(String)j.get("@type");
	 if (Utils.isVoidOrNull(t)) return false;
	 if (t.equals("@id")) return true;
	 return false;
}

private JSONArray ApiDocumentation(){
	String txt = getAPIDoc();
	JSONArray doc = new JSONArray();
	try {
		doc = (JSONArray)parser.parse(txt);
	} catch (ParseException e) {
		e.printStackTrace();
	}
	return doc;
}
@Override
public JSONArray getQualityMeassures(){
	String txt = getQualityMeassuresString();
	JSONArray doc = new JSONArray();
	try {
		doc = (JSONArray)parser.parse(txt);
	} catch (ParseException e) {
		e.printStackTrace();
	}
	return doc;
}
	/////////////////

	@Override
	public JSONObject getVoID() {
		String voidTXT = getVoidString();
    	JSONObject voiddesc = new JSONObject();		
    	try {
			voiddesc = (JSONObject)parser.parse(voidTXT);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
    	setMetadata("void:inDataset", voiddesc);	    	
		return voiddesc; //getMetadata("void:inDataset");		
	}

@Override
public JSONObject getMetadata() {
	return this.jsMetadata;
}

@Override
public void setMetadata(JSONObject js) {
	this.jsMetadata= js;
}

@Override
public URILookup newURILookup (URI uri){
	 Map<String, String> mp = Utils.matchVariables(getMetadataString("uripattern"), uri.toString());
	if (!Utils.isVoidOrNull(mp)){
		return new URILookup (uri, getMetadataString("uripattern"),  getMetadataString("wrapperurl"));
	}else{
		return null;
	}		
}

@Override
public JSONObject deref(URI uri, JSONObject credentials) {
	URILookup callURI = newURILookup (uri);
	
	Long oneStartTime = System.currentTimeMillis();
	JSONObject res = callURI.derefLifting(credentials);
	Long oneStopTime = System.currentTimeMillis();
	//JSONObject individual = (JSONObject)((JSONObject)res.get("individualmetadata")).get("content");
	JSONObject individual = (JSONObject)res.get("content");
	JSONObject context = (JSONObject)individual.get("@context");
	context.put("prv", "http://purl.org/net/provenance/ns#");
	individual.put("@context",context);
	individual.put("prv:createdBy", getProvenance ());
	setMetadata("liftingmetadata", individual);
	
	CheckingThread cth = new CheckingThread(oneStartTime, oneStopTime);
	Thread th = new Thread(cth);
	th.start();
	return individual;
	}

public class CheckingThread extends Thread {
	private Long startTime;
	private Long stopTime;
	public CheckingThread (Long startTime, Long stopTime){
		this.startTime= startTime;
		this.stopTime= stopTime;
	}
	
    public void run(){
       checkQuality(startTime, stopTime);
    }
  }

private void checkQuality(long oneStartTime, long oneStopTime){	
	DecimalFormat df2 = new DecimalFormat("#.##");

	long elapsedTime = oneStopTime - oneStartTime;
    if (elapsedTime < this.latency) this.latency = elapsedTime;
    qualityMetadata.put("P2", this.latency);
	qualityparams.put("#P2measure#", this.latency.toString());
	if (this.latency< 1000) qualityMetadata.put("P2ok", 1);
	else  qualityMetadata.put("P2ok", 0);
	
	Float cont3 = new Float(1000.0/this.latency);
	qualityMetadata.put("P3", df2.format(cont3));
	qualityparams.put("#P3measure#", df2.format(cont3));
	if (cont3 > 1) qualityMetadata.put("P3ok", 1);
	else  qualityMetadata.put("P3ok", 0);
	
	timesCont++;
	if (timesCont>9) timesCont=0;	
	times[timesCont]= elapsedTime;
	long sumTimes = 0;
	for (long t : times){
		sumTimes += t;
	}		
	Long scalability = sumTimes/10;
	qualityMetadata.put("P4", scalability);
	qualityparams.put("#P4measure#", scalability.toString());
	if (scalability <= (latency * 1.5)) qualityMetadata.put("P4ok", 1);
	else  qualityMetadata.put("P4ok", 0);
	
	if (Utils.isVoidOrNull(this.exampleLowering) || Math.random()<0.20){
		JSONObject eLowering = getURIExamples().get(0).derefLowering(null);
		exampleLowering = (JSONObject)eLowering.get("content");
		exampleLowering.put("statuscode", eLowering.get("statuscode").toString());
		JSONObject eLifting = getURIExamples().get(0).derefLifting(null);
		exampleLifting = (JSONObject)eLifting.get("content");
		exampleLifting.put("statuscode", eLifting.get("statuscode").toString());
		JSONObject lifting = exampleLifting;
	
		JSONObject j = Utils.countBrokenLinks(lifting);
		String cont = j.get("count").toString();
		qualityMetadata.put("I1", cont + " broken links: "+ Utils.getKeyValuesAsList((JSONObject)j.get("brokenlinks")).toString().replaceAll("\"", "'"));
		qualityparams.put("#I1measure#", cont);
		if (cont.equals("0")){
			qualityMetadata.put("I1ok", 1);			
		}else{
			qualityMetadata.put("I1ok", 0);
		}	
		JSONObject j2 = Utils.countInterlinks(lifting);
		String cont2 = j2.get("count").toString();
		qualityMetadata.put("I2", cont2 + " interlinks: "+ Utils.getKeyValuesAsList((JSONObject)j2.get("interlinks")).toString().replaceAll("\"", "'") );
		qualityparams.put("#I2measure#", cont2);
		if (cont2.equals("0")){
			qualityMetadata.put("I2ok", 0);			
		}else{
			qualityMetadata.put("I2ok", 1);
		}		
		
		qualityMetadata.putAll(getSyntacticAccurate(lifting));					//SV2
		JSONObject SA2 = getPropertyInaccuracy(lifting);
		qualityMetadata.putAll(SA2);//SA2	Crowdsourcing/Github
		qualityMetadata.putAll(getDeprecatedClassesProperties(lifting)); 		//CS4
		qualityMetadata.putAll(getRedundantAtttributes(lifting)); 				//CN1
		qualityMetadata.putAll(getPropertyCompleteness(lifting, exampleLowering)); 	//CM2
		qualityMetadata.putAll(getInterlinkCompleteness(lifting, exampleLowering)); 	//cm4
		//T7  Crowdsourcing/Github
		qualityMetadata.putAll(getUndefinedData(lifting)); //IN3
		
		qualityparams.put("#T7measure#", "5");
		qualityparams.put("#SA2measure#", "0");
	}
	String loweringStatusCode = exampleLowering.get("statuscode").toString();				
	String statusCode = exampleLifting.get("statuscode").toString();				
	
	//IKER ^	
	setMetadata("qualitymetadata", qualityMetadata);
	int code = (Integer)qualityMetadata.get("I1ok");
		code *= (Integer)qualityMetadata.get("I2ok");
		code *= (Integer)qualityMetadata.get("P2ok");
		code *= (Integer)qualityMetadata.get("P3ok");
		code *= (Integer)qualityMetadata.get("P4ok");
		code *= (Integer)qualityMetadata.get("SV2ok");
		code *= (Integer)qualityMetadata.get("SA2ok");
		code *= (Integer)qualityMetadata.get("CS4ok");
		code *= (Integer)qualityMetadata.get("CN1ok");
		code *= (Integer)qualityMetadata.get("CM2ok");
		code *= (Integer)qualityMetadata.get("CM4ok");
//		code *= (Integer)qualityMetadata.get("T7ok");
		code *= (Integer)qualityMetadata.get("IN3ok");
		
		if (loweringStatusCode.equals(Cons.FAILS_CODE) || statusCode.equals(Cons.FAILS_CODE)) {qualityMetadata.put("statuscode", Cons.FAILS_CODE);}
		else if (loweringStatusCode.equals(Cons.BLANKS_CODE) || statusCode.equals(Cons.BLANKS_CODE)) {qualityMetadata.put("statuscode", Cons.BLANKS_CODE);}
		else {
			qualityMetadata.put("statuscode", statusCode);
			qualityMetadata.put("statusmessage", "Done...");
			if (code == 0) {
				qualityMetadata.put("statuscode", Cons.LOWQUALITY_CODE);
				qualityMetadata.put("statusmessage", "Some quality issues arose.");
			}			
			if ((Integer)qualityMetadata.get("SA2ok")==0) {
				qualityMetadata.put("statuscode", Cons.AUTHS_CODE);
			}			
		}
}

public JSONObject getSyntacticAccurate(JSONObject lifting){
	Integer cont =0;
	String noAccurate = "";
	JSONObject res = new JSONObject();
	for (String name : Utils.getKeysAsList(lifting)){
		if (!name.contains(":")){
			noAccurate += name + ", ";
			cont++;
		}
	}
	res.put("SV2", cont  + " no qualified properties: " + noAccurate);
	qualityparams.put("#SV2measure#", cont.toString());
	if (cont>0) res.put("SV2ok", 0);
	else res.put("SV2ok", 1);
	return res;
}


public JSONObject getUndefinedData(JSONObject lifting){
	Integer cont =0;
	String undefined = "";
	JSONObject res = new JSONObject();
	List<String> list = Utils.getKeysAsList(lifting);
	for (String property: list){
		if (!property.contains(":")){
			undefined += property+ ", ";
			cont++;
		}
	}
	res.put("IN3", cont + " unqualified properties: "+ undefined);
	qualityparams.put("#IN3measure#", cont.toString());
	if (cont>0)  res.put("IN3ok", 0);
	else  res.put("IN3ok", 1);
	return res;
}

public JSONObject getRedundantAtttributes(JSONObject lifting){
	Integer cont =0;
	String redundant = "";
	JSONObject res = new JSONObject();
	List<String> list = Utils.getValuesAsList(lifting);
	for (String value: list){
		if (Utils.countInList (value, list)>1){
			redundant += value+ ", ";
			cont++;
		}
	}
	res.put("CN1", cont + " redundant values: "+ redundant);
	qualityparams.put("#CN1measure#", cont.toString());
	if (cont>0)  res.put("CN1ok", 0);
	else  res.put("CN1ok", 1);
	return res;
}

public JSONObject getPropertyCompleteness(JSONObject lifting, JSONObject selecting){
	DecimalFormat df = new DecimalFormat("#");
	DecimalFormat df2 = new DecimalFormat("#.##");
	JSONObject res = new JSONObject();
	Double contLifting = Utils.countElements (lifting) * 1.0;
	Double contSelecting = Utils.countElements (selecting) * 1.0;
	Double ratio = contLifting/contSelecting;
	res.put ("CM2", "Ratio: "+ df.format(contLifting)+" / "+df.format(contSelecting)+"  = " + df2.format(ratio));
	qualityparams.put("#CM2measure#", df2.format(ratio));
	if (ratio>0.5) res.put ("CM2ok", 1);
	else  res.put ("CM2ok", 0);
	return res;
}

public JSONObject getPropertyInaccuracy(JSONObject lifting){
	DecimalFormat df = new DecimalFormat("#");
	DecimalFormat df2 = new DecimalFormat("#.##");
	JSONObject res = new JSONObject();
	Double conNulls = Utils.countNulls (lifting) * 1.0;
	Double contProperties = Utils.countElements (lifting) * 1.0;
	Double ratio = conNulls/contProperties;
	res.put ("SA2", "Ratio: "+ df.format(conNulls)+" / "+df.format(contProperties)+"  = " + df2.format(ratio));
	qualityparams.put("#SA2measure#", df2.format(ratio));
	if (ratio>0.5) res.put ("SA2ok", 0);
	else  res.put ("SA2ok", 1);
	return res;
}

public JSONObject  getInterlinkCompleteness(JSONObject lifting, JSONObject selecting){
	DecimalFormat df = new DecimalFormat("#");
	DecimalFormat df2 = new DecimalFormat("#.##");		
	JSONObject res = new JSONObject();
	JSONObject j2 = Utils.countInterlinks(lifting);
	String cont2 = j2.get("count").toString();
	Double contLinks = Double.parseDouble(cont2);
	Double contSelecting = Utils.countElements (selecting) * 1.0;
	Double ratio = contLinks/contSelecting;
	res.put ("CM4", "Ratio: "+ df.format(contLinks)+" / "+df.format(contSelecting)+"  = " + df2.format(ratio));
	qualityparams.put("#CM4measure#", df2.format(ratio));
	if (ratio>0.1) res.put ("CM4ok", 1);
	else  res.put ("CM4ok", 0);
	return res;
}

public JSONObject getDeprecatedClassesProperties(JSONObject lifting){
	JSONObject res = new JSONObject();
	Integer contProp =0;
	Integer contCls =0;
	String deprecatedProps = "";
	String deprecatedCls = "";
	try{
	String id = lifting.get("@id").toString();
	id = id + "#PROVISIONAL";
	WrapperManager.DB.saveIndividual(id, lifting);	
	for (String name : WrapperManager.DB.getDeprecatedClasses(id)){
		if (!name.contains(":")){
			deprecatedCls += name + ", ";
			contCls++;
		}
	}
	for (String name : WrapperManager.DB.getDeprecatedProperties(id)){
		if (!name.contains(":")){
			deprecatedProps += name + ", ";
			contProp++;
		}
	}
	res.put("CS4", contProp  + " deprecated properties: " + deprecatedProps +" ; "+ contCls  + " deprecated classes: " + deprecatedCls);
	Integer cont = contProp+contCls;
	qualityparams.put("#CS4measure#", cont.toString());
	if (contProp>0 || contCls > 0) res.put("CS4ok", 0);
	else res.put("CS4ok", 1);
	
	} catch (Exception e) {
		res.put("CS4", contProp  + " deprecated properties: " + deprecatedProps +" ; "+ contCls  + " deprecated classes: " + deprecatedCls);
		res.put("CS4ok", 1);
	}
	return res;
}

private JSONObject checkDereferentiation(JSONObject credentials){
	JSONObject json = new JSONObject();	
	URILookup exuri = getURIExamples().get(0);
	if (Utils.isVoidOrNull(exuri)){
		json.put("result", false);
		return json;
	}
	JSONObject loweringMetadata =  exuri.derefLowering(credentials);
	setMetadata("loweringmetadata", loweringMetadata);		
	JSONObject liftingMetadata =  (JSONObject)exuri.derefLifting(credentials); // (JSONObject)exuri.derefLifting(env, odturl);
	//JSONObject individualMetadata = (JSONObject)liftingMetadata.get("individualmetadata");
	setMetadata("liftingmetadata", liftingMetadata);
	String code = loweringMetadata.get("statuscode").toString();
	Boolean cLowering = code.toString().equals(Cons.OKS_CODE);// exuri.isLoweringCorrect();
	json.put("correctlowering", cLowering);
	String st = "The API responds " + code + ": " + loweringMetadata.get("statusmessage").toString(); 
	json.put("messageslowering", st);
	code = liftingMetadata.get("statuscode").toString();
	Boolean cLifting = code.equals(Cons.OKS_CODE) || code.equals(Cons.LOWQUALITY_CODE);
	json.put("correctlifting", cLifting);
	
//TODO: IKer?	VER si es vacio.
	JSONObject individual = (JSONObject)liftingMetadata.get("content");
	Integer cont = Utils.countElements (individual);
	Boolean cIndividual=false; 
	if (cont > 0){
		cIndividual= true;	
		json.put("messageslifting", liftingMetadata.get("statusmessage").toString());
	}else{
		json.put("messageslifting","The individual is void.");
	}
	json.put("isvoid", !cIndividual);
	json.put("result", cLowering && cLifting && cIndividual);
	return json;
}

private void setMetadata(String key, JSONObject value){
	this.jsMetadata.put(key, value);
}
private void setMetadata(String key, String value){
	this.jsMetadata.put(key, value);
}
private JSONObject getMetadata(String key){
	JSONObject js = new JSONObject();
	try{			
			js = (JSONObject)this.jsMetadata;
			return (JSONObject)js.get(key);
	}catch (Exception e){
		return new JSONObject();
	}
}

private String getMetadataString(String key){
	try{			
	JSONObject js = (JSONObject)this.jsMetadata;
	String value = js.get(key).toString();
	return value;
	}catch (Exception e){
		return "";
	}
}


private JSONObject checkSyntax() {
	JSONObject js = new JSONObject();
	JSONArray matchincorrect =  new JSONArray();
	js.put("incorrectalias", matchincorrect);
	js.put("URIExampleExists", false);
	js.put("URIExampleMatchesPattern", false);
	js.put("URIPatternExists", false);	
	js.put("URIPatternMatchesAliases", false); 	
	js.put("LiftingExists", false);
	js.put("isCorrect", false);
	try{
		Set<String> uris = this.exampleURIs.keySet();
		String exuristr = uris.iterator().next();
		URILookup exuri = this.exampleURIs.get(exuristr);
		if (!Utils.isVoidOrNull(exuri)){
			js.put("URIExampleExists", true);
			String uripattern = getMetadataString("uripattern");
			if (Utils.isVoidOrNull(uripattern)){
				js.put("URIPatternExists", false);
			}else{
				js.put("URIPatternExists", true);
				JSONObject pars = Utils.bindVariables(uripattern, exuri.getURI().toString());
				Boolean ok = (Boolean) pars.get("matches");
				js.put("URIExampleMatchesPattern", ok);
				JSONObject patternannotations = (JSONObject)pars.get("bindings");
				JSONObject alias = getMetadata("inputalias");
				js.put("URIPatternMatchesAliases", true);
				for (String key: (Set<String>)patternannotations.keySet()){
					if (Utils.isVoidOrNull(alias.get(key))) {
						js.put("URIPatternMatchesAliases", false);
						matchincorrect.add(key);
					}
				}
				js.put(Cons.INCORRECTALIASMATCHING_KEY, matchincorrect);
			}
		}else{
			js.put("URIExampleExists", false);
		}
		js.put("LiftingExists", this.LiftingExists);				
		js.put("result", ((boolean)js.get("URIPatternMatchesAliases")) && ((boolean)js.get("URIExampleExists")) && ((boolean)js.get("URIExampleMatchesPattern")) && ((boolean)js.get("URIPatternExists")) && ((boolean)js.get("LiftingExists")));
		return js;
		}catch (Exception e){
			return js;
		}
}
	
	////////////////////////////

	 private JSONObject parseWrapperToJSON (String ODT){
		 JSONObject json = new JSONObject();
			try{
				String service =Cons.NOPROVIDED;
				 String type =Cons.NOPROVIDED;
				 String name=Cons.NOPROVIDED;
				 String URIPattern=Cons.NOPROVIDED;
				 String author=Cons.NOPROVIDED;
				 String documentationURL=Cons.NOPROVIDED;
				 String apiKeyURL=Cons.NOPROVIDED;

				///Item provenance
				String dataSet=Cons.NOPROVIDED;
				String homepage=Cons.NOPROVIDED;
				///Void
				 String publisher= Cons.PUBLISHER;
				 String contributor= Cons.CONTRIBUTOR;
				 String source=Cons.NOPROVIDED;
				 String nms=Cons.NOPROVIDED;
				 String description=Cons.NOPROVIDED;
				// String uriSpace=Cons.NOPROVIDED;
				 String uriRegexPattern=Cons.NOPROVIDED;

//		    	String ODT= LDW;
		    	if (ODT=="problems") return json;

		    	//String reg = "(?i)(<\\s*?url\\s*?>)(.+?)(<\\s*?/\\s*?url>)";
		    	String newodt = ODT.replaceAll("\r\n", "");
		    	newodt = newodt.replaceAll("\n", "");

		    	String reg = "(?i)(<select(.)+</select>)";
		    	String select = Utils.getFirstRegExp(newodt, reg); 
		    	reg = "(?i)(<inputs(.|\n)+</inputs>)";
		    	String inputs = Utils.getFirstRegExp(select, reg); 
		    	inputs = inputs.replaceAll("<inputs>", "");
		    	inputs = inputs.replaceAll("</inputs>", "");
		    	String[] keys = inputs.split("/>");
		    	JSONObject inputkeys = new JSONObject();
		    	JSONObject inputalias = new JSONObject();
		    	JSONObject credentials = new JSONObject();

		      for (String key : keys){
		    	  reg = "(?i)(id=\"[^\"]+)";
			      String in = Utils.getFirstRegExp(key, reg); 
		    	  reg = "(?i)(as=\"[^\"]+)";
			      String alias = Utils.getFirstRegExp(key, reg);
		    	  reg = "(?i)(default=\"[^\"]+)";
			      String def = Utils.getFirstRegExp(key, reg);
			      if (Utils.isVoidOrNull(in) && Utils.isVoidOrNull(alias)){continue;}					    
			      if (!Utils.isVoidOrNull(in)){
			    	  in = in.replaceAll("id=\"", "");
			      }else{
			    	  in = "";
			      }
			      if (!Utils.isVoidOrNull(alias)){
			    	  alias = alias.replaceAll("as=\"", "");
			      }else{
			    	  alias = in;
			      }
			      if (!Utils.isVoidOrNull(def)){
		    	  	reg = "(?i)(default=\")";
			      	def = def.replaceAll(reg, "");
			       	credentials.put(in, def);
			       }
		    	  inputkeys.put(in, alias);
		    	  inputalias.put(alias, in);
		      }
		      json.put("credentials", credentials);
		      json.put("inputkeys", inputkeys);
		      json.put("inputalias", inputalias);
		      reg = "(?i)(<\\s*table[^>]+>)";
		    	nms = Utils.getFirstRegExp(ODT, reg); //source
		    	JSONObject jsOnto = new JSONObject();
		    	if (!nms.equalsIgnoreCase(Cons.VOID)) {
		    		String[] m = nms.split ("xmlns");
		    		for (int i = 1; i < m.length; i++){
		    			String ns = m[i].trim();
		    			if (ns.indexOf("=")==0) continue;
		    			String prefix = ns.substring(1,ns.indexOf("="));
	    				String url = ns.substring(ns.indexOf("=")+1);
	    				reg = "(?i)([^\"]+)";
	    		    	url = Utils.getFirstRegExp(url, reg); //source
	    				url = url.replaceAll("\"", "");
	    				jsOnto.put(prefix, url);
		    		}
		    	}
		    	json.put("uriontologies", jsOnto);

		    	reg = "(?i)(<\\s*?url[^>]*?>\\s*?http(s)*?://)[^/]*";
		    	source = Utils.getFirstRegExp(ODT, reg); //source
		    	if (!source.equalsIgnoreCase(Cons.VOID)) {
		    		source = source.substring(source.indexOf(">")+1, source.length()).trim();
		    	}

		    	reg = "(?i)(<\\s*?author[^>]*?>)(.+?)(<\\s*?/\\s*?author\\s*?>)";
				author = Utils.getFirstRegExp(ODT,reg);        //author
		    	if (!author.equalsIgnoreCase(Cons.VOID)){
		    		author = author.substring(author.indexOf("author>")+7, author.length()).trim();
		    		author = author.substring(0,author.indexOf("</author")).trim();
		    	}

		    	reg = "(?i)(<\\s*?description[^>]*?>)(.+?)(<\\s*?/\\s*?description\\s*?>)";
				description = Utils.getFirstRegExp(ODT,reg);        //description
		    	if (!description.equalsIgnoreCase(Cons.VOID)){
		    		description = description.substring(description.indexOf("description>")+12, description.length()).trim();
		    		description = description.substring(0,description.indexOf("</description")).trim();
		    	}

		    	reg = "(?i)(<\\s*?documentationURL[^>]*?>)(.+?)(<\\s*?/\\s*?documentationURL\\s*?>)";
		    	documentationURL = Utils.getFirstRegExp(ODT,reg);        //description
		    	if (!documentationURL.equalsIgnoreCase(Cons.VOID)){
		    		documentationURL = documentationURL.substring(documentationURL.indexOf("documentationURL>")+17, documentationURL.length()).trim();
		    		documentationURL = documentationURL.substring(0,documentationURL.indexOf("</documentationURL")).trim();
		    	}

		    	reg = "(?i)(<\\s*?apiKeyURL[^>]*?>)(.+?)(<\\s*?/\\s*?apiKeyURL\\s*?>)";
		    	apiKeyURL = Utils.getFirstRegExp(ODT,reg);        //description
		    	if (!apiKeyURL.equalsIgnoreCase(Cons.VOID)){
		    		apiKeyURL = apiKeyURL.substring(apiKeyURL.indexOf("apiKeyURL>")+10, apiKeyURL.length()).trim();
		    		apiKeyURL = apiKeyURL.substring(0,apiKeyURL.indexOf("</apiKeyURL")).trim();
		    	}

		    	reg = "(?i)\\s*?URIPattern\\s*?:(.+?)(<\\s*?/\\s*?sampleQuery\\s*?>)";  //samplequery
		    	URIPattern = Utils.getFirstRegExp(ODT, reg);
		    	URIPattern = URIPattern.replaceAll(" ", "");
		    	URIPattern = URIPattern.replaceAll("(?i)URIPattern:", "");
		    	URIPattern = URIPattern.replaceAll("(?i)</sampleQuery>", "");
		    	if (!URIPattern.startsWith("http")){
		        	if (URIPattern.startsWith("/")){
		        		URIPattern= WrapperManager.derefserver+URIPattern;
		        	} else{
		        		URIPattern= WrapperManager.derefserver+"/"+URIPattern;
		        	}
		    	}
		    	JSONObject jparse =Utils.parseURIPattern(URIPattern);
				service =jparse.get("service").toString();
				type =jparse.get("type").toString();
		    	name=service+"."+type;
		    	uriRegexPattern= URIPattern; 
		    	
		    	 //mappings
				creatingAnnotations (ODT);

		    	String wrapperPattern=uriRegexPattern;
		    	wrapperId = Utils.parseURIPattern(wrapperPattern).get(Cons.WRAPPERID_KEY).toString();
		    	List<String> URIExamples = extractURIExamples(ODT);
		    	JSONArray examples = new JSONArray ();
		    	for (String URIExample: URIExamples){
		    		examples.add(URIExample);
		    	}
	    		  
		    	json.put(Cons.WRAPPERID_KEY,wrapperId);
		    	json.put("service",service);
		    	json.put("type",type);
		    	json.put("documentationURL",documentationURL);
		    	json.put("apiKeyURL",apiKeyURL);
		    	if (Utils.isVoidOrNull(source)){
			    	if (!Utils.isVoidOrNull(source)){source = apiKeyURL;}
			    	else{
				    	if (!Utils.isVoidOrNull(source)){source = documentationURL;}			    		
			    	}
		    	}
		    	json.put("apiurl",source);
		    	json.put("author",author);
		    	json.put("wrappername", name);
		    	json.put("wrapperurl",  Cons.NOPROVIDED);
		    	json.put("datahaburl",  Cons.NOPROVIDED);
		    	json.put("description",  description);		    	
		    	//json.put("userid", this.userId);
	    	    json.put("odtname", Cons.NOPROVIDED);
		    	json.put("uripattern", URIPattern);
		    	json.put("uriexample", examples);
		    	} catch (Exception e) {
					e.printStackTrace();
				}
			return json;
		    }
	 
protected String getVoidString(){
	JSONObject voiddesc = new JSONObject ();
	String URIPattern = getMetadataString("uripattern");
	String DatasetURI=   Utils.getVoIDURIINPUTS(URIPattern, getMetadata("inputalias"));
	
	HashMap propertiesparams = new HashMap();
	String properties="{\"void:property\": \"#PROPERTY#\"}";
	String propertiesTXT = "";
	Boolean first = true;
	for (String property : propertyList){
		propertiesparams.put("#PROPERTY#",property); 
		if (first){
			propertiesTXT = Utils.replaceVariables (properties, propertiesparams);
			first=false;
		}else{
			propertiesTXT += ", " + Utils.replaceVariables (properties, propertiesparams);			
		}
	}
	
	String vocabularyTXT = "";
	first = true;
	for (String ontology : ontologyList){
		if (first){
			vocabularyTXT = "\""+ontology+"\"";
			first=false;
		}else{
			vocabularyTXT += ", \"" + ontology+"\"";			
		}
	}
	
	HashMap interlinksparams = new HashMap();
	String subset="{\"void:subjectsTarget\": \"#URI#\",\"void:objectsTarget\": \"#TARGET#\", \"void:linkPredicate\": \"#INTERLINK#\"}";
	
	String subsetTXT = "";
	first = true;
	interlinksparams.put("#URI#",DatasetURI); 
	
	for (Entry interlink: interlinkList.entrySet()){
		String target = interlink.getKey().toString();
		String property = interlink.getValue().toString();
		interlinksparams.put("#TARGET#",target); 
		interlinksparams.put("#INTERLINK#",property); 
		if (first){
			subsetTXT = Utils.replaceVariables (subset, interlinksparams);
			first=false;
		}else{
			subsetTXT += ", " + Utils.replaceVariables (subset, interlinksparams);			
		}
	}
	
	Date dNow = new Date( );
    SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
	String completedAt=ft.format(dNow);

	JSONObject jparse =Utils.parseURIPattern(URIPattern);
	String service =jparse.get("service").toString();
	String type =jparse.get("type").toString();
	String name=service+"."+type;
	String uriSpace; 
	int bracketpos= URIPattern.indexOf("{");
	if (bracketpos >-1){uriSpace = URIPattern.substring(0, URIPattern.indexOf("{"));}
	else {uriSpace =URIPattern;}
	String wrapperURI= Utils.getVoIDURIINPUTS(URIPattern, getMetadata("inputalias"));
	//wrapperURI= Utils.getWrapperURI(wrapperURI);
	String formatedName = name.replaceAll("\\.", "_");
	setMetadata("datahuburl", "https://datahub.io/dataset/"+formatedName);
	
	HashMap params = new HashMap();
	params.put("#URI#", wrapperURI); 
	params.put("#REGEX#", Utils.getURIRegex(wrapperURI)); 
	params.put("#TITLE#",name); 
	params.put("#DESCRIPTION#",getMetadataString("description")); 
	params.put("#CREATOR#",getMetadataString("author")); 
	params.put("#SOURCE#",getMetadataString("apiurl")); 
	params.put("#CREATED#",completedAt); 
	params.put("#MODIFIED#",completedAt); 
	params.put("#ISSUED#",completedAt); 
   	params.put("#LDWGIT#",getMetadataString("wrapperurl")); 
   	params.put("#DATAHUBIO#",getMetadataString("datahuburl")); 
	params.put("#EXAMPLERESOURCES#",getExampleURIListString()); 
	params.put("#URISPACE#",uriSpace); 
	params.put("#VOCABULARY#",vocabularyTXT); 
	params.put("#TYPE#",typeclass); 
	params.put("#PROPERTIES#",propertiesTXT); 
	params.put("#SUBSETS#",subsetTXT); 
	  
	String txt = "{\"@context\":{\"hydra\":\"http://www.w3.org/ns/hydra/core#\",\"void:exampleResource\":{\"@type\":\"@id\"},\"dcterms\":\"http://purl.org/dc/terms/\",\"hydra:apiDocumentation\":{\"@type\":\"@id\"},\"foaf\":\"http://xmlns.com/foaf/0.1/\",\"rdfs\":\"http://www.w3.org/2000/01/rdf-schema#\"},";
	txt+="\"@id\":\"#URI#\",";
	txt+="\"@type\":\"void:Dataset\",";
	txt+="\"dcterms:title\": \"#TITLE#\",";
	txt+="    \"dcterms:description\": \"#DESCRIPTION#\",";
	txt+="    \"dcterms:creator\": {\"@id\":\"http://rdf.onekin.org/#CREATOR#\",\"rdfs:label\": \"#CREATOR#\"},";
	txt+="    \"dcterms:contributor\": {\"@id\":\"http://rdf.onekin.org\",\"rdfs:label\": \"LDW Community\", \"foaf:homepage\": \"http://rdf.onekin.org\"},";
	txt+="    \"dcterms:publisher\": {\"@id\":\"http://www.onekin.org\",\"rdfs:label\": \"Onekin Research Group\", \"foaf:homepage\": \"http://www.onekin.org\"},";
	txt+="		\"dcterms:source\":\"#SOURCE#\",";
	txt+="    \"dcterms:created\": \"#CREATED#\",";
	txt+="    \"dcterms:modified\": \"#MODIFIED#\",";
	txt+="    \"dcterms:issued\": \"#ISSUED#\",";
	txt+="		\"foaf:homepage\":\"#LDWGIT#\",";	
	txt+="    \"dqv:hasQualityMeasurement\": [";
	txt+="\"#URI#/dqv/IN3measure\",";
	txt+="\"#URI#/dqv/T7measure\",";
	txt+="\"#URI#/dqv/CM4measure\",";
	txt+="\"#URI#/dqv/CM2measure\",";
	txt+="\"#URI#/dqv/CN1measure\",";
	txt+="\"#URI#/dqv/CS4measure\",";
	txt+="\"#URI#/dqv/SA2measure\",";
	txt+="\"#URI#/dqv/SV2measure\",";
	txt+="\"#URI#/dqv/P4measure\",";
	txt+="\"#URI#/dqv/P3measure\",";
	txt+="\"#URI#/dqv/P2measure\",";
	txt+="\"#URI#/dqv/I2measure\",";
	txt+="\"#URI#/dqv/I1measure\"";
	txt+="],";
	txt+="    \"foaf:page\": \"#DATAHUBIO#\",";
	txt+="    \"hydra:apiDocumentation\":\"#URI#/apidocumentation\",";
//	txt+="    \"void:sparqlEndpoint\":\"#URI#/sparql\",";
	txt+="    \"void:feature\": [\"http://www.w3.org/ns/formats/RDF_XML\", \"http://www.w3.org/ns/formats/N3\", \"http://www.w3.org/ns/formats/N-Triples\", \"http://www.w3.org/ns/formats/Turtle\"],";
	txt+="    \"void:exampleResource\":[#EXAMPLERESOURCES#],";
	txt+="    \"void:uriRegexPattern\":\"#REGEX#\",";
	txt+="    \"void:uriSpace\":\"#URISPACE#\",";
	txt+="\"void:vocabulary\": [#VOCABULARY#],";
	txt+="\"void:classPartition\" : {\"void:class\": \"#TYPE#\"},";
	txt+=" \"void:propertyPartition\": [#PROPERTIES#],";
	txt+="	\"void:subset\" : [#SUBSETS#]";
	txt+="}";
	
	txt = Utils.replaceVariables (txt, params);
	return txt;
}

protected String getQualityMeassuresString(){

	String txt="[{";
	txt +="	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/AccessibilityDimensions\",";
	txt +="	\"@type\": \"dqv:Category\",";
	txt +="	\"skos:prefLabel\": \"Accessibility\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/IntrinsicDimensions\",";
	txt +="	\"@type\": \"dqv:Category\",";
	txt +="	\"skos:prefLabel\": \"Intrinsic dimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/ContextualDimensions\",";
	txt +="	\"@type\": \"dqv:Category\",";
	txt +="	\"skos:prefLabel\": \"Contextual dimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/RepresentationalDimensions\",";
	txt +="	\"@type\": \"dqv:Category\",";
	txt +="	\"skos:prefLabel\": \"Representational Dimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Availability\",";
	txt +="	\"@type\": \"dqv:Dimension\",";
	txt +="	\"skos:prefLabel\": \"Availability\",";
	txt +="	\"skos:definition\": \"Availability of a dataset is the extent to which data (or some portion of it) is present, obtainable and ready for use.\",";
	txt +="	\"dqv:inCategory\": \"#VOIDURI#/"+Cons.QUALITY_key+"/AccessibilityDimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Interlinking\",";
	txt +="	\"@type\": \"dqv:Dimension\",";
	txt +="	\"skos:prefLabel\": \"Interlinking\",";
	txt +="	\"skos:definition\": \"Interlinking refers to the degree to which entities that represent the same concept are linked to each other, be it within or between two or more data sources.\",";
	txt +="	\"dqv:inCategory\": \"#VOIDURI#/"+Cons.QUALITY_key+"/AccessibilityDimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Performance\",";
	txt +="	\"@type\": \"dqv:Dimension\",";
	txt +="	\"skos:prefLabel\": \"Interlinking\",";
	txt +="	\"skos:definition\": \"Performance refers to the efficiency of a system that binds to a large dataset, that is, the more performant a data source is the more efficiently a system can process data \",";
	txt +="	\"dqv:inCategory\": \"#VOIDURI#/"+Cons.QUALITY_key+"/AccessibilityDimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/SyntacticVality\",";
	txt +="	\"@type\": \"dqv:Dimension\",";
	txt +="	\"skos:prefLabel\": \"SyntacticVality\",";
	txt +="	\"skos:definition\": \"Syntactic validity is defined as the degree to which an RDF document conforms to the specification of the serialization format.\",";
	txt +="	\"dqv:inCategory\": \"#VOIDURI#/"+Cons.QUALITY_key+"/IntrinsicDimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/SemanticAccuracy\",";
	txt +="	\"@type\": \"dqv:Dimension\",";
	txt +="	\"skos:prefLabel\": \"SemanticAccuracy\",";
	txt +="	\"skos:definition\": \"Semantic accuracy is defined as the degree to which data values correctly represent the real world facts.\",";
	txt +="	\"dqv:inCategory\": \"#VOIDURI#/"+Cons.QUALITY_key+"/IntrinsicDimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Consistency\",";
	txt +="	\"@type\": \"dqv:Dimension\",";
	txt +="	\"skos:prefLabel\": \"Consistency\",";
	txt +="	\"skos:definition\": \"Consistency means that a knowledge base is free of (logical/formal) contradictions with respect to particular knowledge representation and inference mechanisms.\",";
	txt +="	\"dqv:inCategory\": \"#VOIDURI#/"+Cons.QUALITY_key+"/IntrinsicDimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Conciseness\",";
	txt +="	\"@type\": \"dqv:Dimension\",";
	txt +="	\"skos:prefLabel\": \"Conciseness\",";
	txt +="	\"skos:definition\": \"Conciseness refers to the minimization of redundancy of entities at the schema and the data level.\",";
	txt +="	\"dqv:inCategory\": \"#VOIDURI#/"+Cons.QUALITY_key+"/IntrinsicDimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Completeness\",";
	txt +="	\"@type\": \"dqv:Dimension\",";
	txt +="	\"skos:prefLabel\": \"Completeness\",";
	txt +="	\"skos:definition\": \"Completeness refers to the degree to which all required information is present in a particular dataset.\",";
	txt +="	\"dqv:inCategory\": \"#VOIDURI#/"+Cons.QUALITY_key+"/IntrinsicDimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Trustworthiness\",";
	txt +="	\"@type\": \"dqv:Dimension\",";
	txt +="	\"skos:prefLabel\": \"Trustworthiness\",";
	txt +="	\"skos:definition\": \"Trustworthiness is defined as the degree to which the information is accepted to be correct, true, real and credible.\",";
	txt +="	\"dqv:inCategory\": \"#VOIDURI#/"+Cons.QUALITY_key+"/ContextualDimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Interpretability\",";
	txt +="	\"@type\": \"dqv:Dimension\",";
	txt +="	\"skos:prefLabel\": \"Interpretability\",";
	txt +="	\"skos:definition\": \"Interpretability refers to technical aspects of the data, that is, whether information is represented using an appropriate notation and whether the machine is able to process the data.\",";
	txt +="	\"dqv:inCategory\": \"#VOIDURI#/"+Cons.QUALITY_key+"/RepresentationalDimensions\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/IN3Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"Usage of defined classes and properties.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:boolean\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Interpretability\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/IN3measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/IN3Metric\",";
	txt +="	\"dqv:value\": \"#IN3measure#\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/T7Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"Reputation of the dataset.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:integer\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Trustworthiness\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/T7measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/T7Metric\",";
	txt +="	\"dqv:value\": \"#T7measure#\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/CM4Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"Interlink completeness.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:decimal\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Completeness\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/CM4measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/CM4Metric\",";
	txt +="	\"dqv:value\": \"#CM4measure#\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/CM2Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"Property completeness.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:decimal\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Completeness\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/CM2measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/CM2Metric\",";
	txt +="	\"dqv:value\": \"#CM2measure#\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/CN1Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"Intentional conciseness.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:integer\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Conciseness\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/CN1measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/CN1Metric\",";
	txt +="	\"dqv:value\": \"#CN1measure#\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/CS4Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"Deprecated classes and properties.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:integer\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Consistency\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/CS4measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/CS4Metric\",";
	txt +="	\"dqv:value\": \"#CS4measure#\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/SA2Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"Inacurate values.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:integer\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/SemanticAccuracy\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/SA2measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/SA2Metric\",";
	txt +="	\"dqv:value\": \"#SA2measure#\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/SV2Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"Syntactically accurate.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:integer\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/SyntacticAccuracy\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/SV2measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/SV2Metric\",";
	txt +="	\"dqv:value\": \"#SV2measure#\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/P4Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"Scalability.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:integer\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Performance\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/P4measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/P4Metric\",";
	txt +="	\"dqv:value\": \"#P4measure#\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/P3Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"Throughput.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:integer\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Performance\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/P3measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/P3Metric\",";
	txt +="	\"dqv:value\": \"#P3measure#\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/P2Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"Latency.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:integer\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Performance\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/P2measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/P2Metric\",";
	txt +="	\"dqv:value\": \"#P2measure#\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/I2Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"External interlinks.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:integer\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Interlinking\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/I2Measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/I2Metric\",";
	txt +="	\"dqv:value\": \"#I2measure#\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/I1Metric\",";
	txt +="	\"@type\": \"dqv:Metric\",";
	txt +="	\"skos:definition\": \"Dereferenceable interlinks.\",";
	txt +="	\"dqv:expectedDataType\": \"xsd:integer\",";
	txt +="	\"dqv:inDimension\": \"#VOIDURI#/"+Cons.QUALITY_key+"/Interlinking\"},";
	txt +="{	\"@context\":{\"skos\":\"http://www.w3.org/2004/02/skos/core#\", \"dqv\":\"http://www.w3.org/ns/dqv#\", \"xsd\":\"http://www.w3.org/2001/XMLSchema#\", \"dqv:inCategory\":{\"@type\":\"@id\"}, \"dqv:computedOn\":{\"@type\":\"@id\"}, \"dqv:isMeasurementOf\":{\"@type\":\"@id\"}, \"dqv:inDimension\":{\"@type\":\"@id\"}},\"@id\": \"#VOIDURI#/"+Cons.QUALITY_key+"/I1Measure\",";
	txt +="	\"@type\": \"dqv:QualityMeasurement\",";
	txt +="	\"dqv:computedOn\": \"#VOIDURI#\",";
	txt +="	\"dqv:isMeasurementOf\": \"#VOIDURI#/"+Cons.QUALITY_key+"/I1Metric\",";
	txt +="	\"dqv:value\": \"#I1measure#\"}]";

		qualityparams.put("#VOIDURI#",this.getVoID().get("@id"));
	txt = Utils.replaceVariables (txt, qualityparams);
	return txt;
}


private String getRealURIPattern()  {
	String uripattern = getMetadataString("uripattern");
	String wrapperURI= Utils.getVoIDURIINPUTS(uripattern, (JSONObject)getMetadata().get("inputalias"));
	return wrapperURI;
}

protected String getAPIDoc(){
String wrid2 = getRealURIPattern();
		if (!wrid2.endsWith("/")) wrid2+= "/";
		wrid2 += Cons.APIDOC_key;
		String wrid = wrid2 + "/";
		////
		//OPERATION
	String OPERATIONURI= wrid+Cons.OPERATIONLOOKUP_key; 
	String REALURIPATTERN= getRealURIPattern(); 
	String URIPATTERN= getMetadataString("uripattern"); 
	String EXPECTEDCLASSURI = wrid+Cons.CREDENTIALS_key;
	String CLASSURI=  wrid+Cons.CLASS_key;
	
	HashMap params = new HashMap();
	params.put("#OPERATIONURI#", OPERATIONURI); 
	params.put("#URIPATTERN#", URIPATTERN); 
	params.put("#EXPECTEDCLASSURI#", EXPECTEDCLASSURI); 
	params.put("#CLASSURI#", CLASSURI); 
	 
	String txt ="{\"@context\": {\"hydra\": \"http://www.w3.org/ns/hydra/core#\", \"statusCodes\": \"hydra:statusCodes\", \"description\": \"rdfs:comment\", \"label\": \"rdfs:label\", \"method\": \"hydra:method\", \"expects\": {\"@id\": \"hydra:expects\",\"@type\": \"@id\"}, \"returns\": {\"@id\": \"hydra:returns\",\"@type\": \"@id\"}},";
	txt+="\"@type\": \"hydra:Operation\",";
	txt+="\"@id\": \"#OPERATIONURI#\",";
	txt+="\"method\": \"GET\",";
	txt+="\"label\": \"LookupOperation\",";
	txt+="\"description\": \"Lookup URIs matching pattern #URIPATTERN#\",";
	txt+="\"statuscodes\": [],";
	txt+="\"expects\": \"#EXPECTEDCLASSURI#\",";
	txt+="\"returns\": \"#CLASSURI#\"}";
	
	txt = Utils.replaceVariables (txt, params);
	String OPERATIONTXT = txt;
	
		//////				
		//CREDENTIALS done    	
       	String CREDENTIALPROPERTIES = "[";
       	JSONObject credentials = (JSONObject)jsMetadata.get("inputkeys");
       	JSONObject alias = (JSONObject)jsMetadata.get("inputalias");
       	Set<String> creds = credentials.keySet();  //TODO: no son las credenciales.
       	Boolean first = true;
       	
       	URILookup uexample = getURIExamples().get(0);
        first = true;
        Set<String> params2 = new HashSet<String>();
        if (!Utils.isVoidOrNull(uexample)){
       		params2 = uexample.getVariables (); 
         }
       	
    for (String cred : creds){    	
    	String al = credentials.get(cred).toString();
    	if (params2.contains(al)) continue;
		String CREDENTIAL= cred;//.toString();    
	    String DESCRIPTION= cred;//.toString(); 
		params = new HashMap();
		params.put("#CREDENTIAL#", CREDENTIAL); 
		params.put("#DESCRIPTION#", DESCRIPTION);
		txt="{\"@type\": \"hydra:SupportedProperty\",";
		txt+="\"property\": \"http://uri4uri.net/vocab#key\",";
		txt+="\"hydra:title\": \"#CREDENTIAL#\",";
		txt+="\"hydra:description\": \"#DESCRIPTION#\",";
		txt+="\"required\": \"true\",";
		txt+="\"readonly\": \"false\",";
		txt+="\"writeonly\": \"false\"}";
		txt = Utils.replaceVariables (txt, params);
		if (first){
			first = false;
			CREDENTIALPROPERTIES += txt;
		}else{
			CREDENTIALPROPERTIES += ", " + txt;	     
   		}
    }   	
	CREDENTIALPROPERTIES += "]";
	
	////////
	//EXPECTED CLASS";
        
	String CREDURI= wrid+Cons.CREDENTIALS_key; 
	//String EXPECTEDPROPERTIES=CREDENTIALSTXT; 
	
	params = new HashMap();
	params.put("#CREDURI#", CREDURI); 
	params.put("#EXPECTEDPROPERTIES#", CREDENTIALPROPERTIES); 		
    txt="{\"@context\": {\"hydra\": \"http://www.w3.org/ns/hydra/core#\", \"readonly\": \"hydra:readonly\", \"writeonly\": \"hydra:writeonly\", \"required\": \"hydra:required\",\"supportedProperty\": \"hydra:supportedProperty\", \"supportedOperation\": \"hydra:supportedOperation\"},";
	txt+="\"@id\": \"#CREDURI#\",";
	txt+="\"@type\": \"hydra:Class\",";
	txt+="\"hydra:title\": \"Credentials\",";
	txt+="\"hydra:description\": \"Required credentials\",";
	txt+="\"supportedOperation\": [],";
	txt+="\"supportedProperty\": #EXPECTEDPROPERTIES#}";    
	txt = Utils.replaceVariables (txt, params);
	String CREDENTIALS = txt;
	
	//////
	//ENTRYPOINT PROPERTIES.   MAPPINGS";
	String MAPPINGSTXT = "[";
	JSONArray mappings =  new JSONArray();
    first = true;
    	Iterator it = params2.iterator();
    	while (it.hasNext()){
    		String PROPERTY = it.next().toString();
    		String NAME = alias.get(PROPERTY).toString();
    		
			params = new HashMap();
			params.put("#NAME#", NAME); 
			params.put("#PROPERTY#", PROPERTY); 
    		txt="{\"@type\": \"hydra:IriTemplateMapping\",";
			txt+="\"hydra:required\": \"true\",";
			txt+="\"hydra:variable\": \"#NAME#\"";
			if (PROPERTY.contains(":")){
				txt+=", \"hydra:property\": \"#PROPERTY#\""; 
			}
			txt+="}";
			txt = Utils.replaceVariables (txt, params);
			if (first){
				first = false;
	   			MAPPINGSTXT += txt;
			}else{
	   			MAPPINGSTXT += ", " + txt;	     
   			}
    	}
	MAPPINGSTXT += "]";
	
	////////
	//ENTRYPOINT  done";
	
	String EPURI=  wrid+Cons.ENTRYPOINT_key;
	String OPERATION= OPERATIONTXT;
	
params = new HashMap();
params.put("#EPURI#", EPURI); 
params.put("#OPERATIONURI#", OPERATIONURI); 
//params.put("#OPERATION#", OPERATION); 
params.put("#URIPATTERN#", REALURIPATTERN); 
params.put("#MAPPINGS#", MAPPINGSTXT); 

txt="{\"@context\": {\"hydra\": \"http://www.w3.org/ns/hydra/core#\", \"rdfs\": \"http://www.w3.org/2000/01/rdf-schema#\", \"hydra:operation\": {\"@type\": \"@id\"}";
JSONObject js = getMetadata("uriontologies");
for (String key : (Set<String>)js.keySet()){
	String prefix = key;
	String url = (String)js.get(key);
	txt += ",\""+prefix+"\": \""+url+"\"";
}
txt+="},";
txt+="\"@id\": \"#EPURI#\",";
txt+="\"@type\": \"hydra:IriTemplate\",";
txt+="\"rdfs:label\": \"EntryPoint\",";
txt+="\"rdfs:comment\": \"The main entry point\",";
txt+="\"hydra:operation\": \"#OPERATIONURI#\",";
txt+="\"hydra:template\": \"#URIPATTERN#\",";     
txt+="\"hydra:mapping\": #MAPPINGS#}";
	txt = Utils.replaceVariables (txt, params);
		String ENTRYPOINTTXT = txt;
		
	////////
	//"SUPPORTEDCLASSES";
//CLASS  done
    	/////
	String ct = this.getLiftingMetadata().get("@context").toString();
	ct = ct.substring(1, ct.length()-1);
    String CLASSCONTEXT = "{\"property\": {\"@id\": \"hydra:property\",\"@type\": \"@id\"},\"readonly\": \"hydra:readonly\", \"writeonly\": \"hydra:writeonly\",\"required\": \"hydra:required\",\"description\": \"rdfs:comment\", \"rdfs\": \"http://www.w3.org/2000/01/rdf-schema#\", \"supportedOperation\": {\"@id\":\"hydra:supportedOperation\", \"@type\": \"@id\"}, \"hydra\": \"http://www.w3.org/ns/hydra/core#\", \"rdf\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"supportedProperty\": \"hydra:supportedProperty\",";
	CLASSCONTEXT += ct +"}";
	String TITLE=  getMetadataString("wrappername");
	String DESCRIPTION=  getVoID().get("dcterms:description").toString();
	JSONArray properties = getSupportedProperties(this.getLiftingMetadata());
      String SUPPORTEDPROPERTY = properties.toJSONString();
params = new HashMap();
params.put("#CLASSCONTEXT#", CLASSCONTEXT); 
params.put("#CLASSURI#", CLASSURI); 
params.put("#TITLE#", TITLE); 
params.put("#DESCRIPTION#", DESCRIPTION); 
params.put("#OPERATIONURI#", OPERATIONURI); 
//params.put("#OPERATION#", OPERATION); 
params.put("#SUPPORTEDPROPERTY#", SUPPORTEDPROPERTY); 

txt="{\"@context\": #CLASSCONTEXT#,";
txt+="\"@id\": \"#CLASSURI#\",";
txt+="\"@type\": \"hydra:Class\",";
txt+="\"hydra:title\": \"#TITLE#\",";
txt+="\"hydra:description\": \"#DESCRIPTION#\",";
txt+="\"supportedOperation\": [\"#OPERATIONURI#\"],";
txt+="\"supportedProperty\": #SUPPORTEDPROPERTY#}";

	txt = Utils.replaceVariables (txt, params);
		
    String SUPPORTEDCLASSETXT = txt;
		
	//////
	//MAIN
	  String URI =  wrid2;
String NAME = getMetadataString("wrappername");
String DESCRIPTION2 = getVoID().get("dcterms:description").toString();
String ENTRYPOINT = ENTRYPOINTTXT;

params = new HashMap();
params.put("#URI#", URI); 
params.put("#NAME#", NAME); 
params.put("#DESCRIPTION#", DESCRIPTION2); 
params.put("#SUPPORTEDCLASSES#", CLASSURI);//SUPPORTEDCLASSETXT); 
params.put("#ENTRYPOINT#",  EPURI);//ENTRYPOINT); 

	txt="{ \"@context\": {\"hydra\": \"http://www.w3.org/ns/hydra/core#\", \"schema\": \"http://schema.org/\", \"rdf\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",";
txt+="\"ApiDocumentation\" : \"hydra:ApiDocumentation\",";
//txt+="\"property\" : {\"@id\": \"hydra:property\", \"@type\": \"@id\"},";
//txt+="\"readonly\": \"hydra:readonly\",";
///txt+="\"writeonly\": \"hydra:writeonly\",";
//txt+="\"supportedClass\": {\"@id\": \"hydra:supportedClass\", \"@type\": \"@id\"},";
txt+="\"hydra:supportedClass\" : {\"@type\": \"@id\"},";
txt+="\"hydra:hydra:entrypoint\" : {\"@type\": \"@id\"}";
//txt+="\"supportedProperty\": {\"@id\": \"hydra:supportedProperty\", \"@type\": \"@id\"},";
//txt+="\"supportedOperation\": {\"@id\": \"hydra:supportedOperation\", \"@type\": \"@id\"},";
//txt+="\"method\": \"hydra:method\",";
//txt+="\"expects\": {\"@id\": \"hydra:expects\", \"@type\": \"@id\"},";
//txt+="\"returns\": {\"@id\": \"hydra:returns\", \"@type\": \"@id\"},";
//txt+="\"statusCodes\": \"hydra:statusCodes\",";
//txt+="\"code\": \"hydra:statusCode\",";
//txt+="\"rdfs\": \"http://www.w3.org/2000/01/rdf-schema#\",";
//txt+="\"label\": \"rdfs:label\",";
//txt+="\"description\": \"rdfs:comment\",";
//txt+="\"domain\": {\"@id\": \"rdfs:domain\", \"@type\": \"@id\"},";
//txt+="\"range\": {\"@id\": \"rdfs:range\", \"@type\": \"@id\"},";
//txt+="\"subClassOf\": {\"@id\": \"rdfs:subClassOf\", \"@type\": \"@id\"},";
txt+="},";
txt+="\"@type\": \"ApiDocumentation\",";
txt+="\"@id\": \"#URI#\",";
txt+="\"hydra:title\": \"#NAME#\",";
txt+="\"hydra:description\": \"#DESCRIPTION#\",";
txt+="\"hydra:supportedClass\": [\"#SUPPORTEDCLASSES#\"],";
txt+="\"hydra:entrypoint\": \"#ENTRYPOINT#\"}";
	txt = Utils.replaceVariables (txt, params);	
	String MAINTXT = txt;
		
params = new HashMap();
params.put("#APIDOC#", MAINTXT); 
params.put("#ENTRYPOINT#", ENTRYPOINTTXT); 
params.put("#OPERATION#", OPERATIONTXT); 
params.put("#SUPPORTEDCLASS#", SUPPORTEDCLASSETXT); 
params.put("#EXPECTEDCLASS#", CREDENTIALS); 
txt = "[#APIDOC#, #SUPPORTEDCLASS#, #ENTRYPOINT#, #OPERATION#, #EXPECTEDCLASS#]";	
//txt = "[#EXPECTEDCLASS#]";	
txt = Utils.replaceVariables (txt, params);
	String	APIDOC = txt;
	return APIDOC;
}

private String getExampleURIListString () {
	Iterator it = getURIExamples().iterator();
	boolean first = true;
	String txt = "";
	while (it.hasNext()){
		URILookup uri = (URILookup)it.next();
		if (first){
			first= false;
			txt = "\""+uri.getURI().toString()+"\"";
		}
		else txt += ", \"" +uri.getURI().toString()+"\"";
	}
	return txt;
}

protected JSONObject getProvenance (){
	String txt="{\"@context\": {\"void\": \"http://rdfs.org/ns/void#\",";
	txt+="    \"prv\": \"http://purl.org/net/provenance/ns#\",";
	txt+="    \"foaf\": \"http://xmlns.com/foaf/0.1/\",";
	txt+="    \"void:inDataset\": {\"@type\": \"@id\"}},";
	txt+="  \"void:inDataset\": \"#VOIDURL#\",";
	txt+="  \"prv:createdBy\": {\"@type\": \"prv:DataCreation\", ";
	txt+="    \"prv:usedData\": {\"@type\": \"prv:DataItem\", \"foaf:homepage\": \"#API#\"},";
	txt+="    \"prv:usedGuideline\":{\"@type\": \"prv:CreationGuideline\", ";
	txt+="      \"foaf:homepage\": \"#WRAPPERGIT#\"},";
	txt+="    \"prv:performedBy\": \"http://rdf.onekin.org\",";
	txt+="    \"prv:completedAt\": \"#NOWDATE#\"}}";
	Date dNow = new Date( );
    SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
	String completedAt=ft.format(dNow);
	
	HashMap params = new HashMap();
	params.put("#VOIDURL#", getMetadata("void:inDataset").get("@id")); 
	params.put("#API#", getMetadataString("apiurl")); 
	params.put("#WRAPPERGIT#",  getMetadataString("wrapperurl")); 
	params.put("#NOWDATE#",completedAt); 
	txt = Utils.replaceVariables (txt, params);
	JSONObject res = new JSONObject();
	try {
		res= (JSONObject)parser.parse(txt);
	} catch (ParseException e) {
		e.printStackTrace();
	}
	return res; 
}
	 
	 
	    private List<String> extractURIExamples (String ODT){
	    List<String> URIExamples = new ArrayList<String>();
	   	String reg = "(?i)\\s*?URIExample\\s*?:(.+?)(<\\s*?/\\s*?sampleQuery\\s*?>)";  //samplequery
	   	List<String> result = Utils.getRegExp (ODT, reg);
	   	for (String URIExample: result){
	   		URIExample = URIExample.replaceAll(" ", "");
	   		URIExample = URIExample.replaceAll("(?i)URIExample:", "");
	   		URIExample = URIExample.replaceAll("</sampleQuery>", "");
	   		if (!URIExample.startsWith("http")){
	   			if (URIExample.startsWith("/")){
	   				URIExample= WrapperManager.derefserver+URIExample;
	   			}else{
	   				URIExample= WrapperManager.derefserver+"/"+URIExample;
	   			}
	   		}
	   		URIExamples.add(URIExample);
	   	}
	   	return URIExamples;
	}

		////////PARSING WRAPPER/////
	private void creatingAnnotations (String ODT){
	  String wrapper = ODT.substring(ODT.indexOf("lifting"));
	  String[] m = wrapper.split ("((\r\n|\n|\r)|$)");
	  if (m.length> 0 && wrapper.indexOf("<pipe")>-1 && wrapper.indexOf("<key id=\"URI\"")>-1) this.LiftingExists = true;
	  for (int i= 0; i<m.length; i++) {
			String line = m[i].trim();
			if (this.getTypetion(line) == AnnotationType.typeNULL) continue;
			if (this.getTypetion(line)== AnnotationType.typeCONTEXT){
				while (!line.endsWith(";")){
					i++;
					line += m[i].trim();
			}}
			this.annotateIt (line);
		}
	}


private JSONObject embeddedElementContainner = new JSONObject();
private JSONObject resContainner = new JSONObject();

	public void annotateIt (String line){
		JSONObject j = new JSONObject();
		int typetion=getTypetion(line);
	if (AnnotationType.typeTYPE == typetion){
	String type=getTypeClass(line);
	String clas = getClas(type);
	String classontologyprefix = getPrefix(type);
	String classontologyuri = getOntologyUri(classontologyprefix);
	typeclass= type;
	if (!ontologyList.contains(classontologyuri)) ontologyList.add(classontologyuri);		
	  j.put("type", type);
	  j.put("class", clas);
	  j.put("classontologyprefix", classontologyprefix);
	  j.put("classontologyuri", classontologyuri);
	  resContainner.put("type", j);
	}
	//embedded
	if (AnnotationType.typeEMBEDDEDTYPE == typetion){
		String clas=getTypeClass(line);
		String classontologyprefix = getPrefix(clas);
		String classontologyuri = getOntologyUri(classontologyprefix);
        if (!ontologyList.contains(classontologyuri)) ontologyList.add(classontologyuri);		
	
	  String oneJLD=getTypeJLD(line);
	  j = (JSONObject)embeddedElementContainner.get(oneJLD);
	 if (j == null){
		 j = new JSONObject();
	  }
	    j.put("type", "embedded");
	    j.put("class", clas);
	    j.put("classontologyprefix", classontologyprefix);
	    j.put("classontologyuri", classontologyuri);
	  embeddedElementContainner.put(oneJLD, j);
	  if (j.get("attribute")!= null && j.get("property")!= null && j.get("class")!= null){
	   // ldw.annotate (j.path, j);
	    embeddedElementContainner.put(oneJLD, null);
	  }
	}
	if (AnnotationType.typeEMBEDDEDID == typetion){
	 String path = getPath(line);
	 String attribute = path;
	 String uripattern = getLink(line);
	 String regex = getRegexp(line);
	 String oneJLD=getTypeJLD(line);
	 j = (JSONObject) embeddedElementContainner.get(oneJLD);
	 if (j == null){
	   j = new JSONObject();
	 }
	   j.put("type", "embedded");
	   j.put("path", path);
	   j.put("attribute", attribute);
	   j.put("uripattern", uripattern);
	   j.put("regex",  regex);
	 embeddedElementContainner.put(oneJLD, j);
	 if (j.get("attribute") !=null && j.get("property")!=null && j.get("class")!=null){
	   //ldw.annotate (j.path, j);
	   embeddedElementContainner.put (oneJLD, null);
	 }
	}

	if (AnnotationType.typeEMBEDDEDPROPERTY == typetion){
		String  property = getProperty(line);
		String ontologyprefix = getPrefix(property);
		String ontologyuri = getOntologyUri(ontologyprefix);
		if (!ontologyList.contains(ontologyuri)) ontologyList.add(ontologyuri);		
	
		String oneJLD=getTypeJLD(line);
		j = (JSONObject)  embeddedElementContainner.get(oneJLD);
		if (j == null){
			   j = new JSONObject();
			 }
	  j.put("type", "embedded");
	  j.put("property", property);
	  j.put("ontologyprefix", ontologyprefix);
	  j.put("ontologyuri", ontologyuri);
	embeddedElementContainner.put(oneJLD, j);
	if (j.get("attribute") !=null && j.get("property") !=null && j.get("class") !=null){
	  //ldw.annotate (j.path, j);
	  embeddedElementContainner.put(oneJLD, null);
	}
	}
	//others
	//var j = '{"type":"normal","ontologyprefix":"'+ontprefix+'","ontologyuri":"'+onturi+'","property":"'+anchor.get ('formProperties').value+'","dataset":"'+dataSetValue+'","path":"'+path+'", "regex":"'+re+'"}';
	if (AnnotationType.typeNORMAL == typetion || AnnotationType.typeNORMALINTERLINK == typetion){
		String property = getProperty(line);
		String ontologyprefix = getPrefix(property);
		String ontologyuri = getOntologyUri(ontologyprefix);
		if (!ontologyList.contains(ontologyuri)) ontologyList.add(ontologyuri);
		if (AnnotationType.typeNORMALINTERLINK == typetion){
			String target = getLink(line);
			interlinkList.put(Utils.getVoIDURIVALUE(target), property); 
		}else{
			if (!propertyList.contains(property)) propertyList.add(property);
		}
		String path = getPath(line);
		String uripattern = getLink(line);
		String regex = getRegexp(line);
		j = new JSONObject();
	j.put("type", "normal");
	j.put("property", property);
	j.put("ontologyprefix", ontologyprefix);
	j.put("ontologyuri", ontologyuri);
	j.put("path", path);
	// ?? j.put("attribute", attribute);
	j.put("uripattern", uripattern);
	j.put("dataset", uripattern);
	j.put("regex",  regex);
	j.put("line",  line);
	//ldw.annotate (j.path, j);
	resContainner.put(path, j);
	}
	if (AnnotationType.typeCONTEXT == typetion){
		line = Utils.getFromToChar(line, "{", ";");
		contextJS = getContext(line);
		  resContainner.put("context", contextJS);
		  Iterator it = contextJS.keySet().iterator();
		  while (it.hasNext()){
			  String prefix = it.next().toString();
			  String onto = contextJS.get(prefix).toString();
			  WrapperManager.DB.saveOntology(prefix, onto);
		  }
	}
	}


private class AnnotationType {
	private static final int typeNULL = 0;
	private static final int typeTYPE = 1;
	private static final int typeNORMAL = 21;
	private static final int typeNORMALINTERLINK = 22;
	private static final int typeEMBEDDEDTYPE = 3;
	private static final int typeEMBEDDEDID = 4;
	private static final int typeEMBEDDEDPROPERTY =5;
	private static final int typeCONTEXT = 6;
}


private int getTypetion (String txt){
	int res = AnnotationType.typeNULL;
txt = txt.replaceAll(" ", "");
txt= txt.toLowerCase();
if (txt.indexOf("onejsonld['@context']")>-1) return AnnotationType.typeCONTEXT;
if (txt.indexOf("onejsonld['@type']")>-1) return AnnotationType.typeTYPE;
if (txt.indexOf("onejsonld['@id']")>-1 || txt.indexOf("varloop=")>-1 ) return AnnotationType.typeNULL;
if (txt.indexOf("['@type']")>-1) return AnnotationType.typeEMBEDDEDTYPE;
if (txt.indexOf("['@id']")>-1) return AnnotationType.typeEMBEDDEDID;
if (txt.indexOf("onejsonld['")>-1 && txt.indexOf("getinterlink")>-1) return AnnotationType.typeNORMALINTERLINK;
if (txt.indexOf("onejsonld['")>-1 && txt.indexOf("=[]")==-1) return AnnotationType.typeNORMAL;
if (txt.indexOf("=get")==-1 && txt.indexOf("=[]")==-1 && (txt.indexOf("']=")>-1 || txt.indexOf("'].push")>-1)) return AnnotationType.typeEMBEDDEDPROPERTY;
return res;
}


private String getTypeClass (String txt){
	txt = txt.replaceAll(" ", "");
	txt = Utils.getFirstRegExp(txt, "=\'[^\']*");
	txt = txt.replaceAll("='", "");
	return txt;
}


private String  getProperty (String txt){
	txt = txt.replaceAll(" ", "");
	txt = Utils.getFirstRegExp(txt, "\'[^\']*");
	txt = txt.replaceAll("'", "");
	return txt;
}

private String  getPath (String txt){
	txt = txt.replaceAll(" ", "");
	txt = Utils.getFirstRegExp(txt, "\"[^\"]*");
	txt = txt.replaceAll("\"", "");
	return txt;
}

private String getTypeJLD (String txt){
String res;

res = Utils.getFirstRegExp(txt, "[^\\[]*");
res = res.trim();
return res;
}


private String  getLink (String txt){
	txt = txt.replaceAll(" ", "");
	txt = Utils.getFirstRegExp(txt, "\'http[^\']+");
	txt = txt.replaceAll("\'", "");
	return txt;
}


private String  getRegexp (String txt){
	txt = txt.replaceAll(" ", "");
	txt = Utils.getFirstRegExp(txt, "\\(/[^/]+");
	txt = txt.replaceAll("\\(/", "");
	return txt;
}

private JSONObject  getContext(String txt){
	JSONObject contJS = new JSONObject();
	Object obj = null;
	try {
		obj = parser.parse(txt);
	} catch (ParseException e) {
		e.printStackTrace();
	}
	JSONObject txtJS = (JSONObject) obj;

	Iterator<String> iterator = txtJS.keySet().iterator();
	while (iterator.hasNext()) {
		String prefix = iterator.next();
		String onto = null;
		try{
			onto = (String)txtJS.get(prefix);
		} catch (Exception e) {
			continue;
		}
		if (! onto.trim().startsWith("http")) continue;
        contJS.put(prefix, onto);
	}
    return contJS;
  }

private String getPrefix(String txt){
    String[] res = txt.split(":");
    String prefix = null;
    if (res.length>1) prefix= res[0];
    return prefix;
    }

private String  getClas(String txt){
    String[] res = txt.split(":");
    String clas = null;
    if (res.length>1) clas= res[1];
    else clas = txt;
    return clas;
  }

private JSONObject contextJS = new JSONObject();

private String  getOntologyUri(String txt){
if (txt == null) return null;
String onto = (String)contextJS.get (txt);
return onto;
}


@Override
public JSONObject getRegisteringHealth(JSONObject credentials) {
	JSONObject js= new JSONObject();
	JSONObject jsSyntax = checkSyntax();
	js.put("checksyntax", jsSyntax);
    js.put("checkdereferentiation", new JSONObject());
	if ((Boolean)jsSyntax.get("result")){
		JSONObject jsDeref = checkDereferentiation(credentials);
		js.put("checkdereferentiation", jsDeref);
	}
	return js;
}

public List <String> getCredentialValueList (){
	List <String> creds = new ArrayList();
	JSONObject credentials = (JSONObject)jsMetadata.get("credentials");
	Set c = credentials.entrySet();
	for (Object ent: c){
		Entry e = (Entry)ent;
		String key = (String)e.getKey();
		String value = (String)e.getValue();
		creds.add(key);
		creds.add(value);
	}
    return creds; 
}

@Override
public JSONObject getProductionHealth() {
	try{
	JSONObject health = new JSONObject();	
//Quality metrics
	health.put("name", getMetadataString("wrappername"));
	health.put("author", getMetadataString("author"));
	health.put("description", getDescription ());
	health.put("service", getMetadataString("service"));
	health.put("void", getMetadata("void:inDataset").get("@id"));
	String wrid2 = getRealURIPattern();
	if (!wrid2.endsWith("/")) wrid2+= "/";
	health.put("hydra", wrid2+Cons.APIDOC_key);
	JSONObject qualityMetadata = (JSONObject)this.qualityMetadata;	
	JSONArray arr = new JSONArray();
	JSONObject arrObj = new JSONObject();
	URILookup exuri = getURIExamples().get(0);
	JSONArray errarr = new JSONArray();
	String status = qualityMetadata.get("statuscode").toString();
	errarr.add(status);
	health.put("errors", errarr);
	arrObj.put("statement", exuri.getURI().toString());
	arrObj.put("status_code", status);
	arrObj.put("status_msg", qualityMetadata.get("statusmessage"));
	arrObj.put("source", getMetadataString("wrapperurl"));
	arrObj.put("credentials", getCredentialValueList().toString());
	arr.add(arrObj);
	health.put("queries",arr);
	health.putAll(qualityMetadata);		
	return health;
	}catch (Exception err){
		return new JSONObject();
	}
}

@Override
public void setWrapperURL(URL url) {
	setMetadata("wrapperurl", url.toString());
	JSONObject metadata = getMetadata();
	metadata.put("wrapperurl", url);
	Set<Entry<String, URILookup>> uriLs = this.exampleURIs.entrySet();
	for (Entry uriL: uriLs){
		URILookup uri = (URILookup)uriL.getValue();
		uri.setWrapperURL(url);
	}
}


@Override
public String getType() {
	return this.type;
}


}
