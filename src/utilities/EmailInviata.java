package utilities;

import java.util.Date;
import java.util.UUID;

public class EmailInviata extends Email {
	public EmailInviata(UUID id, String mittente, String destinatario, String oggetto, String testo, Date dataSpedizione, char inviataRicevuta) {
		super(id, mittente, destinatario, oggetto, testo, dataSpedizione, inviataRicevuta);
	}
}