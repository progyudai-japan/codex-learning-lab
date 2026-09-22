package com.example.quest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuestApplicationTest {
    @LocalServerPort int port;
    private final HttpClient client = HttpClient.newHttpClient();
    private HttpResponse<String> request(String path, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        if (body != null) builder.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void servesGameAndQuestionsWithoutAnswers() throws Exception {
        var page = request("/", null);
        assertEquals(200, page.statusCode());
        assertTrue(page.body().contains("Java & Spring Quest"));
        var questions = request("/api/quests", null);
        assertEquals(200, questions.statusCode());
        assertTrue(questions.body().contains("spring-6"));
        assertFalse(questions.body().contains("\"answer\""));
        assertFalse(questions.body().contains("\"explanation\""));
    }
    @Test void checksCorrectAndIncorrectAnswers() throws Exception {
        var correct = request("/api/quests/java-1/answers", "{\"option\":1}");
        assertEquals(200, correct.statusCode());
        assertTrue(correct.body().contains("\"correct\":true"));
        assertTrue(correct.body().contains("\"xp\":100"));
        var wrong = request("/api/quests/java-1/answers", "{\"option\":0}");
        assertTrue(wrong.body().contains("\"correct\":false"));
        assertTrue(wrong.body().contains("\"xp\":0"));
    }
    @Test void rejectsInvalidInputAndMissingQuest() throws Exception {
        for (String body : new String[]{"{}", "{\"option\":-1}", "{\"option\":3}", "not-json"}) {
            assertEquals(400, request("/api/quests/java-1/answers", body).statusCode());
        }
        assertEquals(404, request("/api/quests/missing/answers", "{\"option\":0}").statusCode());
    }
    @Test void allTwelveQuestsHaveExactlyOneCorrectAnswer() {
        var service = new QuestService();
        assertEquals(12, service.quests().size());
        assertEquals(12, service.quests().stream().map(QuestService.Quest::id).distinct().count());
        for (var quest : service.quests()) {
            int correct = 0;
            for (int option = 0; option < quest.options().size(); option++) {
                var result = service.check(quest.id(), option);
                if (result.correct()) { correct++; assertFalse(result.explanation().isBlank()); }
            }
            assertEquals(1, correct, quest.id());
        }
    }
}
