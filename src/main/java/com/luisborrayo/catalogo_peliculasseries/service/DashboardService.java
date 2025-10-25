package com.luisborrayo.catalogo_peliculasseries.service;

import com.luisborrayo.catalogo_peliculasseries.model.MediaTitle.TitleType;
import com.luisborrayo.catalogo_peliculasseries.repositories.MediaFileRepository;
import com.luisborrayo.catalogo_peliculasseries.repositories.MediaTitleRepository;
import com.luisborrayo.catalogo_peliculasseries.repositories.MovieGenreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class DashboardService {

    @Inject
    private MediaTitleRepository mediaTitleRepository;

    @Inject
    private MediaFileRepository mediaFileRepository;

    @Inject
    private MovieGenreRepository movieGenreRepository;

    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("totalTitles", mediaTitleRepository.count());

        stats.put("totalMovies", mediaTitleRepository.countByTitleType(TitleType.MOVIE));
        stats.put("totalSeries", mediaTitleRepository.countByTitleType(TitleType.SERIES));

        stats.put("totalGenres", movieGenreRepository.count());

        stats.put("totalFiles", mediaFileRepository.countByIsActive(true));

        stats.put("titlesWithPoster", mediaTitleRepository.countTitlesWithPoster());

        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        stats.put("titlesLastMonth", mediaTitleRepository.countByCreatedAtAfter(lastMonth));

        return stats;
    }
}