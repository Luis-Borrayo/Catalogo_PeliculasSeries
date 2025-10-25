package com.luisborrayo.catalogo_peliculasseries.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movie_genres")
public class MovieGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movieGenreId;

    @Column(length = 50, nullable = false, unique = true)
    @NotNull
    @Size(min = 3, max = 50)
    private String genreName;

    @OneToMany(mappedBy = "movieGenre", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MediaTitleGenre> titles = new HashSet<>();

    public MovieGenre() {
    }

    public MovieGenre(String genreName) {
        this.genreName = genreName;
    }

    public Long getMovieGenreId() {
        return movieGenreId;
    }

    public void setMovieGenreId(Long movieGenreId) {
        this.movieGenreId = movieGenreId;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public Set<MediaTitleGenre> getTitles() {
        return titles;
    }

    public void setTitles(Set<MediaTitleGenre> titles) {
        this.titles = titles;
    }
}