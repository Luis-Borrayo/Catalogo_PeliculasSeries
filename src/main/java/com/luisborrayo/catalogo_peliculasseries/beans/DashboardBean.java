package com.luisborrayo.catalogo_peliculasseries.beans;

import com.luisborrayo.catalogo_peliculasseries.service.DashboardService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Map;

@Named
@ViewScoped
public class DashboardBean implements Serializable {

    @Inject
    private DashboardService dashboardService;

    private Map<String, Long> stats;

    @PostConstruct
    public void init() {
        loadStats();
    }

    public void loadStats() {
        stats = dashboardService.getDashboardStats();
    }

    public Map<String, Long> getStats() {
        return stats;
    }

    public Long getTotalTitles() {
        return stats.getOrDefault("totalTitles", 0L);
    }

    public Long getTotalMovies() {
        return stats.getOrDefault("totalMovies", 0L);
    }

    public Long getTotalSeries() {
        return stats.getOrDefault("totalSeries", 0L);
    }

    public Long getTotalGenres() {
        return stats.getOrDefault("totalGenres", 0L);
    }

    public Long getTotalFiles() {
        return stats.getOrDefault("totalFiles", 0L);
    }

    public Long getTitlesWithPoster() {
        return stats.getOrDefault("titlesWithPoster", 0L);
    }

    public Long getTitlesLastMonth() {
        return stats.getOrDefault("titlesLastMonth", 0L);
    }
}