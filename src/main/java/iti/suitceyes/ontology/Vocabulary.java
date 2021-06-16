package iti.suitceyes.ontology;

import java.util.HashMap;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Vocabulary constants for the 'http://suitceyes.+++/' namespace.
 */

public class Vocabulary {

//		public static final String NAMESPACE_TBOX = "http://160.40.51.22/mklab_ontologies/SUITCEYES/suitceyes_tbox#";
	public static final String NAMESPACE_TBOX = "http://localhost/resources/suitceyes_tbox#";

	//public static final String SERVER_URL = "http://160.40.49.112:7200";
//	public static final String SERVER_URL = "https://graphdb.certh.strdi.me:443";
	public static final String SERVER_URL = "http://160.40.49.192:89";
//	public static final String SERVER_URL = "http://172.24.0.2:89";

	public static final String PREFIX_TBOX = "sot";

	//public static final String SPARQL_ENDPOINT_SELECT = "http://160.40.49.112:7200/repositories/mklab-suitceyes-kb";
	//public static final String SPARQL_ENDPOINT_UPDATE = "http://160.40.49.112:7200/repositories/mklab-suitceyes-kb/statements";
//	public static final String SPARQL_ENDPOINT_SELECT = "https://graphdb.certh.strdi.me/repositories/mklab-suitceyes-kb";
//	public static final String SPARQL_ENDPOINT_UPDATE = "https://graphdb.certh.strdi.me/repositories/mklab-suitceyes-kb/statements";
	public static final String SPARQL_ENDPOINT_SELECT = "http://160.40.49.192:89/repositories/mklab-suitceyes-kb";
	public static final String SPARQL_ENDPOINT_UPDATE = "http://160.40.49.192:89/repositories/mklab-suitceyes-kb/statements";
//	public static final String SPARQL_ENDPOINT_SELECT = "http://172.24.0.2:89/repositories/mklab-suitceyes-kb";
//	public static final String SPARQL_ENDPOINT_UPDATE = "http://172.24.0.2:89/repositories/mklab-suitceyes-kb/statements";
	
	public static final String SOT_OBJECT = NAMESPACE_TBOX + "Object";
	public static final String SOT_SEMANTICSPACE = NAMESPACE_TBOX + "SemanticSpace";
	
	public static final String PREFIXES_ALL = 
			"PREFIX sot: <" + NAMESPACE_TBOX + "> " +
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + 
			"PREFIX seas: <https://w3id.org/seas/> " +
			"PREFIX dem: <http://www.demcare.eu/ontologies/event.owl#> " 
			;
					
	
	private static final HashMap<String, String> mappingSceneTypeWithLabel = 
			new HashMap<String, String>() {{
				put("living_room", "living room");
				put("kitchen", "kitchen");
				put("bathroom", "bathroom");
				put("basement", "basement");
				put("bedroom", "bedroom");
				put("childs_room", "childs room");
				put("closet", "closet");
				put("corridor", "corridor");
				put("dining_room", "dining room");
				put("elevator", "elevator");
				put("laundromat", "laundromat");
				put("office", "office");
				put("pantry", "pantry");
				put("shower", "shower");
				put("staircase", "staircase");
				put("scene_unknown", "unknown");
				}};

	private static final HashMap<String, String> mappingEntityTypeWithLabel =
			new HashMap<String, String>() {{
				put("door", "door");
				put("bottle", "bottle");
				put("drawer", "drawer");
				put("chair", "chair");
				put("window", "window");
				put("table", "table");
				put("mug", "mug");
				put("backpack", "backpack");
			}};
	
	private static final HashMap<String, String> mappingSceneTypeWithOntologyClass = 
			new HashMap<String, String>() {{
				put("living_room", "https://w3id.org/seas/LivingRoom");
				put("kitchen", "https://w3id.org/seas/Kitchen");
				put("bathroom", "https://w3id.org/seas/Bathroom");
				put("basement", "https://w3id.org/seas/Basement");
				put("bedroom", "https://w3id.org/seas/Bedroom");
				put("childs_room", SOT_SEMANTICSPACE); //??
				put("closet", SOT_SEMANTICSPACE); //??
				put("corridor", "https://w3id.org/seas/Corridor");
				put("dining_room", "https://w3id.org/seas/DiningRoom");
				put("elevator", "https://w3id.org/seas/Elevator");
				put("laundromat", "https://w3id.org/seas/Laundry");
				put("office", "https://w3id.org/seas/Office");
				put("pantry", SOT_SEMANTICSPACE); //??
				put("shower", "https://w3id.org/seas/Bathroom"); //??
				put("staircase", "https://w3id.org/seas/Stairs");
				put("scene_unknown", SOT_SEMANTICSPACE);
				}};
				
	private static final HashMap<String, String> mappingObjectTypeWithOntologyClass = 
			new HashMap<String, String>() {{
				//for VA
				put("table", "http://www.demcare.eu/ontologies/event.owl#Table"); //*******NOTE: labels also exist in each object in the ontology
				put("book", "http://www.demcare.eu/ontologies/event.owl#Book");
				put("tv", "http://www.demcare.eu/ontologies/event.owl#TV");
				put("tv_remote", "http://www.demcare.eu/ontologies/event.owl#RemoteControl");
				put("container", SOT_OBJECT);
				put("mug_cup", "http://www.demcare.eu/ontologies/event.owl#Cup");
				put("laptop", NAMESPACE_TBOX + "Laptop");
				put("cup", NAMESPACE_TBOX + "Cup");
				put("mug", NAMESPACE_TBOX + "Mug");
				put("computer", NAMESPACE_TBOX + "Computer");
				put("drawer", NAMESPACE_TBOX + "Drawer");
				put("bottle", NAMESPACE_TBOX + "Bottle");
				put("door", NAMESPACE_TBOX + "Door");
				put("backpack", NAMESPACE_TBOX + "backpack");
				put("cell_phone", "http://www.demcare.eu/ontologies/event.owl#Telephone");
				put("window", "http://www.demcare.eu/ontologies/event.owl#Window");
				//for iBeacon
				put("room_door", "http://www.demcare.eu/ontologies/event.owl#Door");
//				put("computer", "http://160.40.51.22/mklab_ontologies/SUITCEYES/suitceyes_tbox#Computer");
				put("computer", NAMESPACE_TBOX + "Computer");
				put("chair", "http://www.demcare.eu/ontologies/event.owl#Chair");
				put("mug", "http://www.demcare.eu/ontologies/event.owl#Cup");
				put("phone", "http://www.demcare.eu/ontologies/event.owl#Telephone");
				
				}};
				
	private static final HashMap<String, String> mappingPersonTypeWithOntologyClass = 
			new HashMap<String, String>() {{
//				put("person", NAMESPACE_TBOX + "Person");
				put("person_unknown", NAMESPACE_TBOX + "UnknownPerson");
				put("person_xyz", NAMESPACE_TBOX + "KnownPerson");
				//put("Elias Kouslis", NAMESPACE_TBOX + "KnownPerson");
				}};
				
				
	public static boolean isObjectContainedInHashMap (String objectType){
		if (mappingObjectTypeWithOntologyClass.containsKey(objectType))
			return true;
		else
			return false;		
	}


	public static String getLabelOfSemanticSpaceConcept(String sceneType) {
		
		return mappingSceneTypeWithLabel.get(sceneType);
	}

	public static String getLabelOfSemanticEntityConcept(String entity) {

		return mappingEntityTypeWithLabel.get(entity);
	}
	
	
	public static String getClassURIOfSceneType(String sceneType){
	
		return mappingSceneTypeWithOntologyClass.get(sceneType);
	
	}
	
	public static String getClassURIOfObjectType(String objectType){
		
		return mappingObjectTypeWithOntologyClass.get(objectType);
	}
	
	
	public static final IRI ARTIST = getIRI("Artist");
	/**
	 * Creates a new {@link IRI} with this vocabulary's namespace for the given local name.
	 *
	 * @param localName a local name of an IRI, e.g. 'creatorOf', 'name', 'Artist', etc.
	 * @return an IRI using the http://example.org/ namespace and the given local name.
	 */
	private static IRI getIRI(String localName) {
		return SimpleValueFactory.getInstance().createIRI(NAMESPACE_TBOX, localName);
	}
	
	public static IRI createSuitceyesIRI(ValueFactory factory, String localName) {
		return factory.createIRI(NAMESPACE_TBOX, localName);
	}


}
