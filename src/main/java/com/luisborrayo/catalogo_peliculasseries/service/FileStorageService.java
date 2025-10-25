package com.luisborrayo.catalogo_peliculasseries.service;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.luisborrayo.catalogo_peliculasseries.model.MediaFile.FileType;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio unificado de almacenamiento
 * Maneja automáticamente Azure Blob Storage o almacenamiento local
 * según la configuración en STORAGE_MODE
 */
@ApplicationScoped
public class FileStorageService {

    private BlobContainerClient azureContainer;

    private static final String STORAGE_MODE = System.getenv("STORAGE_MODE") != null ?
            System.getenv("STORAGE_MODE") : "LOCAL"; // AZURE o LOCAL

    private static final String AZURE_CONNECTION_STRING = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
    private static final String AZURE_CONTAINER_NAME = System.getenv("AZURE_STORAGE_CONTAINER_NAME") != null ?
            System.getenv("AZURE_STORAGE_CONTAINER_NAME") : "catalogos";

    private static final Path LOCAL_BASE_DIR = Paths.get(
            System.getenv("LOCAL_STORAGE_PATH") != null ?
                    System.getenv("LOCAL_STORAGE_PATH") : "./storage"
    );
    private static final String POSTERS_FOLDER = "posters";
    private static final String FICHAS_FOLDER = "fichas";
    private static final long SAS_URL_VALIDITY_MINUTES = 60; // 60 minutos

    @PostConstruct
    public void init() {
        if (isAzureMode()) {
            initializeAzure();
        } else {
            initializeLocal();
        }
    }

    private void initializeAzure() {
        try {
            if (AZURE_CONNECTION_STRING == null || AZURE_CONNECTION_STRING.isEmpty()) {
                throw new IllegalStateException("AZURE_STORAGE_CONNECTION_STRING no está configurado");
            }

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(AZURE_CONNECTION_STRING)
                    .buildClient();

            azureContainer = blobServiceClient.getBlobContainerClient(AZURE_CONTAINER_NAME);

            if (!azureContainer.exists()) {
                azureContainer.create();
                System.out.println("✓ Contenedor Azure creado: " + AZURE_CONTAINER_NAME);
            }

            System.out.println("✓ Azure Blob Storage inicializado correctamente");
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando Azure Blob Storage: " + e.getMessage(), e);
        }
    }

    private void initializeLocal() {
        try {
            Files.createDirectories(LOCAL_BASE_DIR);
            Files.createDirectories(LOCAL_BASE_DIR.resolve(POSTERS_FOLDER));
            Files.createDirectories(LOCAL_BASE_DIR.resolve(FICHAS_FOLDER));
            System.out.println("✓ Almacenamiento local inicializado: " + LOCAL_BASE_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Error inicializando almacenamiento local: " + e.getMessage(), e);
        }
    }

    public String saveFile(InputStream inputStream, String originalFilename,
                           String titleName, FileType fileType, String contentType, long size) {

        if (isAzureMode()) {
            return saveToAzure(inputStream, originalFilename, titleName, fileType, contentType, size);
        } else {
            return saveToLocal(inputStream, originalFilename, titleName, fileType);
        }
    }


    public InputStream getFileStream(String filePathOrUrl) {
        if (isAzureMode()) {
            return getStreamFromAzure(filePathOrUrl);
        } else {
            return getStreamFromLocal(filePathOrUrl);
        }
    }


    public String getPublicUrl(String filePathOrUrl) {
        if (isAzureMode()) {
            return generateAzureSasUrl(filePathOrUrl);
        } else {
            // Para local, retornamos la ruta que usará ViewServlet
            return "/view?path=" + java.net.URLEncoder.encode(filePathOrUrl, java.nio.charset.StandardCharsets.UTF_8);
        }
    }


    public void deleteFile(String filePathOrUrl) {
        if (isAzureMode()) {
            deleteFromAzure(filePathOrUrl);
        } else {
            deleteFromLocal(filePathOrUrl);
        }
    }

    private String saveToAzure(InputStream inputStream, String originalFilename,
                               String titleName, FileType fileType, String contentType, long size) {
        try {
            String blobName = generateBlobName(titleName, fileType, originalFilename);
            BlobClient blobClient = azureContainer.getBlobClient(blobName);

            // Configurar headers
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(contentType);

            blobClient.upload(inputStream, size, true);
            blobClient.setHttpHeaders(headers);

            Map<String, String> metadata = new HashMap<>();
            metadata.put("title", sanitizeName(titleName));
            metadata.put("fileType", fileType.name());
            metadata.put("originalName", originalFilename);
            blobClient.setMetadata(metadata);

            return blobClient.getBlobUrl();

        } catch (Exception e) {
            throw new RuntimeException("Error subiendo archivo a Azure: " + e.getMessage(), e);
        }
    }

    private InputStream getStreamFromAzure(String blobUrl) {
        try {
            String blobName = extractBlobNameFromUrl(blobUrl);
            BlobClient blobClient = azureContainer.getBlobClient(blobName);
            return blobClient.openInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Error leyendo archivo de Azure: " + e.getMessage(), e);
        }
    }

    private String generateAzureSasUrl(String blobUrl) {
        try {
            String blobName = extractBlobNameFromUrl(blobUrl);
            BlobClient blobClient = azureContainer.getBlobClient(blobName);

            BlobSasPermission permissions = new BlobSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(SAS_URL_VALIDITY_MINUTES);

            BlobServiceSasSignatureValues sasValues =
                    new BlobServiceSasSignatureValues(expiryTime, permissions);

            String sasToken = blobClient.generateSas(sasValues);
            return blobUrl + "?" + sasToken;

        } catch (Exception e) {
            throw new RuntimeException("Error generando SAS URL: " + e.getMessage(), e);
        }
    }

    private void deleteFromAzure(String blobUrl) {
        try {
            String blobName = extractBlobNameFromUrl(blobUrl);
            BlobClient blobClient = azureContainer.getBlobClient(blobName);
            blobClient.deleteIfExists();
        } catch (Exception e) {
            throw new RuntimeException("Error eliminando archivo de Azure: " + e.getMessage(), e);
        }
    }

    private String extractBlobNameFromUrl(String blobUrl) {
        return blobUrl.substring(blobUrl.lastIndexOf("/") + 1);
    }


    private String saveToLocal(InputStream inputStream, String originalFilename,
                               String titleName, FileType fileType) {
        try {
            String fileName = generateFileName(originalFilename);
            String folder = fileType == FileType.POSTER ? POSTERS_FOLDER : FICHAS_FOLDER;

            String sanitizedTitle = sanitizeName(titleName);
            Path titleFolder = LOCAL_BASE_DIR.resolve(folder).resolve(sanitizedTitle);
            Files.createDirectories(titleFolder);

            Path targetPath = titleFolder.resolve(fileName);
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            return folder + "/" + sanitizedTitle + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Error guardando archivo localmente: " + e.getMessage(), e);
        }
    }

    private InputStream getStreamFromLocal(String relativePath) {
        try {
            Path filePath = LOCAL_BASE_DIR.resolve(relativePath);
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo archivo local: " + e.getMessage(), e);
        }
    }

    private void deleteFromLocal(String relativePath) {
        try {
            Path filePath = LOCAL_BASE_DIR.resolve(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error eliminando archivo local: " + e.getMessage(), e);
        }
    }

    private boolean isAzureMode() {
        return "AZURE".equalsIgnoreCase(STORAGE_MODE);
    }

    private String generateBlobName(String titleName, FileType fileType, String originalFilename) {
        String folder = fileType == FileType.POSTER ? "posters" : "fichas";
        String sanitizedTitle = sanitizeName(titleName);
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(originalFilename);

        return String.format("%s/%s/%s%s", folder, sanitizedTitle, timestamp, extension);
    }

    private String generateFileName(String originalFilename) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(originalFilename);
        return timestamp + extension;
    }

    private String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}