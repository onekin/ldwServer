package org.onekin.ldw.datanegotiation;

import java.util.regex.Pattern;

public class DataNegotiator {
	private final static ContentTypeNegotiator dataNegotiator;
	
	static {
		dataNegotiator = new ContentTypeNegotiator();
		dataNegotiator.setDefaultAccept("application/ld+json");
		
		// Send HTML to clients that indicate they accept everything.
		// This is specifically so that cURL sees HTML, and also catches
		// various browsers that send "*/*" in some circumstances.
		dataNegotiator.addUserAgentOverride(null, "*/*", "text/html");

		// MSIE (7.0) sends either */*, or */* with a list of other random types,
		// but always without q values. That's useless. We will simply send
		// HTML to MSIE, no matter what. Boy, do I hate IE.
		dataNegotiator.addUserAgentOverride(Pattern.compile("MSIE"), null, "text/html");
		dataNegotiator.addVariant("application/ld+json").addAliasMediaType("application/json").addAliasMediaType("text/json");
//		dataNegotiator.addVariant("application/ld+json").addAliasMediaType("application/json;q=0.98").addAliasMediaType("text/json;q=0.95");
		//dataNegotiator.addVariant("text/html;q=0.81").addAliasMediaType("application/xhtml+xml;q=0.81");
		dataNegotiator.addVariant("application/rdf+xml").addAliasMediaType("application/xml").addAliasMediaType("text/xml");
		dataNegotiator.addVariant("text/rdf+n3").addAliasMediaType("text/n3").addAliasMediaType("application/n3");
		dataNegotiator.addVariant("text/turtle").addAliasMediaType("application/turtle").addAliasMediaType("application/x-turtle");
		dataNegotiator.addVariant("text/n-quads").addAliasMediaType("application/n-quads");
		dataNegotiator.addVariant("text/n-triples").addAliasMediaType("application/n-triples").addAliasMediaType("application/nt").addAliasMediaType("text/nt").addAliasMediaType("text/plain");
/*		dataNegotiator.addVariant("application/rdf+xml;q=0.9").addAliasMediaType("application/xml;q=0.82").addAliasMediaType("text/xml;q=0.81");
		dataNegotiator.addVariant("text/rdf+n3;charset=utf-8;q=0.9").addAliasMediaType("text/n3;q=0.82").addAliasMediaType("application/n3;q=0.81");
		dataNegotiator.addVariant("text/turtle;charset=utf-8;q=0.9").addAliasMediaType("application/turtle;q=0.8").addAliasMediaType("application/x-turtle;q=0.8");
		dataNegotiator.addVariant("text/n-quads;charset=utf-8;q=0.9").addAliasMediaType("application/n-quads;q=0.8");
		dataNegotiator.addVariant("text/n-triples;charset=utf-8;q=0.9").addAliasMediaType("application/n-triples;q=0.8").addAliasMediaType("application/nt;q=0.8").addAliasMediaType("text/nt;q=0.8");
		*/
	}
	
	public static ContentTypeNegotiator getDataNegotiator() {
		return dataNegotiator;
	}
	
}
