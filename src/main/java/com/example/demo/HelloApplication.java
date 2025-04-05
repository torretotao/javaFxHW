package com.example.demo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;

public class HelloApplication extends Application {
    private final List<Record> records = new ArrayList<>();
    private final BorderPane root = new BorderPane();

    @Override
    public void start(Stage stage) {
        root.setStyle("-fx-background-color: #2b2b2b;");
        Button loadButton = new Button("Выберите файл");
        loadButton.setStyle("-fx-background-color: #444; -fx-text-fill: #90ee90; -fx-font-size: 16px;");

        loadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx", "*.xls"));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    loadData(file);
                    showChartWithData();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        VBox vbox = new VBox(loadButton);
        vbox.setStyle("-fx-alignment: center; -fx-padding: 20;");
        root.setCenter(vbox);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Продажи");
        stage.show();
    }

    private void loadData(File file) throws IOException {
        records.clear();
        FileInputStream fis = new FileInputStream(file);
        Workbook book = new XSSFWorkbook(fis);
        Sheet sheet = book.getSheetAt(0);
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            int id = (int) row.getCell(0).getNumericCellValue();
            String name = row.getCell(1).getStringCellValue();
            double price = row.getCell(2).getNumericCellValue();
            int quantity = (int) row.getCell(3).getNumericCellValue();
            double total = row.getCell(4).getNumericCellValue();
            LocalDate date = row.getCell(5).getDateCellValue().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            records.add(new Record(id, name, price, quantity, total, date));
        }
        book.close();
    }

    private void showChartWithData() {
        Map<Integer, double[]> yearToMonthlySales = new HashMap<>();
        for (Record r : records) {
            int year = r.date().getYear();
            yearToMonthlySales.putIfAbsent(year, new double[12]);
            yearToMonthlySales.get(year)[r.date().getMonthValue() - 1] += r.total();
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("График продаж по годам");
        chart.setStyle("-fx-background-color: #2b2b2b;");

        xAxis.setLabel("Месяц");
        yAxis.setLabel("Сумма прибыли");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.LIGHTGREEN);
        yAxis.setTickLabelFill(javafx.scene.paint.Color.LIGHTGREEN);
        xAxis.lookup(".axis-label").setStyle("-fx-text-fill: #90ee90;");
        yAxis.lookup(".axis-label").setStyle("-fx-text-fill: #90ee90;");

        chart.setStyle("-fx-background-color: #2b2b2b;");
        chart.lookup(".chart-plot-background").setStyle("-fx-background-color: #2b2b2b;");
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);

        List<String> months = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            months.add(LocalDate.of(2000, i + 1, 1)
                    .getMonth().getDisplayName(TextStyle.SHORT, new Locale("ru")));
        }
        xAxis.setCategories(FXCollections.observableArrayList(months));

        for (Map.Entry<Integer, double[]> entry : yearToMonthlySales.entrySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(entry.getKey().toString());
            double[] monthly = entry.getValue();
            for (int i = 0; i < 12; i++) {
                series.getData().add(new XYChart.Data<>(months.get(i), monthly[i]));
            }
            chart.getData().add(series);
        }

        VBox centerBox = new VBox(chart);
        centerBox.setStyle("-fx-background-color: #2b2b2b;");
        root.setCenter(centerBox);

        Map<String, Double> uniqueProducts = new LinkedHashMap<>();
        for (Record r : records) {
            uniqueProducts.put(r.name(), r.price());
        }

        VBox productList = new VBox(5);
        productList.setStyle("-fx-padding: 10; -fx-background-color: #2b2b2b;");
        for (Map.Entry<String, Double> entry : uniqueProducts.entrySet()) {
            Label label = new Label(entry.getKey() + " — " + String.format("%.2f", entry.getValue()) + " ₽");
            label.setStyle("-fx-text-fill: #90ee90; -fx-font-size: 14px;");
            productList.getChildren().add(label);
        }

        root.setBottom(productList);
    }

    public record Record(int id, String name, double price, int quantity, double total, LocalDate date) {}

    public static void main(String[] args) {
        launch();
    }
}
