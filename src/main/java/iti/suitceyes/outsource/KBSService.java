package iti.suitceyes.outsource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/json/product")
public class KBSService {
	
	@GET
	@Path("/get")
	@Produces("application/json")
	public String getProductInJSON() {

		String product = new String();
		product.concat("iPad 3");
		
		return product; 

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
