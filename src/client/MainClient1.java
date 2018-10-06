package client;

/**
 * Created by Fabio Somaglia on 04/01/18.
 */

/**
 * Main del Client test1@email.com
 */
public class MainClient1 {

	public static void main(String[] args) {
		ModelClient modelClient1 = new ModelClient("test1@email.com");
		ClientEmail clientEmail1 = new ClientEmail();
		ControllerClientEmail controllerClientEmail1 = new ControllerClientEmail(modelClient1, clientEmail1);
		modelClient1.addObserver(clientEmail1);
	}

}