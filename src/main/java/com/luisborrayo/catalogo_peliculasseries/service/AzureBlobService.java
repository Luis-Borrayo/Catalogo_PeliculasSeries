package com.luisborrayo.catalogo_peliculasseries.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.luisborrayo.catalogo_peliculasseries.model.MediaFile.FileType;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@ApplicationScoped
public class AzureBlobService {

    private BlobContainerClient containerClient;

    private String connectionString;
    private String containerName = "catalogos";

    @PostConstruct
    public void init() {
        connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        String envContainerName = System.getenv("AZURE_STORAGE_CONTAINER_NAME");

        if (envContainerName != null && !envContainerName.isEmpty()) {
            containerName = envContainerName;
        }

        if (connectionString == null || connectionString.isEmpty()) {
            throw new IllegalStateException("AZURE_STORAGE_CONNECTION_STRING no está configurado");
        }

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);

        // Crear contenedor si no existe
        if (!containerClient.exists()) {
            containerClient.create();
        }
    }
    public String uploadFile(InputStream inputStream, String originalFilename,
                             String contentType, long size, String titleName, FileType fileType) {
        try {
            String blobName = generateBlobName(titleName, fileType, originalFilename);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            blobClient.upload(inputStream, size, true);

            blobClient.setMetadata(Map.of(
                    "title", titleName,
                    "fileType", fileType.name(),
                    "originalName", originalFilename
            ));

            return blobClient.getBlobUrl();

        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo a Azure: " + e.getMessage(), e);
        }
    }

    public String generateSasUrl(String blobUrl, int validityMinutes) {
        try {
            String blobName = blobUrl.substring(blobUrl.lastIndexOf("/") + 1);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            BlobSasPermission permissions = new BlobSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(validityMinutes);

            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);

            String sasToken = blobClient.generateSas(sasValues);

            return blobUrl + "?" + sasToken;

        } catch (Exception e) {
            throw new RuntimeException("Error al generar SAS URL: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String blobUrl) {
        String blobName = blobUrl.substring(blobUrl.lastIndexOf("/") + 1);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.deleteIfExists();
    }

    private String generateBlobName(String titleName, FileType fileType, String originalFilename) {
        String folder = fileType == FileType.POSTER ? "posters" : "fichas";
        String sanitizedTitle = titleName.replaceAll("[^a-zA-Z0-9-_]", "_");
        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(originalFilename);

        return String.format("%s/%s/%s%s", folder, sanitizedTitle, timestamp, extension);
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}