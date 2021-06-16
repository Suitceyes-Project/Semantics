package iti.suitceyes.ontology;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

import iti.suitceyes.communication.MyThreadServeMessage;
import iti.suitceyes.components.IBComponentResults;
import iti.suitceyes.components.IBeacon;
import iti.suitceyes.components.VAComponentResults;
import iti.suitceyes.components.VAEntity;
import iti.suitceyes.util.Utilities;

public class RemoteGraphDB {

	private SPARQLRepository repository_select;
	private SPARQLRepository repository_update;
	private RepositoryResult<Namespace> repository_namespaces;
	public static int counter = 1;

	public RemoteGraphDB() {
		// default repository
		this.repository_select = new SPARQLRepository(Vocabulary.SPARQL_ENDPOINT_SELECT);
		this.repository_update = new SPARQLRepository(Vocabulary.SPARQL_ENDPOINT_UPDATE);

		initialiseSelectRepo();
		initialiseUpdateRepo();

		this.repository_select.setUsernameAndPassword("vasilis", "k5ksxp10kk!");
		this.repository_update.setUsernameAndPassword("vasilis", "k5ksxp10kk!");

		this.repository_namespaces = this.repository_select.getConnection().getNamespaces();

	}
	
	public RemoteGraphDB(String userID){
		// repository per user
		/*
		String SPARQL_ENDPOINT_SELECT = "http://160.40.49.112:7200/repositories/mklab-suitceyes-kb-USER1";	
		String SPARQL_ENDPOINT_UPDATE = "http://160.40.49.112:7200/repositories/mklab-suitceyes-kb-USER1/statements"; 
		*/
		this.repository_select = new SPARQLRepository(Vocabulary.SPARQL_ENDPOINT_SELECT + "-" + userID);
		this.repository_update = new SPARQLRepository(Vocabulary.SPARQL_ENDPOINT_SELECT + "-" + userID + "/statements");

		this.repository_select.setUsernameAndPassword("vasilis", "k5ksxp10kk!");
		this.repository_update.setUsernameAndPassword("vasilis", "k5ksxp10kk!");

		initialiseSelectRepo();
		initialiseUpdateRepo();

		this.repository_namespaces = this.repository_select.getConnection().getNamespaces();
		
		
	}

	public Repository getSelectRepo() {
		return this.repository_select;
	}

	public Repository getUpdateRepo() {
		return this.repository_update;
	}

	public RepositoryResult<Namespace> getRepositoryNamespaces() {
		return this.repository_namespaces;
	}

	public void initialiseSelectRepo() {
		this.repository_select.initialize();
	}

	public void initialiseUpdateRepo() {
		this.repository_update.initialize();
	}

	public String getTimestampOfLatestSceneDetection_URI() {

		String maxtimestamp = null;

		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {
			
			String queryString = 
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
							
					+ "SELECT (max(?timestamp) AS ?maxtimestamp) " 
					+ "WHERE {" 
					+ "?detection a sot:Detection . "
					+ "?detection sot:hasTimestamp ?timestamp . " 
					+ "?detection sot:detectsSemanticSpace ?space . "
					+ "?space rdf:type sot:SemanticSpace . " 
					+ "}";

			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult queryResult = tupleQuery.evaluate();

			while (queryResult.hasNext()) { // iterate over the result
				BindingSet bindingSet = queryResult.next();
				Value value = bindingSet.getValue("maxtimestamp");
				maxtimestamp = value.stringValue();
			}
		}
		return maxtimestamp;
	}

	public String getDetectedSemanticSpace(String maxtimestamp) {
		
		// Returns LATEST detected semantic space
		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {

			String queryString = 
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
					+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " 
					
					+ "SELECT ?label_space " 
					+ "WHERE {"
					+ "?detection a sot:Detection . " 
					+ "?detection sot:hasTimestamp \"" + maxtimestamp.toString() + "\"^^xsd:dateTime . " 
					+ "?detection sot:detectsSemanticSpace ?space . "
					+ "?space rdfs:label ?label_space . " 
					+ "}";

			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult queryResult = tupleQuery.evaluate();

			// String queryString = "SELECT ?label_space "
			// + "WHERE {"
			// + "?detection a sot:Detection . "
			// + "?detection sot:hasTimestamp \"" + maxtimestamp.toString() +
			// "\"^^xsd:dateTime . "
			// + "?detection sot:detectsSemanticSpace ?space . "
			// + "?space rdfs:label ?label_space . "
			// + "}";
			//
			// System.out.println("query: " + queryString);
			//
			//
			// TupleQueryResult queryResult = executeSparqlSelect(queryString);

			while (queryResult.hasNext()) { // iterate over the result
				BindingSet bindingSet = queryResult.next();
				Value value = bindingSet.getValue("label_space");
				return value.stringValue();
			}
		}
		return null;

	}

	public Map<String, String> findEntityVA(String entity){
		Map<String,String> queryAnswers = new HashMap<String,String>();
		String answer = null;
		String timestamp = null;
		String leftright = null;
		String scene = "";
		String distance = "";
		String offset = null;

		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {

			String queryString =
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
							+ "PREFIX dem: <http://www.demcare.eu/ontologies/demlab.owl#> "
							+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "

							+ "SELECT ?detection ?timestamp ?slabel ?elabel ?leftright ?distance ?offset "
							+ "WHERE { "
							+ "?detection a sot:Detection ."
							+ "?detection sot:hasTimestamp ?timestamp ."
							+ "?detection sot:providedBy ?sensor ."
							+ "?detection sot:detectsSemanticSpace ?scene ."
							+ "?detection sot:detectsObject ?entity ."
							+ "?entity rdfs:label ?elabel."
							+ "?scene rdfs:label ?slabel."
							//"{?entity a sot:" +entity + " . } UNION { ?entity a dem:" +entity + " . }"
							//+ "OPTIONAL{{?entity a sot:" +entity + " . } UNION { ?entity a dem:" +entity + " . }}"
							+ "?detection sot:detectsObject ?entity ."
							+ "?scene rdf:type sot:SemanticSpace ."
							+ "?entity sot:hasXGridSpace ?x. "
							+ "?x sot:positionedInXGrid ?leftright."
							+ "?x sot:hasOffset ?offset. "
							+ "?entity sot:hasSpatialContext ?sc."
							+ "?sc sot:definesAbsoluteDistance ?distance."
							+ "}"
							+ "ORDER BY desc(?timestamp)"
							+ "lIMIT 4";

//							+ "SELECT ?timestamp ?detection ?scene ?object ?person ?leftright ?elabel ?timestamp ?slabel ?distance ?offset "
//							+ "WHERE {"
//							+ "?detection a sot:Detection . "
//							+ "?detection sot:hasTimestamp ?timestamp . "
//							//+ "?detection sot:detectsSemanticSpace ?scene . "
//							//+ "?scene rdfs:label ?slabel. "
//							+ "?detection sot:providedBy ?sensor . "
//							+ "?detection sot:detectsObject ?entity ."
////							+ "?entity a sot:" +entity + " . "
//							+ "{?entity a sot:" +entity + " . } UNION { ?entity a dem:" +entity + " . }"
//							+ "?entity rdfs:label ?elabel. "
//							+ "?sensor a sot:Camera. "
//							+ "?entity sot:hasXGridSpace ?x. "
//							+ "?x sot:hasOffset ?offset. "
//							+ "?x sot:positionedInXGrid ?leftright. "
//							+ "?entity sot:hasSpatialContext ?sc. "
//							+ "?sc sot:definesAbsoluteDistance ?distance "
////							+ "filter not exists { "
////      						+ "	?detection2 sot:hasTimestamp ?timestamp2 "
////							+ "filter (?timestamp2 > ?timestamp) "
//							+ "} "
//							+ "ORDER BY desc(?timestamp) "
//							+ "LIMIT 1";

			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
//			System.out.println("executing query.");
			TupleQueryResult queryResult = tupleQuery.evaluate();

			if(!queryResult.hasNext()){
					answer = "not found";
					timestamp = "";
					distance = "";
					offset = "";
					queryAnswers.put("timestamp", timestamp);
					queryAnswers.put("distance", distance);
					queryAnswers.put("offset", offset);
					queryAnswers.put("answer", answer);
			}
			else{
			while (queryResult.hasNext()) { // iterate over the result
//				System.out.println("query exei results");
				BindingSet bindingSet = queryResult.next();
//				System.out.println(bindingSet);
//				System.out.println(bindingSet.getValue("elabel").stringValue());
//				System.out.println(entity.toLowerCase());
				if (bindingSet.size() != 0) {
					if(bindingSet.getValue("elabel").stringValue().equals(entity.toLowerCase()) || bindingSet.getValue("elabel").stringValue().equals("null")) {
						Value value = bindingSet.getValue("timestamp");
						timestamp = value.stringValue();
						queryAnswers.put("timestamp", timestamp);
//						System.out.println(timestamp);
						value = bindingSet.getValue("distance");
						distance = value.stringValue();
						queryAnswers.put("distance", distance);
//						System.out.println(value);
						value = bindingSet.getValue("offset");
						offset = value.stringValue();
						queryAnswers.put("offset", offset);
//						System.out.println(offset);
						queryAnswers.put("answer", "Found: " + timestamp + " , " + distance + " , " + offset + " .");
						break;
					}
				else{
//					System.out.println("query exei results alla den exei " + entity);
					answer = "not found";
					timestamp = "";
					distance = "";
					offset = "";
					queryAnswers.put("timestamp", timestamp);
					queryAnswers.put("distance", distance);
					queryAnswers.put("offset", offset);
					queryAnswers.put("answer", answer);
				}}

				System.out.println("Final Feedback: " + answer);
			}
		}}
		return queryAnswers;

	}

	public String findLatestDetectionOfEntityVA(String entity){
		Map<String,String> interfaceQAnswerMap = new HashMap<>();
		String interfaceQAnswer = "";
		String timestamp = "";
		String leftright = "";
		String scene = "";
		String distance = "";

		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {

			String queryString =
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
							+ "PREFIX dem: <http://www.demcare.eu/ontologies/demlab.owl#> "
							+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
							+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
							+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
							+ "SELECT ?detection ?timestamp ?slabel ?elabel ?leftright ?distance "
							+ "WHERE { "
							+ "?detection a sot:Detection . "
							+ "?detection sot:hasTimestamp ?timestamp . "
							+ "?detection sot:providedBy ?sensor . "
							+ "?detection sot:detectsSemanticSpace ?scene . "
							+ "?detection sot:detectsObject ?entity . "
							+ "?entity rdfs:label ?elabel. "
							+ "?scene rdfs:label ?slabel. "
							+ "{?entity a sot:" + entity + " . } UNION { ?entity a dem:" +entity + " . }"
							+ "?scene rdf:type sot:SemanticSpace . "
							+ "?entity sot:hasXGridSpace ?x. "
							+ "?x sot:positionedInXGrid ?leftright."
							+ "?entity sot:hasSpatialContext ?sc."
							+ "?sc sot:definesAbsoluteDistance ?distance."
							+ "}"
							+ "ORDER BY desc(?timestamp)"
							+ "LIMIT 1";

			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
//			System.out.println("executing query.");
			TupleQueryResult queryResult = tupleQuery.evaluate();

			if(!queryResult.hasNext()){
				interfaceQAnswer = entity + " not found";
				scene = "";
				timestamp = "";
				leftright ="";
				distance = "";
			}
			else {
				while (queryResult.hasNext()) { // iterate over the result
//					System.out.println("query exei results");
					BindingSet bindingSet = queryResult.next();
					System.out.println("Interface set: " + bindingSet);
					System.out.println(bindingSet.getValue("elabel").stringValue());
					System.out.println(entity.toLowerCase());
					if (bindingSet.size() != 0) {
						Value value = bindingSet.getValue("timestamp");
						timestamp = value.stringValue();
						interfaceQAnswerMap.put("timestamp", timestamp);
//						System.out.println(timestamp);
						value = bindingSet.getValue("elabel");
						entity = value.stringValue();
						interfaceQAnswerMap.put("entity", entity);
						if (interfaceQAnswerMap.get("entity").equals("null")) {
							System.out.println("mphkaaaaaaaa");
							interfaceQAnswerMap.put("entity", "cup");
						}
//						System.out.println(entity);
						value = bindingSet.getValue("leftright");
						leftright = value.stringValue();
						interfaceQAnswerMap.put("position", leftright);
//						System.out.println(leftright);
						value = bindingSet.getValue("distance");
						distance = value.stringValue();
						interfaceQAnswerMap.put("distance", distance);
						value = bindingSet.getValue("slabel");
						scene = value.stringValue();
						interfaceQAnswerMap.put("scene", scene);
//						System.out.println(scene);
					}
				}
//				System.out.println(interfaceQAnswerMap.get("entity"));
//				if (interfaceQAnswerMap.get("entity").equals("null")) {
////					System.out.println("mphkaaaaaaaa");
//					interfaceQAnswerMap.put("entity", "cup");
//				}
				SimpleDateFormat formatter= new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
				Date time_now = new Date(System.currentTimeMillis());
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
				Date from_timestamp = fmt.parse(interfaceQAnswerMap.get("timestamp"));
				long diff = time_now.getTime() - from_timestamp.getTime();
				long diffSeconds = diff / 1000 % 60;
				long diffMinutes = diff / (60 * 1000) % 60;
				long diffHours = diff / (60 * 60 * 1000);
				interfaceQAnswerMap.put("mins", String.valueOf(diffMinutes));
				interfaceQAnswerMap.put("secs", String.valueOf(diffSeconds));
//				System.out.println("Time in seconds: " + diffSeconds + " seconds.");
//				System.out.println("Time in minutes: " + diffMinutes + " minutes.");
//				System.out.println("Time in hours: " + diffHours + " hours.");

					if(interfaceQAnswerMap.get("scene").equals("unknown")){
						String queryString2 =
								"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
										+ "PREFIX dem: <http://www.demcare.eu/ontologies/demlab.owl#> "
										+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
										+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
										+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
										+ "SELECT ?slabel "
										+ "WHERE{ "
    									+ "?detection a sot:Detection . "
										+ "?detection sot:hasTimestamp ?timestamp . "
										+ "?detection sot:providedBy ?sensor . "
										+ "?detection sot:detectsSemanticSpace ?scene . "
										+ "?scene rdf:type sot:SemanticSpace . "
										+ "?scene rdfs:label ?slabel . "
										+ "?sensor a sot:Camera. "
										+ "FILTER(?slabel !=\"unknown\" ) . "
										//+ "FILTER(?slabel !=\"unknown\" && ?timestamp <= " + timestamp + ") . "
										+ "}"
										+ "ORDER BY desc(?timestamp)"
										+ "LIMIT 1";

						TupleQuery tupleQuery2 = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString2);
//						System.out.println("executing query2.");
						TupleQueryResult queryResult2 = tupleQuery2.evaluate();

						if(!queryResult2.hasNext()){
							interfaceQAnswer = entity + " not found";
							scene = "";
							timestamp = "";
							leftright ="";
							distance = "";
						}
						else{
							while (queryResult2.hasNext()) { // iterate over the result
//								System.out.println("query 2 exei results");
								BindingSet bindingSet2 = queryResult2.next();
//								System.out.println("Interface set 2: " + bindingSet2);
								if (bindingSet2.size() != 0) {
									Value value = bindingSet2.getValue("slabel");
									scene = value.stringValue();
									interfaceQAnswerMap.put("scene", scene);
//									System.out.println("Scene after query 2: " + scene);
								}

					}}}
							float distanceInMeters = Float.parseFloat(interfaceQAnswerMap.get("distance"));
							distanceInMeters = distanceInMeters/Float.parseFloat("1000.0");
//							System.out.println("distance in meters" + distanceInMeters);
							if(!interfaceQAnswerMap.get("distance").equals("-1.0")){
								if(!interfaceQAnswerMap.get("scene").equals("scene_unknown")) {
									interfaceQAnswer = "The " + interfaceQAnswerMap.get("entity") + " was detected in the " + scene + " , " + interfaceQAnswerMap.get("mins") + " mins and " + interfaceQAnswerMap.get("secs") + " secs ago, ";
									if (leftright.equals("right") || leftright.equals("left"))
										interfaceQAnswer = interfaceQAnswer + " on your " + leftright + ", " + String.format("%.2f", distanceInMeters) + " m from you.";
									else {
										interfaceQAnswer = interfaceQAnswer + " in front of you, " + String.format("%.2f", distanceInMeters) + " m from you, ";
							}
						}
								else{
									interfaceQAnswer = "The " + interfaceQAnswerMap.get("entity") + " was detected in an unknown scene, " + interfaceQAnswerMap.get("mins") + " mins and " + interfaceQAnswerMap.get("secs") + " secs ago, ";
									if (leftright.equals("right") || leftright.equals("left"))
										interfaceQAnswer = interfaceQAnswer + " on your " + leftright + ", " + String.format("%.2f", distanceInMeters) + " m from you.";
									else {
										interfaceQAnswer = interfaceQAnswer + " in front of you, " + String.format("%.2f", distanceInMeters) + " m from you, ";
								}
					}}
					else{
								if(!interfaceQAnswerMap.get("scene").equals("scene_unknown")) {
									interfaceQAnswer = "The " + interfaceQAnswerMap.get("entity") + " was detected in the " + scene + " , " + interfaceQAnswerMap.get("mins") + " mins and " + interfaceQAnswerMap.get("secs") + " secs ago, ";
									if (leftright.equals("right") || leftright.equals("left"))
										interfaceQAnswer = interfaceQAnswer + " on your " + leftright + ". ";
									else {
										interfaceQAnswer = interfaceQAnswer + " in front of you. ";
									}
								}
								else{
									interfaceQAnswer = "The " + interfaceQAnswerMap.get("entity") + " was detected in an unknown scene, " + interfaceQAnswerMap.get("mins") + " mins and " + interfaceQAnswerMap.get("secs") + " secs ago, ";
									if (leftright.equals("right") || leftright.equals("left"))
										interfaceQAnswer = interfaceQAnswer + " on your " + leftright + ". ";
									else {
										interfaceQAnswer = interfaceQAnswer + " in front of you. ";
									}
								}
					}

			}} catch (ParseException e) {
			e.printStackTrace();
		}
//		System.out.println("Final interface: " + interfaceQAnswer);
		return interfaceQAnswer;

	}

	public Map<String, String> findUserLocation(){
		Map<String,String> queryAnswers = new HashMap<String,String>();
		String answer = "";
		String timestamp = "";
		String scene = "";

		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {

			String queryString =
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
							+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "

							+ "SELECT (max(?timestamp) AS ?maxtimestamp) ?slabel "
							+ "WHERE {"
							+ "?detection a sot:Detection . "
							+ "?detection sot:hasTimestamp ?timestamp . "
							+ "?detection sot:detectsSemanticSpace ?scene ."
							+ "?scene rdfs:label ?slabel. "
							+ "?detection sot:providedBy ?sensor . "
							+ "?scene rdf:type sot:SemanticSpace . "
							+ "FILTER(?slabel != \"unknown\") ."
							+ "}"
							+ "GROUP BY ?slabel "
							+ "ORDER BY desc(?maxtimestamp) "
							+ "LIMIT 1";

			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
//			System.out.println("executing query.");
			TupleQueryResult queryResult = tupleQuery.evaluate();

			if (queryResult.hasNext()){
			while (queryResult.hasNext()) { // iterate over the result
//				System.out.println("query exei results");
				BindingSet bindingSet = queryResult.next();
//				System.out.println(bindingSet);
				if (bindingSet.size() != 0) {
					Value value = bindingSet.getValue("maxtimestamp");
					timestamp = value.stringValue();
					queryAnswers.put("timestamp", timestamp);
//					System.out.println(timestamp);
					value = bindingSet.getValue("slabel");
					scene = value.stringValue();
					queryAnswers.put("scene", scene);
//					System.out.println(scene);
				}
				SimpleDateFormat formatter= new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
				Date time_now = new Date(System.currentTimeMillis());
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
				Date from_timestamp = fmt.parse(queryAnswers.get("timestamp"));
				long diff = time_now.getTime() - from_timestamp.getTime();
				long diffSeconds = diff / 1000 % 60;
				long diffMinutes = diff / (60 * 1000) % 60;
				long diffHours = diff / (60 * 60 * 1000);
				answer = "You are located in the " + scene + ". (Detection " + diffMinutes + "mins, " + diffSeconds + " secs ago)";
				queryAnswers.put("answer", answer);
			}}
			else{
				queryAnswers.put("timestamp", timestamp);
				queryAnswers.put("scene", scene);
				queryAnswers.put("answer", "Scene not detected.");
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return queryAnswers;

	}

	public String getTimestampOfLatestDetectionFromSensor_URI(String sensor){
		String maxtimestamp = null;

		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {

			String queryString = 
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
							
					+ "SELECT (max(?timestamp) AS ?maxtimestamp) " 
					+ "WHERE {" 
					+ "?detection a sot:Detection . "
					+ "?detection sot:hasTimestamp ?timestamp . " 
					+ "?detection sot:providedBy ?sensor . "
					+ "?sensor a sot:" + sensor + " . "
					+ "}";
			
			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

			TupleQueryResult queryResult = tupleQuery.evaluate();

			while (queryResult.hasNext()) { // iterate over the result
				BindingSet bindingSet = queryResult.next();
				if (bindingSet.size() != 0) {
					Value value = bindingSet.getValue("maxtimestamp");
					maxtimestamp = value.stringValue();
				}
			}
		}
		return maxtimestamp;
		
	}

	public String getTimestampOfLatestEntityDetection_URI(String sensor) {
		String maxtimestamp = null;

		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {

			String queryString = 
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
							
					+ "SELECT (max(?timestamp) AS ?maxtimestamp) " 
					+ "WHERE {" 
					+ "?detection a sot:Detection . "
					+ "?detection sot:hasTimestamp ?timestamp . " 
					+ "?detection sot:providedBy ?sensor . "
					+ "?sensor a sot:" + sensor + " . "
//					+ "?detection sot:detects ?entity . " 
					+ "{" 
					+ "?detection sot:detectsPerson ?person . "
					+ "?person rdf:type sot:Person . "
//					+ "{"
//					+ "?entity rdf:type sot:Person . "
					+ " } UNION { " 
					+ "?detection sot:detectsObject ?object . "
					+ "?object rdf:type sot:Object . "
//					+ "?entity rdf:type sot:Object . "
					+ "} " 
					+ "}";

			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

			TupleQueryResult queryResult = tupleQuery.evaluate();

			while (queryResult.hasNext()) { // iterate over the result
				BindingSet bindingSet = queryResult.next();
				if (bindingSet.size() != 0) {
					Value value = bindingSet.getValue("maxtimestamp");
					maxtimestamp = value.stringValue();
				}
			}
		}
		return maxtimestamp;
	}

	public void getDetectedEntities(String maxtimestamp, String sensor) throws IOException {
		// public HashMap<String, HashMap<String, Integer>> getDetectedEntities(String maxtimestamp) {
		// Returns LATEST detected object/person
		// key:object/person, value: HashMap<String, Integer> with key: object_type and value: no of detected entities
		
		HashSet<String> detectedPersons = new HashSet<String>();
		HashSet<String> detectedObjects = new HashSet<String>();
		
		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {			
					
			String queryString = 
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
					+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
							
					+ "SELECT DISTINCT ?detection ?object ?person "
					+ "WHERE {"
					+ "?detection a sot:Detection . "
					+ "?detection sot:hasTimestamp \"" + maxtimestamp.toString() + "\"^^xsd:dateTime . " 
					+ "?detection sot:providedBy ?sensor . "
					+ "?sensor a sot:" + sensor + " . "
					+ "{" 
					+ "?detection sot:detectsPerson ?person . "
//					+ "?person rdf:type sot:Person . " 
					+ " } UNION { " 
					+ "?detection sot:detectsObject ?object . "
//					+ "?object rdf:type sot:Object . " 
					+ "} "
					+ "} "
					+ "ORDER BY ?detection";
					
			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult queryResult = tupleQuery.evaluate();
			
			while (queryResult.hasNext()) { // iterate over the result
				BindingSet bindingSet = queryResult.next();

				Value object = bindingSet.getValue("object");
				Value person = bindingSet.getValue("person");
				Value detection = bindingSet.getValue("detection");
				
				if (person != null)
					detectedPersons.add(person.toString());
				if (object != null)
					detectedObjects.add(object.stringValue());

//				System.out.println("Detection: " + detection + " \tobject: " + object + " \tperson: " + person);
				
			}			
//			System.out.println("- Number of latest detected persons: \n\t" + detectedPersons.size() + "\n- Number of latest detected objects: \n\t" + detectedObjects.size() );
			
//			MyThreadServeMessage.logOntologyPopulationAndInference.appendNewLineWithTimestamp(
//					"- Number of latest detected persons: " + detectedPersons.size()
//					);
			if (sensor.contains("Camera")){
				MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp(
						"- Number of latest detected persons: " + detectedPersons.size()
						);
			}
			else if (sensor.contains("iBeacon")){
				MyThreadServeMessage.logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp(
						"- Number of latest detected persons: " + detectedPersons.size()
						);
			}
			if (detectedPersons.size()!= 0){
//				System.out.print("- List of persons: \n");
				Iterator<String> person_iterator = detectedPersons.iterator();
			     while(person_iterator.hasNext()){
//			        System.out.println("-- Person: " + person_iterator.next());
			    	 if (sensor.contains("Camera")){
					        MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp(
					        		"-- Person: " + person_iterator.next()
					        		);
			    		 
			    	 }
			    	 else if (sensor.contains("iBeacon")){
					        MyThreadServeMessage.logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp(
					        		"-- Person: " + person_iterator.next()
					        		);
			    		 
			    	 }
//			        MyThreadServeMessage.logOntologyPopulationAndInference.appendNewLineWithTimestamp(
//			        		"-- Person: " + person_iterator.next()
//			        		);
			        
			     }				
			}
			
//			MyThreadServeMessage.logOntologyPopulationAndInference.appendNewLineWithTimestamp(
//					"- Number of latest detected objects: " + detectedObjects.size()
//					);
			if (sensor.contains("Camera")){
				MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp(
						"- Number of latest detected objects: " + detectedObjects.size()
						);
			}
			else if (sensor.contains("iBeacon")){
				MyThreadServeMessage.logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp(
						"- Number of latest detected objects: " + detectedObjects.size()
						);
			}
			if (detectedObjects.size()!= 0){
//				System.out.print("- List of objects: \n");
				Iterator<String> object_iterator = detectedObjects.iterator();
			     while(object_iterator.hasNext()){
			     	String obj = object_iterator.next();
//			        System.out.println("-- Object: " + obj);
//			        MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp(
//			        		"-- Object: " + object_iterator.next()
//			        		);
			    	 if (sensor.contains("Camera")){
					        MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp(
					        		"-- Object: " + obj
					        		);
			    	 }
			    	 else if (sensor.contains("iBeacon")){
					        MyThreadServeMessage.logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp(
					        		"-- Object: " + obj
					        		);
			    	 }
			     }				
			}


		}
			
	}
	

	public void getDetectedEntitiesAndDistance(String maxtimestamp, Float distance_threshold, String sensor, String lessOrMore) throws IOException {
		
		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {
		
//			String queryString = 
//						"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
//					+ 	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
//					+ 	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
//							
//					+ 	"SELECT DISTINCT ?detection ?entity ?distance "
//					+ 	"WHERE {"
//					+ 		"?detection a sot:Detection . "
//					+ 		"?detection sot:hasTimestamp \"" + maxtimestamp.toString() + "\"^^xsd:dateTime . " 
//					+ 		"?detection sot:providedBy ?sensor ."
//					+ 		"?sensor a sot:Camera ."
//					+ 		"?detection sot:detects ?entity ."
//					+ 		"{" 
//					+ 			"?entity rdf:type sot:Person . " 
//					+ 		" } UNION { " 
//					+ 			"?entity rdf:type sot:Object . " 
//					+ 		"} ."
//					+		"?entity sot:hasSpatialContext ?spatialContext ."
//					+ 		"?spatialContext a sot:AbsoluteDistanceSpatialContext ."
//					+ 		"?spatialContext sot:definesAbsoluteDistance ?distance ."
//					;
			
			String queryString = 
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
				+ 	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ 	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
						
				+ 	"SELECT DISTINCT ?detection ?entity ?distance "
				+ 	"WHERE {"
				+ 		"?detection a sot:Detection . "
				+ 		"?detection sot:hasTimestamp \"" + maxtimestamp.toString() + "\"^^xsd:dateTime . " 
				+ 		"?detection sot:providedBy ?sensor . "
				+ 		"?sensor a sot:" + sensor + ". " //either Camera or iBeacon
				+ 		"{" 
				+ 			"?detection sot:detectsPerson ?entity . "
//				+			"?entity rdf:type sot:Person . " 
				+ 		" } UNION { " 
				+ 			"?detection sot:detectsObject ?entity. "
//				+ 			"?entity rdf:type sot:Object . " 
				+ 		"} ."
				+		"?entity sot:hasSpatialContext ?spatialContext . "
				+ 		"?spatialContext a sot:AbsoluteDistanceSpatialContext . "
				+ 		"?spatialContext sot:definesAbsoluteDistance ?distance . "
				
				;
			
			if (lessOrMore.equals("less"))
				queryString = queryString 
						+ 	"FILTER (?distance < " + distance_threshold + ")"
						;
			else if(lessOrMore.equals("more"))
				queryString = queryString 
						+ 	"FILTER (?distance > " + distance_threshold + ")"
						;
			
			queryString = queryString
					+ 	"} "
					+ 	"ORDER BY ?distance";
			
			
			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult queryResult = tupleQuery.evaluate();
			
			HashMap<String, Float> results = new HashMap<String, Float>();
			
			while (queryResult.hasNext()) { // iterate over the result
				BindingSet bindingSet = queryResult.next();
	
				Value detection = bindingSet.getValue("detection");
				Value entity = bindingSet.getValue("entity");
				Value distance = bindingSet.getValue("distance");
				
				if (entity != null){
					results.put(entity.toString(), new Float(distance.stringValue().replace("^^<http://www.w3.org/2001/XMLSchema#float>", "").replace("\"", ""))); //Nullpointer exception
				}				
			}
			if (lessOrMore.equals("less")){
//				System.out.println("- Number of entities that are closer (less than " + distance_threshold + ") to the user: \n\t" + results.size());
//				MyThreadServeMessage.logOntologyPopulationAndInference.appendNewLineWithTimestamp(
//						"- Number of entities that are closer (less than " + distance_threshold + ") to the user: " + results.size()
//						);
				if (sensor.contains("Camera")){
					MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp(
							"- Number of entities that are closer (less than " + distance_threshold + ") to the user: " + results.size()
							);
				}
				else if (sensor.contains("iBeacon")){
					MyThreadServeMessage.logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp(
							"- Number of entities that are closer (less than " + distance_threshold + ") to the user: " + results.size()
							);
				}
			}
			else if (lessOrMore.equals("more")){
//				System.out.println("- Number of entities that are far (more than " + distance_threshold + ") from the user: \n\t" + results.size());
//				MyThreadServeMessage.logOntologyPopulationAndInference.appendNewLineWithTimestamp(
//						"- Number of entities that are far (more than " + distance_threshold + ") from the user: " + results.size()
//						);
				if (sensor.contains("Camera")){
					MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp(
							"- Number of entities that are far (more than " + distance_threshold + ") from the user: " + results.size()
							);
				}
				if (sensor.contains("iBeacon")){
					MyThreadServeMessage.logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp(
							"- Number of entities that are far (more than " + distance_threshold + ") from the user: " + results.size()
							);
				}
			}
			if (results.size() != 0){
				results = Utilities.sortByValue(results);
				for (Map.Entry<String, Float> entry : results.entrySet()) {
//					System.out.println("Entity: " + entry.getKey() + "\t" + "Distance: " + entry.getValue().toString());
//					MyThreadServeMessage.logOntologyPopulationAndInference.appendNewLineWithTimestamp(
//							"-- Entity: " + entry.getKey() + "\t" + "Distance: " + entry.getValue().toString()
//							);
					if (sensor.contains("Camera")){
						MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp(
						"-- Entity: " + entry.getKey() + "\t" + "Distance: " + entry.getValue().toString()
						);
					}
					else if (sensor.contains("iBeacon")){
						MyThreadServeMessage.logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp(
						"-- Entity: " + entry.getKey() + "\t" + "Distance: " + entry.getValue().toString()
						);
					}
				}
			}

		
		}		
	}
	
	public TupleQueryResult getLatestDetectionOfSpecificEntity(String sensor, String entityName) {

		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {
			String queryString = 
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
				+ 	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ 	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ 	"PREFIX dem: <http://www.demcare.eu/ontologies/event.owl#> "
				;
			
			if (sensor.equals("VA")){
				queryString = queryString 
						+ 	"SELECT ?detection ?timestamp "
						+ 	"WHERE {"
						+ 		"?detection a sot:Detection . "
						+ 		"?detection sot:hasTimestamp ?timestamp . " 
						+ 		"?detection sot:providedBy ?sensor . "
						+ 		"?sensor rdf:type sot:Camera . "
						+ 		"?detection sot:detectsObject ?object . "
						+ 		"?object rdf:type sot:Object . "
//						+ 		"?object rdf:type " + "<" + objectURI + "> ."
						+ 		"?object rdf:type dem:Cup . "
						+ 		"}"
						+ 		"ORDER BY DESC(?timestamp) "
						+ 		"LIMIT 1 "
						;
			}
			else if (sensor.equals("IB")){
				queryString = queryString 
						+ 	"SELECT ?detection ?timestamp "
						+ 	"WHERE {"
						+ 		"?detection a sot:Detection . "
						+ 		"?detection sot:hasTimestamp ?timestamp . " 
						+ 		"?detection sot:providedBy ?sensor . "
						+ 		"?sensor rdf:type sot:iBeacon . "
						+ 		"?sensor sot:hasName ?name . "
						+		"FILTER (?name = \"mug\") . "	//********* hard coded here
						+ 		"?detection sot:detectsObject ?object . "
						+ 		"?object rdf:type sot:Object . "
//						+ 		"?object rdf:type " + "<" + objectURI + "> ."
						+ 		"?object rdf:type dem:Cup . "
						+ 		"}"
						+ 		"ORDER BY DESC(?timestamp) "
						+ 		"LIMIT 1 "							
						;
				
			}
			else{
				System.out.println("No valid sensor.");
				return null;
			}
			

			
//			System.out.println("query: " + queryString);
			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult queryResult = tupleQuery.evaluate();

			
//			while (queryResult.hasNext()) { // iterate over the result
//				BindingSet bindingSet = queryResult.next();
//	
//				Value detection = bindingSet.getValue("detection");
//				Value timestamp = bindingSet.getValue("timestamp");
//				System.out.println("Results || detection: " + detection.stringValue() + ", timestamp: " + timestamp.stringValue());
//			}
		

			return queryResult;
		
			
		}
		catch (Exception e){
			System.out.println("Exception here");
		}
		return null;
	}
	
	public TupleQueryResult getLatestDetectionOfVAForSpecificEntity(String entityName) {
		
		TupleQueryResult queryResult = null;

		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {
			String queryString = 
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
				+ 	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ 	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ 	"PREFIX dem: <http://www.demcare.eu/ontologies/event.owl#> "
				
						+ 	"SELECT ?detection ?timestamp "
						+ 	"WHERE {"
						+ 		"?detection a sot:Detection . "
						+ 		"?detection sot:hasTimestamp ?timestamp . " 
						+ 		"?detection sot:providedBy ?sensor . "
						+ 		"?sensor rdf:type sot:Camera . "
						+ 		"?detection sot:detectsObject ?object . "
						+ 		"?object rdf:type sot:Object . "
//						+ 		"?object rdf:type " + "<" + objectURI + "> ."
						+ 		"?object rdf:type dem:Cup . "
						+ 		"}"
						+ 		"ORDER BY DESC(?timestamp) "
						+ 		"LIMIT 1 "
						;
			
			
//			System.out.println("query: " + queryString);
			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			queryResult = tupleQuery.evaluate();

			
//			while (queryResult.hasNext()) { // iterate over the result
//				BindingSet bindingSet = queryResult.next();
//	
//				Value detection = bindingSet.getValue("detection");
//				Value timestamp = bindingSet.getValue("timestamp");
//				System.out.println("Results || detection: " + detection.stringValue() + ", timestamp: " + timestamp.stringValue());
//			}
		
		}
		
		return queryResult;
}
	
	public TupleQueryResult getLatestDetectionOfIBForSpecificEntity(String entityName) {
		
		TupleQueryResult queryResult = null;

		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {
			String queryString = 
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
				+ 	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ 	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ 	"PREFIX dem: <http://www.demcare.eu/ontologies/event.owl#> "
				
						+ 	"SELECT ?detection ?timestamp "
						+ 	"WHERE {"
						+ 		"?detection a sot:Detection . "
						+ 		"?detection sot:hasTimestamp ?timestamp . " 
						+ 		"?detection sot:providedBy ?sensor . "
						+ 		"?sensor rdf:type sot:iBeacon . "
						+ 		"?sensor sot:hasName ?name . "
						+		"FILTER (?name = \"mug\") . "	//********* hard coded here
						+ 		"?detection sot:detectsObject ?object . "
						+ 		"?object rdf:type sot:Object . "
						//+ 		"?object rdf:type " + "<" + objectURI + "> ."
						+ 		"?object rdf:type dem:Cup . "
						+ 		"}"
						+ 		"ORDER BY DESC(?timestamp) "
						+ 		"LIMIT 1 "							
						;
			
			
//			System.out.println("query: " + queryString);
			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			queryResult = tupleQuery.evaluate();

			
//			while (queryResult.hasNext()) { // iterate over the result
//				BindingSet bindingSet = queryResult.next();
//	
//				Value detection = bindingSet.getValue("detection");
//				Value timestamp = bindingSet.getValue("timestamp");
//				System.out.println("Results || detection: " + detection.stringValue() + ", timestamp: " + timestamp.stringValue());
//			}
		
		}
		
		return queryResult;
}
	
	public void populateVADataWithSPARQL(VAComponentResults vaResults){

		/* detection */		
		//String detectionURI = Vocabulary.NAMESPACE_TBOX + "detection_" + UUID.randomUUID().toString();
		String detectionURI = Vocabulary.NAMESPACE_TBOX + "detection_" + counter++;
		
		String strInsert =  "<" + detectionURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "Detection" + "> ." //detection_ABC rdf:type sot:Detection 
								+ "<" + detectionURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasTimestamp" 	+ "> \"" + vaResults.getTimestamp() + "\"^^xsd:dateTime" + " ." //detection sot:hasTimestamp timestamp
								+ "<" + detectionURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasImageName" 	+ "> \"" + vaResults.getImageName() + "\"^^xsd:string" + " ."
								+ "<" + detectionURI + "> <" + Vocabulary.NAMESPACE_TBOX + "providedBy" 	+ "> <" + Vocabulary.NAMESPACE_TBOX + "camera" + "> ." //detection sot:providedBy camera
								+ "<" + Vocabulary.NAMESPACE_TBOX + "camera" + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "Camera" + "> ." //camera is a sensor of Camera type
								;
		
		/* semantic space */		
		if (!vaResults.getSceneType().trim().isEmpty()){
			
			String semanticSpaceURI = Vocabulary.NAMESPACE_TBOX + vaResults.getSceneType();
			
			strInsert = strInsert							
							+ "<" + detectionURI + "> <" + Vocabulary.NAMESPACE_TBOX + "detectsSemanticSpace" + "> <" + semanticSpaceURI + "> ." //detection sot:detectsSemanticSpace spaceXY
							+ "<" + semanticSpaceURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "SemanticSpace" + "> ." //semantic space rdf:type sot:SemanticSpace
							+ "<" + semanticSpaceURI + "> <" + RDFS.LABEL + "> \"" + Vocabulary.getLabelOfSemanticSpaceConcept(vaResults.getSceneType()) + "\"^^xsd:string" + " ." //semantic space rdfs:label label
							+ "<" + detectionURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasConfidence" + "> \"" + (new Float(vaResults.getSceneScore().toString())).toString() + "\"^^xsd:float" + " ." //detection sot:hasConfidence 0.77
							;	
		}
		
		/* grid space */
		String detectionGridSpaceURI = Vocabulary.NAMESPACE_TBOX + "detection_grid_space_" + UUID.randomUUID().toString();
		strInsert = strInsert
				+ "<" + detectionURI + "> <" + Vocabulary.NAMESPACE_TBOX + "locatedInGridSpace" + "> <" + detectionGridSpaceURI + "> ." //detection locatedInGridSpace detectionSpaceXY
				+ "<" + detectionGridSpaceURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "GridSpace" + "> ." //detectionSpaceXY rdf:type sot:GridSpace
				+ "<" + detectionGridSpaceURI + "> <" + Vocabulary.NAMESPACE_TBOX + "width" + "> \"" + (new Integer(vaResults.getImageWidth().toString())).toString() + "\"^^xsd:integer" + " ." //detectionSpaceXY sot:width 800
				+ "<" + detectionGridSpaceURI + "> <" + Vocabulary.NAMESPACE_TBOX + "height" + "> \"" + (new Integer(vaResults.getImageHeight().toString())).toString() + "\"^^xsd:integer" + " ." //detectionSpaceXY sot:height 600
				;				
		
		/* detected entities */
		ArrayList<VAEntity> entitiesFound = vaResults.getEntitiesFound();
		
		for (VAEntity entity : entitiesFound) {
			if (entity.getType().contains("person")) {
				/* detected persons */
				String personURI = "";
				// known/unknown person
				if (entity.getType().equals("person_unknown"))
					personURI = Vocabulary.NAMESPACE_TBOX + "person_" + UUID.randomUUID().toString();
				else //person is known (person_xyz)
//					personURI = Vocabulary.NAMESPACE_TBOX + entity.getType().toLowerCase(); //TODO: altered because the other relations were attached into one single instance and queries did not operate properly
					personURI = Vocabulary.NAMESPACE_TBOX + entity.getType().toLowerCase() + "_" + UUID.randomUUID().toString();					

							
				strInsert = strInsert
						+ "<" + detectionURI + "> <" + Vocabulary.NAMESPACE_TBOX + "detectsPerson" + "> <" + personURI + "> ." //detection detectsPerson person
						;
				
				// known/unknown person
				if (entity.getType().equals("person_unknown"))
					strInsert = strInsert
							+ "<" + personURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "UnknownPerson" + "> ."
							+ "<" + personURI + "> <" + RDFS.LABEL + "> \"" + entity.getType().replaceFirst("person_unknown", "unknown_person") + "\"^^xsd:string" + " ." // add name as label
							;					
				else if (entity.getType().contains("person_")){
					//System.out.println("Mphka edw");
					strInsert = strInsert
							+ "<" + personURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "KnownPerson" + "> ."
							+ "<" + personURI + "> <" + RDFS.LABEL + "> \"" + entity.getType().replaceFirst("person_", "") + "\"^^xsd:string" + " ." // add name as label
							;				
				}
				//System.out.println(strInsert);
				//distance from person
				String spatialContextURI = Vocabulary.NAMESPACE_TBOX + "spatialContext_" + UUID.randomUUID().toString();
				strInsert = strInsert
						//+ "<" + personURI + "> <" + Vocabulary.NAMESPACE_TBOX + "facemask" + "> <" + "> \"" + entity.getFacemask() + "\"^^xsd:string" + " ."
						+ "<" + personURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasSpatialContext" + "> <" + spatialContextURI + "> ." //person hasSpatialContext spatialContext
						+ "<" + spatialContextURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "AbsoluteDistanceSpatialContext" + "> ."
						+ "<" + spatialContextURI + "> <" + Vocabulary.NAMESPACE_TBOX + "definesAbsoluteDistance" + "> \"" + (new Float(entity.getDistance())).toString() + "\"^^xsd:float" + " ."
						;
				//hasOccupiedArea
				String entityOccupiedAreaURI = Vocabulary.NAMESPACE_TBOX + "occupiedArea_" + UUID.randomUUID().toString();
				strInsert = strInsert
						+ "<" + personURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasOccupiedArea" + "> <" + entityOccupiedAreaURI + "> ." //person hasOccupiedArea occupiedAreaXYZ
						+ "<" + entityOccupiedAreaURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "OccupiedArea" + "> ."
						+ "<" + entityOccupiedAreaURI + "> <" + Vocabulary.NAMESPACE_TBOX + "xPosition" + "> \"" + (new Integer(entity.getLeft().toString())).toString() + "\"^^xsd:integer" + " ." //occupiedAreaXYZ sot:width 200
						+ "<" + entityOccupiedAreaURI + "> <" + Vocabulary.NAMESPACE_TBOX + "yPosition" + "> \"" + (new Integer(entity.getTop().toString())).toString() + "\"^^xsd:integer" + " ." //occupiedAreaXYZ sot:height 10
						+ "<" + entityOccupiedAreaURI + "> <" + Vocabulary.NAMESPACE_TBOX + "width" + "> \"" + (new Integer(entity.getWidth().toString())).toString() + "\"^^xsd:integer" + " ." //occupiedAreaXYZ sot:width 123
						+ "<" + entityOccupiedAreaURI + "> <" + Vocabulary.NAMESPACE_TBOX + "height" + "> \"" + (new Integer(entity.getHeight().toString())).toString() + "\"^^xsd:integer" + " ." //occupiedAreaXYZ sot:height 456
						;
				//hasXGridSpace
				String entityXGridSpaceURI = Vocabulary.NAMESPACE_TBOX + "xGridSpace_" + UUID.randomUUID().toString();
				strInsert = strInsert
						+ "<" + personURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasXGridSpace" + "> <" + entityXGridSpaceURI + "> ." //person hasXGridSpace xGridSpaceXY
						+ "<" + entityXGridSpaceURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "XGridSpace" + "> ."
						+ "<" + entityXGridSpaceURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasOffset" + "> \"" + entity.getOffset() + "\"^^xsd:integer" + " ."
						+ "<" + entityXGridSpaceURI + "> <" + Vocabulary.NAMESPACE_TBOX + "positionedInXGrid" + "> \"" + entity.getPositionInXGridSpace() + "\"^^xsd:string" + " ."//xGridSpaceXY sot:positionedInXGrid "middle"
						;				
			}
			else{
				/* detected objects */
				String objectURI = Vocabulary.NAMESPACE_TBOX + entity.getType() + "_" + UUID.randomUUID().toString(); 
//				String objectURI = Vocabulary.NAMESPACE_TBOX + entity.getType(); // without random URI. Counter will define how many of them exist
				
				strInsert = strInsert
						+ "<" + detectionURI + "> <" + Vocabulary.NAMESPACE_TBOX + "detectsObject" + "> <" + objectURI + "> ."
						+ "<" + objectURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "Object" + "> ."
//						+ "<" + objectURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasTotalNo" + "> \"" + entity.getTotalNo().toString() + "\"^^xsd:integer" + " ." // number of objects of that type detected
						;
				
				//distance from object
				String spatialContextURI = Vocabulary.NAMESPACE_TBOX + "spatialContext_" + UUID.randomUUID().toString();
				strInsert = strInsert
						+ "<" + objectURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasSpatialContext" + "> <" + spatialContextURI + "> ." //object hasSpatialContext spatialContext
						+ "<" + spatialContextURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "AbsoluteDistanceSpatialContext" + "> ."
						+ "<" + spatialContextURI + "> <" + Vocabulary.NAMESPACE_TBOX + "definesAbsoluteDistance" + "> \"" + (new Float(entity.getDistance())).toString() + "\"^^xsd:float" + " ."
						;
				
				// if object contained in HashMap then getClassURI else use only objectClass
				if (Vocabulary.isObjectContainedInHashMap(entity.getType()))
					
					strInsert = strInsert
							+ "<" + objectURI + "> <" + RDF.TYPE + "> <" + Vocabulary.getClassURIOfObjectType(entity.getType()) + "> ."
							+ "<" + objectURI + "> <" + RDFS.LABEL + "> \"" + Vocabulary.getLabelOfSemanticEntityConcept(entity.getType().toLowerCase()) + "\"^^xsd:string" + " .";
							;
				//System.out.println(entity.getType());
				//hasOccupiedArea
				String entityOccupiedAreaURI = Vocabulary.NAMESPACE_TBOX + "occupiedArea_" + UUID.randomUUID().toString();
				strInsert = strInsert
						+ "<" + objectURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasOccupiedArea" + "> <" + entityOccupiedAreaURI + "> ." //object hasOccupiedArea occupiedAreaXYZ
						+ "<" + entityOccupiedAreaURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "OccupiedArea" + "> ."
						+ "<" + entityOccupiedAreaURI + "> <" + Vocabulary.NAMESPACE_TBOX + "xPosition" + "> \"" + (new Integer(entity.getLeft().toString())).toString() + "\"^^xsd:integer" + " ." //occupiedAreaXYZ sot:width 200
						+ "<" + entityOccupiedAreaURI + "> <" + Vocabulary.NAMESPACE_TBOX + "yPosition" + "> \"" + (new Integer(entity.getTop().toString())).toString() + "\"^^xsd:integer" + " ." //occupiedAreaXYZ sot:height 10
						+ "<" + entityOccupiedAreaURI + "> <" + Vocabulary.NAMESPACE_TBOX + "width" + "> \"" + (new Integer(entity.getWidth().toString())).toString() + "\"^^xsd:integer" + " ." //occupiedAreaXYZ sot:width 123
						+ "<" + entityOccupiedAreaURI + "> <" + Vocabulary.NAMESPACE_TBOX + "height" + "> \"" + (new Integer(entity.getHeight().toString())).toString() + "\"^^xsd:integer" + " ." //occupiedAreaXYZ sot:height 456
						;
				
				//hasXGridSpace
				String entityXGridSpaceURI = Vocabulary.NAMESPACE_TBOX + "xGridSpace_" + UUID.randomUUID().toString();
				strInsert = strInsert
						+ "<" + objectURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasXGridSpace" + "> <" + entityXGridSpaceURI + "> ." //person hasXGridSpace xGridSpaceXY
						+ "<" + entityXGridSpaceURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "XGridSpace" + "> ."
						+ "<" + entityXGridSpaceURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasOffset" + "> \"" + entity.getOffset() + "\"^^xsd:integer" + " ."
						+ "<" + entityXGridSpaceURI + "> <" + Vocabulary.NAMESPACE_TBOX + "positionedInXGrid" + "> \"" + entity.getPositionInXGridSpace() + "\"^^xsd:string" + " ." //xGridSpaceXY sot:positionedInXGrid "middle"
						;
				
			}
		}
		
		strInsert = Vocabulary.PREFIXES_ALL + 
							"INSERT DATA {"
							+ strInsert 
							+ " }";
		
//		System.out.println("QUERY CREATED: \n" + strInsert);
							
		try (RepositoryConnection connection = this.getUpdateRepo().getConnection()) {
			
			connection.begin();
			Update updateOperation = connection.prepareUpdate(QueryLanguage.SPARQL, strInsert);
			updateOperation.execute();
				    
			try {
				connection.commit();
			} catch (Exception e) {
				if (connection.isActive())
					connection.rollback();
			}
		}		
	}
	
	/*
	public void populateVADataWithLib(VAComponentResults vaResults) {

		ValueFactory factory = this.getUpdateRepo().getValueFactory();

		IRI detection = factory.createIRI(Vocabulary.NAMESPACE_TBOX + "detection_" + UUID.randomUUID().toString());

		Literal detection_timestamp = factory.createLiteral(vaResults.getTimestamp(),
				factory.createIRI("http://www.w3.org/2001/XMLSchema#dateTime")); // e.g."2019-01-15T09:32:00"
		Literal detection_confidence = factory.createLiteral(new Float(vaResults.getSceneScore()));

		// classes URI
		IRI semanticSpaceClass = factory.createIRI(Vocabulary.NAMESPACE_TBOX + "SemanticSpace");
		IRI personClass = Vocabulary.createSuitceyesIRI(factory, "Person"); // alternative method
		IRI personUknownClass = Vocabulary.createSuitceyesIRI(factory, "UnknownPerson"); 
		IRI personKnownClass = Vocabulary.createSuitceyesIRI(factory, "KnownPerson"); 

		IRI objectClass = Vocabulary.createSuitceyesIRI(factory, "Object");
		
		IRI cameraIRI = factory.createIRI(Vocabulary.NAMESPACE_TBOX + "camera");

		try (RepositoryConnection connection = this.getUpdateRepo().getConnection()) {
			// detection //
			// detection rdf:type sot:Detection
			connection.add(detection, RDF.TYPE, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "Detection"));
			// detection sot:hasTimestamp timestamp
			connection.add(detection, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "hasTimestamp"),
					detection_timestamp);
			
			// detection sot:providedBy camera
			connection.add(detection, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "providedBy"),
					cameraIRI);
			// camera is a sensor
			connection.add(cameraIRI, RDF.TYPE, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "Camera"));

			// semantic space //
			if (!vaResults.getSceneType().trim().isEmpty()){
				
				IRI semanticSpace = factory.createIRI(Vocabulary.NAMESPACE_TBOX + vaResults.getSceneType());
				
				// detection sot:detectsSemanticSpace spaceXY
				connection.add(detection, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "detectsSemanticSpace"),
						semanticSpace);
				// semantic space rdf:type sot:Seman.TYPE, semanticSpaceClass);
				// semantic space rdfs:label label
				connection.add(semanticSpace, RDFS.LABEL,
						factory.createLiticSpace
				connection.add(semanticSpace, RDFteral(Vocabulary.getLabelOfSemanticSpaceConcept(vaResults.getSceneType()),
								factory.createIRI("http://www.w3.org/2001/XMLSchema#string")));
				// detection sot:hasConfidence 0.77
				connection.add(detection, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "hasConfidence"),
						detection_confidence);
			}

			// detected entities //
			ArrayList<VAEntity> entitiesFound = vaResults.getEntitiesFound();
			for (VAEntity entity : entitiesFound) {
				if (entity.getType().contains("person")) {
					// detected persons //
					// detection detectsPerson person
					IRI person = factory
							.createIRI(Vocabulary.NAMESPACE_TBOX + "person_" + UUID.randomUUID().toString());
					connection.add(detection, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "detectsPerson"), person);
					// known/unknown
					if (entity.getType().equals("person_unknown"))
						connection.add(person, RDF.TYPE, personUknownClass);
					else if (entity.getType().contains("person_")){
						connection.add(person, RDF.TYPE, personKnownClass);
						// add name as label
						connection.add(person, RDFS.LABEL, factory.createLiteral(entity.getType().replaceFirst("person_", ""), factory.createIRI("http://www.w3.org/2001/XMLSchema#string")));
					}
				} else {
					// detected objects
					IRI object = factory.createIRI(
							Vocabulary.NAMESPACE_TBOX + entity.getType() + "_" + UUID.randomUUID().toString());
					connection.add(detection, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "detectsObject"), object);
					connection.add(object, RDF.TYPE, objectClass);
					// if object contained in HashMap then getClassURI else use only objectClass
					if (Vocabulary.isObjectContainedInHashMap(entity.getType()))
						connection.add(object, RDF.TYPE, factory.createIRI(Vocabulary.getClassURIOfObjectType(entity.getType())));
				}
			}

		}
	}
	*/
	
	public void populateIBDataWithSPARQL(IBComponentResults IBResults) throws ParseException{
		
		String strInsert = "";
		
		SimpleDateFormat timestampInputFormat = new SimpleDateFormat("dd-MM-yy hh:mm:ss");		
		SimpleDateFormat timestampOutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		/* one detection for all detected iBeacons */
		String detectionURI = Vocabulary.NAMESPACE_TBOX + "detection_" + UUID.randomUUID().toString();
		
		strInsert = strInsert + "<" + detectionURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "Detection" + "> ." //detection_ABC rdf:type sot:Detection 
				+ "<" + detectionURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasTimestamp" 	+ "> \""  
					+ timestampOutputFormat.format(timestampInputFormat.parse(IBResults.getTimestamp())) + "." + Math.round(Math.random()*(999 - 100) + 100) 
					+ "\"^^xsd:dateTime" + " ." //detection sot:hasTimestamp timestamp ****I have added a 3-digit random number 
				;		
			
		//create a list of iBeacons
		ArrayList<IBeacon> iBeaconsFound = IBResults.getIBeaconsFound();		
		for (IBeacon iBeacon : iBeaconsFound) {			
			
			/* detection provided by different iBeacons */				
			strInsert = strInsert
					+ "<" + detectionURI + "> <" + Vocabulary.NAMESPACE_TBOX + "providedBy" 	+ "> <" + Vocabulary.NAMESPACE_TBOX + "iBeacon_" + iBeacon.getID() + "> ." //detection sot:providedBy iBeacon with ID
					+ "<" + Vocabulary.NAMESPACE_TBOX + "iBeacon_" + iBeacon.getID() + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "iBeacon" + "> ." //camera is a sensor of iBeacon type
					+ "<" + Vocabulary.NAMESPACE_TBOX + "iBeacon_" + iBeacon.getID() + "> <" + Vocabulary.NAMESPACE_TBOX + "hasID" + "> \"" + new Integer(iBeacon.getID()) + "\"^^xsd:integer" + " ." //iBeacon hasID id
					+ "<" + Vocabulary.NAMESPACE_TBOX + "iBeacon_" + iBeacon.getID() + "> <" + Vocabulary.NAMESPACE_TBOX + "hasName" + "> \"" + new String(iBeacon.getIBname()) + "\"^^xsd:string" + " ." //iBeacon hasName name
					;
			
			// detection detects Entity = Person
			if (iBeacon.getIBname().contains("person_")){
				
				String personURI = "";
				
				if (iBeacon.getIBname().equals("person_unknown")){
					personURI = Vocabulary.NAMESPACE_TBOX + "person_" +  UUID.randomUUID().toString();
				}
				else{//person is known
//					personURI = Vocabulary.NAMESPACE_TBOX + iBeacon.getIBname().toLowerCase();	
					personURI = Vocabulary.NAMESPACE_TBOX + iBeacon.getIBname().toLowerCase() + "_" + UUID.randomUUID().toString();	
				}				
				strInsert = strInsert
						+ "<" + detectionURI + "> <" + Vocabulary.NAMESPACE_TBOX + "detectsPerson" 	+ "> <" + personURI + "> ." //detection sot:detectsPerson person_XYZ
						;
				if (iBeacon.getIBname().equals("person_unknown"))
					strInsert = strInsert
					+ "<" + personURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "UnknownPerson" + "> ." 
					;
				else			
					strInsert = strInsert
							+ "<" + personURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "KnownPerson" + "> ." 
							;
				
				//distance from person
				String spatialContextURI = Vocabulary.NAMESPACE_TBOX + "spatialContext_" + UUID.randomUUID().toString();
				strInsert = strInsert
						+ "<" + personURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasSpatialContext" + "> <" + spatialContextURI + "> ." //object hasSpatialContext spatialContext
						+ "<" + spatialContextURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "AbsoluteDistanceSpatialContext" + "> ."
						+ "<" + spatialContextURI + "> <" + Vocabulary.NAMESPACE_TBOX + "definesAbsoluteDistance" + "> \"" + (new Float(iBeacon.getDistance())).toString() + "\"^^xsd:float" + " . " 
						+ "<" + spatialContextURI + "> <" + Vocabulary.NAMESPACE_TBOX + "definesAbsoluteDistanceClass" + "> \"" + iBeacon.getDistance_class().toLowerCase().toString() + "\"^^xsd:string" + " . "						
						;
			}
			else{
			// detection detects Entity = Object
				String objectURI = Vocabulary.NAMESPACE_TBOX + iBeacon.getIBname() + "_" + UUID.randomUUID().toString(); 
				
				strInsert = strInsert
						+ "<" + detectionURI + "> <" + Vocabulary.NAMESPACE_TBOX + "detectsObject" + "> <" + objectURI + "> ."
						+ "<" + objectURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "Object" + "> ."
						;
				
				//distance from object
				String spatialContextURI = Vocabulary.NAMESPACE_TBOX + "spatialContext_" + UUID.randomUUID().toString();
				strInsert = strInsert
						+ "<" + objectURI + "> <" + Vocabulary.NAMESPACE_TBOX + "hasSpatialContext" + "> <" + spatialContextURI + "> ." //object hasSpatialContext spatialContext
						+ "<" + spatialContextURI + "> <" + RDF.TYPE + "> <" + Vocabulary.NAMESPACE_TBOX + "AbsoluteDistanceSpatialContext" + "> ."
						+ "<" + spatialContextURI + "> <" + Vocabulary.NAMESPACE_TBOX + "definesAbsoluteDistance" + "> \"" + (new Float(iBeacon.getDistance())).toString() + "\"^^xsd:float" + " . "
						+ "<" + spatialContextURI + "> <" + Vocabulary.NAMESPACE_TBOX + "definesAbsoluteDistanceClass" + "> \"" + iBeacon.getDistance_class().toLowerCase().toString() + "\"^^xsd:string" + " . "						
						;
				
				// if object contained in HashMap then getClassURI else use only objectClass
				if (Vocabulary.isObjectContainedInHashMap(iBeacon.getIBname()))
					
					strInsert = strInsert
							+ "<" + objectURI + "> <" + RDF.TYPE + "> <" + Vocabulary.getClassURIOfObjectType(iBeacon.getIBname()) + "> ."
							;			
		
			}
		}
				
		strInsert = Vocabulary.PREFIXES_ALL + 
									"INSERT DATA {"
										+ strInsert 
											+ " }";
	
//		System.out.println("QUERY CREATED: \n" + strInsert);
						
		try (RepositoryConnection connection = this.getUpdateRepo().getConnection()) {
		
			connection.begin();
			Update updateOperation = connection.prepareUpdate(QueryLanguage.SPARQL, strInsert);
			updateOperation.execute();
					    
			try {
				connection.commit();
			} catch (Exception e) {
				if (connection.isActive())
					connection.rollback();
			}				
		}	
	}

	/*
	public void populateIBDataWithLib(IBComponentResults IBResults) throws ParseException {
		ValueFactory factory = this.getUpdateRepo().getValueFactory();
		
		SimpleDateFormat timestampInputFormat = new SimpleDateFormat("dd-MM-yy hh:mm:ss");		
		SimpleDateFormat timestampOutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		Literal detection_timestamp = factory.createLiteral(IBResults.getTimestamp(),
				factory.createIRI("http://www.w3.org/2001/XMLSchema#dateTime")); // e.g."2019-01-15T09:32:00"
		detection_timestamp = factory.createLiteral(timestampOutputFormat.format(timestampInputFormat.parse(IBResults.getTimestamp())),
				factory.createIRI("http://www.w3.org/2001/XMLSchema#dateTime")); // e.g."2019-01-15T09:32:00"
		
		try (RepositoryConnection connection = this.getUpdateRepo().getConnection()) {
			
			//create a list of iBeacons
			ArrayList<IBeacon> iBeaconsFound = IBResults.getIBeaconsFound();
			for (IBeacon iBeacon : iBeaconsFound) {
				
				// detection //
				IRI detection = factory.createIRI(Vocabulary.NAMESPACE_TBOX + "detection_" + UUID.randomUUID().toString());
				// detection rdf:type sot:Detection
				connection.add(detection, RDF.TYPE, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "Detection"));
				// detection sot:hasTimestamp timestamp
				connection.add(detection, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "hasTimestamp"),
						detection_timestamp);
				
				IRI iBeaconIRI = factory.createIRI(Vocabulary.NAMESPACE_TBOX + "iBeacon_" + iBeacon.getID());
				
				// detection sot:providedBy iBeaconXYZ
				connection.add(detection, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "providedBy"),
						iBeaconIRI);
				// iBeacon is a sensor
				connection.add(iBeaconIRI, RDF.TYPE, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "iBeacon"));
				// iBeacon hasID ID
				connection.add(iBeaconIRI, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "hasID"), 
						factory.createLiteral(new Integer(iBeacon.getID())), factory.createIRI("http://www.w3.org/2001/XMLSchema#integer"));
				// iBeacon hasName name
//				connection.add(iBeaconIRI, factory.createIRI(Vocabulary.NAMESPACE_TBOX + "hasName"), 
//						factory.createLiteral(new Integer(iBeacon.getIBname())), factory.createIRI("http://www.w3.org/2001/XMLSchema#string"));
				// detection detects Entity (iBeacon name)
				// Entity hasSpatialContext with me
				// type of spatial context = far/near/immediate
				// Entity located in grid space (hasOccupiedArea)
				// distance (absolute distance)
			}
			
		}
		
	}
	*/

	public TupleQueryResult executeSparqlSelect(String query) { //NOTE: do not use this because tupleQuery result can not be returned properly..
		// add prefixes
		query = Vocabulary.PREFIXES_ALL + query;
		// System.out.println("query: " + query);

		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {
			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
			return tupleQuery.evaluate();
		}

	}


	public HashMap<String, String> getLatestDetectionOfSpecificEntityFromSensor(String sensor, String entityName, String entityType) {
		// HashMap: String key: binding name, String value: binding value
		
		try (RepositoryConnection connection = this.getSelectRepo().getConnection()) {
						
			String queryString = 
					"PREFIX sot: <" + Vocabulary.NAMESPACE_TBOX + "> "
				+ 	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ 	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ 	"PREFIX dem: <http://www.demcare.eu/ontologies/event.owl#> "
				;
			
			if (entityType.toLowerCase().equals("o")){
			
				if (sensor.equals("IB")){
					queryString = queryString 
							
							+ 	"SELECT DISTINCT ?detection ?timestamp ?entity ?distance "
							+ 	"WHERE {"
							+ 		"?detection a sot:Detection . "
							+ 		"?detection sot:hasTimestamp ?timestamp . " 
							+ 		"?detection sot:providedBy ?sensor . "
							+ 		"?sensor a sot:iBeacon . "					
							+ 		"?sensor sot:hasName ?name . "
							+		"FILTER (?name = \"" + entityName + "\") . " //e.g. FILTER (?name = "mug")
							
							+ 		"?detection sot:detectsObject ?entity . "
							+ 		"?entity rdf:type sot:Object . "
							+ 		"?entity rdf:type " + "<" + Vocabulary.getClassURIOfObjectType(entityName) + "> . " // e.g. "?object rdf:type dem:Cup . "
							+		"?entity sot:hasSpatialContext ?spatialContext . "
							+ 		"?spatialContext sot:definesAbsoluteDistanceClass ?distance . "
							;
				}
				else if (sensor.equals("VA")){
					queryString = queryString 
							
							+ 	"SELECT DISTINCT ?detection ?timestamp ?entity ?distance ?xGridPosition ?semanticLabel "
							+ 	"WHERE {"
							+ 		"?detection a sot:Detection . "
							+ 		"?detection sot:hasTimestamp ?timestamp . " 
							+ 		"?detection sot:providedBy ?sensor . "							
							+ 		"?sensor rdf:type sot:Camera . "
							
							+ 		"?detection sot:detectsObject ?entity . "
							+ 		"?entity rdf:type sot:Object . "
							+ 		"?entity rdf:type " + "<" + Vocabulary.getClassURIOfObjectType(entityName) + "> . " // e.g. "?object rdf:type dem:Cup . "	
							
							+		"?entity sot:hasSpatialContext ?spatialContext . "
							+ 		"?spatialContext sot:definesAbsoluteDistance ?distance . "
							+		"?entity sot:hasXGridSpace ?xGridSpace . "
							+ 		"?xGridSpace sot:positionedInXGrid ?xGridPosition . "
							
							+ 		"?detection sot:detectsSemanticSpace ?semanticSpace . "
							+ 		"?semanticSpace rdf:type sot:SemanticSpace . "
							+ 		"?semanticSpace rdfs:label ?semanticLabel . "
							;
					
				}
				else{
					System.out.println("No valid sensor.");
					return null;
				}
			}
			else if (entityType.toLowerCase().equals("p")){
				if (sensor.equals("IB")){
					if (entityName.equals("unknown")){
						queryString = queryString 								
								+ 	"SELECT DISTINCT ?detection ?timestamp ?entity ?distance "
								+ 	"WHERE {"
								+ 		"?detection a sot:Detection . "
								+ 		"?detection sot:hasTimestamp ?timestamp . " 
								+ 		"?detection sot:providedBy ?sensor . "
								+ 		"?sensor a sot:iBeacon . "					
								+ 		"?sensor sot:hasName ?name . "
								+ 		"?detection sot:detectsPerson ?entity . "
								+ 		"?entity rdf:type sot:UnknownPerson . "
								+		"?entity sot:hasSpatialContext ?spatialContext . "
								+ 		"?spatialContext sot:definesAbsoluteDistanceClass ?distance . "
								;												
					}
					else{
						queryString = queryString 								
								+ 	"SELECT DISTINCT ?detection ?timestamp ?entity ?distance "
								+ 	"WHERE {"
								+ 		"?detection a sot:Detection . "
								+ 		"?detection sot:hasTimestamp ?timestamp . " 
								+ 		"?detection sot:providedBy ?sensor . "
								+ 		"?sensor a sot:iBeacon . "					
								+ 		"?sensor sot:hasName ?name . "
								+		"FILTER (?name = \"person_" + entityName + "\") . " //e.g. FILTER (?name = "person_NameSurname")					
								+ 		"?detection sot:detectsPerson ?entity . "
			//					+ 		"?entity rdf:type sot:Person . "
								+ 		"?entity rdf:type sot:KnownPerson . "
								+		"?entity sot:hasSpatialContext ?spatialContext . "
								+ 		"?spatialContext sot:definesAbsoluteDistanceClass ?distance . "
								;						
					}
				}
				else if (sensor.equals("VA")){
					if (entityName.equals("unknown")){
						queryString = queryString 
								
								+ 	"SELECT DISTINCT ?detection ?timestamp ?entity ?distance ?xGridPosition ?semanticLabel "
								+ 	"WHERE {"
								+ 		"?detection a sot:Detection . "
								+ 		"?detection sot:hasTimestamp ?timestamp . " 
								+ 		"?detection sot:providedBy ?sensor . "							
								+ 		"?sensor rdf:type sot:Camera . "
								
								+ 		"?detection sot:detectsPerson ?entity . "
								+		"?entity rdf:type sot:UnknownPerson . "
								
								+		"?entity sot:hasSpatialContext ?spatialContext . "
								+ 		"?spatialContext sot:definesAbsoluteDistance ?distance . "
								+		"?entity sot:hasXGridSpace ?xGridSpace . "
								+ 		"?xGridSpace sot:positionedInXGrid ?xGridPosition . "
								
								+ 		"?detection sot:detectsSemanticSpace ?semanticSpace . "
								+ 		"?semanticSpace rdf:type sot:SemanticSpace . "
								+ 		"?semanticSpace rdfs:label ?semanticLabel . "
								;						
					
					}
					else{
						queryString = queryString 
								
								+ 	"SELECT DISTINCT ?detection ?timestamp ?entity ?distance ?xGridPosition ?semanticLabel "
								+ 	"WHERE {"
								+ 		"?detection a sot:Detection . "
								+ 		"?detection sot:hasTimestamp ?timestamp . " 
								+ 		"?detection sot:providedBy ?sensor . "							
								+ 		"?sensor rdf:type sot:Camera . "
								
								+ 		"?detection sot:detectsPerson ?entity . "
								+		"?entity rdf:type sot:KnownPerson . "
								+ 		"?entity rdfs:label " + "\"" + entityName + "\"^^xsd:string . "	
								
								+		"?entity sot:hasSpatialContext ?spatialContext . "
								+ 		"?spatialContext sot:definesAbsoluteDistance ?distance . "
								+		"?entity sot:hasXGridSpace ?xGridSpace . "
								+ 		"?xGridSpace sot:positionedInXGrid ?xGridPosition . "
								
								+ 		"?detection sot:detectsSemanticSpace ?semanticSpace . "
								+ 		"?semanticSpace rdf:type sot:SemanticSpace . "
								+ 		"?semanticSpace rdfs:label ?semanticLabel . "
								;						
					}					
					
				}
				else{
					System.out.println("No valid sensor.");
					return null;
				}
			}
			
			
			queryString = queryString
					+ 	"} "
					+ 	"ORDER BY DESC(?timestamp) "
					+ 	"LIMIT 1 "	
					;
			
//			System.out.println("QUERY NEW ATTEMPT: \n" + queryString);
			
			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult queryResult = tupleQuery.evaluate();
/*			
			HashMap<String, String> results = new HashMap<String, String>();
			while (queryResult.hasNext()) { // iterate over the result
				BindingSet bindingSet = queryResult.next();	
				Value detection = bindingSet.getValue("detection");
				Value timestamp = bindingSet.getValue("timestamp");				
				results.put(detection.toString(), timestamp.toString());
			}
*/		
//			if (results.size() != 0){
//				for (Map.Entry<String, String> entry : results.entrySet()) {
//					System.out.println("Detection from results: " + entry.getKey() + "\t" + "Timestamp from results: " + entry.getValue().toString());
//				}
//			}
			
			HashMap<String, String> bindingResult = new HashMap<String, String>();
			 
			while (queryResult.hasNext()) { // iterate over the result
				BindingSet bindingSet = queryResult.next();
				Set<String> bindingNames = bindingSet.getBindingNames();
				for (String bindingName: bindingNames)
					bindingResult.put(bindingName, bindingSet.getValue(bindingName).toString());
			}
			
			return bindingResult;
		}			
	}

}
