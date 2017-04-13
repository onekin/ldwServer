package org.onekin.ldw.SDK;

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public interface Wrapper {
	
	public JSONObject deref(URI uri, JSONObject credentials);
		
	//Quality
	public JSONObject getRegisteringHealth (JSONObject credentials);
	public JSONObject getProductionHealth ();

	//Getter and setters
	public JSONObject getMetadata ();
	public void setMetadata (JSONObject js);
	public String getSourceCode();
	public String getType();
	public void setWrapperURL(URL url);
	public List<URILookup> getURIExamples();
	public boolean addURIExample (URI exampleURI);
	public URILookup newURILookup (URI uri);
    //LD lookup
	public JSONArray getHydraApiDocumentation ();
	public JSONArray getQualityMeassures ();
	public JSONObject getVoID ();			
}
