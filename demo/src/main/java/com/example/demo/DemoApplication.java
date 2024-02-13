package com.example.demo;

import org.apache.poi.hssf.record.HCenterRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.scheduling.annotation.EnableScheduling;
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
@EnableScheduling
public class DemoApplication {
	public static void main(String[] args) throws SQLException {
		SpringApplication.run(DemoApplication.class, args);
		Controller c = new Controller();
	}
}


	/*
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

	 */
