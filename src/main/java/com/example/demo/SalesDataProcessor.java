package com.example.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SalesDataProcessor {
    private Map<Integer, Map<String, Double>> yearlyProfits = new HashMap<>();


    private final String[] monthNames = {
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    };

    public void loadData(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            Cell dateCell = row.getCell(5);
            Cell profitCell = row.getCell(4);

            LocalDate date;
            if (dateCell.getCellType() == CellType.STRING) {
                date = LocalDate.parse(dateCell.getStringCellValue(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            } else {
                date = dateCell.getDateCellValue().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            }

            double profit = profitCell.getNumericCellValue();
            int year = date.getYear();
            String month = monthNames[date.getMonthValue() - 1];

            yearlyProfits
                    .computeIfAbsent(year, k -> new HashMap<>())
                    .merge(month, profit, Double::sum);
        }

        workbook.close();
    }

    public Map<String, Double> getMonthlyProfits(int year) {
        Map<String, Double> profits = yearlyProfits.getOrDefault(year, new HashMap<>());

        Map<String, Double> sortedProfits = new LinkedHashMap<>();
        for (String month : monthNames) {
            if (profits.containsKey(month)) {
                sortedProfits.put(month, profits.get(month));
            }
        }

        return sortedProfits;
    }
}
