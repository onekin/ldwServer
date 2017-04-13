package org.onekin.ldw.util;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.onekin.ldw.WrapperManager;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.gson.JsonElement;
////

//////


public class Utils {

private static DocumentBuilder ldocBuilder;
private static SSLContext sc;
private static TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){}
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){}
        }
    };

	
	static{
	    try {
	    	sc = SSLContext.getInstance("SSL");
			ldocBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	
	 public static String getWrapperName (String ODT){

		    	String reg = "(?i)\\s*?URIPattern\\s*?:(.+?)(<\\s*?/\\s*?sampleQuery\\s*?>)";  //samplequery
		    	String URIPattern = getFirstRegExp(ODT, reg);
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
		    	String name= Utils.parseURIPattern(URIPattern).get(Cons.WRAPPERID_KEY).toString();
	
			return name;
		    }    

	 public static String getFromToChar (String text, String from, String to){
			String result = null;
				int begin = text.indexOf(from);
				int end = text.indexOf(to);
				if (begin == -1 || end ==-1) return "";
				result = text.substring(begin, end);
				return result;
		}

	 public static String clean (String text, List<String> what){
			String result = text;
			for (String w: what){
				result = result.replaceAll(w, "");
			}
			return result;
		}

	 public static List<String> splitFirst (String text, String  what){
		    List<String> result = new ArrayList<String>();
			int begin = text.indexOf(what);
			result.add(text.substring(0, begin));
			result.add(text.substring(begin+1, text.length()));
			return result;
		}

		public static String getFirstRegExp (String text, String reg){
			String result = Cons.VOID;
			try{
	    		result = getRegExp (text, reg).get(0).trim();
			}finally{
				return result;
			} 
		}
		

		public static List<String> getRegExp (String text, String reg){
		    Pattern pattern = Pattern.compile(reg);
	        Matcher matcher = pattern.matcher(text);
	        boolean found = false;
	        String result = "";
	        List<String> results = new ArrayList<String>();
	        while (matcher.find()) {
	        	result = matcher.group();
	        	results.add(result.trim());
	    		//int begin = matcher.start(),
	            //int end = matcher.end());
	                    found = true;
	                }
	           	return results;     			
		}

		    
	public static JSONObject parseURIPattern (String pattern){
		pattern = pattern.replace(WrapperManager.derefserver, "");
		JSONObject obj = new JSONObject();
		String[] parts = pattern.split("/");
		String service = pattern;
		String type = pattern;
		String method = pattern;
		if (parts.length>=3){
			service = parts[1];
			type = parts[2];
		}
		obj.put("service", service);
		obj.put("type", type);
		obj.put("wrapperid", service+'.'+type);
		return obj; 		
	}
	
	
	
	public static String toHTML(String txt){
		 String res = txt.replaceAll(",",",<br/>");
	   try{		
		 res= res.replaceAll("(http[^\"]*)", "<a href=\"$1\">$1</a>");
		   
	} catch (Exception e) {
		return "";
	}
		     return res; 
	}
	
	public static String parseTXT (String txt, String startTXT, String endTXT){
	try{		
	    	int start = txt.indexOf(startTXT);
		if (start == -1) return "";
		String parsed = txt.substring(start+startTXT.length());
		parsed = parsed.substring(0,parsed.indexOf(endTXT)).trim();
		return parsed;
	} catch (Exception e) {
		return "";
	}
	}
	
	public static StringBuffer read(InputStream in, int contentLength) {
		StringBuffer result = new StringBuffer();
		if (contentLength <= 0) {
			try {
				in.close();
			} catch (Exception e) {
			}
			return result;
		}
		try {
			int readBytes = -1;
			byte[] buffer = new byte[contentLength];
			while ((readBytes = in.read(buffer, 0, contentLength)) > 0) {
				String blah = new String(buffer, 0, readBytes, "UTF-8");
				result.append(blah);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return result;
	}

	
	 public static String makeString(Node node) {
	        try {
	            Source source = new DOMSource(node);
	            StringWriter stringWriter = new StringWriter();
	            Result result = new StreamResult(stringWriter);
	            TransformerFactory factory = TransformerFactory.newInstance();
	            Transformer transformer = factory.newTransformer();
	            transformer.transform(source, result);
	            String text=  stringWriter.getBuffer().toString();	            
	            text=text.replaceAll("<[?]xml version=\"1.0\" encoding=\"UTF-8\"[?]>", "");
	            return text;
	        } catch (TransformerConfigurationException e) {
	            e.printStackTrace();
	        } catch (TransformerException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	 
	public static void sendRequest(String request)
	{
	    // i.e.: request = "http://example.com/index.php?param1=a&param2=b&param3=c";
	    URL url;
		try {
			url = new URL(request);
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
	    connection.setDoOutput(true); 
	    connection.setInstanceFollowRedirects(false); 
			connection.setRequestMethod("GET");
	    connection.setRequestProperty("Content-Type", "text/plain"); 
	    connection.setRequestProperty("charset", "utf-8");
			connection.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 /**
	   * Encodes the passed String as UTF-8 using an algorithm that's compatible
	   * with JavaScript's <code>encodeURIComponent</code> function. Returns
	   * <code>null</code> if the String is <code>null</code>.
	   * 
	   * @param s The String to be encoded
	   * @return the encoded String
	   */
	  public static String encodeURIComponent(String s)
	  {
	    String result = null;

	    try
	    {
	      result = URLEncoder.encode(s, "UTF-8")
	                         .replaceAll("\\+", "%20")
	                         .replaceAll("\\%21", "!")
	                         .replaceAll("\\%27", "'")
	                         .replaceAll("\\%28", "(")
	                         .replaceAll("\\%29", ")")
	                         .replaceAll("\\%7E", "~");
	    }

	    // This exception should never occur.
	    catch (UnsupportedEncodingException e)
	    {
	      result = s;
	    }

	    return result;
	  }  
	  
	public static JSONObject string2json (String txt){
		JSONParser parser = new JSONParser();
		JSONObject res = new JSONObject();
		try {
			res = (JSONObject)parser.parse(txt);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	
	public static String stream2string (InputStream stream){
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		String res = sb.toString();
		return res;
	}

	public static JSONObject callJson(URL url) {
		JSONObject YQLJson= new JSONObject (); 
		JSONObject r = null;
		try {
			YQLJson = callingDelay (url.toString(), 0);
			String res = YQLJson.get("content").toString();
			JSONParser parser = new JSONParser();
			r = (JSONObject)parser.parse(res);		
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}


public static JSONObject copyJSON (JSONObject json1, JSONObject json2){
	for (Object j2 : json2.entrySet()) {
		//import com.google.gson.JsonElement;
		Map.Entry<String,JSONObject> j = (Map.Entry<String,JSONObject>) j2;
		String key = j.getKey();
			Object data = json1.get(key);
			Object data22 =json2.get(key);
			if (Utils.isVoidOrNull(data22)) continue;
			if (Utils.isVoidOrNull(data)){
				json1.put(key, data22);
			}else{
				if (data instanceof JSONArray) {
				    // It's an array
					if (!(Utils.areEqual(data, data22))){
						((JSONArray)data).add(data22);
						json1.put(key,data);
					}
				}else if (data instanceof JSONObject) {
					    // It's an object
						if (!(Utils.areEqual(data, data22))){
							JSONArray data2 = new JSONArray();
							data2.add(data);
							data2.add(data22);
							json1.put(key, data2);
						}
				}else{
					 // It's an string or integer
					if (data.equals(data22)) continue;
					JSONArray data2 = new JSONArray();
					data2.add(data);
					data2.add(data22);
					json1.put(key, data2);
				}
		}
	}
	return json1;
}

	public static String getURIRegex(String URI) {
		URI= getVoIDURIVALUE(URI);
		while (URI.indexOf(Cons.VARCODINGBEGIN)>-1){
			URI = URI.substring(0,URI.indexOf(Cons.VARCODINGBEGIN)) +"[^/]+" + URI.substring(URI.indexOf(Cons.VARCODINGEND)+1);
		}
		return URI;
	}
	


	public static String getWrapperURI(String URI) {
		URI= getVoIDURIVALUE(URI);
		while (URI.indexOf(Cons.VARCODINGBEGIN)>-1){
			URI = URI.substring(0,URI.indexOf(Cons.VARCODINGBEGIN)) +Cons.URLCODINGBEGIN + URI.substring(URI.indexOf(Cons.VARCODINGBEGIN)+1);
		}
		while (URI.indexOf(Cons.VARCODINGEND)>-1){
			URI = URI.substring(0,URI.indexOf(Cons.VARCODINGEND)) +Cons.URLCODINGEND + URI.substring(URI.indexOf(Cons.VARCODINGEND)+1);
		}
		return URI;
	}

	public static String getVoIDURIVALUE(String URI) {
		String ID = URI;
		if (ID.indexOf(Cons.VARCODINGBEGIN)>-1){
			while (ID.indexOf(Cons.VARCODINGBEGIN)>-1){
				ID = ID.substring(0, ID.indexOf(Cons.VARCODINGBEGIN))+"#VALUE#"+ID.substring(ID.indexOf(Cons.VARCODINGEND)+1);
			}
			HashMap param = new HashMap();
			param.put("#VALUE#", Cons.URLCODINGBEGIN+"value"+Cons.URLCODINGEND);
			ID = replaceVariables (ID, param);
		}else{
			String txt = URI.substring(URI.indexOf("://")+3); 
			if (txt.indexOf("/")>-1){
				txt = txt.substring(0,txt.indexOf("/"));	
				ID= URI.substring(0, URI.indexOf("://")+3) + txt;
			}else{
				ID=URI;
			}
		}
		return ID;
	}

	public static String getVoIDURIINPUTS(String URI, JSONObject ALIAS) {
		String ID = URI;
		for (Object alias : ALIAS.keySet()){
			String ali = alias.toString();
			String input = ALIAS.get(ali).toString();
			ID= ID.replaceAll("\\"+Cons.VARCODINGBEGIN+ali+"\\"+Cons.VARCODINGEND, Cons.URLCODINGBEGIN+input+Cons.URLCODINGEND);
		}
		return ID;
	}

    /**
     * Convert the request parameters to a string
     *
     * @param request Servlet request
     * @return Request parameters in the form &amp;name=value
     */
    public static String convertRequestParams(HttpServletRequest request) {
        Enumeration paramNames = request.getParameterNames();
        StringBuffer buffer = new StringBuffer();
        while (paramNames.hasMoreElements()) {
            String name = (String) paramNames.nextElement();
            String value = request.getParameter(name);
            try {
                buffer.append(URLEncoder.encode(name, Cons.UTF8)).append("=").append(URLEncoder.encode(value, Cons.UTF8));
            } catch (UnsupportedEncodingException e) {
            }
            if (paramNames.hasMoreElements()) {
                buffer.append("&");
            }
        }
        return buffer.toString();
    }
    
    /**
     * Replace parameters on a string
     *
     * @param String the string
     * @return HashMap parameters in the form name,value
     */
    public static String replaceVariables(String text, HashMap<String,String> parameters) {
        for (String name: parameters.keySet()){
        	String value = parameters.get(name);
        	text = text.replaceAll(name, parameters.get(name));
        }
        return text;
    }
    
    /**
     * Checks to see if the string is null or blank (after trimming)
     *
     * @param input Input string
     * @return <code>true</code> if the string is null or blank (after trimming), <code>false</code> otherwise
     */
    public static boolean checkNullOrBlank(String input) {
        return (input == null || "".equals(input.trim()));
    }
    
    /**
     * Tries to retrieve a given key using getParameter(key) and if not available, will
     * use getAttribute(key) from the servlet request
     *
     * @param key  Parameter to retrieve
     * @param httpServletRequest Request
     * @param preferAttributes   If request attributes should be checked before request parameters
     * @return Value of the key as a string, or <code>null</code> if there is no parameter/attribute
     */
    public static String getRequestValue(String key, HttpServletRequest httpServletRequest, boolean preferAttributes) {
        if (!preferAttributes) {
            if (httpServletRequest.getParameter(key) != null) {
                return httpServletRequest.getParameter(key);
            } else if (httpServletRequest.getSession().getAttribute(key) != null) {
                return httpServletRequest.getSession().getAttribute(key).toString();
            }
        } else {
            if (httpServletRequest.getSession().getAttribute(key) != null) {
                return httpServletRequest.getSession().getAttribute(key).toString();
            } else if (httpServletRequest.getParameter(key) != null) {
                return httpServletRequest.getParameter(key);
            }
        }
        return null;
    }
    
 // convert InputStream to String
 	public static String getStringFromInputStream(InputStream is) {
  
 		BufferedReader br = null;
 		StringBuilder sb = new StringBuilder();
  
 		String line;
 		try {
  
 			br = new BufferedReader(new InputStreamReader(is));
 			while ((line = br.readLine()) != null) {
 				sb.append(line+"\n");
 			}
  
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return sb.toString(); 
 	}

 	public static String getStringFromInputStream2(InputStream is) {
 		  
 		BufferedReader br = null;
 		StringBuilder sb = new StringBuilder();
  
 		String line;
 		try {
  
 			br = new BufferedReader(new InputStreamReader(is));
 			while ((line = br.readLine()) != null) {
 				sb.append(line);
 			}
  
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return sb.toString(); 
 	}

	public static Map<String, String> matchVariables (String pattern, String uri){
		Map<String, String> pares = new HashMap<String, String>();
		String[] partsp = pattern.split("/");
		String[] partsu = uri.split("/");
		for (int i=0;i<partsp.length;i++){
			if (partsp[i].startsWith("{")){
				String key =partsp[i];
				key = key.replace("{", "");
				key = key.replace("}", "");
				String value = partsu[i];
				pares.put(key, value);
			}else {
				String vp=partsp[i];
			    String vu=partsu[i];
				if (vp.compareTo(vu)!=0) return null;
				}			
		}		
		return pares;			
		}

	public static JSONObject bindVariables (String pattern, String uri){
		JSONObject pares = new JSONObject();
		JSONObject bindings = new JSONObject();
		String[] partsp = pattern.split("/");
		String[] partsu = uri.split("/");		
		if (partsp.length != partsu.length){
			bindings.put("matches", false);
			bindings.put("bindings", pares);
			return bindings;
		}
		for (int i=0;i<partsp.length;i++){
			if (partsp[i].startsWith("{")){
				String key =partsp[i];
				key = key.replace("{", "");
				key = key.replace("}", "");
				String value = partsu[i];
				pares.put(key, value);
			}else {
				String vp=partsp[i];
			    String vu=partsu[i];
				if (vp.compareTo(vu)!=0){
					bindings.put("matches", false);
					bindings.put("bindings", pares);
					return bindings;
				}
				}			
		}
		bindings.put("matches", true);
		bindings.put("bindings", pares);
		return bindings;
	}

	  
public static String JSON2html (JSONObject json){
	String html = json.toString();
	html = html.replaceAll(",", ",<br/>");
	html = URL2HTML(html);
	return html;	
}

public static String JSONString2html (String html){
	html = html.replaceAll(",", ",<br/>");
	html = URL2HTML(html);
	return html;	
}

public static String URL2HTML(String txt) {
    String [] parts =txt.split("\"");
    String res = "";
    // Attempt to convert each item into an URL.   
    for( String item : parts ) try {
        URL url = new URL(item);
        // If possible then replace with anchor...
        res +="<a  target='_blank' href='" + url + "'>"+ url + "</a>\"";    
    } catch (MalformedURLException e) {
        // If there was an URL that was not it!...
        res += item + "\"";
    }
    return res;	
}

///////////


public static String convert(String content, RDFFormat targetFormat) {
	OutputStream outputStream = null;
	try {
		RDFParser rdfParser = Rio.createParser(RDFFormat.JSONLD);
		outputStream = new ByteArrayOutputStream(); //System.out;
		RDFWriter rdfWriter = null;
		rdfWriter = Rio.createWriter(targetFormat, outputStream);	
		rdfParser.setRDFHandler(rdfWriter);
//		InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		InputStream stream = new ByteArrayInputStream(content.getBytes("UTF-8"));
			rdfParser.parse(stream, "ik.ik");
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedRDFormatException e) {
			e.printStackTrace();
		}
	return outputStream.toString();
}

public static Boolean areEqual (Object js1, Object js2){
	JSONObject json1= ((JSONObject) js1);
	JSONObject json2= ((JSONObject) js2);
	for (Object j2 : ((JSONObject)json2).entrySet()) {
		Map.Entry<String,JsonElement> j = (Map.Entry<String,JsonElement>) j2;
		String key = j.getKey();
		Object data1 = json1.get(key);
		Object data2 = json2.get(key);
			if (data1 == null && data2 != null){
				return false; 
			}else if (data2 != null && data2 == null){
					return false; 
			}else if (data1 instanceof JSONArray) {
				if (!(data2 instanceof JSONArray)) return false;
				if (((JSONArray)data1).size() != ((JSONArray)data2).size()) return false;
				for (int k = 0; k<((JSONArray)data1).size(); k++){
					Boolean res = areEqual (((JSONArray)data1).get(k), ((JSONArray)data2).get(k));
					if (res ==false) return false;
				}	
			}
     		else if (data1 instanceof JSONObject) {
			    // It's an object
     			if (!(data2 instanceof JSONObject)) return false;
     			Boolean res =  areEqual (data1, data2);
     			if (res ==false) return false;
			}else{
				 // It's an string or integer
				if (!(data1.equals(data2))) return false;
				}
	}
	return true;
}

public static Boolean isVoidOrNull (Object js1){
	if (js1== null) return true;
	if(js1 instanceof JSONArray) {
		int s = ((JSONArray) js1).size();
		return s==0;}
	if(js1 instanceof String) {
		String value = (String)js1;
		return value.equals("") || value.equals("null");}
	if(js1 instanceof JSONObject) {
		Boolean value = ((JSONObject)js1).size() == 0;
		return value;}
	return false;
}


public static String calling(String url){
	return callingDelay(url, 0).get("content").toString();	
}


// HTTP POST request
private static String sendPost(String url, JSONObject data) throws Exception {

	URL obj = new URL(url);
	HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

	//add reuqest header
	con.setRequestMethod("POST");
//	con.setRequestProperty("User-Agent", USER_AGENT);
	con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
//*	con.setRequestProperty( "X-CKAN-API-Key", this._apikey );
//*	con.setRequestProperty( "Authorization", this._apikey );
	con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	    
//	String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

	// Send post request
	con.setDoOutput(true);
	DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//	wr.writeBytes(urlParameters);
	wr.writeBytes(data.toJSONString());
	wr.flush();
	wr.close();

	/*OutputStreamWriter wr2 = new OutputStreamWriter(con.getOutputStream());
	wr2.write(data.toString());
	wr2.flush();
	*/
	int responseCode = con.getResponseCode();
	System.out.println("\nSending 'POST' request to URL : " + url);
	//System.out.println("Post parameters : " + urlParameters);
	System.out.println("Response Code : " + responseCode);

	BufferedReader in = new BufferedReader(
	        new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
		response.append(inputLine);
	}
	in.close();
	//print result
	System.out.println(response.toString());
	return response.toString();
}

public static synchronized JSONObject callingDelay(String url, int time){
    URL u;
    String res = "";
    JSONObject response = new JSONObject();
    HttpURLConnection uc= null; 
	try {
		InputStream resp=null;
		if (url.startsWith("https")){
			        sc.init(null, trustAllCerts, new java.security.SecureRandom());
			        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
					u = new URL(url);
					uc= (HttpsURLConnection)u.openConnection();
					uc.setRequestProperty("Accept", "application/rdf+xml;level=1, */*");
				    resp=uc.getInputStream();
//			        resp=new URL(url).openStream();
			        res = stream2string (resp);
				    response.put ("content", res);
				    response.put ("statuscode", Integer.toString(uc.getResponseCode()));
				    response.put ("statusmessage", uc.getResponseMessage());
		}
		else{
			u = new URL(url);
			uc= (HttpURLConnection)u.openConnection();
		    resp=uc.getInputStream();
		    res = stream2string (resp);
		    response.put ("content", res);
		    response.put ("statuscode", Integer.toString(uc.getResponseCode()));
		    response.put ("statusmessage", uc.getResponseMessage());
		}
		
    } catch (Exception e) {
    	time += 1;
    	if (time < 3){
    		System.out.println(url + " CONNECTION PROBLEMS. RETRYING AFTER."+ time + " seconds");
    		try {
    			TimeUnit.SECONDS.sleep(time);
    		} catch (InterruptedException e1) {
    			e1.printStackTrace();
    		}
    		return callingDelay(url, time);
    	}else{
    		e.printStackTrace();
    		response.put ("content", "");
		    try {
				response.put ("statuscode", Integer.toString(uc.getResponseCode()));
				response.put ("statusmessage",uc.getResponseMessage());
			} catch (IOException e1) {
				response.put ("statuscode", Cons.FAILS_CODE);
				response.put ("statusmessage","Problems retrieving data.");
			} 		    
    	}
    }
    return response;
}

/////DEREFS:
public static JSONObject countBrokenLinks(JSONObject lifting){
	JSONObject res = new JSONObject ();
	JSONObject resa = new JSONObject ();
	Integer cont =0;
	if (Utils.isVoidOrNull(lifting)){
		res.put("count", cont.toString());
		res.put("brokenlinks", resa);		
		return res;
	}
	JSONObject context = (JSONObject)lifting.get("@context");
	if (Utils.isVoidOrNull(context)){
		res.put("count", cont.toString());
		res.put("brokenlinks", resa);		
		return res;
	}			
	for (Object obj: context.keySet()){
		String name = obj.toString();
		JSONObject value = null;
		try {
			value = (JSONObject)context.get(name);
		}catch (Exception e){
			continue;
		}
		if (value.get("@type").equals("@id")){
			String url = "";
		    try{
			  try{
		    	url = ((String)lifting.get(name));		
			  }catch(Exception e){
			    	url = ((JSONArray)lifting.get(name)).get(0).toString();		
			  }
			if (Utils.isVoidOrNull(url) || url.startsWith("http://rdf.onekin.org/")){ //To avoid circular dead locking calls.
				continue;					
			}
			if (!isDereferenceable(url)) {
				cont ++; 
				resa.put(name, url);
				continue;					
			}
		    }catch(Exception e){
				cont++;
				resa.put(name, url);
				continue;
			}
		}
	}		
	res.put("count", cont);
	res.put("brokenlinks", resa);		
	return res;
}

public static Boolean isDereferenceable (String url){
			JSONObject resp = Utils.callingDelay(url, 0);
			return resp.get("statuscode").toString().equals("200");
}


public static List<String> getKeyValuesAsList(JSONObject obje){
	List<String> list = new ArrayList();
	if (Utils.isVoidOrNull(obje)) return list;

	for (Object obj: obje.keySet()){
		String name = obj.toString();
		JSONObject valueObj = null;
		JSONArray valueArr = null;
		if (name.startsWith("@")) continue;
		try {
			valueObj = (JSONObject)obje.get(name);
			list.addAll(getKeyValuesAsList(valueObj));
			
		}catch (Exception e){
			try {
				valueArr = (JSONArray)obje.get(name);
				Iterator it = valueArr.iterator();
				while (it.hasNext()){
					valueObj = (JSONObject)it.next();
					list.addAll(getKeysAsList(valueObj));
				}
				continue;
			}catch (Exception e1){
				String value2 = obje.get(name).toString();
				list.add(name + "="+value2 );
			}
			}
	}
	return list;
}

public static int countNulls(JSONObject obje){
	int count=0;
	if (Utils.isVoidOrNull(obje)) return count;
	for (Object obj: obje.keySet()){
		String name = obj.toString();
		if (name.startsWith("@")) continue;
		try {String value = (String)obje.get(name);
			if (isVoidOrNull(value)) count++;
		}catch (Exception e1){
		try {Boolean value = (Boolean)obje.get(name);
			if (isVoidOrNull(value)) count++;
		}catch (Exception e2){
		try {JSONObject value = (JSONObject)obje.get(name);
			if (isVoidOrNull(value)) count++;
		}catch (Exception e3){
		try {
			JSONArray valueArr = (JSONArray)obje.get(name);
			if (valueArr.size()==0) count++;
		}catch (Exception e4){
			count++;
		}}}
		}
	}
	return count;
}


public static JSONObject countInterlinks(JSONObject lifting){
	JSONObject res = new JSONObject ();
	JSONObject resa = new JSONObject ();
	Integer cont =0;
	if (Utils.isVoidOrNull(lifting)){
		res.put("count", cont.toString());
		res.put("interlinks", resa);		
		return res;
	}
	JSONObject context = (JSONObject)lifting.get("@context");
	if (Utils.isVoidOrNull(context)){
		res.put("count", cont.toString());
		res.put("interlinks", resa);		
		return res;
	}			
	for (Object obj: context.keySet()){
		String name = obj.toString();
		JSONObject value = null;
		try {
			value = (JSONObject)context.get(name);
		}catch (Exception e){
			continue;
		}
		if (!Utils.isVoidOrNull(value.get("@type"))){
			String url = "";
		    try{
			     url = lifting.get(name).toString();
				cont ++; 
				resa.put(name, url);
				continue;					
		    }catch(Exception e){
				cont++;
				resa.put(name, url);
				continue;
			}
		}
	}		
	res.put("count", cont);
	res.put("interlinks", resa);		
	return res;
}

public static List<String> getKeysAsList(JSONObject obje){
	List<String> list = Utils.getKeyValuesAsList(obje);
	List<String> keys = new ArrayList();
	for (String KV: list){
		String key = KV.substring(0, KV.indexOf("="));
		keys.add(key);
	}
	return keys;
}

public static List<String> getValuesAsList(JSONObject obje){
	List<String> list = Utils.getKeyValuesAsList(obje);
	List<String> keys = new ArrayList();
	for (String KV: list){
		String key = KV.substring(KV.indexOf("=")+1);
		keys.add(key);
	}
	return keys;
}
		
public static int countInList(String value, List<String> list){
	int cont =0;		
	for (String v: list){
		if (v.equals(value)) cont++;
	}
	return cont;
}

public static int countElements (JSONObject data){
	List<String> list = getValuesAsList (data);
	return list.size();
}

public int countAppearings(JSONObject json, String value2 ){
	int cont =0;		
	for (Object obj: json.keySet()){
		String name = obj.toString();
		String value = null;
		try {
			value = json.get(name).toString();
		}catch(Exception e){
			continue;
		}
		if (value.equals(value2)){
			cont++;
		}
	}
	return cont;
}

}

