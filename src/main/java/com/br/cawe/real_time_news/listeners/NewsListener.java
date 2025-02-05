package com.br.cawe.real_time_news.listeners;

import com.br.cawe.real_time_news.entities.NewsItem;

public interface NewsListener {
    void onNewsItemAdded(NewsItem newsItem);
}
