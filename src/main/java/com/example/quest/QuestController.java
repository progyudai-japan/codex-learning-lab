package com.example.quest;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quests")
public class QuestController {
    private final QuestService service;
    public QuestController(QuestService service) { this.service = service; }
    public record Answer(Integer option) {}
    @GetMapping
    public List<QuestService.Quest> quests() { return service.quests(); }
    @PostMapping("/{id}/answers")
    public QuestService.Result answer(@PathVariable String id, @RequestBody Answer answer) {
        return service.check(id, answer.option());
    }
}
