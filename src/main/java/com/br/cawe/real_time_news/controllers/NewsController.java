package com.br.cawe.real_time_news.controllers;

import com.br.cawe.real_time_news.entities.NewsItem;
import com.br.cawe.real_time_news.listeners.NewsListener;
import com.br.cawe.real_time_news.services.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class NewsController implements NewsListener {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
        this.newsService.registerListener(this);
    }

    @GetMapping("/news")
    public SseEmitter subscribeToNews() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        // Send existing news on subscription
        newsService.getNewsItems().forEach(newsItem -> {
            try {
                emitter.send(SseEmitter.event().name("NEWS").data(newsItem.toString()));
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PostMapping("/news/{title}/{content}")
    public ResponseEntity registerNews(@PathVariable String title, @PathVariable String content) {
        newsService.addNewsItem(title, content);
        return ResponseEntity.ok("news created with success");
    }

    @Override
    public void onNewsItemAdded(NewsItem newsItem) {
        dispatchNewsItem(newsItem);
    }

    public void dispatchNewsItem(NewsItem newsItem) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("NEWS").data(newsItem.toString()));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        });
        emitters.removeAll(deadEmitters);
    }
}