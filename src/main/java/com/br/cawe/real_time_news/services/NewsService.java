package com.br.cawe.real_time_news.services;

import com.br.cawe.real_time_news.entities.NewsItem;
import com.br.cawe.real_time_news.listeners.NewsListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewsService {
    private final List<NewsItem> newsItems = new ArrayList<>();
    private NewsListener newsListener;

    public void addNewsItem(String title, String content) {
        NewsItem newsItem = new NewsItem(title, content, LocalDateTime.now());
        newsItems.add(newsItem);
        notifySubscribers(newsItem);
    }

    public void registerListener(NewsListener listener) {
        this.newsListener = listener;
    }

    private void notifySubscribers(NewsItem newsItem) {
        if (newsListener != null) {
            newsListener.onNewsItemAdded(newsItem);
        }
    }

    public List<NewsItem> getNewsItems() {
        return newsItems;
    }
}
