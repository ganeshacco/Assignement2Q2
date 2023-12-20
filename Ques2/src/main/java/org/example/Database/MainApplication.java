package org.example.Database;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.*;

import com.itextpdf.layout.element.Image;
import InterviewObject.Interview;
import org.jfree.chart.JFreeChart;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.List;
public class MainApplication {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/q2data";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "Acc0@user";

    public static void main(String[] args) {

        SpringApplication.run(MainApplication.class, args);
        ReadExcelFile fileReader = new ReadExcelFile();
        String filePath = "C:\\Users\\ganesh.raigond\\Downloads\\Accolite Interview Data.xlsx";
        List<Interview> interviewList = fileReader.readExcelFile(filePath);
        System.out.println(interviewList.size());

        interviewList.parallelStream().forEach(MainApplication::insertDataIntoSQLTable);

        generateCharts(interviewList);;
    }

    private static void generateCharts(List<Interview> interviewList) {
        String pdfPath = "generatedcharts.pdf";

        try (OutputStream os = new FileOutputStream(pdfPath);
             PdfWriter writer = new PdfWriter(os);
             PdfDocument pdfDocument = new PdfDocument(writer);
             Document document = new Document(pdfDocument)) {


            WriteSQLQueries queries = new WriteSQLQueries();
            JFreeChart chart1 = queries.MaxInterviewsQuery();
            System.out.println(chart1);
            BufferedImage image = chart1.createBufferedImage(700, 500);
            Image itextImage = new Image(ImageDataFactory.create(image, Color.blue));

            document.add(itextImage);

            JFreeChart chart2 = queries.MinInterviewsQuery();
            BufferedImage image2 = chart2.createBufferedImage(700, 500);
            Image itextImage2 = new Image(ImageDataFactory.create(image2, Color.blue));

            document.add(itextImage2);

            JFreeChart chart5 = queries.getTop3killsForPeakTime();
            BufferedImage image5 = chart5.createBufferedImage(700, 500);
            Image itextImage5 = new Image(ImageDataFactory.create(image5, Color.blue));

            document.add(itextImage5);

            JFreeChart chart4 = queries.getTop3Skills();
            BufferedImage image4 = chart4.createBufferedImage(700, 500);
            Image itextImage4 = new Image(ImageDataFactory.create(image4, Color.blue));

            document.add(itextImage4);

            JFreeChart chart3 = queries.getTop3Panels(interviewList);
            System.out.println(chart3);
            BufferedImage image3 = chart3.createBufferedImage(700, 500);
            Image itextImage3 = new Image(ImageDataFactory.create(image3, Color.blue));

            document.add(itextImage3);

            System.out.println("Path of the generated pdf: " + pdfPath);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertDataIntoSQLTable(Interview interview) {
        String sql = "INSERT INTO interviews (idate, imonth,teamName,panelName,round,skill,itime,currLocation,prefLocation,candidateName) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, interview.getDate());
            statement.setDate(2, interview.getMonth());
            statement.setString(3, interview.getTeamName());
            statement.setString(4, interview.getPanelName());
            statement.setString(5, interview.getRound());
            statement.setString(6, interview.getSkill());
            statement.setTime(7, interview.getTime());
            statement.setString(8, interview.getCurrLocation());
            statement.setString(9, interview.getPrefLocation());
            statement.setString(10, interview.getCandidateName());
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Employee inserted successfully!");
            } else {
                System.out.println("Failed to insert employee.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)
}