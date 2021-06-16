package iti.suitceyes.ontology;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import iti.suitceyes.communication.MyThreadServeMessage;

public class Inference {

	public static void runInferenceSceneDetection(RemoteGraphDB remoteOntology) throws IOException {

		/* Q: Where am I located now? */

		// get latest timestamp
		String maxtimestamp = remoteOntology.getTimestampOfLatestSceneDetection_URI();
		// execute select to find the latest detected semantic space
		String semanticSpace = remoteOntology.getDetectedSemanticSpace(maxtimestamp);
//		System.out.println("-- Latest scene detection: \t" + semanticSpace);
		MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp(
				"- Latest scene detection: " + semanticSpace
				);
	}

	public static void runInferenceEntityTypeDetection(RemoteGraphDB remoteOntology, String sensor) throws IOException {

		/* Q: What type of entities (person/object) were detected? */
		// get latest timestamp
//		String maxtimestamp = remoteOntology.getTimestampOfLatestEntityDetection_URI(sensor);
		String maxtimestamp = remoteOntology.getTimestampOfLatestDetectionFromSensor_URI(sensor);

		if (maxtimestamp != null){
			// detected entities in HashMap<String, Integer>
//			HashMap <String, Integer> detectedEntities = remoteOntology.getDetectedEntities(maxtimestamp);
			remoteOntology.getDetectedEntities(maxtimestamp, sensor);
		}
		else{
			if (sensor.contains("Camera")){
//				System.out.println("- There is no entity available in latest detection.");
				MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLineWithTimestamp(
						"- There is no entity available in latest detection."
						);
				
			}
			else if (sensor.contains("iBeacon")){
//				System.out.println("- There is no entity available in latest detection.");
				MyThreadServeMessage.logOntologyPopulationAndInferenceIB.appendNewLineWithTimestamp(
						"- There is no entity available in latest detection."
						);
				
			}
		}
//		else
//////			System.out.println("- There is no entity available in latest detection.");
//			MyThreadServeMessage.logOntologyPopulationAndInference.appendNewLineWithTimestamp(
//					"- There is no entity available in latest detection."
//					);

	}

	public static Map<String, String> runInferenceFindEntity(RemoteGraphDB remoteOntology, String entity) throws IOException {

		/* Q: Where is my <>? */
//		String maxtimestamp = remoteOntology.getTimestampOfLatestEntityDetection_URI(sensor);
		Map<String,String> queryAnswers;
		queryAnswers = remoteOntology.findEntityVA(entity);
//		System.out.println(queryAnswers);
//
//		if (queryAnswers.get("answer").equals("not found")){
//			System.out.println(entity + " not found.");
//		}
//		else{
//			System.out.println("Inference answer: " + queryAnswers.get("answer"));
//		}

		return queryAnswers;

	}

	public static String runInferenceFindLatestDetectionOfEntity(RemoteGraphDB remoteOntology, String entity) throws IOException {

		/* Q: Where is my <>? */
//		String maxtimestamp = remoteOntology.getTimestampOfLatestEntityDetection_URI(sensor);
		String interfaceQAnswer;
		interfaceQAnswer = remoteOntology.findLatestDetectionOfEntityVA(entity);
//		System.out.println("Interface answer: " + interfaceQAnswer);

		return interfaceQAnswer;

	}

	public static Map<String, String> runInferenceFindUserLocation(RemoteGraphDB remoteOntology) throws IOException {

		/* Q: Where is my <>? */
//		String maxtimestamp = remoteOntology.getTimestampOfLatestEntityDetection_URI(sensor);
		Map<String,String> queryAnswers = new HashMap<String,String>();
		queryAnswers = remoteOntology.findUserLocation();

		return queryAnswers;

	}


	public static void runInferenceEntitiesFarFromUser(RemoteGraphDB remoteOntology, Float distanceThreshold, String sensor) throws IOException {
		/* Q: What type of entities (person/object) were detected in a distance more than the distance threshold, i.e. most far? */
		
		// get latest timestamp
//		String maxtimestamp = remoteOntology.getTimestampOfLatestEntityDetection_URI(sensor);
		String maxtimestamp = remoteOntology.getTimestampOfLatestDetectionFromSensor_URI(sensor);
		if (maxtimestamp != null)
			remoteOntology.getDetectedEntitiesAndDistance(maxtimestamp, distanceThreshold, sensor, "more");
		else{
			if (sensor.contains("Camera")){
//				System.out.println("-  There is no entity available in latest detection.");
				MyThreadServeMessage.logOntologyPopulationAndInferenceVA.appendNewLine(
						"- There is no entity available in latest detection."
						);
				
			}
			else if (sensor.contains("iBeacon")){
//				System.out.println("-  There is no entity available in latest detection.");
				MyThreadServeMessage.logOntologyPopulationAndInferenceIB.appendNewLine(
						"- There is no entity available in latest detection."
						);
				
			}
////			System.out.println("-  There is no entity available in latest detection.");
//			MyThreadServeMessage.logOntologyPopulationAndInference.appendNewLine(
//					"- There is no entity available in latest detection."
//					);
			
		}

	}
	
//	public static TupleQueryResult findLatestDetectionOfSpecificEntity(RemoteGraphDB remoteOntology, String sensor, String entity){
		public static void findLatestDetectionOfSpecificEntity(RemoteGraphDB remoteOntology, String sensor, String entityName){
			TupleQueryResult queryResult = null;
			
//			if (sensor.equals("VA"))
//				queryResult = remoteOntology.getLatestDetectionOfVAForSpecificEntity(entityName);
//			else if (sensor.equals("IB"))
	
				queryResult = remoteOntology.getLatestDetectionOfIBForSpecificEntity(entityName);
	
				
		
//			if (queryResult.hasNext())
//				System.out.println("result " + sensor + " has NEXT");
//			else
//				System.out.println("result " + sensor + " does not have NEXT");
			
			while (queryResult.hasNext()) { // iterate over the result
				BindingSet bindingSet = queryResult.next();
	
				Value detection = bindingSet.getValue("detection");
				Value timestamp = bindingSet.getValue("timestamp");
				System.out.println("Results || detection: " + detection.stringValue() + ", timestamp: " + timestamp.stringValue());
			}
			
//			return queryResult;
			
		
	}

		
			
		public static HashMap<String, String> runInferenceIsEntityFoundFromSensor(RemoteGraphDB remoteOntology, String sensor, String entityName, String entityType) {
			
			HashMap<String, String> results = remoteOntology.getLatestDetectionOfSpecificEntityFromSensor(sensor, entityName, entityType);

			return results;
			
			
		}

}
