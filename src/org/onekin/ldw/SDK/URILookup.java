package org.onekin.ldw.SDK;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.onekin.ldw.WrapperManager;
import org.onekin.ldw.util.Cons;
import org.onekin.ldw.util.Utils;

public class URILookup {   //implements URILookupI {

	private Boolean matches = false;
	private String ODTTable;
	private JSONObject params = new JSONObject();
	//IKER
	private String uriPattern = null;  //statement
	//private Long elapsedTime= new Long (0);
//	private Long latency= new Long(99999);
	//private Integer throughput = 0;		
//	private Integer transientThroughput = 0;
//	private Long lastCallMiliseconds = new Long(0); 
	
	private static Random randomGenerator = new Random(System.currentTimeMillis());
	  
	private String use;	
	//Diagnostics
			private String id = "";     //uri example returns data
			private URI uri = null;  //statement
			private JSONObject liftingMetadata = new JSONObject();     //call result.
			private JSONObject loweringMetadata = new JSONObject();     //call result.
			private JSONObject qualityMetadata = new JSONObject();     //call result.
					
	 public URILookup(URI uriExample, String uriPattern,String ODTurl){
		 this.uriPattern= uriPattern;
		 this.use= ODTurl;
		 JSONObject pars = Utils.bindVariables(uriPattern, uriExample.toString());
		 this.matches = (Boolean) pars.get("matches");
		 this.ODTTable = Utils.parseURIPattern(uriPattern).get(Cons.WRAPPERID_KEY).toString();
  		 this.uri = uriExample;
		 JSONObject binds = (JSONObject)pars.get("bindings");
		 for (Object keyS : binds.keySet()){
			 String key = (String) keyS;
			 String paramV = (String) binds.get(key);
			  paramV = paramV.replaceAll("'", "");
			 paramV = paramV.replaceAll("\"", "");		 
			 this.params.put(key, paramV);		 
		 }	
	 } 
	 
	 public Set<String> getVariables (){
		 return this.params.keySet();
	 }
	 
	 public String getMatchingParams (){
		 JSONObject binds = this.params;
		 String res = "";
		 for (Object keyS : binds.keySet()){
			 String key = (String) keyS;
			 String paramV = (String) binds.get(key);
			  paramV = paramV.replaceAll("'", "");
			 paramV = paramV.replaceAll("\"", "");		 
			 res += " " + key.toString() +"=>"+ paramV.toString();		 
		 }	
		 return res;
	 }

	 public Boolean checkMatches (){
		 return this.matches;
	 }
	 
	 public URI getURI (){
		 return this.uri;
	 }
	 
	 private URL toLiftingRest (String env, String use, JSONObject credentials){
		 String yql=YQLSelect (env, use, credentials);
		 yql = yql + " | "+  ODTTable +".lifting ( '"+ uri  +"')";  //" + type + "
		 try {
			yql = URLEncoder.encode(yql, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 yql = "https://query.yahooapis.com/v1/public/yql?q="+yql+"&format=json&jsonCompat=new&diagnostics=true&debug=true";//&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
		 URL uri = null;
		try {
			uri = new URL(yql);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return uri;
	 }
	 
	 private String YQLSelect (String env, String use, JSONObject credentials){
		 String yql="";
		 String num = Integer.toString(randomGenerator.nextInt(10000));
		 if (! Utils.isVoidOrNull(env)) yql= yql + "ENV '"+env+"'; ";   //the environment always includes a use clause 
		 else yql= yql + "use '"+use+"?id="+num+"' as " + ODTTable + "; ";		 
		 
		 yql=yql + "SELECT * FROM " + ODTTable ;
		 Boolean first=true;
		 if (params.size()>0){
			 for (Object keyS : params.keySet()){
				 String key = (String) keyS;
				 if (first){
					 yql = yql+ " WHERE " + key + "= '"+params.get(key)+ "'";
					 first=false;
				 }else{
					 yql = yql + " AND "+  key + "= '"+params.get(key)+ "'";
				 }
			}
		 }
		 if (! Utils.isVoidOrNull(credentials)){
			 try{
			 JSONArray properties = (JSONArray)credentials.get("hydra:supportedProperty");
			 for (Object prop :properties){
				 JSONObject property = (JSONObject)prop;
				 property = (JSONObject) property.get("hydra:property");
				 String key = (String) property.get("schema:name");
				 String value = (String) property.get("schema:value");
				 if (first){
					 yql = yql+ " WHERE " + key + "= '"+value+ "'";
					 first=false;
				 }else{
					 yql = yql + " AND "+  key + "= '"+value+ "'";
				 }
			}
			 }catch (Exception e){
				 JSONObject property = (JSONObject)credentials.get("hydra:supportedProperty");
				 	 property = (JSONObject) property.get("hydra:property");
					 String key = (String) property.get("schema:name");
					 String value = (String) property.get("schema:value");
					 if (first){
						 yql = yql+ " WHERE " + key + "= '"+value+ "'";
						 first=false;
					 }else{
						 yql = yql + " AND "+  key + "= '"+value+ "'";
					 }					 
				}				 
			 }		 
		 
		 return yql;
	 }
	 
	 private URL toSelectRest (String env, String use, JSONObject credentials){
		 String yql=YQLSelect (env, use, credentials);
		 try {
			yql = URLEncoder.encode(yql, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 yql = "https://query.yahooapis.com/v1/public/yql?q="+yql+"&format=json&jsonCompat=new&diagnostics=true&debug=true";//&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
		 URL uri = null;
		try {
			uri = new URL(yql);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return uri;
	 }
		
	public JSONObject derefLifting (JSONObject credentials){
		String env = null;
		if (!Utils.isVoidOrNull(credentials)){
			env = (String)credentials.get("environment");
		}		
		JSONObject res ;
		res = callingYQL(this.toLiftingRest(env, use, credentials));   
		return res;
		}
	
	public JSONObject derefLowering(JSONObject credentials) {
		//TODO: iker. si en lugar de token environment ofrecen user y password??
		String env = null;
		if (!Utils.isVoidOrNull(credentials)){
			env = (String)credentials.get("environment");
		}
		JSONObject res = callingYQL(this.toSelectRest(env, use, credentials));		
		this.loweringMetadata = res;
		return res;
		}
	
	public void setWrapperURL(URL url) {
		this.use = url.toString();
	}
	
	public JSONObject callingYQL (URL url){
		JSONObject res ;
        res = callYQL(url);
		
		if (res.get("statuscode").toString().equals(Cons.BLANKS_CODE)) {
//			res.put("statuscode", Cons.OKS_CODE);
			String st = "";
			JSONObject r = (JSONObject)res.get("content");
			JSONArray ra = new JSONArray();
			try{
			ra = (JSONArray)r.get("javascript");
			}catch(Exception e){
				try{
				JSONObject a = (JSONObject)r.get("javascript");
				if (a != null){
					ra.add(a);
				}
				}catch (Exception e1){}
			}
				for (int i = 0; i < ra.size(); i++){
					try {
						String key = (String)ra.get(i);				
					if (key.startsWith("Error") || key.startsWith("Exception") ){ 
						st += "\n\r" + key;
						res.put("statuscode", Cons.FAILS_CODE);
					}
					}catch (Exception e){}
				}
				
				try{
					ra = (JSONArray)r.get("url");
					}catch(Exception e){
						try{
						JSONObject a = (JSONObject)r.get("url");
						if (a != null){
							ra.add(a);
						}
						}catch (Exception e1){}
					}
						for (int i = 0; i < ra.size(); i++){
							try {
								JSONObject key = (JSONObject)ra.get(i);				
								String skey = key.get("error").toString();				
							if (skey != null){ 
								st += "\n\r" + skey;
								res.put("statuscode", Cons.FAILS_CODE);
							}
							}catch (Exception e){}
						}				
     		res.put("statusmessage",st);
		}
		return res;
		}

	private JSONObject callYQLDELETE(URL url) {
		JSONObject result= new JSONObject (); 
		try {
			result = Utils.callingDelay (url.toString(), 0);
			String res = result.get("content").toString();
			JSONParser parser = new JSONParser();
			JSONObject r = null;
			r = (JSONObject)parser.parse(res);
			
			JSONObject diagnostics = (JSONObject)r.get("query");
			diagnostics = (JSONObject)diagnostics.get("diagnostics");
			r = (JSONObject)r.get("query");
			r = (JSONObject)r.get("results");
			if (Utils.isVoidOrNull(r)){
				result.put ("statuscode", Cons.FAILS_CODE);
				result.put ("statusmessage", "See diagnostics messages.");
				result.put("content", diagnostics);
				return result;
			}
			JSONObject item = null;
			if (!Utils.isVoidOrNull(r.get("result"))){
				    Boolean iterate = false;
			    if (r.get("result") instanceof JSONArray) {
				    // It's an array
					item = (JSONObject)((JSONArray)r.get("result")).get(0);
					iterate =true;
				}
				else if (r.get("result") instanceof JSONObject) {
				    // It's an object
					item = (JSONObject)r.get("result");
					iterate =false;
				}
				else {
				    // It's something else, like a string or number
					item=r;
				}				
				if (iterate){
					for (Object j2: ((JSONArray)r.get("result"))){
						item=Utils.copyJSON(item, (JSONObject) j2);
					}
				}
		}else{
			item=r;
		}
		r= item;
		result.put("content", r);					
		} catch (ParseException e) {
			e.printStackTrace();
			result.put ("statuscode", Cons.CRITICALERROR_CODE);
			result.put ("statusmessage", "Unknown error"); //"See diagnostics messages.");
			JSONObject error = new JSONObject();
			error.put("error", "Unknown error");
			result.put("content", error);
		}
		return result;
	}
	
	
	private JSONObject callYQL(URL url) {
		JSONObject result= new JSONObject (); 
		try {
			result = Utils.callingDelay (url.toString(), 0);
			String res = result.get("content").toString();
			JSONParser parser = new JSONParser();
			JSONObject r = null;
			r = (JSONObject)parser.parse(res);

			if (result.get("statuscode").toString().startsWith(Cons.FAILSXXX_CODE)){
				result.put ("statuscode", Cons.FAILS_CODE);
				result.put ("statusmessage", "Error in the API server");
				/*JSONObject error = new JSONObject();
				error.put("error", "No data retrieved.");
				result.put("content", error);*/
				return result;
			}
			
			JSONObject diagnostics = (JSONObject)r.get("query");
			diagnostics = (JSONObject)diagnostics.get("diagnostics");
			if (result.get("statuscode").toString().startsWith(Cons.BLANKSXXX_CODE)){
				result.put ("statuscode", Cons.BLANKS_CODE);
				result.put ("statusmessage", "URI not found");
				result.put("content", diagnostics);
/*				JSONObject error = new JSONObject();
				error.put("error", "No data retrieved.");
				result.put("content", error);*/
				return result;
			}

			if (result.get("statuscode").toString().startsWith(Cons.OKSXXX_CODE)){
				result.put ("statuscode", Cons.OKS_CODE);
			}
			
			
			r = (JSONObject)r.get("query");
			r = (JSONObject)r.get("results");
			if (Utils.isVoidOrNull(r)){
				result.put ("statuscode", Cons.BLANKS_CODE);
				result.put ("statusmessage", "See diagnostics messages.");
				result.put("content", diagnostics);
				return result;
			}
			JSONObject item = null;
			if (!Utils.isVoidOrNull(r.get("result"))){
				    Boolean iterate = false;
			    if (r.get("result") instanceof JSONArray) {
				    // It's an array
					item = (JSONObject)((JSONArray)r.get("result")).get(0);
					iterate =true;
				}
				else if (r.get("result") instanceof JSONObject) {
				    // It's an object
					item = (JSONObject)r.get("result");
					iterate =false;
				}
				else {
				    // It's something else, like a string or number
					item=r;
				}				
				if (iterate){
					for (Object j2: ((JSONArray)r.get("result"))){
						item=Utils.copyJSON(item, (JSONObject) j2);
					}
				}
		}else{
			item=r;
		}
		r= item;
		result.put("content", r);					
		} catch (ParseException e) {
			e.printStackTrace();
			result.put ("statuscode", Cons.FAILS_CODE);
			result.put ("statusmessage", "Unknown error"); //"See diagnostics messages.");
			JSONObject error = new JSONObject();
			error.put("error", "Unknown error");
			result.put("content", error);
		}
		return result;
	}
}