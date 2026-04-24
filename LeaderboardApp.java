import java.net.URI;
import java.net.http.*;
import java.util.*;
import org.json.*;

public class LeaderboardApp {

    static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    static final String REG_NO = "RA2311003011635";

    public static void main(String[] args) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        Set<String> uniqueSet = new HashSet<>();
        Map<String, Integer> scoreMap = new HashMap<>();

        int poll = 0;

        while (poll < 10) {

            int retries = 0;
            boolean success = false;

            while (retries < 5) {

                String url = BASE_URL + "/quiz/messages?regNo=" + REG_NO + "&poll=" + poll;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                String body = response.body();

                System.out.println("API Response: " + body);

                // ✅ Valid JSON check
                if (body != null && body.trim().startsWith("{")) {

                    JSONObject obj = new JSONObject(body);
                    JSONArray events = obj.getJSONArray("events");

                    for (int j = 0; j < events.length(); j++) {

                        JSONObject event = events.getJSONObject(j);

                        String roundId = event.getString("roundId");
                        String participant = event.getString("participant");
                        int score = event.getInt("score");

                        String key = roundId + "_" + participant;

                        // Deduplication
                        if (uniqueSet.add(key)) {
                            scoreMap.put(participant,
                                    scoreMap.getOrDefault(participant, 0) + score);
                        }
                    }

                    success = true;
                    break; // exit retry loop
                }

                retries++;
                System.out.println("Retrying poll " + poll + " (" + retries + "/5)");
                Thread.sleep(8000);
            }

            if (!success) {
                System.out.println("Skipping poll " + poll + " after 5 retries");
            }

            poll++;
            Thread.sleep(5000);
        }

        // Sort leaderboard
        List<Map.Entry<String, Integer>> leaderboard =
                new ArrayList<>(scoreMap.entrySet());

        leaderboard.sort((a, b) -> b.getValue() - a.getValue());

        int total = 0;

        System.out.println("\n===== FINAL LEADERBOARD =====");
        for (Map.Entry<String, Integer> entry : leaderboard) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
            total += entry.getValue();
        }

        System.out.println("Total Score = " + total);

        // Submit result
        submitLeaderboard(client, leaderboard);
    }

    static void submitLeaderboard(HttpClient client,
            List<Map.Entry<String, Integer>> leaderboard) throws Exception {

        JSONArray arr = new JSONArray();

        for (Map.Entry<String, Integer> entry : leaderboard) {
            JSONObject obj = new JSONObject();
            obj.put("participant", entry.getKey());
            obj.put("totalScore", entry.getValue());
            arr.put(obj);
        }

        JSONObject finalObj = new JSONObject();
        finalObj.put("regNo", REG_NO);
        finalObj.put("leaderboard", arr);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(finalObj.toString()))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        System.out.println("\n===== SUBMISSION RESPONSE =====");
        System.out.println(response.body());
    }
}