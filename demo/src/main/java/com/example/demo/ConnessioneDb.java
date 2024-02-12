package com.example.demo;

import java.sql.*;

public class ConnessioneDb {
    private Connection conn = null;
    private Statement db = null;
    private ResultSet rs = null;

    public ConnessioneDb(){
        try {
            //driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");

            //connessione al database
            String url = "jdbc:mysql://localhost:3306/calvino_academy";
            String username = "root"; // Nome utente di default di XAMPP è root
            String password = ""; // Password vuota di default

            conn = DriverManager.getConnection(url, username, password);

            db = conn.createStatement();

        } catch (ClassNotFoundException e){
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void chiudi() throws SQLException {
        conn.close();
    }

    public ResultSet select(String query) throws SQLException {
        return db.executeQuery(query);
    }
}
