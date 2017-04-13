package org.onekin.ldw.persistence;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;


//import org.apache.catalina.util.Base64;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.onekin.ldw.WrapperManager;
import org.onekin.ldw.util.Utils;

public class GithubAPI {
    DocumentBuilder docBuilder;
        XPathFactory xpathFactory;
        public  String clientID;
        public  String clientSecret; 
        public String code;       
        public String accessToken;
       public String personalAccessToken;
       public String repoOwner;
       public String repo;

        String githubBase = "https://api.github.com/";
        RestCall rc=new RestCall();
        
public URL getWrapperURL (String wrapperName){
    	//OAuth2 token authentication
   	URL newURL= null;
   	String repoOwner = WrapperManager.getrepoOwner();
    String repo = WrapperManager.getrepo();
    String[] parts = wrapperName.split("\\.");
    String path = parts[0];
     try {
		newURL= new URL("https://raw.githubusercontent.com/"+repoOwner+"/"+repo+"/master/"+path+"/"+wrapperName+".xml");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}     	
     return newURL;
    }

public URL getRepositoryURL (String path){
	URL newURL = null;
	try {
		newURL = new URL("https://raw.githubusercontent.com/"+WrapperManager.getrepoOwner()+"/"+WrapperManager.getrepo()+"/master/"+path);
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return newURL;
}

public String getRepositoryPath (String url){
	String path = url.replace("https://raw.githubusercontent.com/"+WrapperManager.getrepoOwner()+"/"+WrapperManager.getrepo()+"/master/", "");
	return path;
}
        
        
    public  String createJsonString(Map<String, String> parameters) throws Exception {
        String res=null;
        Boolean first = true; 
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (first ){
            	first = false;
            	res = "\""+key +"\" : \""+ value+"\"";
            }else{
            	res += ", \""+key +"\" : \""+ value+"\"";
            }
        }
        res = "{"+res+"}";        
        return res;
    }
   
    public URL saveFile (String path, String content, String message){
    	//OAuth2 token authentication
    	URL newURL= null;
    	String repoOwner = this.repoOwner;
        String repo = this.repo;
     try {
    	 String sha = getSha(path);
    	 Map<String, String> params=new Hashtable<String, String>();
	     if (! sha.equals("")){
	    	 params.put("sha", sha);
	     }
    	params.put("access_token", personalAccessToken);
  	    params.put("message", message);
  	    byte[] encodedBytes = Base64.encodeBase64(content.getBytes());
  	    params.put("content",new String(encodedBytes));
  	     String url = githubBase+"repos/"+repoOwner+"/"+repo+"/contents/"+path+"?access_token="+personalAccessToken;
  		String payload = createJsonString(params);
		rc.HttpPut (url, payload);
		newURL= getRepositoryURL (path);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}     	
     return newURL;
    }
    	  
    public void deleteFile (String path, String message){
    	URL newURL= null;
    	String repoOwner = this.repoOwner;
        String repo = this.repo;
     try {
    	 String sha = getSha(path);
    	 Map<String, String> params=new Hashtable<String, String>();
	     if (! sha.equals("")){
	    	 params.put("sha", sha);
	     }
    	params.put("access_token", personalAccessToken);
  	    params.put("message", message);
        String url = githubBase+"repos/"+repoOwner+"/"+repo+"/contents/"+path+"?access_token="+personalAccessToken;
  		String payload = createJsonString(params);
		rc.HttpDelete(url, payload);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}     	
    }
    	  

    public String  getSha(String path) throws Exception {
    	String repoOwner = this.repoOwner;
        String repo = this.repo;
        try {
        Map<String, String> params=new Hashtable<String, String>();        
        InputStream response;
        params.put("access_token", personalAccessToken);
        String url = githubBase+ "repos/"+repoOwner+"/"+repo+"/contents/"+path;
        response = rc.callRestfulWebServiceStream("GET",url, params);
        createDocumentBuilder();
        createXpathFactory();
        String txt=createString(response);
//        System.out.println(txt);
        JSONParser jsonObj = new JSONParser();
        JSONObject json = (JSONObject) jsonObj.parse(txt);
        String sha = (String) json.get("sha");
        return sha;
    } catch (Exception e) {
              e.printStackTrace();
      }
		return "";
    }
    
    	  public void example (){
  	   RestCall rc=new RestCall();
    	        InputStream response;
    	        //String response;
 //   	      response = rc.callRestfulWebServiceStream("PUT","https://api.github.com/repos/onekin/owc/contents/tesONE.xml", params, header);

    	      createDocumentBuilder();
    	         createXpathFactory();
    	//      System.out.println(createString(response));
    }    
  	  
    	  
    public void getAccessToken() throws Exception {
        try {
        Map<String, String> params=new Hashtable<String, String>();

        params.put("client_id",clientID);
        params.put("client_secret",clientSecret);
        params.put("code",code);
        //params.put("redirect_uri","localhost");
        
        InputStream response;
        //String response;
      response = rc.callRestfulWebServiceStream("POST","https://github.com/login/oauth/access_token", params);

      createDocumentBuilder();
         createXpathFactory();
      System.out.println(createString(response));

    } catch (Exception e) {
              e.printStackTrace();
      }


    }
    public void getRepositories() throws Exception {
        try {
        Map<String, String> params=new Hashtable<String, String>();

        //params.put("access_token",accessToken);

        //params.put("access_token",accessToken);
        //params.put("redirect_uri","localhost");
        RestCall rc=new RestCall();
        InputStream response;
        //String response;
      response = rc.callRestfulWebServiceStream("GET","https://api.github.com/users/jononekin/repos", params);

      createDocumentBuilder();
         createXpathFactory();
      System.out.println(createString(response));

    } catch (Exception e) {
              e.printStackTrace();
      }
    }
    
    public void getUser() throws Exception {
        try {
        Map<String, String> params=new Hashtable<String, String>();


        params.put("access_token",accessToken);
        //params.put("redirect_uri","localhost");
        RestCall rc=new RestCall();
        InputStream response;
        //String response;
      response = rc.callRestfulWebServiceStream("GET","https://api.github.com/user", params);

      createDocumentBuilder();
         createXpathFactory();
      System.out.println(createString(response));

    } catch (Exception e) {
              e.printStackTrace();
      }


    }
    private  void createDocumentBuilder(){
        try {
            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            docBuilder = factory.newDocumentBuilder();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private  void createXpathFactory(){
        try {
            xpathFactory=XPathFactory.newInstance();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private String createString(InputStream inputStream){
        try {
              BufferedReader br;

            br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            return  sb.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
       }

private class RestCall {

    public void HttpDelete (String url1, String content) throws Exception {
        URL url = new URL(url1);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
        osw.write(content);
        osw.flush();
        osw.close();
        System.err.println(connection.getResponseCode());
        System.err.println(connection.getResponseMessage());
    }

    public void HttpPut (String url1, String content) throws Exception {
        URL url = new URL(url1);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
        osw.write(content);
        osw.flush();
        osw.close();
        System.err.println(connection.getResponseCode());
        System.err.println(connection.getResponseMessage());
    }

        public  String buildWebQuery(Map<String, String> parameters) throws Exception {
            String res=null;
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String key = URLEncoder.encode(entry.getKey(), "UTF-8");
                String value = URLEncoder.encode(entry.getValue(), "UTF-8");
sb.append(key).append("=").append(value).append("&");
            }
            if(sb.length()>0){
                res=sb.toString().substring(0, sb.length() - 1);
            }else{
                res=sb.toString();
            }
            return res;
        }

        public  Map<String, String> parseWebResult(String parameters) throws Exception {
            StringTokenizer amp = new StringTokenizer(parameters,"&");
            Map<String, String> res=new Hashtable<String, String>();
            while (amp.hasMoreTokens()) {
                String paramVal=amp.nextToken();
                int ind = paramVal.indexOf("=");
                String key = URLDecoder.decode(paramVal.substring(0,ind), "UTF-8");
                String value = URLDecoder.decode(paramVal.substring(ind+1), "UTF-8");
                res.put(key, value);
            }
            return res;
        }


        public String callRestfulWebService(String method,String address, Map<String, String> parameters) throws Exception {
            return callRestfulWebService(method,address,parameters,new Hashtable<String,String>());
        }

        public String callRestfulWebService(String method,String address, Map<String, String> parameters,Map<String, String> header) throws Exception {
            String query = buildWebQuery(parameters);

            String curl=address;
            if(query.length()>1){
                curl=curl+"?"+query;
            }
            URL url = new URL(curl);

            // make post mode connection
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestMethod(method);
            for (Map.Entry<String, String> entry : header.entrySet()) {
((HttpURLConnection)urlc).setRequestProperty(entry.getKey(), entry.getValue());
            }
            urlc.setAllowUserInteraction(false);
            // retrieve result
            BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            return sb.toString();
        }
        public InputStream callRestfulWebServiceStream(String method,String address, Map<String, String> parameters) throws Exception {
            return callRestfulWebServiceStream(method,address,parameters,new Hashtable<String,String>());
        }

        public InputStream callRestfulWebServiceStream(String method,String address, Map<String, String> parameters,Map<String, String> header) throws Exception {
            String query = buildWebQuery(parameters);
            String curl=address;
            if(query.length()>1){
                curl=curl+"?"+query;
            }
            URL url = new URL(curl);
            // make post mode connection
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestMethod(method);
            for (Map.Entry<String, String> entry : header.entrySet()) {
((HttpURLConnection)urlc).setRequestProperty(entry.getKey(), entry.getValue());
            }
            // retrieve result
            return urlc.getInputStream();
        }
    }
}