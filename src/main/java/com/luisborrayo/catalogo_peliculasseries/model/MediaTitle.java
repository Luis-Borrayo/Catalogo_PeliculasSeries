package com.luisborrayo.catalogo_peliculasseries.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "media_titles")
public class MediaTitle {

    public enum TitleType {
        MOVIE,
        SERIES
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mediaTitleId;

    @NotNull
    @Size(min = 2, max = 150)
    private String titleName;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TitleType titleType;

    @NotNull
    @Min(1900)
    @Max(2100)
    private Integer releaseYear;

    @Size(max = 1000)
    private String synopsis;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private Double averageRating;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "mediaTitle", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MediaTitleGenre> genres = new HashSet<>();

    @OneToMany(mappedBy = "mediaTitle", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MediaFile> mediaFiles = new HashSet<>();

    public MediaTitle() {
    }

    public MediaTitle(String titleName, TitleType titleType, Integer releaseYear) {
        this.titleName = titleName;
        this.titleType = titleType;
        this.releaseYear = releaseYear;
    }

    public Long getMediaTitleId() {
        return mediaTitleId;
    }

    public void setMediaTitleId(Long mediaTitleId) {
        this.mediaTitleId = mediaTitleId;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public TitleType getTitleType() {
        return titleType;
    }

    public void setTitleType(TitleType titleType) {
        this.titleType = titleType;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<MediaTitleGenre> getGenres() {
        return genres;
    }

    public void setGenres(Set<MediaTitleGenre> genres) {
        this.genres = genres;
    }

    public Set<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(Set<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }
}