package client;

import utilities.Email;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Fabio Somaglia on 10/11/17.
 */

/**
 * Controller della View InviaEmail.
 */
public class ControllerInviaEmail {

    private ModelClient modelClient;
    private InviaEmail inviaEmail; //View

	private static final String REGEX_EMAIL = "^(([a-zA-Z0-9_\\-.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(]?)(\\s*([;,])\\s*|\\s*$))*$";

	/**
	 * Costruttore della classe con listener dei componenti della View.
	 *
	 * @param modelClient: oggetto ModelClient (modello del Client)
	 * @param inviaEmail: oggetto InviaEmail (Vista invio di una nuova email)
	 */
    public ControllerInviaEmail(ModelClient modelClient, InviaEmail inviaEmail) {
        this.modelClient = modelClient;
        this.inviaEmail = inviaEmail;

		inviaEmail.getMittenteTextField().setText(modelClient.getEmailUtente()); //imposto l'email dell'utente

		//listener eseguito quando premo sul button Invia email
        inviaEmail.getInviaEmailButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (inviaEmail.getDestinatarioTextField().getText().equals("")) { //se il campo destinatario è vuoto
                    JOptionPane.showMessageDialog(null, "Inserisci il destinatario dell'email!", "Destinatario non inserito", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Hai provato ad inviare un'email ma non è stato inserito il destinatario!");
                } else {
					Pattern pattern = Pattern.compile(REGEX_EMAIL);
					Matcher matcher = pattern.matcher(inviaEmail.getDestinatarioTextField().getText()); //controllo che il testo scritto nel campo destinatario rispetti il pattern definito da REGEX_EMAIL
					String destinatarioFormattato = "";
					while (matcher.find()) { //costruisco una stringa con i destinatari formattati a dovere
						destinatarioFormattato = matcher.group();
					}
                	if (destinatarioFormattato.equals("")) { //se la stringa è vuota, il campo destinatario non era formattato correttamente
						JOptionPane.showMessageDialog(null, "Il campo destinatario non è formattato correttamente.\nSi ricordi che se ci fossero più destinatari, questi devono essere separati\nda una virgola (,) o da un punto e virgola (;).", "Impossibile inviare l'email", JOptionPane.ERROR_MESSAGE);
					} else { //altrimenti istanzio un oggetto di tipo Email e invio
						if (inviaEmail.getOggettoTextField().getText().equals(""))
							inviaEmail.getOggettoTextField().setText("Nessun oggetto");
						if (inviaEmail.getMessaggioTextArea().getText().equals(""))
							inviaEmail.getMessaggioTextArea().setText("Nessun contenuto");
						destinatarioFormattato = destinatarioFormattato.replaceAll(";\\s*", ", ");
						Email newEmail = new Email(UUID.randomUUID(), inviaEmail.getMittenteTextField().getText(), destinatarioFormattato, inviaEmail.getOggettoTextField().getText(), inviaEmail.getMessaggioTextArea().getText(), new Date(), 'i');
						modelClient.inviaEmail(newEmail);
						System.out.println("Hai inviato con successo un'email a " + newEmail.getDestinatario() + ". Ecco i dettagli dell'email: " + newEmail.toString());
						inviaEmail.dispose(); //chiudo il JFrame nuova email /\s+/|,
						//JOptionPane.showMessageDialog(null, "Email inviata a " + newEmail.getDestinatario(), "Email inviata", JOptionPane.INFORMATION_MESSAGE);
					}
                }
            }
        });
    }

	/**
	 * Metodo utilizzato per comporre il messaggio quando si preme sul button Rispondi.
	 *
	 * @param email: email selezionata dalla JList
	 */
	public void rispondi(Email email) {
		inviaEmail.getDestinatarioTextField().setText(email.getMittente());
		inviaEmail.getOggettoTextField().setText("Re: " + email.getOggetto());
		inviaEmail.getMessaggioTextArea().setText("\n\nIl " + email.getDataSpedizione() + ", " + email.getMittente() + " ha scritto:\n\n" + email.getTesto());
	}

	/**
	 * Metodo utilizzato per comporre il messaggio quando si preme sul button Rispondi a tutti.
	 *
	 * @param email: email selezionata dalla JList
	 */
	public void rispondiTutti(Email email) {
    	List<String> listDestinatari = new LinkedList<>(Arrays.asList((email.getMittente() + ", " + email.getDestinatario()).split(",\\s*"))); //seleziono il mittente e tutti i destinatari dell'email

    	while (listDestinatari.contains(modelClient.getEmailUtente())) //escludo l'utente che fa l'azione di rispondere, non ha senso rispondere a se stessi
    		listDestinatari.remove(modelClient.getEmailUtente());

    	String destinatari = "";
    	for (int i = 0; i < listDestinatari.size(); i++) { //compongo la stringa con i destinatari formattata a dovere
    		if (i == 0) {
    			destinatari = destinatari + listDestinatari.get(i);
			} else {
    			destinatari = destinatari + ", " + listDestinatari.get(i);
			}
		}

		inviaEmail.getDestinatarioTextField().setText(destinatari);
		inviaEmail.getOggettoTextField().setText("Re: " + email.getOggetto());
		inviaEmail.getMessaggioTextArea().setText("\n\nIl " + email.getDataSpedizione() + ", " + email.getMittente() + " ha scritto:\n\n" + email.getTesto());
	}

	/**
	 * Metodo utilizzato per comporre il messaggio quando si preme sul button Inoltra.
	 *
	 * @param email: email selezionata dalla JList
	 */
	public void inoltra(Email email) {
		inviaEmail.getOggettoTextField().setText("Fwd: " + email.getOggetto());
		inviaEmail.getMessaggioTextArea().setText("\n\nInizio messaggio inoltrato:\n\nA: " + email.getDestinatario() + "\nDa: " + email.getMittente() + "\nData: " + email.getDataSpedizione() + "\nOggetto: " + email.getOggetto() + "\n\n" + email.getTesto());
	}

}