package client;

import utilities.Email;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Fabio Somaglia on 08/11/17.
 */

/**
 * View del Client email
 */
public class ClientEmail extends JFrame implements Observer { //View

    private JPanel clientEmailPanel;
    private JTabbedPane emailTabPane;
    private JPanel emailRicevutePanel;
    private JPanel emailInviatePanel;
    private JList<Email> emailRicevuteList;
    private JList<Email> emailInviateList;
    private JPanel messaggioPanel;
    private JButton scriviNuovaEmailButton;
    private JLabel mittenteLabel;
    private JLabel destinatarioLabel;
    private JLabel dataLabel;
    private JLabel oggettoLabel;
    private JTextArea messaggioTextArea;
    private JPanel emailPanel;
    private JButton cancellaEmailButton;
    private JPanel accountPanel;
    private JLabel accountLabel;
    private JLabel accountEmailLabel;
    private JPanel intestazionePanel;
	private JButton rispondiEmailButton;
	private JButton rispondiTuttiEmailButton;
	private JButton inoltraButton;

	/**
	 * Costruttore della classe.
	 */
	public ClientEmail() {
        setTitle("Client Email");
        add(clientEmailPanel);
        pack();
        setVisible(true);
        //setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public JList<Email> getEmailRicevuteList() {
        return emailRicevuteList;
    }

    public JList<Email> getEmailInviateList() {
        return emailInviateList;
    }

    public JButton getScriviNuovaEmailButton() {
        return scriviNuovaEmailButton;
    }

    public JButton getCancellaEmailButton() {
        return cancellaEmailButton;
    }

    public JTabbedPane getEmailTabPane() {
        return emailTabPane;
    }

	public JLabel getAccountEmailLabel() {
		return accountEmailLabel;
	}

	public JButton getRispondiEmailButton() {
		return rispondiEmailButton;
	}

	public JButton getRispondiTuttiEmailButton() {
		return rispondiTuttiEmailButton;
	}

	public JButton getInoltraButton() {
		return inoltraButton;
	}

	/**
	 * Metodo che svuota le label e textArea del Client.
	 */
	public void setNullWholeMessaggioPanel() {
        mittenteLabel.setText("");
        destinatarioLabel.setText("");
        dataLabel.setText("");
        oggettoLabel.setText("");
        messaggioTextArea.selectAll();
        messaggioTextArea.replaceSelection("");
    }

	/**
	 * Metodo Observer eseguito quando avvengono dei cambiamenti nella classe Observable.
	 *
	 * @param o: oggetto Observable
	 * @param arg: parametro supplementare utilizzato per passare oggetti Stringa e EmailRicevuta dal ModelClient alla View
	 */
	@Override
    public void update(Observable o, Object arg) {
        //System.out.println("sono in update client");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M/yyyy HH:mm"); //pattern per la generazione della data del messaggio

		if (arg.getClass().getSimpleName().equals("String[]")) {
			String[] mittenteOggettoEmail = (String[]) arg;
			JOptionPane.showMessageDialog(null, "Hai ricevuto una nuova email da " + mittenteOggettoEmail[0] + "\ncon oggetto: " + mittenteOggettoEmail[1], "Nuova email ricevuta", JOptionPane.INFORMATION_MESSAGE);
		} else {
			ModelClient m = (ModelClient) o;
			Email e = (Email) arg;
			mittenteLabel.setText(e.getMittente());
			destinatarioLabel.setText(e.getDestinatario());
			dataLabel.setText(dateFormat.format(e.getDataSpedizione()));
			oggettoLabel.setText(e.getOggetto());
			messaggioTextArea.selectAll();
			messaggioTextArea.replaceSelection(e.getTesto());
		}
    }

}