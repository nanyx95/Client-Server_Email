package client;

import utilities.Email;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;

/**
 * Created by Fabio Somaglia on 09/11/17.
 */

/**
 * Controller del Client email.
 */
public class ControllerClientEmail {

    private ModelClient modelClient;
    private ClientEmail clientEmail; //View

	/**
	 * Costruttore della classe che contiene anche tutti i listener della View.
	 *
	 * @param modelClient: oggetto ModelClient (modello del Client)
	 * @param clientEmail: oggetto ClientEmail (Vista del Client)
	 */
    public ControllerClientEmail(ModelClient modelClient, ClientEmail clientEmail) {
        this.modelClient = modelClient;
        this.clientEmail = clientEmail;

		clientEmail.getAccountEmailLabel().setText(modelClient.getEmailUtente()); //imposto l'email dell'utente

        clientEmail.getEmailRicevuteList().setModel(modelClient.getEmailRicevuteSortedListModel());
        clientEmail.getEmailRicevuteList().setCellRenderer(new emailRicevuteListCellRenderer()); //richiamo ListCellRender per personalizzare la cella della JList
        clientEmail.getEmailRicevuteList().setFixedCellHeight(55); //per modificare lo spazio tra una cella della JList e l'altra

        clientEmail.getEmailInviateList().setModel(modelClient.getEmailInviateSortedListModel());
        clientEmail.getEmailInviateList().setCellRenderer(new emailInviateListCellRenderer()); //richiamo ListCellRender per personalizzare la cella della JList
        clientEmail.getEmailInviateList().setFixedCellHeight(55); //per modificare lo spazio tra una cella della JList e l'altra

        //listener eseguito quando seleziono un'email ricevuta dalla JList
        clientEmail.getEmailRicevuteList().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    clientEmail.getEmailInviateList().clearSelection(); //deseleziono item della JList nell'altro pannello
                    System.out.println("Hai scelto di visualizzare l'email ricevuta numero: " + clientEmail.getEmailRicevuteList().getSelectedIndex());
                    modelClient.setSceltaEmailRicevute(clientEmail.getEmailRicevuteList().getSelectedIndex());
                }
            }
        });

        //listener eseguito quando seleziono un'email inviata dalla JList
        clientEmail.getEmailInviateList().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    clientEmail.getEmailRicevuteList().clearSelection(); //deseleziono item della JList nell'altro pannello
                    System.out.println("Hai scelto di visualizzare l'email inviata numero: " + clientEmail.getEmailInviateList().getSelectedIndex());
                    modelClient.setSceltaEmailInviate(clientEmail.getEmailInviateList().getSelectedIndex());
                }
            }
        });

        //listener eseguito quando premo sul button nuova email
        clientEmail.getScriviNuovaEmailButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InviaEmail nuovaEmail = new InviaEmail();
                ControllerInviaEmail controllerInviaEmail = new ControllerInviaEmail(modelClient, nuovaEmail);
            }
        });

        //listener eseguito quando premo sul button cancella email
        clientEmail.getCancellaEmailButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (clientEmail.getEmailTabPane().getSelectedIndex() == 0) {
                    if (clientEmail.getEmailRicevuteList().getSelectedIndex() != -1) {
                    	modelClient.cancellaEmail(clientEmail.getEmailRicevuteList().getSelectedValue().getId());
                        SortedListModel<Email> newModel = (SortedListModel<Email>) clientEmail.getEmailRicevuteList().getModel();
                        newModel.removeElement(clientEmail.getEmailRicevuteList().getSelectedValue()); //rimuovo l'email selezionata
                        System.out.println("Hai eliminato con successo un'email ricevuta");
                        if (clientEmail.getEmailRicevuteList().getSelectedIndex() < modelClient.getEmailRicevuteSortedListModel().getSize()) //una volta rimossa l'email, viene selezionato automaticamente l'item successivo presente nella JList. Se questo elemento è presente
                            modelClient.setSceltaEmailRicevute(clientEmail.getEmailRicevuteList().getSelectedIndex()); //visualizzo il suo contenuto
                        else {
                            clientEmail.getEmailRicevuteList().clearSelection(); //deseleziono tutti gli item del JList
                            clientEmail.setNullWholeMessaggioPanel(); //altrimenti libero il messaggioPanel
                        }
                    } else
                        JOptionPane.showMessageDialog(null, "Devi selezionare un'email!", "Impossibile eliminare un'email", JOptionPane.ERROR_MESSAGE);
                } else if (clientEmail.getEmailTabPane().getSelectedIndex() == 1) {
                    if (clientEmail.getEmailInviateList().getSelectedIndex() != -1) {
                    	modelClient.cancellaEmail(clientEmail.getEmailInviateList().getSelectedValue().getId());
                        SortedListModel<Email> newModel = (SortedListModel<Email>) clientEmail.getEmailInviateList().getModel();
                        newModel.removeElement(clientEmail.getEmailInviateList().getSelectedValue()); //rimuovo l'email selezionata
                        System.out.println("Hai eliminato con successo un'email inviata");
                        if (clientEmail.getEmailInviateList().getSelectedIndex() < modelClient.getEmailInviateSortedListModel().getSize()) //una volta rimossa l'email, viene selezionato automaticamente l'item successivo presente nella JList. Se questo elemento è presente
                            modelClient.setSceltaEmailInviate(clientEmail.getEmailInviateList().getSelectedIndex()); //visualizzo il suo contenuto
                        else {
                            clientEmail.getEmailInviateList().clearSelection(); //deseleziono tutti gli item del JList
                            clientEmail.setNullWholeMessaggioPanel(); //altrimenti libero il messaggioPanel
                        }
                    } else
                        JOptionPane.showMessageDialog(null, "Devi selezionare un'email!", "Impossibile eliminare un'email", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        //listener eseguito quando premo sul button rispondi email
        clientEmail.getRispondiEmailButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (clientEmail.getEmailTabPane().getSelectedIndex() == 0) {
					if (clientEmail.getEmailRicevuteList().getSelectedIndex() != -1) {
						InviaEmail nuovaEmail = new InviaEmail();
						ControllerInviaEmail controllerInviaEmail = new ControllerInviaEmail(modelClient, nuovaEmail);
						controllerInviaEmail.rispondi(clientEmail.getEmailRicevuteList().getSelectedValue());
						System.out.println("Hai risposto ad un'email ricevuta");
					} else
						JOptionPane.showMessageDialog(null, "Devi selezionare un'email!", "Impossibile rispondere ad un'email", JOptionPane.ERROR_MESSAGE);
				} else if (clientEmail.getEmailTabPane().getSelectedIndex() == 1) {
					if (clientEmail.getEmailInviateList().getSelectedIndex() != -1) {
						JOptionPane.showMessageDialog(null, "Devi selezionare un'email ricevuta!", "Impossibile rispondere a se stessi", JOptionPane.ERROR_MESSAGE);
					} else
						JOptionPane.showMessageDialog(null, "Devi selezionare un'email!", "Impossibile rispondere ad un'email", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

        //listener eseguito quando premo sul button rispondi a tutti
		clientEmail.getRispondiTuttiEmailButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (clientEmail.getEmailTabPane().getSelectedIndex() == 0) {
					if (clientEmail.getEmailRicevuteList().getSelectedIndex() != -1) {
						InviaEmail nuovaEmail = new InviaEmail();
						ControllerInviaEmail controllerInviaEmail = new ControllerInviaEmail(modelClient, nuovaEmail);
						controllerInviaEmail.rispondiTutti(clientEmail.getEmailRicevuteList().getSelectedValue());
						System.out.println("Hai risposto a tutti ad un'email ricevuta");
					} else
						JOptionPane.showMessageDialog(null, "Devi selezionare un'email!", "Impossibile rispondere a tutti ad un'email", JOptionPane.ERROR_MESSAGE);
				} else if (clientEmail.getEmailTabPane().getSelectedIndex() == 1) {
					if (clientEmail.getEmailInviateList().getSelectedIndex() != -1) {
						JOptionPane.showMessageDialog(null, "Devi selezionare un'email ricevuta!", "Impossibile rispondere a se stessi", JOptionPane.ERROR_MESSAGE);
					} else
						JOptionPane.showMessageDialog(null, "Devi selezionare un'email!", "Impossibile rispondere a tutti ad un'email", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		//listener eseguito quando si preme sul button inoltra
		clientEmail.getInoltraButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (clientEmail.getEmailTabPane().getSelectedIndex() == 0) {
					if (clientEmail.getEmailRicevuteList().getSelectedIndex() != -1) {
						InviaEmail nuovaEmail = new InviaEmail();
						ControllerInviaEmail controllerInviaEmail = new ControllerInviaEmail(modelClient, nuovaEmail);
						controllerInviaEmail.inoltra(clientEmail.getEmailRicevuteList().getSelectedValue());
						System.out.println("Hai inoltrato un'email ricevuta");
					} else
						JOptionPane.showMessageDialog(null, "Devi selezionare un'email!", "Impossibile inoltrare un'email", JOptionPane.ERROR_MESSAGE);
				} else if (clientEmail.getEmailTabPane().getSelectedIndex() == 1) {
					if (clientEmail.getEmailInviateList().getSelectedIndex() != -1) {
						InviaEmail nuovaEmail = new InviaEmail();
						ControllerInviaEmail controllerInviaEmail = new ControllerInviaEmail(modelClient, nuovaEmail);
						controllerInviaEmail.inoltra(clientEmail.getEmailInviateList().getSelectedValue());
						System.out.println("Hai inoltrato un'email inviata");
					} else
						JOptionPane.showMessageDialog(null, "Devi selezionare un'email!", "Impossibile inoltrare un'email", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		//listener eseguito quando chiudo l'applicazione
        clientEmail.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				modelClient.closeConnectionToServer();
				System.exit(0); //termina l'esecuzione
			}
		});
    }

	/**
	 * Classe che definisce il design delle celle della JList nel tab Email Ricevute.
	 */
	private class emailRicevuteListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Email label = (Email) value;
            String mittente = label.getMittente();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M/yyyy HH:mm"); //pattern per la generazione della data del messaggio
            String data = dateFormat.format(label.getDataSpedizione());

            String oggetto = label.getOggetto();
            String labelText = "<html>Da: <b>" + mittente + "</b><br/><i>" + data + "</i><br/>" + oggetto + "</html>";
            setText(labelText);
            //setBorder(BorderFactory.createEmptyBorder(5,5,5,0));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            //setBorder(BorderFactory.createTitledBorder(destinatario));

            return this;
        }

    }

	/**
	 * Classe che definisce il design delle celle della JList nel tab Email Inviate.
	 */
    private class emailInviateListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Email label = (Email) value;
            String destinatario = label.getDestinatario();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M/yyyy HH:mm"); //pattern per la generazione della data del messaggio
            String data = dateFormat.format(label.getDataSpedizione());

            String oggetto = label.getOggetto();
            String labelText = "<html>A: <b>" + destinatario + "</b><br/><i>" + data + "</i><br/>" + oggetto + "</html>";
            setText(labelText);
            //setBorder(BorderFactory.createEmptyBorder(5,5,5,0));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            //setBorder(BorderFactory.createTitledBorder(destinatario));

            return this;
        }

    }

}