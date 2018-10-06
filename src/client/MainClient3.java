package client;

/**
 * Created by Fabio Somaglia on 04/01/18.
 */

/**
 * Main del Client test3@email.com
 */
public class MainClient3 {

	public static void main(String[] args) {
		ModelClient modelClient3 = new ModelClient("test3@email.com");
		ClientEmail clientEmail3 = new ClientEmail();
		ControllerClientEmail controllerClientEmail3 = new ControllerClientEmail(modelClient3, clientEmail3);
		modelClient3.addObserver(clientEmail3);
	}

}