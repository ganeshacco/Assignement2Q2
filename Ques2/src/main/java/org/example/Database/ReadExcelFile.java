package org.example.Database;

import InterviewObject.Interview;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReadExcelFile {

public List<Interview> readExcelFile(String fileLocation){
    List<Interview> InterviewList = new ArrayList<>();
    try{
        FileInputStream file = new FileInputStream(fileLocation);
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        while(rowIterator.hasNext()) {
            Row row = rowIterator.next();
            int i=0;
            Interview interview = new Interview();
            //for avoiding first row and last one
            if(row.getRowNum() == 0 || row.getRowNum() > 3264) {
                continue;
            }


            java.util.Date utilDate = row.getCell(i++).getDateCellValue();
            java.sql.Date sqlDate=null;
            if(utilDate!=null) {
                sqlDate = new java.sql.Date(utilDate.getTime());
            }
            Cell cell = row.getCell(i++); // Get the cell containing the formula
            CellValue cellValue = evaluator.evaluate(cell);
            String excelDateString = cellValue.getStringValue();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-yy");
            java.util.Date date = null;
            java.sql.Date sql_Date = null;
            if(excelDateString!=null) {
                try {
                    date = dateFormat.parse(excelDateString);
                    // Now 'javaDate' can be used in your database insertion logic
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(date!=null) {
                    sql_Date = new java.sql.Date(date.getTime());
                }
            }
            interview.setDate(sqlDate);
            interview.setMonth(sql_Date);
            interview.setTeamName(getCellValue(row.getCell(i++)));
            interview.setPanelName(getCellValue(row.getCell(i++)));
            interview.setRound(getCellValue(row.getCell(i++)));
            interview.setSkill(getCellValue(row.getCell(i++)));
            Cell timeCell = row.getCell(i++);
            Time timeValue = null;
            if (timeCell.getCellType() == CellType.NUMERIC) {
                // If the cell type is numeric, convert it to LocalTime
                timeValue = new Time((long) (timeCell.getNumericCellValue() * 24 * 60 * 60 * 1000));
            }
            interview.setTime(timeValue);
            interview.setCurrLocation(getCellValue(row.getCell(i++)));
            interview.setPrefLocation(getCellValue(row.getCell(i++)));
            interview.setCandidateName(getCellValue(row.getCell(i++)));

            InterviewList.add(interview);
        }

    } catch (Exception e) {
        throw new RuntimeException(e);
    }

    return InterviewList;
}






    private static String getCellValue(Cell cell) {
        try {
            if (cell == null) {
                return null;
            }
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    return String.valueOf(cell.getNumericCellValue());
                default:
                    return null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return null;
    }
}
