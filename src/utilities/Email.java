package utilities;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Fabio Somaglia on 08/11/17.
 */

public class Email implements Comparable<Email>, Serializable {

	private UUID id;
	private String mittente;
	private String destinatario;
	private String oggetto;
	private String testo;
	private Date dataSpedizione;
	private char inviataRicevuta; //distinguere email inviata da email ricevuta

	public Email(UUID id, String mittente, String destinatario, String oggetto, String testo, Date dataSpedizione, char inviataRicevuta) {
		this.id = id;
		this.mittente = mittente;
		this.destinatario = destinatario;
		this.oggetto = oggetto;
		this.testo = testo;
		this.dataSpedizione = dataSpedizione;
		this.inviataRicevuta = inviataRicevuta;
	}

	public UUID getId() {
		return id;
	}

	public String getMittente() {
		return mittente;
	}

	public String getDestinatario() {
		return destinatario;
	}

	public String getOggetto() {
		return oggetto;
	}

	public String getTesto() {
		return testo;
	}

	public Date getDataSpedizione() {
		return dataSpedizione;
	}

	public char getInviataRicevuta() {
		return inviataRicevuta;
	}

	@Override
	public String toString() {
		return "▶" + id +
				"♦" + mittente +
				"♦" + destinatario +
				"♦" + oggetto +
				"♦" + testo +
				"♦" + dataSpedizione +
				"♦" + inviataRicevuta +
				"◀";
	}

	@Override
	public int compareTo(Email o) {
		return this.dataSpedizione.compareTo(o.dataSpedizione);
	}
}