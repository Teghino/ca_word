package com.example.demo;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
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

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {
	@Setter
	@Getter
	private ArrayList<String> alunni = new ArrayList<>();
	@Setter
	@Getter
	private ArrayList<String> alunniNome = new ArrayList<>();
	@Setter
	@Getter
	private double sommaTotale;
	@Getter
	@Setter
	private JsonArray jsonArray = new JsonArray();
	@Setter
	@Getter
	private String classe;
	@Setter
	@Getter
	private String anno;

	public DemoApplication(){
		this.sommaTotale = 0;
}

	public void addAlunni(String a){
		alunni.add(a);
	}
	public void addAlunniNome(String a){ alunniNome.add(a);}

	public void getCorsiAlunno(String nome) throws SQLException {
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
			if(jsonObject.get("somma_ore") != null && !jsonObject.get("somma_ore").isJsonNull()) {
				this.sommaTotale = this.sommaTotale + jsonObject.get("somma_ore").getAsInt();
			}

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

	public void deleteAll(){
		this.jsonArray = new JsonArray();
		this.sommaTotale = 0;
		this.anno = null;
		this.classe = null;
	}

	public void stampaAttestato(String classe, String anno) throws SQLException {
		DemoApplication application = new DemoApplication();

		ConnessioneDb c = new ConnessioneDb();
		ResultSet r = c.select("select u.name, u.email, c.name as classe, y.description as anno from ca_users as u, ca_frequented_classes as fc, ca_school_classes as c, ca_school_years as y\n" +
				"    where fc.school_class_id = c.id and\n" +
				"          fc.user_id = u.id and\n" +
				"          c.school_year_id = y.id and\n" +
				"          y.description = \"Anno scolastico "+ anno + "\" and\n" +
				"          c.name = \"" + classe + "\"");
		while(r.next()){
			application.addAlunniNome(r.getString("name"));
			application.addAlunni(r.getString("email"));
			application.setAnno(r.getString("anno"));
			application.setClasse(r.getString("classe"));
		}

		c.chiudi();

		application.getCorsiAlunno(application.getAlunni().get(0));
		System.out.println(application.getJsonArray().get(1).getAsJsonObject().get("somma_ore").getClass().getName());

		try {
			FileInputStream fis = new FileInputStream("attestato2324.docx");
			XWPFDocument doc = new XWPFDocument(fis);
			List<XWPFTable> tables = doc.getTables();
			for(int i= 0; i<application.getJsonArray().size(); i++){
				XWPFTableRow newRow = tables.get(0).createRow();
				newRow.getCell(0).setText(application.getJsonArray().get(i).getAsJsonObject().get("title").getAsString().replaceAll("\"", ""));
				if (!application.getJsonArray().get(i).getAsJsonObject().get("somma_ore").isJsonNull()){
					newRow.getCell(1).setText(application.getJsonArray().get(i).getAsJsonObject().get("somma_ore").getAsString().replaceAll("\"", ""));
				}else{
					newRow.getCell(1).setText("0");
				}
				if (!application.getJsonArray().get(i).getAsJsonObject().get("ore_totali").isJsonNull()){
					newRow.getCell(2).setText(application.getJsonArray().get(i).getAsJsonObject().get("ore_totali").getAsString().replaceAll("\"", ""));
				}else{
					newRow.getCell(2).setText("0");
				}
				newRow.getCell(3).setText(application.getJsonArray().get(i).getAsJsonObject().get("professore").getAsString().replaceAll("\"", ""));
			}
			// Itera sui paragrafi del documento
			boolean finePagina = false;
			int j = 1;
			int x = 0;
			for (XWPFParagraph p : doc.getParagraphs()) {
				// Itera sui runs (porzioni di testo) del paragrafo
				for (XWPFRun run : p.getRuns()) {
					String text = run.getText(0);
					System.out.println(text);
					if (text != null && text.contains("{name}")) {
						// Sostituisci il testo del tag con il nuovo testo desiderato
						System.out.println(application.getJsonArray().toString());
						text = text.replace("{name}", application.getAlunniNome().get(j-1));
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
					if (text != null && text.contains("dataCorrente")) {
						// Sostituisci il testo del tag con il nuovo testo desiderato
						DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
						text = text.replace("dataCorrente", LocalDate.now().format(formatoFecha));
						run.setText(text, 0);
						System.out.println(j);
						if(j < application.getAlunni().size()){
							application.deleteAll();
							application.getCorsiAlunno((application.getAlunni().get(j)));
							for(int i= 0; i<application.getJsonArray().size(); i++){
								XWPFTableRow newRow = tables.get(j).createRow();
								newRow.getCell(0).setText(application.getJsonArray().get(i).getAsJsonObject().get("title").getAsString().replaceAll("\"", ""));
								if (!application.getJsonArray().get(i).getAsJsonObject().get("somma_ore").isJsonNull()){
									newRow.getCell(1).setText(application.getJsonArray().get(i).getAsJsonObject().get("somma_ore").getAsString().replaceAll("\"", ""));
								}else{
									newRow.getCell(1).setText("0");
								}
								if (!application.getJsonArray().get(i).getAsJsonObject().get("ore_totali").isJsonNull()){
									newRow.getCell(2).setText(application.getJsonArray().get(i).getAsJsonObject().get("ore_totali").getAsString().replaceAll("\"", ""));
								}else{
									newRow.getCell(2).setText("0");
								}
								newRow.getCell(3).setText(application.getJsonArray().get(i).getAsJsonObject().get("professore").getAsString().replaceAll("\"", ""));
							}
							j++;

						}else{
							finePagina = true;
						}

					}
				}
				x++;
				if (finePagina) break;
			}
			for (; x < doc.getParagraphs().size() ; x++) {
				XWPFParagraph paragraph = doc.getParagraphs().get(x);
				paragraph.getCTP().newCursor().removeXml();
			}
			//elimina tutte le tabelle in più
			for(int z = j; z<doc.getTables().size(); z++){
				XWPFTable table = doc.getTables().get(z);
				table.getCTTbl().newCursor().removeXml();
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
		} catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
