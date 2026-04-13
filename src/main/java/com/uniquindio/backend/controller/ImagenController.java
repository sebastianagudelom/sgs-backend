package com.uniquindio.backend.controller;

import com.uniquindio.backend.service.ImagenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor
public class ImagenController {

    private final ImagenService imagenService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> subirImagen(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        String url = imagenService.guardarImagen(archivo);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
