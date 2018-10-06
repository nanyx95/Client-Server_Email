package server;

import utilities.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Fabio Somaglia on 16/12/17.
 */

/**
 * Model del Server.
 * Classe che si occupa della parte logica del Server.
 */
public class ModelServer extends Observable implements Runnable {

	private static final int PORT_NUM = 1234;
	private CopyOnWriteArrayList<EmailRicevuta> listEmailRicevuta; //ArrayList thread-safe utilizzato per salvare tutte le email che devono ricevere i Client
	private BlockingQueue<String> logBlockingQueue; //LinkedBlockingQueue utilizzata per salvare i log che dovranno essere stampati su UI

	/**
	 * Costruttore della classe.
	 */
	public ModelServer() {
		listEmailRicevuta = new CopyOnWriteArrayList<>();
		logBlockingQueue = new LinkedBlockingQueue<>();
		Runnable runnableConnectionToClient = new ConnectionToClientThread(PORT_NUM, listEmailRicevuta, logBlockingQueue);
		Thread threadConnectionToClient = new Thread(runnableConnectionToClient);
		threadConnectionToClient.start();
	}

	/**
	 * Metodo che si occupa di estrarre dalla coda tutti i log che sono avvenuti durante l'esecuzione e con il pattern
	 * Observe Observable comunicarli alla View.
	 */
	@Override
	public void run() {
		String log;
		while (true) {
			try {
				log = logBlockingQueue.take();

				//notifico alla View il log
				setChanged();
				notifyObservers(log);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

/**
 * Classe che si occupa di connettersi con i Client e creare le relative istanze della classe ServerThread.
 */
class ConnectionToClientThread implements Runnable {

	private int PORT_NUM;
	private CopyOnWriteArrayList<EmailRicevuta> listEmailRicevuta;
	private BlockingQueue<String> logBlockingQueue;
	private IOFile ioFile;

	/**
	 * Costruttore della classe.
	 *
	 * @param PORT_NUM: numero della porta del socket
	 * @param listEmailRicevuta: ArrayList thread-safe dove vengono salvate tutte le email che devono ricevere i Client
	 * @param logBlockingQueue: LinkedBlockingQueue utilizzata per salvare i log che dovranno essere stampati su UI
	 */
	public ConnectionToClientThread(int PORT_NUM, CopyOnWriteArrayList<EmailRicevuta> listEmailRicevuta, BlockingQueue<String> logBlockingQueue) {
		this.PORT_NUM = PORT_NUM;
		this.listEmailRicevuta = listEmailRicevuta;
		this.logBlockingQueue = logBlockingQueue;
		ioFile = new IOFile(); //viene istanziata la classe che si occupa di leggere e scrivere le email su file
	}

	/**
	 * Metodo che si occupa di connettersi con i Client e creare le relative istanze della classe ServerThread.
	 */
	@Override
	public void run() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		InetAddress IPAddress = null;

		try {
			IPAddress = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			System.out.println("Server Email in ascolto sulla porta " + PORT_NUM + "...");
			serverSocket = new ServerSocket(PORT_NUM, 0, IPAddress);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Errore del Server");
		}

		IOFileRegisteredUsers fileRegisteredUsers = new IOFileRegisteredUsers(); //classe per controllare se un indirizzo email è valido

		while (true) {
			try {
				if (serverSocket != null) {
					socket = serverSocket.accept();
					System.out.println("Connessione stabilita");
					Runnable runnableServerThread = new ServerThread(socket, listEmailRicevuta, logBlockingQueue, fileRegisteredUsers, ioFile);
					Thread threadServer = new Thread(runnableServerThread);
					threadServer.start();

					//aggiungo alla BlockingQueue che un'utente si è connesso
					//logBlockingQueue.put(threadServer.getName() + " si è connesso al Server.");
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Errore nella connessione col Client");
			}
		}
	}

}

/**
 * Classe che si occupa di gestire le operazioni effettuate dal Client, più precisamente invio, ricezione e cancellazione email.
 */
class ServerThread implements Runnable {

	private Socket socket;
	private ObjectInputStream inStream = null;
	private ObjectOutputStream outStream = null;
	private CopyOnWriteArrayList<EmailRicevuta> listEmailRicevuta;
	private BlockingQueue<String> logBlockingQueue;
	private IOFileRegisteredUsers fileRegisteredUsers;
	private AtomicBoolean stopThread; //variabile che serve a terminare il loop infinito dei thread che gestiscono le richieste di un Client che però si è disconnesso
	private IOFile ioFile;

	/**
	 * Costruttore della classe.
	 *
	 * @param socket: socket creato con il Client
	 * @param listEmailRicevuta: ArrayList thread-safe dove vengono salvate tutte le email che devono ricevere i Client
	 * @param logBlockingQueue: LinkedBlockingQueue utilizzata per salvare i log che dovranno essere stampati su UI
	 * @param fileRegisteredUsers: classe che si occupa di salvare gli utenti registrati su file per verificare la correttezza dei destinatari nelle email
	 * @param ioFile: classe che si occupa di scrivere e leggere le email su file
	 */
	public ServerThread(Socket socket, CopyOnWriteArrayList<EmailRicevuta> listEmailRicevuta, BlockingQueue<String> logBlockingQueue, IOFileRegisteredUsers fileRegisteredUsers, IOFile ioFile){
		this.socket = socket;
		this.listEmailRicevuta = listEmailRicevuta;
		this.logBlockingQueue = logBlockingQueue;
		this.fileRegisteredUsers = fileRegisteredUsers;
		stopThread = new AtomicBoolean(false);
		this.ioFile = ioFile;
	}

	/**
	 * Riceve l'email dell'utente che si è collegato, invia al client le email inviate e ricevute e resta in attesa di nuovo email o email da cancellare.
	 */
	@Override
	public void run() {
		String emailUtente = null;

		try{
			//apro lo stream di dati in ricezione/invio dal client
			inStream = new ObjectInputStream(socket.getInputStream());
			outStream = new ObjectOutputStream(socket.getOutputStream());
		} catch(IOException e) {
			System.out.println("Errore nell'apertura degli Stream in ServerThread");
		}

		//ricevo username dell'utente per identificare le mail
		try {
			emailUtente = (String) inStream.readObject();
		} catch (IOException |ClassNotFoundException e) {
			e.printStackTrace();
		}

		//aggiungo alla BlockingQueue il nome dell'utente connesso
		try {
			logBlockingQueue.put(emailUtente + ": si è connesso.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ioFile.createFileIfNotExist(emailUtente); //creo, se non dovesse esistere già, il file dove verranno salvate le email
		fileRegisteredUsers.addUserToFile(emailUtente); //aggiungo username al file degli utenti registrati, se già presente non viene fatto niente

		//creo e eseguo il thread che si occupa di gestire le email che vengono salvate nel CopyOnWriteArrayList
		Runnable runnableCopyOnWriteArrayListThread = new CopyOnWriteArrayListThread(listEmailRicevuta, logBlockingQueue, outStream, emailUtente, stopThread);
		Thread threadArrayList = new Thread(runnableCopyOnWriteArrayListThread);
		threadArrayList.start();

		//invio tutte le email ricevute dell'utente
		try {
			outStream.writeObject(ioFile.leggiEmailRicevute(emailUtente));
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//invio tutte le email inviate dell'utente
		try {
			outStream.writeObject(ioFile.leggiEmailInviate(emailUtente));
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
			Se viene inviata un'email viene ricevuta qui, inviata al mittente sotto forma di email inviata, viene controllata
			la legittimità del destinatario e aggiunta al CopyOnWriteArrayList per futura gestione.
			Nel caso in cui il destinatario non sia presente nel registro degli utenti registrati, viene inviata un'email di errore.
		 */
		try {
			while (true) {
				Object o = inStream.readObject();
				switch (o.getClass().getSimpleName()) {
					case "Email": //nuova email
						outStream.writeObject(ioFile.scriviEmailInviata(emailUtente, (Email) o)); //invio al client l'email che ha appena inviato
						outStream.flush();

						//aggiungo alla BlockingQueue che l'utente ha inviato una nuova email
						try {
							logBlockingQueue.put(emailUtente + ": l'utente ha inviato un'email a " + ((Email) o).getDestinatario() + ".");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						EmailRicevuta emailRicevuta = new EmailRicevuta(UUID.randomUUID(), ((Email) o).getMittente(), ((Email) o).getDestinatario(), ((Email) o).getOggetto(), ((Email) o).getTesto(), ((Email) o).getDataSpedizione(), 'r');

						//controllo che i/il destinatari/o siano corretti, nel caso scrivo l'email sui rispettivi file, altrimenti invio un messaggio di errore
						String[] fieldDestinatario = ((Email) o).getDestinatario().split(",\\s*");
						for (int i = 0; i < fieldDestinatario.length; i++) {
							if (!fileRegisteredUsers.checkUser(fieldDestinatario[i])) { //se il destinatario non esiste
								EmailRicevuta emailErrore = new EmailRicevuta(UUID.randomUUID(), "no-reply@server.it", emailUtente, "Impossibile inviare email", "Salve,\nin quanto l'indirizzo di posta elettronica \"" + fieldDestinatario[i] + "\" non è stato riconosciuto dai nostri Sistemi, l'email non è stata recapitata.\nCi scusiamo per l'incoveniente.", new Date(), 'r');

								ioFile.scriviEmailRicevuta(emailUtente, emailErrore); //scrivo emailErrore sul file di emailUtente
								listEmailRicevuta.add(emailErrore); //invio al client un errore in quanto il destinatario è errato

								//aggiungo alla BlockingQueue che l'email non è stata inviata in quanto il destinatario è inesistente
								try {
									logBlockingQueue.put(emailUtente + ": l'email inviata a " + fieldDestinatario[i] + " non è stata recapitata in quanto il destinatario è inesistente.");
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							} else { //se il destinatario esiste
								ioFile.scriviEmailRicevuta(fieldDestinatario[i], emailRicevuta); //salvo l'email sul file del utente
							}
						}
						listEmailRicevuta.add(emailRicevuta); //aggiungo email al CopyOnWriteArrayList
						break;
					case "UUID": //email da cancellare
						ioFile.cancellaEmail(emailUtente, (UUID) o);

						//aggiungo alla BlockingQueue che l'utente ha cancellato un'email
						try {
							logBlockingQueue.put(emailUtente + ": l'utente ha cancellato un'email con UUID " + o.toString() + ".");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						break;
					default:
						break;
				}

			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Client disconnesso");
		} finally {
			//aggiungo alla BlockingQueue che l'utente si è disconnesso
			try {
				logBlockingQueue.put(emailUtente + ": l'utente si è disconnesso dal Server.");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			stopThread.set(true); //faccio terminare il loop infinito del thread CopyOnWriteArrayListThread in quanto il Client si è disconnesso

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
	}



}

/**
 * Classe che si occupa di gestire le email che vengono salvate nel CopyOnWriteArrayList, ogni email lì presente viene associata all'utente interessato.
 * Quando viene trovata la corrispondenza, viene inviata al Client.
 */
class CopyOnWriteArrayListThread implements Runnable {

	private CopyOnWriteArrayList<EmailRicevuta> listEmailRicevuta;
	private BlockingQueue<String> logBlockingQueue;
	private ObjectOutputStream outStream;
	private String emailUtente;
	private AtomicBoolean stopThread;

	/**
	 * Costruttore del thread.
	 *
	 * @param listEmailRicevuta: ArrayList thread-safe dove vengono salvate tutte le email che devono ricevere i Client
	 * @param logBlockingQueue: LinkedBlockingQueue utilizzata per salvare i log che dovranno essere stampati su UI
	 * @param outStream: stream per invio oggetti al Client
	 * @param emailUtente: email dell'utente per identificazione
	 * @param stopThread: variabile che serve a terminare il loop infinito del while quando un Client si disconnette
	 */
	public CopyOnWriteArrayListThread(CopyOnWriteArrayList<EmailRicevuta> listEmailRicevuta, BlockingQueue<String> logBlockingQueue, ObjectOutputStream outStream, String emailUtente, AtomicBoolean stopThread) {
		this.listEmailRicevuta = listEmailRicevuta;
		this.logBlockingQueue = logBlockingQueue;
		this.outStream = outStream;
		this.emailUtente = emailUtente;
		this.stopThread = stopThread;
	}

	/**
	 * Rimane in attesa di nuove email nel CopyOnWriteArrayList e nel caso di corrispondenza con l'utente, vengono gestite.
	 */
	@Override
	public void run() {
		int i = listEmailRicevuta.size();
		while (!stopThread.get()) {
			if (i == listEmailRicevuta.size() - 1) {
				String[] fieldDestinatario = listEmailRicevuta.get(i).getDestinatario().split(",\\s*");
				Boolean trovatoUser = false;
				for (int j = 0; j < fieldDestinatario.length && !trovatoUser; j++) {
					if (fieldDestinatario[j].equals(emailUtente))
						trovatoUser = true;
				}
				if (trovatoUser) {
					try {
						outStream.writeObject(listEmailRicevuta.get(i)); //il client interessato riceve l'email che è stata inviata
						outStream.flush();

						//aggiungo alla BlockingQueue che l'utente ha ricevuto una nuova email
						logBlockingQueue.put(emailUtente + ": l'utente ha ricevuto una nuova email da " + listEmailRicevuta.get(i).getMittente() + ".");
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
				i++;
			}
		}
	}

}

/**
 * Classe che si occupa di leggere e scrivere le email su file. Per ogni utente viene creato un file personale dove vengono salvate le relative email.
 */
class IOFile {

	/**
	 * Costruttore della classe.
	 */
	public IOFile() {}

	/**
	 * Metodo che crea, se non esiste già, il file dove verranno salvate le email.
	 *
	 * @param emailUtente: email dell'utente
	 */
	public void createFileIfNotExist(String emailUtente) {
		File file = new File("src/server/emails_" + emailUtente + ".txt");

		try {
			if (!file.exists()) //controllo se il file esiste, se non esiste, ne creo uno
				file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Legge nel file dell'utente tutte le email ricevute.
	 *
	 * @param emailUtente: email dell'utente
	 * @return un'array con tutte le email ricevute
	 */
	public EmailRicevuta[] leggiEmailRicevute(String emailUtente) {
		LockRegistry.INSTANCE.acquire(emailUtente, LockRegistry.LockType.READ); //acquisisco il lock (in questo caso di tipo READ) sul file relativo ad emailUtente
		File file = new File("src/server/emails_" + emailUtente + ".txt");
		Scanner sc = null;
		List<EmailRicevuta> emailRicevutaList = new ArrayList<>();

		try {
			sc = new Scanner(file);

			while (sc.hasNextLine()) {
				String string = sc.nextLine();
				if (string.charAt(0) == '▶') {
					while (!string.contains("◀")) {
						string = string + sc.nextLine();
					}
					string = string.substring(string.indexOf("▶") + 1, string.lastIndexOf("◀"));
					String[] field = string.split("♦");
					String[] fieldDestinatario = field[2].split(",\\s*");
					Boolean trovatoUser = false;
					for (int i = 0; i < fieldDestinatario.length && !trovatoUser; i++) {
						if (fieldDestinatario[i].equals(emailUtente) && field[6].charAt(0) == 'r')
							trovatoUser = true;
					}
					if (trovatoUser) {
						try {
							emailRicevutaList.add(new EmailRicevuta(UUID.fromString(field[0]), field[1], field[2], field[3], field[4], new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(field[5]), field[6].charAt(0))); //da date a string new SimpleDateFormat("dd/MM/yyyy").format(new Date())
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (sc != null) {
			sc.close();
		}

		EmailRicevuta[] arrayEmailRicevuta = emailRicevutaList.toArray(new EmailRicevuta[emailRicevutaList.size()]);
		LockRegistry.INSTANCE.release(emailUtente, LockRegistry.LockType.READ); //rilascio il lock su quel file
		return arrayEmailRicevuta;
	}

	/**
	 * Legge nel file dell'utente tutte le email inviate.
	 *
	 * @param emailUtente: email dell'utente
	 * @return un'array con tutte le email inviate
	 */
	public EmailInviata[] leggiEmailInviate(String emailUtente) {
		LockRegistry.INSTANCE.acquire(emailUtente, LockRegistry.LockType.READ);
		File file = new File("src/server/emails_" + emailUtente + ".txt");
		Scanner sc = null;
		List<EmailInviata> emailInviataList = new ArrayList<>();

		try {
			sc = new Scanner(file);

			while (sc.hasNextLine()) {
				String string = sc.nextLine();
				if (string.charAt(0) == '▶') {
					while (!string.contains("◀")) {
						string = string + sc.nextLine();
					}
					string = string.substring(string.indexOf("▶")+1, string.lastIndexOf("◀"));
					String[] field = string.split("♦");
					if (field[1].equals(emailUtente) && field[6].charAt(0) == 'i') {
						try {
							emailInviataList.add(new EmailInviata(UUID.fromString(field[0]), field[1], field[2], field[3], field[4], new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(field[5]), field[6].charAt(0))); //da date a string new SimpleDateFormat("dd/MM/yyyy").format(new Date())
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (sc != null) {
			sc.close();
		}
		EmailInviata[] arrayEmailInviata = emailInviataList.toArray(new EmailInviata[emailInviataList.size()]);
		LockRegistry.INSTANCE.release(emailUtente, LockRegistry.LockType.READ);
		return arrayEmailInviata;
	}

	/**
	 * Scrive nel file dell'utente l'email inviata.
	 *
	 * @param emailUtente: email dell'utente
	 * @param email: l'email che deve scrivere su file
	 * @return l'email con cast EmailInviata pronta per essere inviata al Client
	 */
	public EmailInviata scriviEmailInviata(String emailUtente, Email email) {
		LockRegistry.INSTANCE.acquire(emailUtente, LockRegistry.LockType.WRITE);
		EmailInviata emailInviata = null;
		Path file = Paths.get("src/server/emails_" + emailUtente + ".txt");
		String sEmail = "▶" + email.getId() + "♦" + email.getMittente() + "♦" + email.getDestinatario() + "♦" + email.getOggetto() + "♦" + email.getTesto() + "♦" + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(email.getDataSpedizione()) + "♦" + email.getInviataRicevuta() + "◀\n";
		try {
			Files.write(file, sEmail.getBytes("UTF-8"), StandardOpenOption.APPEND);
			emailInviata = new EmailInviata(email.getId(), email.getMittente(), email.getDestinatario(), email.getOggetto(), email.getTesto(), email.getDataSpedizione(), email.getInviataRicevuta());
		} catch (IOException e) {
			e.printStackTrace();
		}
		LockRegistry.INSTANCE.release(emailUtente, LockRegistry.LockType.WRITE);
		return emailInviata;
	}

	/**
	 * Scrive nel file dell'utente l'email ricevuta.
	 *
	 * @param emailUtente: email dell'utente
	 * @param email: l'email che devo scrivere su file
	 */
	public void scriviEmailRicevuta(String emailUtente, EmailRicevuta email) {
		LockRegistry.INSTANCE.acquire(emailUtente, LockRegistry.LockType.WRITE);
		Path file = Paths.get("src/server/emails_" + emailUtente + ".txt");
		String sEmail = "▶" + email.getId() + "♦" + email.getMittente() + "♦" + email.getDestinatario() + "♦" + email.getOggetto() + "♦" + email.getTesto() + "♦" + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(email.getDataSpedizione()) + "♦" + email.getInviataRicevuta() + "◀\n";
		try {
			Files.write(file, sEmail.getBytes("UTF-8"), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
		LockRegistry.INSTANCE.release(emailUtente, LockRegistry.LockType.WRITE);
	}

	/**
	 * Cancello l'email dal file.
	 *
	 * @param emailUtente: email dell'utente
	 * @param id: l'id dell'email da cancellare
	 */
	public void cancellaEmail(String emailUtente, UUID id) {
		LockRegistry.INSTANCE.acquire(emailUtente, LockRegistry.LockType.WRITE);
		List<Email> emailList = new ArrayList<>();
		Path file = Paths.get("src/server/emails_" + emailUtente + ".txt");
		Scanner sc = null;
		int indiceEmail = -1; //l'indice dell'ArrayList dove si troverà l'email da cancellare

		try {
			sc = new Scanner(file);

			//salvo tutto il file su un'ArrayList
			while (sc.hasNextLine()) {
				String string = sc.nextLine();
				if (string.charAt(0) == '▶') {
					while (!string.contains("◀")) {
						string = string + sc.nextLine();
					}
					string = string.substring(string.indexOf("▶") + 1, string.lastIndexOf("◀"));
					String[] field = string.split("♦");
					if (UUID.fromString(field[0]).equals(id)) //controllo se l'email attuale è l'email da eliminare
						indiceEmail = emailList.size(); //salvo l'indice dell'email da eliminare
					try {
						emailList.add(new Email(UUID.fromString(field[0]), field[1], field[2], field[3], field[4], new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(field[5]), field[6].charAt(0))); //da date a string new SimpleDateFormat("dd/MM/yyyy").format(new Date())
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		emailList.remove(indiceEmail); //elimino l'email con l'indice trovato

		//sovrascrivo il file con le email salvate nell'ArrayList senza ovviamente l'email cancellata precedentemente
		for (int i = 0; i < emailList.size(); i++) {
			String sEmail = "▶" + emailList.get(i).getId() + "♦" + emailList.get(i).getMittente() + "♦" + emailList.get(i).getDestinatario() + "♦" + emailList.get(i).getOggetto() + "♦" + emailList.get(i).getTesto() + "♦" + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(emailList.get(i).getDataSpedizione()) + "♦" + emailList.get(i).getInviataRicevuta() + "◀\n";
			try {
				if (i == 0)
					Files.write(file, sEmail.getBytes("UTF-8"));
				else
					Files.write(file, sEmail.getBytes("UTF-8"), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//nel caso in cui non ci siano email da scrivere, il file deve essere vuoto
		if (emailList.size() == 0) {
			try {
				Files.write(file, "".getBytes("UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (sc != null) {
			sc.close();
		}
		LockRegistry.INSTANCE.release(emailUtente, LockRegistry.LockType.WRITE);
	}

}

/**
 * Classe che si occupa di creare e gestire in mutua esclusione un file contenente tutti gli indirizzi email degli utenti registrati in modo da
 * confrontarli con eventuali destinatari inesistenti.
 */
class IOFileRegisteredUsers {

	private ReadWriteLock rwl = new ReentrantReadWriteLock();
	private Lock rl = rwl.readLock();
	private Lock wl = rwl.writeLock();
	private File file;
	private Scanner sc;
	private CopyOnWriteArrayList<String> listRegisteredUsers;

	/**
	 * Costruttore della classe.
	 */
	public IOFileRegisteredUsers() {
		listRegisteredUsers = new CopyOnWriteArrayList<>();
		file = new File("src/server/registered_users.txt");

		try {
			if (!file.exists()) //controllo se il file esiste, se non esiste, ne creo uno
				file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		fillTheList();
	}

	/**
	 * Riempo il CopyOnWriteArrayList listRegisteredUsers con le email degli utenti già registrati per una più veloce consultazione in seguito.
	 */
	private void fillTheList() {
		rl.lock();
		try {
			sc = new Scanner(file);

			while (sc.hasNextLine()) {
				listRegisteredUsers.add(sc.nextLine());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			sc.close();
			rl.unlock();
		}
	}

	/**
	 * Metodo che si occupa di aggiungere un nuovo utente al registro, viene sia salvato su file sia aggiunto al CopyOnWriteArrayList.
	 *
	 * @param user: l'utente da aggiungere
	 */
	public void addUserToFile(String user) {
		wl.lock();
		Path file = Paths.get("src/server/registered_users.txt");
		try {
			if (!listRegisteredUsers.contains(user)) {
				Files.write(file, (user + "\n").getBytes("UTF-8"), StandardOpenOption.APPEND);
				listRegisteredUsers.add(user);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			wl.unlock();
		}
	}

	/**
	 * Metodo che si occupa di controllare se l'email è valida o meno.
	 *
	 * @param userCandidate: l'email da controllare
	 * @return true se e solo se l'email esiste, false altrimenti
	 */
	public boolean checkUser(String userCandidate) {
		return listRegisteredUsers.contains(userCandidate);
	}

}