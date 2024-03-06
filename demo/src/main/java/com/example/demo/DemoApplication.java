package com.example.demo;

import com.google.gson.JsonElement;
import lombok.Getter;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {
	@Getter
	private double sommaTotale;
	private JsonArray jsonArray = new JsonArray();
	@Getter
	private String classe;
	@Getter
	private String anno;
	public DemoApplication(){
		this.sommaTotale = 0;
	}



	public static void main(String[] args) throws SQLException {
		DemoApplication application = new DemoApplication();

		application.getCorsiAlunno("giancarlo.massa.2006@calvino.edu.it");
		System.out.println(application.jsonArray.get(1).getAsJsonObject().get("somma_ore").getClass().getName());



		try {
			FileInputStream fis = new FileInputStream("attestato2324.docx");
			XWPFDocument doc = new XWPFDocument(fis);

			// Itera sui paragrafi del documento
			for (XWPFParagraph p : doc.getParagraphs()) {
				// Itera sui runs (porzioni di testo) del paragrafo
				for (XWPFRun run : p.getRuns()) {
					String text = run.getText(0);
					System.out.println(text);
					if (text != null && text.contains("{name}")) {
						// Sostituisci il testo del tag con il nuovo testo desiderato
						text = text.replace("{name}", application.jsonArray.get(0).getAsJsonObject().get("name").getAsString());
						run.setText(text, 0);
					}
					if (text != null && text.contains("{tot}")) {
						// Sostituisci il testo del tag con il nuovo testo desiderato
						text = text.replace("{tot}", Double.toString(application.getSommaTotale()));
						run.setText(text, 0);
					}
					if (text != null && text.contains("{classe}")) {
						// Sostituisci il testo del tag con il nuovo testo desiderato
						text = text.replace("{classe}", application.getClasse());
						run.setText(text, 0);
					}
					if (text != null && text.contains("{anno}")) {
						// Sostituisci il testo del tag con il nuovo testo desiderato
						text = text.replace("{anno}", application.getAnno());
						run.setText(text, 0);
					}
					if (text != null && text.contains("{dataCorrente}")) {
						// Sostituisci il testo del tag con il nuovo testo desiderato
						DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
						text = text.replace("{dataCorrente}", LocalDate.now().format(formatoFecha));
						run.setText(text, 0);
					}
				}
			}
			for (XWPFTable table : doc.getTables()) {
				for(int i= 0; i<application.jsonArray.size(); i++){
					XWPFTableRow newRow = table.createRow();
					newRow.getCell(0).setText(application.jsonArray.get(i).getAsJsonObject().get("title").getAsString().replaceAll("\"", ""));
					if (!application.jsonArray.get(i).getAsJsonObject().get("somma_ore").isJsonNull()){
						newRow.getCell(1).setText(application.jsonArray.get(i).getAsJsonObject().get("somma_ore").getAsString().replaceAll("\"", ""));
					}else{
						newRow.getCell(1).setText("0");
					}
					if (!application.jsonArray.get(i).getAsJsonObject().get("ore_totali").isJsonNull()){
						newRow.getCell(2).setText(application.jsonArray.get(i).getAsJsonObject().get("ore_totali").getAsString().replaceAll("\"", ""));
					}else{
						newRow.getCell(2).setText("0");
					}
					newRow.getCell(3).setText(application.jsonArray.get(i).getAsJsonObject().get("professore").getAsString().replaceAll("\"", ""));
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

	private void getCorsiAlunno(String nome) throws SQLException {
		ConnessioneDb c = new ConnessioneDb();
		String query = "SELECT u.name, c.title, SUM(TIMESTAMPDIFF(HOUR, p.joined_at,  p.exited_at)) as somma_ore,\n" +
				"                (select SUM(TIMESTAMPDIFF(HOUR, a.started_at, a.ended_at)) from ca_activities as a where c.id = a.course_id) as ore_totali,\n" +
				"                (select u.name from ca_users as u where u.id = c.user_id) as professore\n" +
				"                FROM\n" +
				"\t\t\t\tca_users as u, ca_presences as p, ca_registrations as r, ca_activities as a, ca_courses as c WHERE\n" +
				"\t\t\t\tu.id = r.user_id AND c.id = r.course_id AND\n" +
				"\t\t\t\tc.id = a.course_id AND u.id = p.user_id AND\n" +
				"\t\t\t\ta.id = p.activity_id AND\n" +
				"\t\t\t\tu.email = \"" + nome + "\"\n" +
				"\t\t\t\tGROUP BY c.title;";
		System.out.println(query);

		ResultSet rs = c.select(query);

		while (rs.next()) {
			JsonObject jsonObject = new JsonObject();
			int numColumns = rs.getMetaData().getColumnCount();
			for (int i = 1; i <= numColumns; i++) {
				String columnName = rs.getMetaData().getColumnName(i);
				String columnValue = rs.getString(i);
				jsonObject.addProperty(columnName, columnValue);
			}
			this.jsonArray.add(jsonObject);
		}

		for (JsonElement element : this.jsonArray) {
			// Estrai il JsonObject corrente dall'elemento JsonArray
			JsonObject jsonObject = element.getAsJsonObject();

			// Estrai il valore "ore_totali" come intero e aggiungilo alla somma
			this.sommaTotale = this.sommaTotale + jsonObject.get("ore_totali").getAsInt();
		}

		query = "select c.name as classe, a.description as anno from ca_school_classes as c, ca_school_years as a, ca_users as u, ca_frequented_classes as fc\n" +
				"    where fc.user_id = u.id and\n" +
				"          fc.school_class_id = c.id and\n" +
				"          c.school_year_id = a.id and\n" +
				"          u.email = \"" + nome + "\"";
		rs = c.select(query);

		while (rs.next()){
			this.classe = rs.getString("classe");
			this.anno = rs.getString("anno");
		}
		System.out.println(anno);
		System.out.println(classe);

		// Stampa l'oggetto JSON
		System.out.println(this.sommaTotale);
		System.out.println(this.jsonArray.toString());
		c.chiudi();
	}
}
