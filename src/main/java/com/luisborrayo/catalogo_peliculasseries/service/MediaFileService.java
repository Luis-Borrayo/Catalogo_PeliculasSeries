package com.luisborrayo.catalogo_peliculasseries.service;

import com.luisborrayo.catalogo_peliculasseries.model.MediaFile;
import com.luisborrayo.catalogo_peliculasseries.model.MediaFile.FileType;
import com.luisborrayo.catalogo_peliculasseries.model.MediaTitle;
import com.luisborrayo.catalogo_peliculasseries.repositories.MediaFileRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.Part;
import jakarta.transaction.Transactional;

import java.io.InputStream;

@ApplicationScoped
@Transactional
public class MediaFileService {

    @Inject
    private MediaFileRepository mediaFileRepository;

    @Inject
    private AzureBlobService azureBlobService;

    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2 MB
    private static final long MAX_PDF_SIZE = 5 * 1024 * 1024;   // 5 MB

    public MediaFile uploadFile(MediaTitle mediaTitle, Part filePart,
                                FileType fileType, String uploadedBy) {
        try {
            validateFile(filePart, fileType);

            if (fileType == FileType.POSTER) {
                deactivatePreviousPoster(mediaTitle.getMediaTitleId());
            }

            String blobUrl = azureBlobService.uploadFile(
                    filePart.getInputStream(),
                    getFileName(filePart),
                    filePart.getContentType(),
                    filePart.getSize(),
                    mediaTitle.getTitleName(),
                    fileType
            );

            MediaFile mediaFile = new MediaFile(mediaTitle, fileType, blobUrl, uploadedBy);
            mediaFile.setContentType(filePart.getContentType());
            mediaFile.setSizeBytes(filePart.getSize());
            mediaFile.setIsActive(true);

            return mediaFileRepository.save(mediaFile);

        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo: " + e.getMessage(), e);
        }
    }

    public void deleteFile(Long fileId) {
        MediaFile file = mediaFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

        file.setIsActive(false);
        mediaFileRepository.save(file);
    }

    public MediaFile findById(Long fileId) {
        return mediaFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));
    }

    private void deactivatePreviousPoster(Long titleId) {
        mediaFileRepository.findActiveFileByTitleAndType(titleId, FileType.POSTER)
                .ifPresent(poster -> {
                    poster.setIsActive(false);
                    mediaFileRepository.save(poster);
                });
    }

    private void validateFile(Part filePart, FileType fileType) {
        String contentType = filePart.getContentType();
        long size = filePart.getSize();

        if (fileType == FileType.POSTER) {
            if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
                throw new IllegalArgumentException("Solo se permiten imágenes JPEG o PNG para pósters");
            }
            if (size > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("El póster no puede superar 2 MB");
            }
        } else if (fileType == FileType.TECHNICAL_SHEET) {
            if (!contentType.equals("application/pdf")) {
                throw new IllegalArgumentException("Solo se permiten archivos PDF para fichas técnicas");
            }
            if (size > MAX_PDF_SIZE) {
                throw new IllegalArgumentException("El PDF no puede superar 5 MB");
            }
        }
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "file";
    }
}