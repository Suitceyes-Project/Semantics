package iti.suitceyes.ontology;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

public class CreateRemoteRepository {
	
	private String repositoryID = "";

	public CreateRemoteRepository(String repositoryID) throws RDFParseException, RDFHandlerException, IOException {
		
		this.repositoryID = repositoryID;
//		Repository repo = new HTTPRepository(Vocabulary.SERVER_URL, repositoryID);
//		repo.getConnection();
//		repo.getDataDir();
//		System.out.println("done");

		Path path = Paths.get(".").toAbsolutePath().normalize();
		String strRepositoryConfig = path.toFile().getAbsolutePath() + "\\src\\main\\resources\\repo-defaults.ttl";
		System.out.println(strRepositoryConfig);
		String strServerUrl = Vocabulary.SERVER_URL;
		System.out.println("ServerURL: " + strServerUrl);
		
	// Instantiate a local repository manager and initialize it
//		RepositoryManager repositoryManager  = RepositoryProvider.getRepositoryManager(strServerUrl);

		RemoteRepositoryManager repositoryManager = RemoteRepositoryManager.getInstance(strServerUrl, "", "");
//		repositoryManager.initialize();
		repositoryManager.getAllRepositories();

		// Instantiate a repository graph model
		TreeModel graph = new TreeModel();

		// Read repository configuration file
		InputStream config = new FileInputStream(strRepositoryConfig);
		RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
		rdfParser.setRDFHandler(new StatementCollector(graph));
		rdfParser.parse(config, RepositoryConfigSchema.NAMESPACE);
		config.close();



		// Retrieve the repository node as a resource
    	Resource repositoryNode =  Models.subject(graph.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY)).orElseThrow(() -> new RuntimeException
    		("Oops, no <http://www.openrdf.org/config/repository#> subject found!"));

		// Create a repository configuration object and add it to the repositoryManager		
		RepositoryConfig repositoryConfig = RepositoryConfig.create(graph, repositoryNode);

		//Change repository ID with respect to the value of the repositoryID variable
		repositoryConfig.setID("mklab-suitceyes-kb-" + repositoryID);
		repositoryManager.addRepositoryConfig(repositoryConfig);

		// Get the repository from repository manager, note the repository id set in configuration .ttl file
//		Repository repository = repositoryManager.getRepository("mklab-suitceyes-kb-" + repositoryID);
		Repository repository = repositoryManager.getRepository(repositoryConfig.getID());

		// Open a connection to this repository
		RepositoryConnection repositoryConnection = repository.getConnection();

		// ... use the repository

		// Shutdown connection, repository and manager
		repositoryConnection.close();
		repository.shutDown();
		repositoryManager.shutDown();	
		
		System.out.println(String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis())) + "] " +
				"Repository named: " + repositoryConfig.getID() + " created..."));

	}
	
	public void uploadDataToRemoteRepository() throws IOException{
		System.out.println("Upload to GraphDB");
		/* curl command */		
		/*
		String curl_command = "curl -X POST -H \"Content-Type:application/x-turtle\" "
		+ "-T \"C:\\Users\\mriga\\TBCFreeWorkspace\\SUITCEYES\\suitceyes_tbox.ttl\" "
		+ "http://160.40.49.112:7200/repositories/mklab-suitceyes-kb-DEFAULT/statements\"";
		*/
			
		Path path = Paths.get(".").toAbsolutePath().normalize();

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials("", "");
		credentialsProvider.setCredentials(AuthScope.ANY, creds);
//	    CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpClient client =
				HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();


		HttpPost httpPost = new HttpPost("");
		String user = "";
		String pwd = "";
		String encoding = Base64.getEncoder().encodeToString((user + ":" + pwd).getBytes());
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
		System.out.println("Executing Request " + httpPost.getRequestLine());
	    
    	String data = new String(Files.readAllBytes(Paths.get(path.toFile().getAbsolutePath() + "\\src\\main\\resources\\" + "suitceyes_abox2.ttl")));
    	
    	HttpEntity entity = new StringEntity(data);
	    httpPost.setEntity(entity);

	    httpPost.setHeader("Accept", "*/*");
	    httpPost.setHeader("Content-type", "application/x-turtle");

	    System.out.println("Connecting to GraphDB");
	    CloseableHttpResponse response = client.execute(httpPost);
		StatusLine sl = response.getStatusLine();
		System.out.println("Request ended with code " + sl.getStatusCode());
		client.close();
	    
	    System.out.println(String.format("[" + (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis())) + "] " +
				"Data uploaded to repository named: " + "mklab-suitceyes-kb-" + this.repositoryID ));


	}

	public static void main(String[] args) throws RDFParseException, RDFHandlerException, IOException, ClientProtocolException, IOException  {
		
		String repositoryID = "DEFAULT";
	
		CreateRemoteRepository repository = new CreateRemoteRepository(repositoryID);
		repository.uploadDataToRemoteRepository();
		

	    

		}

	}


