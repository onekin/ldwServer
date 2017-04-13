package org.onekin.ldw.SDK;

import java.net.URI;
import java.net.URL;

import org.json.simple.JSONObject;

public interface URILookupI {
		 
		public URI getURI ();
		public JSONObject checkQuality ();		
		public JSONObject derefLifting (JSONObject credentials);
		public JSONObject derefLowering (JSONObject credentials);
		public void setWrapperURL(URL url);
}
