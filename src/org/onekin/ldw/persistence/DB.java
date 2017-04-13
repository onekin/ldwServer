package org.onekin.ldw.persistence;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.ontology.Individual;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RIOT;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.onekin.ldw.util.Utils;

public class DB {
	
	Model model = ModelFactory.createDefaultModel(); // creates an in-memory Jena Model
	Map<String, String> ontologies= new HashMap<String, String>();
	Map<String, Resource> individuals = new HashMap<String, Resource>();
		
public void saveOntology (String prefix, String onto){
	if (ontologies.get(prefix) == null){	
		String jurl = "http://lov.okfn.org/dataset/lov/api/v2/vocabulary/info?vocab="+prefix;
		String url;
		JSONObject obj =null;
		try {
			obj = Utils.callJson(new URL(jurl));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
			JSONArray arr = (JSONArray) obj.get("versions");
			JSONObject j = (JSONObject) arr.get(0);
			url = j.get("fileURL").toString();
		}catch (Exception e){
			JSONObject j = (JSONObject) obj.get("versions");
			url = j.get("fileURL").toString();			
		}
		
			ontologies.put(prefix, onto);
			RIOT.init();
			model.read(url, null, "TURTLE");
	}
}

	
public void  saveIndividual (String id, JSONObject json){
	if (individuals.get(id) == null){	
		Resource individual =  null;
		try{	
			// create the resource
//			String id = json.get("@id").toString();
			individual = model.createResource(id);
			Iterator<Entry> it = json.entrySet().iterator();
			while (it.hasNext()){
				Entry entry = it.next();
				String property = entry.getKey().toString();
				if (property.startsWith("@type")){
					property = "rdf:type";
					saveOntology ("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
				}else{if (property.startsWith("@")){
						continue;
					}					
				}
				String value = entry.getValue().toString();
				individual.addProperty( model.createProperty(property),  value);
			}
		}catch (Exception e){
			
		}	
		individuals.put(id, individual);
	}
}

	public ArrayList<String> getDeprecatedClasses (String id){
		 String queryString = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
				 "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				 	"SELECT ?cl ?p WHERE { <" +
	        		id + "> rdf:type ?cl. " +
 					"?cl ?p owl:DeprecatedClass.}";
	        Query query = QueryFactory.create(queryString);
	        QueryExecution qexec = QueryExecutionFactory.create(query, model);
	        ArrayList<String> res = new ArrayList<String>  ();
	        try {
	            ResultSet results = qexec.execSelect();
	            while ( results.hasNext() ) {	            	
	            	  QuerySolution soln = results.nextSolution();
		              String cl = soln.get("cl").toString();
		              res.add(cl);
	            }
	        } finally {
	            qexec.close();
	        }
	        return res;
	}

	public ArrayList<String>  getDeprecatedProperties (String id){
		 String queryString = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
				 "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				 	"SELECT ?cl ?p WHERE { <" +
	        		id + "> ?p ?cl. " +
					"?cl ?p owl:DeprecatedProperty.}";
	        Query query = QueryFactory.create(queryString);
	        QueryExecution qexec = QueryExecutionFactory.create(query, model);
	        ArrayList<String>  res = new ArrayList<String>  ();
	        try {
	            ResultSet results = qexec.execSelect();
	            while ( results.hasNext() ) {	            	
	            	  QuerySolution soln = results.nextSolution();
		              String cl = soln.get("p").toString();
		              res.add(cl);
	            }
	        } finally {
	            qexec.close();
	        }
	        return res;
	}

	public Boolean isDeprecatedProperty (String pr){
		///use a fully qualified propertylass
		 String queryString = "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				 	"SELECT ?p WHERE { " +
	        		pr + "?p owl:DeprecatedProperty.}";
	        Query query = QueryFactory.create(queryString);
	        QueryExecution qexec = QueryExecutionFactory.create(query, model);
	        Boolean result = false;
	        try {
	            ResultSet results = qexec.execSelect();
	            if ( results.hasNext() ) {result = true;}
	        } finally {
	            qexec.close();
	        }
	        return result;
	}

	public void test(){
		///testing Jena		
		 String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
	                "SELECT ?c ?p WHERE { " +
	        		"?c ?p rdfs:Class.}";
	        Query query = QueryFactory.create(queryString);
	        QueryExecution qexec = QueryExecutionFactory.create(query, model);
	        try {
	            ResultSet results = qexec.execSelect();
	            while ( results.hasNext() ) {
	                QuerySolution soln = results.nextSolution();
	                String name = soln.get("c").toString();
	                String p = soln.getResource("p").toString();
	                System.out.println(name + " :: "+p);
	            }
	        } finally {
	            qexec.close();
	        }
	}
	
	public void write (){

        // Write the Jena Model in Turtle, RDF/XML and N-Triples format
        System.out.println("\n---- Turtle ----");
        model.write(System.out, "TURTLE");
        System.out.println("\n---- RDF/XML ----");
        model.write(System.out, "RDF/XML");
        System.out.println("\n---- RDF/XML Abbreviated ----");
        model.write(System.out, "RDF/XML-ABBREV");
        System.out.println("\n---- N-Triples ----");
        model.write(System.out, "N-TRIPLES");
        System.out.println("\n---- RDF/JSON ----");
        model.write(System.out, "RDF/JSON");
    }

}
