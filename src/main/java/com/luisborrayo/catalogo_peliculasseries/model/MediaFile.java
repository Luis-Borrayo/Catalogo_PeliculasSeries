package com.luisborrayo.catalogo_peliculasseries.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "media_files")
public class MediaFile {

    public enum FileType {
        POSTER,
        TECHNICAL_SHEET
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mediaFileId;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "media_title_id", nullable = false)
    private MediaTitle mediaTitle;

    @NotNull
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @NotNull
    @Size(max = 500)
    private String blobUrl;

    @Size(max = 100)
    private String etag;

    @Size(max = 50)
    private String contentType;

    private Long sizeBytes;

    @Column(updatable = false)
    private LocalDateTime uploadedAt;

    @Size(max = 50)
    private String uploadedBy;

    @Column(nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onUpload() {
        this.uploadedAt = LocalDateTime.now();
    }

    public MediaFile() {
    }

    public MediaFile(MediaTitle mediaTitle, FileType fileType, String blobUrl, String uploadedBy) {
        this.mediaTitle = mediaTitle;
        this.fileType = fileType;
        this.blobUrl = blobUrl;
        this.uploadedBy = uploadedBy;
    }

    public Long getMediaFileId() {
        return mediaFileId;
    }

    public void setMediaFileId(Long mediaFileId) {
        this.mediaFileId = mediaFileId;
    }

    public MediaTitle getMediaTitle() {
        return mediaTitle;
    }

    public void setMediaTitle(MediaTitle mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getBlobUrl() {
        return blobUrl;
    }

    public void setBlobUrl(String blobUrl) {
        this.blobUrl = blobUrl;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}