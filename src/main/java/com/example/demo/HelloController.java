package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Map;

public class HelloController {
    @FXML
    private LineChart<String, Number> salesChart;
    @FXML
    private ComboBox<Integer> yearComboBox;

    private SalesDataProcessor dataProcessor = new SalesDataProcessor();

    @FXML
    public void initialize() {
        // Предлагаемые годы
        yearComboBox.getItems().addAll(2023, 2024, 2025);
    }

    @FXML
    public void handleLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                dataProcessor.loadData(file); // Загружаем данные
                System.out.println("Файл успешно загружен!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void updateChart() {
        Integer year = yearComboBox.getValue();
        if (year == null) {
            System.out.println("Пожалуйста, выберите год!");
            return;
        }

        Map<String, Double> monthlyProfits = dataProcessor.getMonthlyProfits(year);

        salesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Прибыль за " + year);

        for (Map.Entry<String, Double> entry : monthlyProfits.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        salesChart.getData().add(series);
    }
}