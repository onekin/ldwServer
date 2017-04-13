package org.onekin.ldw.SDK;

import org.json.simple.JSONObject;
import org.onekin.ldw.WrapperManager;


public class ODTWrapperFactory implements WrapperFactory {
	private static String type = "ODT";
	
	public ODTWrapperFactory(){
		WrapperManager.registerWrapperFactory(this);
	}

	public ODTWrapperFactory(JSONObject js){
		type = (String)js.get("type");
		WrapperManager.registerWrapperFactory(this);
	}
	
   	public String getType (){
   		return type;
   	}

	public boolean acceptsType(String type) {
		// TODO Auto-generated method stub
		return type.equalsIgnoreCase(type);
	}

	@Override
	public Wrapper newWrapper(String wrapperToken) {
		 return new ODTWrapper(wrapperToken);
	}

}
