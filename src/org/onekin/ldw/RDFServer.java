package org.onekin.ldw;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.onekin.ldw.SDK.Wrapper;
import org.onekin.ldw.datanegotiation.ContentTypeNegotiator;
import org.onekin.ldw.datanegotiation.DataNegotiator;
import org.onekin.ldw.datanegotiation.MediaRangeSpec;
import org.onekin.ldw.util.Cons;
import org.onekin.ldw.util.Utils;
import org.onekin.ldw.util.VelocityDispatcher;
import org.openrdf.rio.RDFFormat;

public class RDFServer extends HttpServlet{
	private static final long serialVersionUID = 1L;

	private VelocityDispatcher dispatcher;

	public void doOptions(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		String path = request.getPathInfo();
	       System.out.println(" >>OPTIONS: " + path);
		
		response = makeResponse(response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		String path = request.getPathInfo();
	       System.out.println(" >>POST: " + path);
		
        String format = request.getHeader("Accept");
        InputStream input = request.getInputStream();
        String data = Utils.stream2string(input);
        JSONObject payload = Utils.string2json (data);
		String uri = request.getParameter("uri");
		 JSONObject uridata = Utils.parseURIPattern(uri.toString());
			String wrapperName = (String)uridata.get(Cons.WRAPPERID_KEY);
	        String serviceId = (String)uridata.get("service");
			Wrapper wr = null;
			if (!Utils.isVoidOrNull(wr)){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		return;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		
		request.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		 String format = request.getHeader("Accept");
		String uri = request.getParameter("uri");
		String IP = request.getRemoteAddr();
		
		String path = request.getPathInfo();
	       System.out.println(" >>GET: " + path);
		
		 if (path == null || path.equals("/") || path.equals("")){
			 String url= WrapperManager.weburl + "/ldw/page";
			    response.sendRedirect(response.encodeRedirectURL(url));
			    out.close();
			    return;
		 }
		if (uri== null){
			uri= WrapperManager.derefserver+path;
		}
        JSONObject uridata = Utils.parseURIPattern(uri.toString());
     	String wrapperId = (String)uridata.get(Cons.WRAPPERID_KEY);
	
		String txt = "";
			// Get the HTTP Authorization header from the request
			String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
	        // Check if the HTTP Authorization header is present and formatted correctly 
        	JSONObject credentials = null;
	        if (!Utils.isVoidOrNull(authorizationHeader)) {
		    	JSONParser parser = new JSONParser();
		    	try {
					credentials = (JSONObject)parser.parse (authorizationHeader);
				} catch (ParseException e) {
					e.printStackTrace();
				} 
	        }
			JSONObject solution= null;
				solution = WrapperManager.deref(uri, credentials, IP);
			txt = "";
			if (Utils.isVoidOrNull(solution)){
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				txt = "{}";
			}else{
				response.setStatus((int)solution.get(Cons.STATUS));
				txt = ((Object)solution.get("content")).toString();//.toJSONString();
			}

		RDFFormat targetFormat = getTargetFormat(request, response);
		response = makeResponse(response);
		String	res = txt;
		if (targetFormat != RDFFormat.JSONLD){
			res = Utils.convert(txt, targetFormat);}
		
		out.write(res);
	  out.close();
		return;
	}

	private HttpServletResponse makeResponse (HttpServletResponse response){
	       response.addHeader("Access-Control-Allow-Origin", "*");
	        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
	        response.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept, Authorization");
	        response.addHeader("Access-Control-Max-Age", "1728000");

	          response.setHeader("Access-Control-Allow-Credentials", "true");

		response.setHeader("Connection", "Keep-Alive");
		response.setContentType(format);
		response.setCharacterEncoding("UTF-8");
		    
//		response.setHeader("Content-Type",format);
		response.setContentType(format);
		return response;
	}

	private String format = "text/html";
    public RDFFormat getTargetFormat(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//		response.addHeader("Vary", "Accept, User-Agent");
		ContentTypeNegotiator negotiator = DataNegotiator.getDataNegotiator();
		MediaRangeSpec bestMatch = negotiator.getBestMatch(request.getHeader("Accept"), request.getHeader("User-Agent"));
		if (bestMatch == null) {
/*			response.setStatus(406);
			response.setContentType("text/plain");
			response.getOutputStream().println(
					"406 Not Acceptable: The requested data format ("+request.getHeader("Accept")+") is not supported. " +
					"Supported formats: JSON-LD (default), RDFXML, N3, NQUADS, NTRIPLES, TURTLE.");
			return null;*/
			return RDFFormat.JSONLD;
		}

		format = bestMatch.getMediaType();
//		response.setHeader("Content-Type",format);
		//response.setContentType(format);
		RDFFormat targetFormat = RDFFormat.JSONLD;
		if (format.contains("application/ld+json") ){

		}else if (format.contains("application/rdf+xml")){
			targetFormat = RDFFormat.RDFXML;
		}else if (format.contains("text/rdf+n3")){
			targetFormat = RDFFormat.N3;
		}else if (format.contains("text/n-quads")){
			targetFormat = RDFFormat.NQUADS;
		}else if (format.contains("text/n-triples")){
			targetFormat = RDFFormat.NTRIPLES;
		}else if (format.contains("text/turtle")){
			targetFormat = RDFFormat.TURTLE;
		}
    return targetFormat;
}
    

}
