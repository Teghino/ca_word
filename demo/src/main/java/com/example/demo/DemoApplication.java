package com.example.demo;

import org.apache.poi.hssf.record.HCenterRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.sql.*;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) throws SQLException {

		
		ResultSet rs;
		ConnessioneDb c = new ConnessioneDb();

		rs = c.select("SELECT u.name, c.title, SUM(TIMESTAMPDIFF(HOUR, p.joined_at,  p.exited_at)) as somma_ore FROM\n" +
				"ca_users as u, ca_presences as p, ca_registrations as r, ca_activities as a, ca_courses as c WHERE\n" +
				"u.id = r.user_id AND c.id = r.course_id AND\n" +
				"    \tc.id = a.course_id AND u.id = p.user_id AND\n" +
				"    \ta.id = p.activity_id AND\n" +
				"    \tc.title = \"04 -Web attack\"\n" +
				"    \tGROUP BY u.email;");

		try {
			FileInputStream fis = new FileInputStream("attestato2324.docx");
			XWPFDocument doc = new XWPFDocument(fis);

			// Itera sui paragrafi del documento
			for (XWPFParagraph p : doc.getParagraphs()) {
				// Itera sui runs (porzioni di testo) del paragrafo
				for (XWPFRun run : p.getRuns()) {
					String text = run.getText(0);
					if (text != null && text.contains("{name}")) {
						// Sostituisci il testo del tag con il nuovo testo desiderato
						text = text.replace("{name}", "wow");
						run.setText(text, 0);
					}
				}
			}

			// Salva le modifiche al documento
			FileOutputStream fos = new FileOutputStream("documento_modificato.docx");
			doc.write(fos);
			fos.close();
			doc.close();
			fis.close();

			System.out.println("Documento modificato salvato con successo.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
		//SpringApplication.run(DemoApplication.class, args);
		//esecuzione query
	/*	ResultSet rs;
		ConnessioneDb c = new ConnessioneDb();
		String valore = "";

		rs = c.select("select SUM(TIMESTAMPDIFF(HOUR, a.started_at, a.ended_at)) as somma_ore from ca_activities as a, ca_courses as c\n" +
				"\t\t\t\t    where a.course_id = c.id AND c.title = \"04 -Web attack\"\n" +
				"\t\t\t\t   group by c.id");

		ResultSetMetaData metaData1 = rs.getMetaData();

		if (rs.next()) {
			valore = rs.getString(1); // Recupera il valore dalla prima colonna del ResultSet
			// Puoi utilizzare il valore per fare qualcosa
			System.out.println("Valore recuperato: " + valore);
		} else {
			// Se non ci sono risultati nel ResultSet, gestisci il caso di assenza di dati
			System.out.println("Nessun dato disponibile nel ResultSet.");
		}

			rs = c.select("SELECT u.email, c.title, SUM(TIMESTAMPDIFF(HOUR, p.joined_at,  p.exited_at)) as somma_ore FROM\n" +
					"ca_users as u, ca_presences as p, ca_registrations as r, ca_activities as a, ca_courses as c WHERE\n" +
					"u.id = r.user_id AND c.id = r.course_id AND\n" +
					"    \tc.id = a.course_id AND u.id = p.user_id AND\n" +
					"    \ta.id = p.activity_id AND\n" +
					"    \tc.title = \"04 -Web attack\"\n" +
					"    \tGROUP BY u.email;");

		ResultSetMetaData metaData = rs.getMetaData();


		//creazione del documento word
		XWPFDocument document = new XWPFDocument();
		XWPFTable table = document.createTable();


		// Creazione della riga per l'intestazione
		XWPFTableRow headerRow = table.getRow(0);
		if (headerRow == null) {
			headerRow = table.createRow(); // Crea una nuova riga per l'intestazione
		}
		// Imposta l'intestazione della tabella
		for (int i = 1; i <= metaData.getColumnCount()+1; i++) {
			XWPFTableCell headerCell = headerRow.getCell(i - 1); // Ottieni la cella corrispondente
			if (headerCell == null) {
				headerCell = headerRow.createCell(); // Crea una nuova cella se non esiste
			}
			if (i == metaData.getColumnCount()+1){
				headerCell.setText("percentuale_totale");
			}else headerCell.setText(metaData.getColumnName(i)); // Imposta il nome della colonna come testo della cella
		}


		// Inserisci i dati nella tabella
		while (rs.next()) {
			XWPFTableRow row = table.createRow(); // Crea una nuova riga
			for (int i = 1; i <= metaData.getColumnCount()+1; i++) {
				XWPFTableCell cell = row.getCell(i - 1); // Ottieni la cella corrispondente
				if (cell == null) {
					cell = row.createCell(); // Crea una nuova cella se non esiste
				}
				if (i == metaData.getColumnCount()+1) {
					XWPFTableCell cellaPrima = row.getCell(i-2);
					if (!cellaPrima.getText().equals("")) {
						int numeroCellaprima = Integer.parseInt(cellaPrima.getText());
						float numero = Float.parseFloat(valore)/100;
						if (numero != 0){
							cell.setText(numeroCellaprima / numero + " %");
						}
					}else cell.setText("0 %");

				}
				else cell.setText(rs.getString(i)); // Imposta il valore della cella con il dato della colonna
			}
		}

		XWPFParagraph paragraph = document.createParagraph();
		XWPFRun run = paragraph.createRun();
		run.addBreak(BreakType.PAGE);

		// Esegui la seconda query
		ResultSet rs2;
		rs2 = c.select("SELECT * FROM ca_users");
		ResultSetMetaData metaData2 = rs2.getMetaData();

		// Creazione di una nuova tabella nel documento
		XWPFTable table2 = document.createTable();

		// Creazione della riga per l'intestazione della seconda tabella
		XWPFTableRow headerRow2 = table2.getRow(0);
		if (headerRow2 == null) {
			headerRow2 = table2.createRow();
		}

		// Imposta l'intestazione della seconda tabella
		for (int i = 1; i <= metaData2.getColumnCount(); i++) {
			XWPFTableCell headerCell2 = headerRow2.getCell(i - 1);
			if (headerCell2 == null) {
				headerCell2 = headerRow2.createCell();
			}
			headerCell2.setText(metaData2.getColumnName(i));
		}

		// Inserisci i dati nella seconda tabella
		while (rs2.next()) {
			XWPFTableRow row2 = table2.createRow();
			for (int i = 1; i <= metaData2.getColumnCount(); i++) {
				String columnValue = rs2.getString(i);
				XWPFTableCell cell2 = row2.getCell(i - 1);
				if (cell2 == null) {
					cell2 = row2.createCell();
				}
				cell2.setText(columnValue);
			}
		}

		if (headerRow2 != null) {
			int numberOfColumns = headerRow2.getTableCells().size();
			int cellWidth = 8000 / numberOfColumns; // Larghezza desiderata per ciascuna cella
			for (XWPFTableCell cell : headerRow2.getTableCells()) {
				cell.setWidth(String.valueOf(cellWidth));
			}
			for (XWPFTableRow row : table2.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					cell.setWidth(String.valueOf(cellWidth));
				}
			}
		}

		// Salvataggio del documento Word
		try {
			FileOutputStream out = new FileOutputStream(new File("output.docx"));
			document.write(out);
			out.close();
			System.out.println("Documento Word creato con successo.");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
*/