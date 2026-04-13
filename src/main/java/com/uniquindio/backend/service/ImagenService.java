package com.uniquindio.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ImagenService {

    private static final List<String> TIPOS_PERMITIDOS = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5 MB

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Guarda un archivo de imagen y retorna la URL relativa para acceder a ella.
     */
    public String guardarImagen(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }

        if (archivo.getSize() > MAX_SIZE) {
            throw new RuntimeException("El archivo no puede superar los 5 MB");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new RuntimeException("Solo se permiten imágenes (JPEG, PNG, WebP, GIF)");
        }

        String originalFilename = archivo.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String nombreArchivo = UUID.randomUUID() + extension;

        Path directorio = Paths.get(uploadDir);
        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }

        Path rutaArchivo = directorio.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + nombreArchivo;
    }
}
