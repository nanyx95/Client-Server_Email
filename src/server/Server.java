package server;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Fabio Somaglia on 16/12/17.
 */

/**
 * View del Server.
 */
public class Server extends JFrame implements Observer { //View

	private JPanel serverPanel;
	private JLabel serverLabel;
	private JTextArea logTextArea;

	/**
	 * Costruttore della classe.
	 */
	public Server() {
		setTitle("Server Email");
		add(serverPanel);
		pack();
		setVisible(true);
		//setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	public JTextArea getLogTextArea() {
		return logTextArea;
	}

	/**
	 * Metodo Observer eseguito quando avvengono dei cambiamenti nella classe Observable. Viene utilizzato per scrivere i log nella View del Server.
	 *
	 * @param o: oggetto Observable
	 * @param arg: parametro supplementare utilizzato per passare oggetti Stringa dal ModelServer alla View
	 */
	@Override
	public void update(Observable o, Object arg) {
		//System.out.println("sono in update server");
		if (arg != null) {
			String s = (String) arg;
			logTextArea.append("- " + s + "\n\n");
		}
	}

	public static void main(String[] args) {
		System.setProperty("java.security.policy","file:src/server/server.policy"); //setto il file delle policy di sicurezza per farlo leggere dal SecurityManager
		if (System.getSecurityManager() == null) //creo il SecurityManager, se non esiste già
			System.setSecurityManager(new SecurityManager());

		ModelServer modelServer = new ModelServer();
		Thread modelServerThread = new Thread(modelServer);
		modelServerThread.start();

		Server server = new Server();
		ControllerServer controllerServer = new ControllerServer(modelServer, server);
		modelServer.addObserver(server);
	}

}