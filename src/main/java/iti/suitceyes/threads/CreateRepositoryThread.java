package iti.suitceyes.threads;
import java.io.IOException;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;

import iti.suitceyes.communication.Messaging;
import iti.suitceyes.ontology.CreateRemoteRepository;

public class CreateRepositoryThread implements Runnable {
	
	private static final String repositoryID = Messaging.repositoryID;

	@Override
	public void run(){
		
		CreateRemoteRepository repository = null;
		
		try {
			repository = new CreateRemoteRepository(repositoryID);
			repository.uploadDataToRemoteRepository();
			
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			e.printStackTrace();
		}
		
	}
}
