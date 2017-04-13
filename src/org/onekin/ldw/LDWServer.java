package org.onekin.ldw;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.onekin.ldw.SDK.URILookup;
import org.onekin.ldw.SDK.Wrapper;
import org.onekin.ldw.util.Cons;
import org.onekin.ldw.util.Utils;
import org.onekin.ldw.util.VelocityDispatcher;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/** 
* @author Iker Azpeitia
*/
public class LDWServer  extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	private VelocityDispatcher dispatcher;	
	
	
	@Override
	public void init(ServletConfig cfg) throws ServletException
	{
        ServletContext ctx = cfg.getServletContext();
        WebApplicationContext wac = WebApplicationContextUtils
                .getRequiredWebApplicationContext(ctx);
        dispatcher = (VelocityDispatcher) wac.getBean("dispatcher");
        dispatcher.init();
	}

	@Override
	public void destroy() {
	//	trygger = null;
	}
	   
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		PrintWriter out = response.getWriter();		
	
		String path = request.getPathInfo();			
		String servletPath = request.getServletPath();				
		String base = WrapperManager.weburl;
		String action = request.getParameter("action");
		
		Map context = new HashMap();		
		context.put(Cons.BASEURL_KEY, base);
		context.put(Cons.SERVLETPATH_KEY, base+servletPath);
    	context.put(Cons.IMG_KEY, Cons.base_IMAGEPAGE);
    	context.put(Cons.CSS_KEY, Cons.base_CSSPAGE);
        String pageParameter = Utils.getRequestValue(Cons.PAGE_PARAM, request, true);
        
	  if (path == null || path.equals("/") || path.equals("")){
	        	response.setStatus(HttpServletResponse.SC_NOT_FOUND); 
			  //set the page
			  	request.getSession().setAttribute(Cons.PAGE_PARAM, Cons.PROJECT_PAGE);	    
				dispatcher.dispatch(request, response, context);		
	  }else if(path.startsWith("/page/healthchecker")) {
		  InputStream inputStream = LDWServer.class.getResourceAsStream(Cons.REDIRECT_HEALTH_PAGE);
		  String html = Utils.getStringFromInputStream(inputStream);
		  out.write(html);
          response.setStatus(HttpServletResponse.SC_OK);
	  }else if(path.startsWith("/page/Healthchecker/")) {
				  InputStream inputStream = LDWServer.class.getResourceAsStream(Cons.HEALTH_PAGE);
		  String html = Utils.getStringFromInputStream(inputStream);
//		  String tablesDiagnostics = LinkedDataBean.getWrappersDiagnostics();
			//	  HashMap params = LinkedDataBean.getWrappersStatistics();
		  HashMap params = new HashMap();
		  JSONObject health =  WrapperManager.getHealth();
		  JSONObject count =  (JSONObject)health.get("counts");
		  params.put(Cons.OKS,count.get(Cons.OKS_key).toString()); 
		  params.put(Cons.FAILS,count.get(Cons.FAILS_key).toString()); 
		  params.put(Cons.BLANKS,count.get(Cons.BLANKS_key).toString()); 
		  params.put(Cons.AUTHS,count.get(Cons.AUTHS_key).toString()); 
		  params.put(Cons.LOWQUALITY,count.get(Cons.LOWQUALITY_key).toString()); 
		  params.put(Cons.WRAPPERDIAGNOSTICS,health.toString()); // tablesDiagnostics);
		  params.put(Cons.RESOURCESURL, WrapperManager.weburl);
		  html = Utils.replaceVariables (html, params);
		  out.write(html);
          response.setStatus(HttpServletResponse.SC_OK);	    		 		    		 	
	  }else if(path.startsWith("/page/ldbrowser")) {
		  request.getSession().setAttribute(Cons.PAGE_PARAM, Cons.LDBROWSER_PAGE);	    
		  dispatcher.dispatch(request, response, context);	    		 		
	  }else if(path.startsWith("/page/project")) {
		  request.getSession().setAttribute(Cons.PAGE_PARAM, Cons.PROJECT_PAGE);	    
		  dispatcher.dispatch(request, response, context);	    		 		
	  }else if(path.startsWith("/page")) {
    	  request.getSession().setAttribute(Cons.PAGE_PARAM, Cons.PROJECT_PAGE);
	  	dispatcher.dispatch(request, response, context);  	  	
	  }else if(path.startsWith("/registerwrapper")) {
		    Map cont= addWrapperFile(request, response);
	    	response.setStatus(HttpServletResponse.SC_OK);
      		request.getSession().setAttribute(Cons.PAGE_PARAM, Cons.REGISTERWRAPPER_PAGE);	
      		context.putAll(cont);
		  dispatcher.dispatch(request, response, context);	    		 		  		  
	  }else {
		  	request.getSession().setAttribute(Cons.PAGE_PARAM, Cons.PROJECT_PAGE);	    
			dispatcher.dispatch(request, response, context);
	  }
	  out.close();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {	
		BufferedRequestWrapper buf = new BufferedRequestWrapper(request);
	  final StringBuffer messageBuf = Utils.read(buf.getInputStream(), request.getContentLength());
	  final String strMessage = messageBuf.toString();	
		String path = request.getPathInfo();
		String action = request.getParameter("action");
		if (path == null || path.length() == 0) {
			response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}else if (path.startsWith("/log")){			
			String user = request.getParameter("user");
			String name = request.getParameter("name");
			PrintWriter out = response.getWriter();
			//out.write("<result>"+trygger.logTrygger(user, name)+"</result>");
			response.setStatus(HttpServletResponse.SC_OK);	
		}else {
			response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
	}
	
	///////////
	

public Map addWrapperFile (HttpServletRequest request, HttpServletResponse response){
		Map context = new HashMap();
		Wrapper wProposed = null;
	try{
//     	String userId = request.getParameter(Cons.USERID_KEY);
	   	String fileURL = request.getParameter(Cons.FILE);	
     	String wrapperType = request.getParameter(Cons.WRAPPERTYPE_KEY);
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		JSONParser parser = new JSONParser();
		JSONObject credentials = new JSONObject();
        try {
        	credentials = (JSONObject)parser.parse(authorizationHeader);
        } catch (Exception e) {
        	System.out.println ("No credentials");
        }

	wProposed = WrapperManager.createWrapper(wrapperType, fileURL);
    URL wrpurl = WrapperManager.saveWrapperProvisionally(wProposed);
    WrapperManager.setStorageURL(wProposed, wrpurl);

	JSONObject correctness= wProposed.getRegisteringHealth(credentials);
	
	context.put(Cons.WRAPPER_KEY, wProposed);
	context.put(Cons.WRAPPERNAME_KEY, WrapperManager.getName(wProposed));

	context.put(Cons.URIPATTERN_KEY, WrapperManager.getURIPattern(wProposed));
	context.put(Cons.HASURIEXAMPLE_KEY, !Utils.isVoidOrNull(WrapperManager.getExampleURI(wProposed)));
	context.put(Cons.URIEXAMPLELIST_KEY, WrapperManager.getExampleURIListHTML(wProposed));
	
	URILookup uri = WrapperManager.getExampleURI(wProposed);
	
	context.put(Cons.ISMATCHINGCORRECT_KEY, uri.checkMatches());
	context.put(Cons.MATCHINGPARAMS_KEY, uri.getMatchingParams());
	context.put(Cons.LIFTINGEXISTS_KEY,  (boolean)((JSONObject)correctness.get("checksyntax")).get("LiftingExists"));

	
	boolean isSintacticallyCorrect = (boolean)((JSONObject)correctness.get("checksyntax")).get("result");
    context.put(Cons.ISSYNTACTICALLYCORRECT_KEY,  isSintacticallyCorrect);
	boolean matchesAliases = (boolean)((JSONObject)correctness.get("checksyntax")).get("URIPatternMatchesAliases");
	context.put(Cons.ALIASMATCHING_KEY,  matchesAliases);
	context.put(Cons.INCORRECTALIASMATCHING_KEY,  ((JSONArray)((JSONObject)correctness.get("checksyntax")).get(Cons.INCORRECTALIASMATCHING_KEY)).toJSONString());
	boolean isDereferentiallyCorrect = false;
	if (isSintacticallyCorrect){
		isDereferentiallyCorrect =  (boolean)((JSONObject)correctness.get("checkdereferentiation")).get("result");
		context.put(Cons.ISDEREFERENTIALLYCORRECT_KEY,  isDereferentiallyCorrect);
//    context.put(Cons.ISLOWERINGCORRECT_KEY,  (boolean)((JSONObject)((JSONObject)correctness.get("checkdereferentiation")).get("loweringmetadata")).get("statuscode").toString().equals(Cons.OKS_CODE));
		context.put(Cons.ISLOWERINGCORRECT_KEY,  (boolean)((JSONObject)correctness.get("checkdereferentiation")).get("correctlowering"));
		context.put(Cons.GETMESSAGELOWERING_KEY,  ((JSONObject)correctness.get("checkdereferentiation")).get("messageslowering"));
		context.put(Cons.ISLIFTINGCORRECT_KEY,  (boolean)((JSONObject)correctness.get("checkdereferentiation")).get("correctlifting"));
		context.put(Cons.GETMESSAGELIFTING_KEY,  ((JSONObject)correctness.get("checkdereferentiation")).get("messageslifting"));
		context.put(Cons.ISVOID_KEY,  (boolean)((JSONObject)correctness.get("checkdereferentiation")).get("isvoid"));
		context.put(Cons.ISBACKWARDCOMPATIBLE_KEY,  true);
		context.put(Cons.ISTHEFIRST,  true);
	}
	
	String id = WrapperManager.getId(wProposed);
 	Wrapper wrOriginal = WrapperManager.getWrapperById(id); //Global.getWrapper(id);
	if (!(isSintacticallyCorrect && isDereferentiallyCorrect)){
		WrapperManager.deleteProvisionalWrapper(wProposed);
		return context;
	}
	if (wrOriginal == null){
		WrapperManager.registerWrapper(wProposed);
		return context;
	}else {
		JSONObject result =WrapperManager.checkBackwardCompatibility(wrOriginal, wProposed, credentials);
		boolean isBackwardCompatible = (boolean)result.get("result");
		context.put(Cons.ISBACKWARDCOMPATIBLE_KEY,  isBackwardCompatible);
		context.put(Cons.ISTHEFIRST,  false);
		
		List<String> list = new ArrayList();
		Iterator<String> it = ((JSONArray)result.get("lostproperties")).iterator();
		while (it.hasNext()){
			String lost = it.next().toString();
			list.add(lost);
		}		
		context.put(Cons.GETLOSTPROPERTIES, list);
			
		if (isBackwardCompatible == false){
			WrapperManager.deleteProvisionalWrapper(wProposed);
	    	return context;
		}
		WrapperManager.updateWrapper(wProposed);
		return context;
		}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		WrapperManager.deleteProvisionalWrapper(wProposed);
		e.printStackTrace();
		context.put(Cons.ISBACKWARDCOMPATIBLE_KEY,  false);
		return context;
	}
}
	
	
	
////////////////
	
	private class BufferedRequestWrapper extends HttpServletRequestWrapper {

		ByteArrayInputStream bais;
		ByteArrayOutputStream baos;
		BufferedServletInputStream bsis;
		byte [] buffer;

		public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
		super(req);
		// Read InputStream and store its content in a buffer.
		InputStream is = req.getInputStream();
		baos = new ByteArrayOutputStream();
		byte buf[] = new byte[1024];
		int letti;
		while ((letti=is.read(buf))>0) baos.write(buf,0,letti);
		buffer = baos.toByteArray();
		}

		public ServletInputStream getInputStream() {
		try {
		// Generate a new InputStream by stored buffer
		bais = new ByteArrayInputStream(buffer);
		// Istantiate a subclass of ServletInputStream
		// (Only ServletInputStream or subclasses of it are accepted by the servlet engine!)
		bsis = new BufferedServletInputStream(bais);
		}
		catch (Exception ex) {
		ex.printStackTrace();
		}
		finally {
		return bsis;
		}
		}


		/*
		Subclass of ServletInputStream needed by the servlet engine.
		All inputStream methods are wrapped and are delegated to
		the ByteArrayInputStream (obtained as constructor parameter)!
		*/
		private class BufferedServletInputStream extends ServletInputStream {

		ByteArrayInputStream bais;

		public BufferedServletInputStream(ByteArrayInputStream bais) {
		this.bais = bais;
		}

		public int available() {
		return bais.available();
		}

		public int read() {
		return bais.read();
		}

		public int read(byte[] buf,int off,int len) {
		return bais.read(buf,off,len);
		}

		}
		}

		
}