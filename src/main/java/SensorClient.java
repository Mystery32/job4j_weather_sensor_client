import dto.MeasurementDTO;
import dto.MeasurementsResponse;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SensorClient {

    public static void main(String[] args) {

        final String sensorName = "Sensor2";
//        addSensor(sensorName);
//        Random random = new Random();
//        for (int i = 0; i < 1000; i++) {
//            System.out.println(i);
//            addMeasurement(random.nextDouble(-100, 100), random.nextBoolean(), sensorName);
//        }
        List<Double> temperatures = getMeasurements();
        drawChart(temperatures);
    }

    private static void addSensor(String sensorName) {
        final String url = "http://localhost:8080/sensors/registration";
        Map<String, Object> jsonToSend = new HashMap<>();
        jsonToSend.put("name", sensorName);
        makePostRequestWithJSONData(url, jsonToSend);
    }

    private static void addMeasurement(double value, boolean raining, String sensorName) {
        final String url = "http://localhost:8080/measurements/add";
        Map<String, Object> jsonToSend = new HashMap<>();
        jsonToSend.put("value", value);
        jsonToSend.put("raining", raining);
        jsonToSend.put("sensor", Map.of("name", sensorName));
        makePostRequestWithJSONData(url, jsonToSend);
    }

    private static void makePostRequestWithJSONData(String url, Map<String, Object> jsonData) {
        final RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(jsonData, headers);
        try {
            restTemplate.postForObject(url, request, String.class);
            System.out.println("Данные успешно отправлены на сервер");
        } catch (HttpClientErrorException e) {
            System.out.println("Ошибка!");
            System.out.println(e.getMessage());
        }
    }

    private static List<Double> getMeasurements() {
        final String url = "http://localhost:8080/measurements/";
        final RestTemplate restTemplate = new RestTemplate();
        MeasurementsResponse jsonResponse = restTemplate.getForObject(url, MeasurementsResponse.class);
        if (jsonResponse == null || jsonResponse.getMeasurements() == null) {
            return Collections.emptyList();
        }
        return jsonResponse.getMeasurements().stream().map(MeasurementDTO::getValue).collect(Collectors.toList());
    }

    private static void drawChart(List<Double> temperatures) {
        double[] xData = IntStream.range(0, temperatures.size()).asDoubleStream().toArray();
        double[] yData = temperatures.stream().mapToDouble(x -> x).toArray();
        XYChart chart = QuickChart.getChart("Temperatures", "X", "Y", "temperature",
                xData, yData);
        new SwingWrapper(chart).displayChart();
    }
}
