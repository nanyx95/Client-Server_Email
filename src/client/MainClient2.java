package client;

/**
 * Created by Fabio Somaglia on 04/01/18.
 */

/**
 * Main del Client test2@email.com
 */
public class MainClient2 {

	public static void main(String[] args) {
		ModelClient modelClient2 = new ModelClient("test2@email.com");
		ClientEmail clientEmail2 = new ClientEmail();
		ControllerClientEmail controllerClientEmail2 = new ControllerClientEmail(modelClient2, clientEmail2);
		modelClient2.addObserver(clientEmail2);
	}

}