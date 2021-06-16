package iti.suitceyes.ontology;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.RDFParserHelper;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

public class TestOntology {

	public static void main(String[] args) throws RDFParseException, RDFHandlerException, IOException {
		
		// Instantiate a local repository manager and initialize it
//		RepositoryManager repositoryManager = new LocalRepositoryManager(new File("."));
//		repositoryManager.initialize();
		RepositoryManager repositoryManager =
		        new RemoteRepositoryManager( "http://beaware-server.mklab.iti.gr:7200" );
				//new RemoteRepositoryManager( "https://graphdb.certh.strdi.me:7200" );
		repositoryManager.initialize();

		// Instantiate a repository graph model
		TreeModel graph = new TreeModel();

		// Read repository configuration file
//		InputStream config = EmbeddedGraphDB.class.getResourceAsStream("/repo-defaults.ttl");
		InputStream config = RDFParserHelper.class.getResourceAsStream("\\repo-defaults.ttl");
		RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
		rdfParser.setRDFHandler(new StatementCollector(graph));
		rdfParser.parse(config, RepositoryConfigSchema.NAMESPACE);
		config.close();

		// Retrieve the repository node as a resource
//		Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
		Resource repositoryNode = graph.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY).subjects().iterator().next();

		Model model = graph.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
		Iterator<Statement> iterator = model.iterator(); 
		if (!iterator.hasNext()) 
		   throw new RuntimeException("Oops, no <http://www.openrdf.org/config/repository#> subject found!");
		Statement statement = iterator.next();
		Resource repositoryNodeXXNEW =  statement.getSubject();
		

		// Create a repository configuration object and add it to the repositoryManager
		RepositoryConfig repositoryConfig = RepositoryConfig.create(graph, repositoryNode);
		repositoryManager.addRepositoryConfig(repositoryConfig);

		// Get the repository from repository manager, note the repository id set in configuration .ttl file
		Repository repository = repositoryManager.getRepository("graphdb-repo");

		// Open a connection to this repository
		RepositoryConnection repositoryConnection = repository.getConnection();

		// ... use the repository

		// Shutdown connection, repository and manager
		repositoryConnection.close();
		repository.shutDown();
		repositoryManager.shutDown();

	}

}
