package com.example.demo;

import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        DemoApplication application = new DemoApplication();
        application.stampaAttestato( "4AII","2022 - 2023");
    }

}
