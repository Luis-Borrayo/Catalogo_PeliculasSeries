package com.luisborrayo.catalogo_peliculasseries.beans;

import com.luisborrayo.catalogo_peliculasseries.model.MediaFile;
import com.luisborrayo.catalogo_peliculasseries.model.MediaFile.FileType;
import com.luisborrayo.catalogo_peliculasseries.model.MediaTitle;
import com.luisborrayo.catalogo_peliculasseries.service.AzureBlobService;
import com.luisborrayo.catalogo_peliculasseries.service.MediaFileService;
import com.luisborrayo.catalogo_peliculasseries.repositories.MediaTitleRepository;  // ← CAMBIO AQUÍ

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/media-files")
@Produces(MediaType.APPLICATION_JSON)
public class MediaFileController {

    @Inject
    private MediaFileService mediaFileService;

    @Inject
    private MediaTitleRepository mediaTitleRepository;

    @Inject
    private AzureBlobService azureBlobService;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@Context HttpServletRequest request) {

        try {
            Part filePart = request.getPart("file");

            String mediaTitleIdStr = request.getParameter("mediaTitleId");
            String fileTypeStr = request.getParameter("fileType");
            String uploadedBy = request.getParameter("uploadedBy");

            if (filePart == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "No se proporcionó ningún archivo"))
                        .build();
            }

            if (mediaTitleIdStr == null || fileTypeStr == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Faltan parámetros requeridos"))
                        .build();
            }

            Long mediaTitleId = Long.parseLong(mediaTitleIdStr);
            FileType fileType = FileType.valueOf(fileTypeStr);

            MediaTitle mediaTitle = mediaTitleRepository.findById(mediaTitleId)
                    .orElseThrow(() -> new RuntimeException("Título no encontrado"));

            MediaFile savedFile = mediaFileService.uploadFile(
                    mediaTitle,
                    filePart,
                    fileType,
                    uploadedBy != null ? uploadedBy : "sistema"
            );

            return Response.ok(savedFile).build();

        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "ID de título inválido"))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al subir archivo: " + e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{fileId}")
    public Response deleteFile(@PathParam("fileId") Long fileId) {
        try {
            mediaFileService.deleteFile(fileId);
            return Response.ok(Map.of("message", "Archivo eliminado correctamente")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{fileId}/download-url")
    public Response getDownloadUrl(
            @PathParam("fileId") Long fileId,
            @QueryParam("validityMinutes") @DefaultValue("60") int validityMinutes) {

        try {
            MediaFile file = mediaFileService.findById(fileId);
            String sasUrl = azureBlobService.generateSasUrl(file.getBlobUrl(), validityMinutes);

            return Response.ok(Map.of(
                    "downloadUrl", sasUrl,
                    "expiresIn", validityMinutes + " minutos"
            )).build();

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }
}