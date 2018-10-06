package client;

import utilities.Email;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Fabio Somaglia on 09/11/17.
 */

/**
 * Modello del Client.
 */
public class ModelClient extends Observable {

    private List<Email> emailRicevuteList;
    private List<Email> emailInviateList;
    private SortedListModel<Email> emailRicevuteSortedListModel;
    private SortedListModel<Email> emailInviateSortedListModel;
    private int scelta;
    private String emailUtente;
	public static final int PORT_NUM = 1234;
	private Socket socket = null;
	private ObjectOutputStream outStream = null;
	private ObjectInputStream inStream = null;

	/**
	 * Costruttore della classe.
	 *
	 * @param emailUtente: email dell'utente che sta utilizzando il Client
	 */
    public ModelClient(String emailUtente) {
		this.emailUtente = emailUtente;
        emailRicevuteList = new ArrayList<>();
        emailInviateList = new ArrayList<>();
        emailRicevuteSortedListModel = new SortedListModel<>(emailRicevuteList);
        emailInviateSortedListModel = new SortedListModel<>(emailInviateList);
        scelta = 0;
        connectionToServer();
        openStreams(emailUtente);
        //thread che gestisce la ricezione delle email
		Runnable runnable = new ClientThread(this, inStream);
		Thread thread = new Thread(runnable);
		thread.start();
    }

	/**
	 * Metodo utilizzato per connettersi col Server.
	 */
	public void connectionToServer() {
		InetAddress IPAddress = null;

		try {
			IPAddress = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			System.out.println("Client Email in attesa del Server...");
			socket = new Socket(IPAddress, PORT_NUM);
			System.out.println("Connesso col Server al socket " + socket);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Errore del Client");
		}
	}

	/**
	 * Metodo utilizzato per aprire gli Stream per trasferire e ricevere.
	 *
	 * @param utente: email utente che viene inviato al Server per autenticarsi
	 */
	public void openStreams(String utente) {
		try{
			//Apro lo stream di dati in ricezione/invio dal client
			outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());
		} catch(IOException e) {
			System.out.println("Errore nell'apertura degli Stream in ModelClient");
		}

		//Invio l'email dell'utente per far selezionare al server le email da inviare
		try {
			outStream.writeObject(utente);
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metodo utilizzato per chiudere Stream e Socket.
	 */
	public void closeConnectionToServer() {
		if (socket != null) {
			try {
				if (inStream != null) {
					inStream.close();
				}
				if (outStream != null) {
					outStream.close();
				}
				socket.close();
				System.out.println("Connessione terminata");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Errore nella chiusura del Socket");
			}
		}
	}

	/**
	 * Metodo che restituisce l'email dell'utente.
	 *
	 * @return l'email dell'utente
	 */
	public String getEmailUtente() {
		return emailUtente;
	}

	/**
	 * Metodo utilizzato per inviare l'email.
	 *
	 * @param email: email da inviare
	 */
	public void inviaEmail(Email email) {
		try {
			outStream.writeObject(email);
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metodo utilizzato quando si vuole cancellare un'email.
	 *
	 * @param id: id dell'email da cancellare
	 */
	public void cancellaEmail(UUID id) {
		try {
			outStream.writeObject(id);
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metodo eseguito ogni volta che si seleziona un'email ricevuta diversa dalla JList e tramite pattern Observe Observable viene restituita alla View.
	 *
	 * @param scelta: l'indice dell'email ricevuta selezionata nella JList
	 */
	public void setSceltaEmailRicevute(int scelta) {
        this.scelta = scelta;
        setChanged();
        notifyObservers(getEmailRicevuteSelezionata());
    }

	/**
	 * Metodo eseguito ogni volta che si seleziona un'email inviata diversa dalla JList e tramite pattern Observe Observable viene restituita alla View.
	 *
	 * @param scelta: l'indice dell'email inviata selezionata nella JList
	 */
    public void setSceltaEmailInviate(int scelta) {
        this.scelta = scelta;
        setChanged();
        notifyObservers(getEmailInviateSelezionata());
    }

	/**
	 * Metodo di supporto a setSceltaEmailRicevute() per mostrare l'email ricevuta selezionata.
	 *
	 * @return l'email di indice scelta salvata in SortedListModel
	 */
	private Email getEmailRicevuteSelezionata() {
        return emailRicevuteSortedListModel.getElementAt(scelta);
    }

	/**
	 * Metodo di supporto a setSceltaEmailInviate() per mostrare l'email inviata selezionata.
	 *
	 * @return l'email di indice scelta salvata in SortedListModel
	 */
    private Email getEmailInviateSelezionata() {
        return emailInviateSortedListModel.getElementAt(scelta);
    }

	/**
	 * Metodo utilizzato per settare il modello della JList.
	 *
	 * @return l'oggetto SortedListModel utile per settare i modello nel ControllerClientEmail
	 */
	public SortedListModel<Email> getEmailRicevuteSortedListModel() {
        return emailRicevuteSortedListModel;
    }

	/**
	 * Metodo utilizzato per riempire di email ricevute la List quando viene aperto il client.
	 *
	 * @param emailRicevute: array di email ricevute ricevute dal Server
	 */
	public void addAllEmailRicevuteSortedListModel(Email[] emailRicevute) {
    	emailRicevuteSortedListModel.addAll(emailRicevute);
		emailRicevuteList.sort(Collections.reverseOrder()); //per ordinare la lista delle email in ordine di data discendente
	}

	/**
	 * Metodo utilizzato per aggiungere una nuova email ricevuta appena ricevuta dal Server, viene notificata alla View in modo da evocare una finestra di dialogo.
	 *
	 * @param email: email ricevuta dal Server
	 */
	public void addEmailRicevuteSortedListModel(Email email) {
		emailRicevuteSortedListModel.add(email);
		emailRicevuteList.sort(Collections.reverseOrder()); //per ordinare la lista delle email in ordine di data discendente

		//notifico via pattern Observe Observable alla View mittente e oggetto dell'email per far comparire una finestra di dialogo alla ricezione di un'email
		String[] mittenteOggettoEmail = new String[2];
		mittenteOggettoEmail[0] = email.getMittente();
		mittenteOggettoEmail[1] = email.getOggetto();
		setChanged();
		notifyObservers(mittenteOggettoEmail);
	}

	/**
	 * Metodo utilizzato per settare il modello della JList.
	 *
	 * @return l'oggetto SortedListModel utile per settare i modello nel ControllerClientEmail
	 */
    public SortedListModel<Email> getEmailInviateSortedListModel() {
        return emailInviateSortedListModel;
    }

	/**
	 * Metodo utilizzato per riempire di email inviate la List quando viene aperto il client.
	 *
	 * @param emailInviate: array di email inviate ricevute dal Server
	 */
	public void addAllEmailInviateSortedListModel(Email[] emailInviate) {
		emailInviateSortedListModel.addAll(emailInviate);
		emailInviateList.sort(Collections.reverseOrder()); //per ordinare la lista delle email in ordine di data discendente
	}

	/**
	 * Metodo utilizzato per aggiungere una nuova email inviata appena ricevuta dal Server.
	 *
	 * @param email: email ricevuta dal Server
	 */
	public void addEmailInviateSortedListModel(Email email) {
		emailInviateSortedListModel.add(email);
		emailInviateList.sort(Collections.reverseOrder()); //per ordinare la lista delle email in ordine di data discendente
	}

    public List<Email> getEmailRicevuteList() {
        return emailRicevuteList;
    }

    public List<Email> getEmailInviateList() {
        return emailInviateList;
    }
}

/**
 * Thread che si occupa di ricevere nuove email dal Server.
 */
class ClientThread implements Runnable {

	private ModelClient modelClient;
	private ObjectInputStream inStream;

	/**
	 * Costruttore della classe.
	 *
	 * @param modelClient: oggetto ModelClient (modello del Client)
	 * @param inStream: stream di input
	 */
	public ClientThread(ModelClient modelClient, ObjectInputStream inStream) {
		this.modelClient = modelClient;
		this.inStream = inStream;
	}

	/**
	 * Metodo che viene eseguito dal thread.
	 */
	@Override
	public void run() {
		try {
			while (true) {
				Object o = inStream.readObject(); //continua a ricevere dal Server
				switch (o.getClass().getSimpleName()) { //differenzia quello che è stato ricevuto e si comporta di conseguenza
					case "EmailRicevuta[]": //entra qui quando riceve l'array di email ricevute quando viene aperto il Client per popolare la casella
						modelClient.addAllEmailRicevuteSortedListModel((Email[]) o);
						break;
					case "EmailInviata[]": //entra qui quando riceve l'array di email inviate quando viene aperto il Client per popolare la casella
						modelClient.addAllEmailInviateSortedListModel((Email[]) o);
						break;
					case "EmailRicevuta": //entra qui quando riceve un'email ricevuta
						modelClient.addEmailRicevuteSortedListModel((Email) o);
						break;
					case "EmailInviata": //entra qui quando riceve un'email inviata
						modelClient.addEmailInviateSortedListModel((Email) o);
						break;
					default:
						break;
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Client disconnesso dal Server");
		} finally { //se perde la connessione col Server
			System.exit(0);
		}
	}

}