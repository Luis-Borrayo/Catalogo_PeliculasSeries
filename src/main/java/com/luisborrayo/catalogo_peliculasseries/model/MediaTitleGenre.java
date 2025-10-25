package com.luisborrayo.catalogo_peliculasseries.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "media_title_genres",
        uniqueConstraints = @UniqueConstraint(columnNames = {"media_title_id", "movie_genre_id"})
)
public class MediaTitleGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "media_title_id", nullable = false)
    private MediaTitle mediaTitle;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "movie_genre_id", nullable = false)
    private MovieGenre movieGenre;

    public MediaTitleGenre() {
    }

    public MediaTitleGenre(MediaTitle mediaTitle, MovieGenre movieGenre) {
        this.mediaTitle = mediaTitle;
        this.movieGenre = movieGenre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MediaTitle getMediaTitle() {
        return mediaTitle;
    }

    public void setMediaTitle(MediaTitle mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public MovieGenre getMovieGenre() {
        return movieGenre;
    }

    public void setMovieGenre(MovieGenre movieGenre) {
        this.movieGenre = movieGenre;
    }
}