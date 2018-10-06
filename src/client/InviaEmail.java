package client;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Fabio Somaglia on 09/11/17.
 */

/**
 * View della finestra InviaEmail.
 */
public class InviaEmail extends JFrame { //View

    private JPanel inviaEmailPanel;
    private JTextField destinatarioTextField;
    private JTextField oggettoTextField;
    private JTextArea messaggioTextArea;
    private JTextField mittenteTextField;
    private JButton inviaEmailButton;
	private JPanel messaggioPanel;

	/**
	 * Costruttore della classe.
	 */
	public InviaEmail() {
        setTitle("Invia nuova Email");
        add(inviaEmailPanel);
        setLocation(300, 300);
        pack();
        setVisible(true);
    }

    public JTextField getDestinatarioTextField() {
        return destinatarioTextField;
    }

    public JTextField getOggettoTextField() {
        return oggettoTextField;
    }

    public JTextArea getMessaggioTextArea() {
        return messaggioTextArea;
    }

    public JTextField getMittenteTextField() {
        return mittenteTextField;
    }

    public JButton getInviaEmailButton() {
        return inviaEmailButton;
    }
}