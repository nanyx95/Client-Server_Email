package server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Fabio Somaglia on 16/12/17.
 */

/**
 * Controller del Server.
 */
public class ControllerServer {

	private ModelServer modelServer;
	private Server server; //View

	/**
	 * Costruttore della classe con listener.
	 *
	 * @param modelServer: oggetto modello del Server
	 * @param server: oggetto View del Server
	 */
	public ControllerServer(ModelServer modelServer, Server server) {
		this.modelServer = modelServer;
		this.server = server;

		//listener eseguito quando chiudo il Server
		server.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0); //termina l'esecuzione
			}
		});
	}

}