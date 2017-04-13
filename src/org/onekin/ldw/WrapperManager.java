package org.onekin.ldw;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.ckan.CKANException;
import org.ckan.Client;
import org.ckan.Connection;
import org.ckan.resource.impl.Dataset;
import org.ckan.resource.impl.Extra;
import org.ckan.resource.impl.Tag;
import org.ckan.result.impl.DatasetResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.onekin.ldw.SDK.ODTWrapperFactory;
import org.onekin.ldw.SDK.URILookup;
import org.onekin.ldw.SDK.Wrapper;
import org.onekin.ldw.SDK.WrapperFactory;
import org.onekin.ldw.persistence.DB;
import org.onekin.ldw.persistence.GithubAPI;
import org.onekin.ldw.util.Cons;
import org.onekin.ldw.util.Utils;

public class WrapperManager {
    public static String weburl;
    public static String filepath;
    public static String derefserver;
    public static String callback;
    public static String callbackredirection;
    public static DB DB = new DB();
    public static String datahubAPIKEY;
    private static GithubAPI github=new GithubAPI();
    private static Client DataHub;
		
    private static Random randomGenerator = new Random(System.currentTimeMillis());
	  
    protected static Map <String, Wrapper> pattern2wrappers = new  HashMap <String,Wrapper>(); //uripattern to wrapper
    protected static Map <String, Wrapper> wid2wrappers = new  HashMap <String,Wrapper>(); //Id to wrapper
    protected static Map <String, JSONObject> wid2metadata = new  HashMap <String,JSONObject>(); //Id to {#deref, #ip, [ip1 , ip2..]}
    private static Map<String, WrapperFactory> factories = new HashMap<String, WrapperFactory>(); //wrappertype to factory
	private static WrapperFactory ODTFACTORY; //default factory
	static {
		ODTFACTORY = new ODTWrapperFactory();		;
}
	
	   //use createWrapper method to create a new wrapper of type type 
	   public static Wrapper createWrapper(String type, String wrapperFile){
			 //use getWrapper method to get object of type wrapper 
		   Wrapper wrp= null;
		   WrapperFactory factory= factories.get(type);
		   if (Utils.isVoidOrNull(factory)){
		   		wrp = ODTFACTORY.newWrapper(wrapperFile);
		   }else{
		   		wrp= factory.newWrapper(wrapperFile);
		   }
		   return wrp;
	   }
	   
	  public static JSONObject deref(String uri, JSONObject credentials, String IP){
		   JSONObject uridata = Utils.parseURIPattern(uri.toString());
	     	String wrapperId = (String)uridata.get(Cons.WRAPPERID_KEY);
			Wrapper wr = getWrapperById(wrapperId);			
			
   		    JSONObject json = new JSONObject();
			JSONObject context = new JSONObject();
			context.put ("schema", "http://schema.org/");
			JSONObject cont1 = new JSONObject();
			cont1.put ("@type", "@id");
			context.put ("schema:actionStatus", cont1);
			json.put("@context", context);
			json.put("@type", "schema:DownloadAction");
			json.put("schema:actionStatus", "schema:FailedActionStatus");			
			if (wr == null){				
				json.put(Cons.STATUS, HttpServletResponse.SC_BAD_REQUEST);
				return json;
			}else{
				JSONObject js = wid2metadata.get(getId(wr));
				Integer derefCount = Integer.parseInt(js.get(Cons.DEREFS).toString());
				derefCount++;
				js.put(Cons.DEREFS, derefCount);
				Integer ipsCount = Integer.parseInt(js.get(Cons.IPSCOUNT).toString());
				JSONArray ips = (JSONArray)js.get(Cons.IPS);
				if (!ips.toString().contains(IP)){
					ipsCount++;					
					js.put(Cons.IPSCOUNT, ipsCount);
					ips.add(IP);
					js.put(Cons.IPS, ips);					
				}
				wid2metadata.put(getId(wr), js);
				//String wruri = getURIPattern(wr);
				String wruri = getRealURIPattern(wr);
				if (!wruri.endsWith("/")) {
					wruri += "/";				
				}
				System.out.print(" >>Wrapper URI: " + wruri);
				System.out.print(" >>APidoc URI: " + wruri+Cons.APIDOC_key);
				System.out.print(" >>Deref URI: " + uri);
			if (uri.equals(getVoIDURI(wr))){  //VoID  +"/"+ Cons.VOID_key
				 json = new JSONObject();
				json.put("content", wr.getVoID());	
				json.put(Cons.STATUS, HttpServletResponse.SC_OK);
			}else if (uri.startsWith(wruri+Cons.APIDOC_key)){
				 json = new JSONObject();
				 json.put("content", wr.getHydraApiDocumentation());
				json.put(Cons.STATUS, HttpServletResponse.SC_OK);
			}else if (uri.startsWith(wruri+Cons.QUALITY_key)){
				 json = new JSONObject();
				 json.put("content", wr.getQualityMeassures());
				json.put(Cons.STATUS, HttpServletResponse.SC_OK);
			}else{  //Lookup a URI				 
				JSONObject solution= null;
				try {
					solution = wr.deref(new URI(uri), credentials);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				if (Utils.isVoidOrNull(solution)){					
					json.put(Cons.STATUS, HttpServletResponse.SC_NOT_FOUND);
				}else{
					json = new JSONObject();
					json.put("content",solution);
					json.put(Cons.STATUS, HttpServletResponse.SC_OK);	
				}
			 }
			}
			return json;
		}
	   
	  //////////////////
	  
		public static void setStorageURL(Wrapper wr, URL url){
 		    wr.setWrapperURL(url);
		}
	   
	    
	    public static URL saveWrapperProvisionally (Wrapper wrp){
			String content= wrp.getSourceCode();
			//String type= getType(wrp);
			String service = getServiceName(wrp);
	    	String num = Integer.toString(randomGenerator.nextInt(10000));
	    	//String path ="testing/"+type+"/"+service+"/"+num+".xml";
	    	String path ="testing/"+service+"/"+num+".xml";
			URL url = github.saveFile(path, content, "provisional");
			return url;
	    }

	    private static URL saveWrapperDefinitivelly (Wrapper wrp){
			String content= wrp.getSourceCode();
			//String type= getType(wrp);
	    	String name = getName(wrp);
	    	String service = getServiceName(wrp);
	    	//String path =type+"/"+service+"/"+name+".xml";
	    	String path = service+"/"+name+".xml";
			URL url = github.saveFile(path, content, "New wrapper");
			return url;
	    }

	    private static String getDefinitivePath (Wrapper wrp){
			//String type= getType(wrp);
	    	String name = getName(wrp);
	    	String service = getServiceName(wrp);
	    	//String path =type+"/"+service+"/"+name+".xml";
	    	String path =service+"/"+name+".xml";
	    	return path;
	    }

		//this method registers a wrapper 
	   public static void registerWrapper(Wrapper wrapper){
		   try {
			  recordWrapper(wrapper);
   		   wrapper.deref(wrapper.getURIExamples().get(0).getURI(), null);
		   createDataset(wrapper);
	   }catch (Exception e){
			   e.printStackTrace();
		   };
	   }

		//this method registers a wrapper 
	   public static void updateWrapper(Wrapper wrapper){
		try{
			recordWrapper(wrapper);
			   updateDataset(wrapper);
	   	}catch (Exception e){
	   		e.printStackTrace();
	   	};
	   }

	   
	   private static void setWrapper (Wrapper wrapper){
		   wid2wrappers.put(getId(wrapper), wrapper);
		   pattern2wrappers.put(getURIPattern(wrapper), wrapper);
		   if (Utils.isVoidOrNull(wid2metadata.get(getId(wrapper)))){
			   JSONObject js = new JSONObject();
			   JSONArray jsA = new JSONArray();
			   js.put(Cons.IPS, jsA);
			   js.put(Cons.IPSCOUNT, 0);
			   js.put(Cons.DEREFS, 0);		   
			   wid2metadata.put(getId(wrapper), js);
		   }
	   }
	   
		//this method registers a wrapper 
	   public static void deleteProvisionalWrapper(Wrapper wrapper){
		   if (Utils.isVoidOrNull(wrapper)) return;
		   String path = github.getRepositoryPath(getStorageURL(wrapper));
		   github.deleteFile(path, "deleting provisional");
	   }
	   
		//this method registers a wrapper 
	   private static void recordWrapper(Wrapper wrapper){
		   deleteProvisionalWrapper(wrapper);
		   String path = getDefinitivePath(wrapper);
		   github.deleteFile(path, "Deleting just in case");
		   URL wrpurl = saveWrapperDefinitivelly(wrapper);
		   setStorageURL(wrapper, wrpurl);
		   setWrapper (wrapper);
		   save();
	   }	   

	   private static void createDataset(Wrapper wrapper){
		   Dataset ds = new Dataset();
		   JSONObject vd = wrapper.getVoID();
		   String name = vd.get("dcterms:title").toString(); 
		   String formatedName = name.replaceAll("\\.", "_");
		   String api = "";
		   if (name.indexOf(".")>-1) api = name.substring(0,name.indexOf("."));
		   else api = name;
		   ds.setAuthor("Onekin Research Group");//vd.get("dcterms:creator").toString());
		   ds.setAuthor_email("iker.azpeitia@ehu.eus");
		   ds.setCkan_url("https://datahub.io/dataset/"+formatedName);
		   ds.setDownload_url(getVoIDURI(wrapper));
		   List<Extra> extras = new ArrayList<Extra>();
		   Extra e1 = new Extra();
		   e1.setKey("Example URI");
		   e1.setValue(vd.get("void:exampleResource").toString());
		   extras.add(e1);
		   Extra e2 = new Extra();
		   e2.setKey("Health Checker");
		   e2.setValue("http://rdf.onekin.org/ldw/page/healthchecker");
		   extras.add(e2);
		   Extra e3 = new Extra();
		   e3.setKey("Hydra description");
		   e3.setValue(vd.get("@id").toString()+"/"+Cons.APIDOC_key);
		   extras.add(e3);
		   ds.setExtras(extras);
		   ds.setOpen(true);
		   ds.setPrivate(false);
//		   ds.setGroups(groups);
		   String id = vd.get("@id").toString();
//quitar!!		   ds.setId(id);
//		   ds.setLicense(license);
//		   ds.setLicense_url(license_url);
//		   ds.setMaintainer(maintainer);
//		   ds.setMaintainer_email(maintainer_email);
		   Date dNow = new Date( );
		    SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
			String completedAt=ft.format(dNow);
		   ds.setMetadata_created(vd.get("dcterms:created").toString().toString());
		   ds.setMetadata_modified(vd.get("dcterms:modified").toString().toString());		  
   		   ds.setName(formatedName);
		   String description = vd.get("dcterms:description").toString(); 
		   ds.setNotes(description);
		   ds.setOwner_org("linked-data-wrappers");
//		   ds.setRevision_id(revision_id);
		   ds.setRevision_timestamp(completedAt);
		   ds.setState("active");
		   List<Tag> lst = new ArrayList<Tag>();
		   Tag tg = new Tag();
		   tg.setId("ldw");
		   tg.setName("ldw");
		   tg.setName("ldw");
		   lst.add(tg);
		   Tag tg0 = new Tag();
		   tg0.setId("lod");
		   tg0.setName("lod");
		   tg0.setName("lod");
		   lst.add(tg0);
		   Tag tg1 = new Tag();
		   tg1.setId(api);
		   tg1.setName(api);
		   tg1.setDisplayName(api);
		   lst.add(tg1);
		   ds.setTags(lst);
		   ds.setTitle(name);
		   ds.setType("ldw");
		   ds.setUrl(id);
		   try {
			ds = DataHub.createDataset(ds);
		} catch (CKANException e) {
			e.printStackTrace();
		}
       	System.out.println (ds.toString());
	   }
	   
	   
	   private static void updateDataset(Wrapper wrapper){
		   DatasetResult dr = new  DatasetResult();
		   Dataset ds = new Dataset();
		   JSONObject vd = wrapper.getVoID();
		   String name = vd.get("dcterms:title").toString(); 
		   String formatedName = name.replaceAll("\\.", "_");
		   String api = "";
		   ds.setAuthor("Onekin Research Group");//vd.get("dcterms:creator").toString());
		   ds.setAuthor_email("iker.azpeitia@ehu.eus");
		   ds.setCkan_url("https://datahub.io/dataset/"+formatedName);
		   ds.setDownload_url(getVoIDURI(wrapper));
		   List<Extra> extras = new ArrayList<Extra>();
		   Extra e1 = new Extra();
		   e1.setKey("Example URI");
		   e1.setValue(vd.get("void:exampleResource").toString());
		   extras.add(e1);
		   Extra e2 = new Extra();
		   e2.setKey("Health Checker");
		   e2.setValue("http://rdf.onekin.org/ldw/page/healthchecker");
		   extras.add(e2);
		   Extra e3 = new Extra();
		   e3.setKey("Hydra description");
		   e3.setValue(vd.get("@id").toString()+"/"+Cons.APIDOC_key);
		   extras.add(e3);
		   ds.setExtras(extras);
		   ds.setOpen(true);
		   ds.setPrivate(false);
//		   ds.setGroups(groups);
		   String id = vd.get("@id").toString();
//quitar!!		   ds.setId(id);
//		   ds.setLicense(license);
//		   ds.setLicense_url(license_url);
//		   ds.setMaintainer(maintainer);
//		   ds.setMaintainer_email(maintainer_email);
		   Date dNow = new Date( );
		    SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
			String completedAt=ft.format(dNow);
		   ds.setMetadata_created(vd.get("dcterms:created").toString().toString());
		   ds.setMetadata_modified(vd.get("dcterms:modified").toString().toString());		  
		   if (name.indexOf(".")>-1) api = name.substring(0,name.indexOf("."));
		   else api = name;
   		   ds.setName(formatedName);
		   String description = vd.get("dcterms:description").toString(); 
		   ds.setNotes(description);
		   ds.setOwner_org("linked-data-wrappers");
//		   ds.setRevision_id(revision_id);
		   ds.setRevision_timestamp(completedAt);
		   ds.setState("active");
		   List<Tag> lst = new ArrayList<Tag>();
		   Tag tg = new Tag();
		   tg.setId("ldw");
		   tg.setName("ldw");
		   tg.setName("ldw");
		  // tg.setVolcabularyId("tag");
		   lst.add(tg);
		   Tag tg0 = new Tag();
		   tg0.setId("lod");
		   tg0.setName("lod");
		   tg0.setName("lod");
		  // tg0.setVolcabularyId("tag");
		   lst.add(tg0);
		   Tag tg1 = new Tag();
		   tg1.setId(api);
		   tg1.setName(api);
		   tg1.setDisplayName(api);
		  // tg1.setVolcabularyId("API");
		   lst.add(tg1);
		   ds.setTags(lst);
		   ds.setTitle(name);
		   ds.setType("ldw");
		   ds.setUrl(id);
//		   ds.setVersion(version)
		   try {
			dr = DataHub.updateDataset(ds);
		} catch (CKANException e) {
			e.printStackTrace();
		}
       	System.out.println (ds.toString());
	   }
	   

	   private static void updateDatasetDELETE(Wrapper wrapper){
		   Dataset ds = new Dataset();
		   DatasetResult dr = new  DatasetResult();
		   JSONObject vd = wrapper.getVoID();
		   ds.setAuthor("http://rdf.onekin.org");//vd.get("dcterms:creator").toString());
		   ds.setAuthor_email("iker.azpeitia@ehu.eus");
		   ds.setCkan_url("https://datahub.io/user/ikerazpeitia");
		   ds.setDownload_url(getVoIDURI(wrapper));
		   String id = vd.get("@id").toString();
//quitar!!		   ds.setId(id);
		   Date dNow = new Date( );
		    SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
			String completedAt=ft.format(dNow);
		   ds.setMetadata_created(vd.get("dcterms:created").toString().toString());
		   ds.setMetadata_modified(vd.get("dcterms:modified").toString().toString());
		   String name = vd.get("dcterms:title").toString(); 
		   String formatedName = name.replaceAll("\\.", "_");
		   String api = "";
		   if (name.indexOf(".")>-1) api = name.substring(0,name.indexOf("."));
		   else api = name;
   		   ds.setName(formatedName);
		   String description = vd.get("dcterms:description").toString(); 
		   ds.setNotes(description);
		   ds.setOwner_org("linked-data-wrappers");
		   ds.setRevision_timestamp(completedAt);
		   ds.setState("active"); //o quitar. 
		   List<Tag> lst = new ArrayList<Tag>();
		   Tag tg = new Tag();
		   tg.setId("ldw");
		   tg.setName("ldw");
		   lst.add(tg);
		   Tag tg1 = new Tag();
		   tg1.setId(api);
		   tg1.setName(api);
		   lst.add(tg1);
		   ds.setTags(lst);
		   ds.setTitle(name);
		   ds.setType("ldw");
		   ds.setUrl(id);
		   try {
			  dr =  DataHub.updateDataset(ds);
		} catch (CKANException e) {
			e.printStackTrace();
		}
       	System.out.println (dr.toString());
	   }
	   
	 //use getWrapper method to get object of type wrapper 
	   public static void registerWrapperFactory(WrapperFactory factory){
		    factories.put(factory.getType(), factory);
	   }
	   
	   public static Wrapper getWrapperById (String Id){
		   return wid2wrappers.get(Id);
	   }
	   
	   public static Wrapper getWrapperByPattern (String Id){
		   return pattern2wrappers.get(Id);
	   }
	   
	  public static JSONObject checkBackwardCompatibility(Wrapper wOriginal, Wrapper wProposed, JSONObject credentials){//validation: compatibility
		  URILookup exURI = getExampleURI(wOriginal);
		  JSONObject derefOriginal = (JSONObject)wOriginal.deref(exURI.getURI(), credentials); //.get("content"); 
		  JSONObject derefProposed = (JSONObject)wProposed.deref(exURI.getURI(), credentials); //.get("content"); 			  		
		 JSONObject json = new JSONObject(); 
		json.put("result", true);
		JSONArray res = new JSONArray();
		for (Object keyS : derefOriginal.keySet()){
				 String key = (String) keyS;
				 if (!derefProposed.containsKey(key)){
					 	res.add(key);
						json.put("result", false);
				 }
			}
		json.put("lostproperties", res);

			return json;
		}
		
	   /**
	    * Persistent saving
	    *
	    */
	   private static synchronized void save() {   		 
	   		JSONObject obj = new JSONObject();
	   		JSONArray metadata = new JSONArray();
	   		//JSONArray Qmetadata = new JSONArray();
	   		for (String wId: wid2metadata.keySet()){
	   			JSONObject wrpjs = new JSONObject();
	   			wrpjs.put (Cons.WRAPPER, wId);
	   			wrpjs.put (Cons.METADATA, wid2metadata.get(wId));
	   			metadata.add(wrpjs);
	   			//JSONObject wrpQjs = new JSONObject();
	   			//wrpQjs.put (Cons.WRAPPER, wId);
	   			//wrpQjs.put (Cons.METADATA, wid2wrappers.get(wId).getProductionHealth());
	   			//Qmetadata.add(wrpQjs);
	   		}
	   		obj.put(Cons.METADATA, metadata);
	   		//obj.put(Cons.METADATA, Qmetadata);
	   		Set <String> wrapperSet = wid2wrappers.keySet();
	   		Iterator<String> itWSet = wrapperSet.iterator();
	   		JSONArray listWrappers = new JSONArray();
	   		while (itWSet.hasNext()){
	   			Wrapper wr = getWrapperById(itWSet.next());
	   			JSONObject wrpjs = new JSONObject();	   			
	   			String type = WrapperManager.getType(wr);
	   			wrpjs.put("type", type);
	   			//String userid = WrapperManager.getUserName(wr);
	   			//wrpjs.put("userid", userid);
	   			String storageURL = WrapperManager.getStorageURL(wr);
	   			wrpjs.put("storageurl", storageURL);
	   			listWrappers.add(wrpjs);		    		
	   		}
	   		obj.put("wrappers", listWrappers);    	 
	   		
	   		Set <String> factorySet = factories.keySet();
	   		Iterator<String> itFSet = factorySet.iterator();
	   		JSONArray listFactories = new JSONArray();
	   		while (itFSet.hasNext()){
	   			WrapperFactory wf = factories.get(itFSet.next());
	   			JSONObject js = new JSONObject();
	   			String type = wf.getType();
	   			js.put("type", type);
	   			listFactories.add(js);		    		
	   		}
	   		obj.put("factories", listFactories);    	 
	   		
	   		try {
	   			FileWriter file = new FileWriter(filepath);
	   			file.write(obj.toJSONString());
	   			file.flush();
	   			file.close();
	   		} catch (IOException e) {
	   			e.printStackTrace();
	   		}
	   }
	   
	   /**
	    * Load data
	    *
	    */
	   public static synchronized void load() {
	   	JSONParser parser = new JSONParser();  	 
	   	try {
	   		JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filepath));
	   		// loop array
	   		JSONArray metadatas = (JSONArray) jsonObject.get(Cons.METADATA);
	   		Iterator<JSONObject> iterator = metadatas.iterator();
	   		while (iterator.hasNext()) {  
	   			JSONObject wrpjs = iterator.next();
	   			String wId = (String)wrpjs.get(Cons.WRAPPER);
	   			JSONObject metadata = (JSONObject)wrpjs.get(Cons.METADATA);
		   		wid2metadata.put(wId, metadata);
	   		}
	   		
	   		JSONArray wrappersJ = (JSONArray) jsonObject.get("wrappers");
	   		if (wrappersJ==null) return;
	   		iterator = wrappersJ.iterator();
	   		while (iterator.hasNext()) {  
	   			try{
	   			JSONObject wrpjs = iterator.next();
	   			String type = wrpjs.get("type").toString();
	   			//String userId= wrpjs.get("userid").toString();
	   			String storageURL = wrpjs.get("storageurl").toString();	   	
	   			Wrapper wr = WrapperManager.createWrapper(type, storageURL);			
	   		 	// recordWrapper(wr);
	   			String name = getName(wr);
					   URL url = github.getWrapperURL(name);
					   setStorageURL(wr, url);	
	   			setWrapper (wr);
	   			deref(getExampleURI(wr).getURI().toString(), null, "0");
	   			}catch (Exception e){
	   				e.printStackTrace();
	   			}
	   		}
	   	} catch (FileNotFoundException e) {
	   		e.printStackTrace();
	   	} catch (IOException e) {
	   		e.printStackTrace();
	   	} catch (ParseException e) {
	   		e.printStackTrace();
	   	}
	   }
  
		public static JSONObject getHealth (){
			JSONObject healthJSON = new JSONObject();
			JSONObject j = new JSONObject();
			JSONArray health = new JSONArray();
			JSONObject count = new JSONObject();
			int contOK = 0;
			int contVoid = 0;
			int contCredentials = 0;
			int contError = 0;
			int lowQuality = 0;
			count.put(Cons.OKS_key, contOK);
			count.put(Cons.BLANKS_key, contVoid);
			count.put(Cons.AUTHS_key, contCredentials);
			count.put(Cons.FAILS_key, contError);		
			count.put(Cons.LOWQUALITY_key, lowQuality);		
			healthJSON.put("counts", count);			
			healthJSON.put("data", health);
			try{
				for (String key : wid2wrappers.keySet()){
					Wrapper w = wid2wrappers.get(key);
					JSONObject jsh= w.getProductionHealth();
					JSONObject js = wid2metadata.get(key);
					Integer derefs = Integer.parseInt(js.get(Cons.DEREFS).toString());
					Integer ipCount = Integer.parseInt(js.get(Cons.IPSCOUNT).toString());
					Float avg = (float)derefs/ipCount;
					if (avg < 10){
						jsh.put("T7ok", 0);					
					}else{
						jsh.put("T7ok", 1);										
					}
					jsh.put("T7", "Usage: "+derefs.toString()+" dereferentiations from "+ipCount.toString()+" different IPs = "+avg+" derefs/IP");
					health.add(jsh);
				}
				
				for (Object h : health){
					JSONObject he = (JSONObject)h;
					String statuscode = ((JSONArray)he.get("errors")).get(0).toString();
					if (statuscode.equals(Cons.OKS_CODE)) contOK ++;   //OK
					if (statuscode.equals(Cons.BLANKS_CODE)) contVoid ++;  //Notfound
					if (statuscode.equals(Cons.AUTHS_CODE)) contCredentials ++;  //Unauthorized
					if (statuscode.equals(Cons.FAILS_CODE)) contError ++;  //Errors
					if (statuscode.equals(Cons.LOWQUALITY_CODE)) lowQuality ++;  //Errors
				}
				count.put(Cons.OKS_key, contOK);
				count.put(Cons.BLANKS_key, contVoid);
				count.put(Cons.AUTHS_key, contCredentials);
				count.put(Cons.FAILS_key, contError);		
				count.put(Cons.LOWQUALITY_key, lowQuality);		
				healthJSON.put("counts", count);			
				healthJSON.put("data", health);			
			}finally{ 
				return healthJSON;
			}
		}

	   ////Wrapper information

public static boolean isSintacticallyCorrect(Wrapper wr, JSONObject credentials) {
	JSONObject js = wr.getRegisteringHealth(credentials);
	return (boolean)((JSONObject)js.get("checkSyntax")).get("result");
}

public static boolean isDereferentiallyCorrect(Wrapper wr, JSONObject credentials) {
	JSONObject js = wr.getRegisteringHealth(credentials);
	return (boolean)((JSONObject)js.get("checkDereferentiation")).get("result");
}

public static String getName(Wrapper wr)  {
	return getMetadataString(wr,"wrappername");
}

public static String getServiceName(Wrapper wr)  {
	return  getMetadataString(wr,"service");
}

public static String getUserName(Wrapper wr)  {
	return  getMetadataString(wr,"username");
}

public static String getStorageURL(Wrapper wr)  {
	return  getMetadataString(wr,"wrapperurl");
}

public static String getURIPattern(Wrapper wr)  {
	return getMetadataString(wr, "uripattern");
}

public static String getVoIDURI(Wrapper wr)  {
	JSONObject js = (JSONObject)wr.getVoID();
	return js.get("@id").toString();
}

public static String getType(Wrapper wr)  {
	return wr.getType();
}

public static String getRealURIPattern(Wrapper wr)  {
	String uripattern = getMetadataString(wr, "uripattern");
	String wrapperURI= Utils.getVoIDURIINPUTS(uripattern, (JSONObject)wr.getMetadata().get("inputalias"));
	return wrapperURI;
}


public static String getId(Wrapper wr) {
		/*JSONObject jsMetadata = (JSONObject) wr.getMetadata();
		if (Utils.isVoidOrNull(jsMetadata)){
			return null;			
		}
		return (String)jsMetadata.get(Cons.WRAPPERID_KEY);
		*/
		return getMetadataString (wr, Cons.WRAPPERID_KEY);
	}
	
	public static URILookup getExampleURI (Wrapper wr) {
		return wr.getURIExamples().get(0);
	}
   
	public static List<String> getExampleURIList (Wrapper wr) {
		List<String> list = new ArrayList();
		Iterator it = wr.getURIExamples().iterator();
		while (it.hasNext()){
			URILookup uri = (URILookup)it.next();
			list.add(uri.getURI().toString());
		}
		return list;
	}

	public static List<String> getExampleURIListHTML (Wrapper wr) {
		List<String> list = new ArrayList();
		Iterator it = wr.getURIExamples().iterator();
		while (it.hasNext()){
			URILookup uri = (URILookup)it.next();
			String link = "<a href='http://jsonviewer.stack.hu/#"+uri.getURI().toString()+"' target='_blank'>"+uri.getURI().toString()+"</a>";
			list.add(link);
		}
		return list;
	}
	
private static String getMetadataString(Wrapper wr, String key){
	try{			
		JSONObject js = (JSONObject) wr.getMetadata();
	String value = js.get(key).toString();
	return value;
	}catch (Exception e){
		return "";
	}
}
	
	   //////SETTER /////
	   
	   //Bean	    
	    public void setfilepath (String  value){
	    	filepath= value;
	    }
	    public void setderefserver (String  value){
	    	derefserver= value;
	    }
	    public void setcallback (String  value){
	    	callback= value;
	    }
	    public void setcallbackredirection (String  value){
	    	callbackredirection= value;
	    }
	   
	    public void setweburl (String path){
	    	weburl= path;
	    }
	    
	    public void setrepoOwner (String value){
	    	github.repoOwner= value;
	    }

	    public static String getrepoOwner (){
	    	return github.repoOwner;
	    }
	    
	    public void setdatahubapikey (String value){
	    	datahubAPIKEY= value;
			DataHub=new Client(new Connection(), datahubAPIKEY);
	    }
	  
	    public void setrepo (String value){
	    	github.repo= value;
	    }
	    	    
	    public static String getrepo (){
	    	return github.repo;
	    }
	    	    
	    public void setclientID (String value){
	    	github.clientID= value;
	    }
	    	    
	    public void setclientSecret (String value){
	    	github.clientSecret= value;
	    }
  
	    public void setcode (String value){
	    	github.code= value;
	    }
	    	    
	    public void setaccessToken (String value){
	    	github.accessToken= value;
	    }
	    	    
	    public void setpersonalAccessToken (String value){
	    	github.personalAccessToken= value;
	    }	       	    
	   
	}