import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class Main {

    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";
    private static final double LAT = 55.75;
    private static final double LON = 37.62;
    private static final int LIMIT = 7;

    public static void main(String[] args) throws IOException, InterruptedException {
        String apiKey = System.getenv("YANDEX_WEATHER_KEY");
        if (apiKey == null) {
            throw new IllegalStateException("Не задана переменная окружения YANDEX_WEATHER_KEY");
        }

        String body = fetchForecast(apiKey);
        System.out.println(body);

        Object json = Json.parse(body);
        System.out.println("Текущая температура: " + path(json, "fact", "temp") + " °C");
        System.out.printf("Средняя температура за %d дн.: %.1f °C%n", LIMIT, averageDayTemp(json));
    }

    private static String fetchForecast(String apiKey) throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "?lat=" + LAT + "&lon=" + LON + "&limit=" + LIMIT);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Сервис вернул " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    private static double averageDayTemp(Object json) {
        List<?> forecasts = (List<?>) path(json, "forecasts");
        return forecasts.stream()
                .limit(LIMIT)
                .mapToDouble(day -> Double.parseDouble((String) path(day, "parts", "day", "temp_avg")))
                .average()
                .orElseThrow();
    }

    private static Object path(Object node, String... keys) {
        for (String key : keys) {
            node = ((Map<?, ?>) node).get(key);
        }
        return node;
    }
}
