package leshan.server.clienttest.bootstrap.normal.bootstrap;

import leshan.server.LeshanMain;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BootstrapTest {

	private LeshanMain server;

	@Before
	public void setup() {
		server = new LeshanMain();
        server.start();
	}
	
	@Test
	public void clientInitiatedTest() {
		// Start client -- possibly in setup()?
		
		// call bootstrap endpoint on server from client
		
		// process results
		
		// ???
		
		// profit!
	}

	@After
	public void tearDown() {
		server.stop();
	}
	
}
