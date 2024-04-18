package com.mdiasbranco.processador;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
public class ProcessadorController {
    private static final String UPLOAD_DIR = "TEMP_DIR/";

    private static final String BASE_DIR = "TEMP_DIR/";

    @PostMapping("/upload")
    public ResponseEntity<Resource> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_DIR + "zip_mdiasbranco.zip");
            Files.write(path, bytes);

            ZipProcessor.main(null);

            File fileResult = new File("TEMP_DIR/output_mdiasbranco.txt");

            Path filePath = Paths.get(fileResult.getAbsolutePath());
            byte[] fileBytes = Files.readAllBytes(filePath);

            Resource resource = new ByteArrayResource(fileBytes);
            deleteDirectory(new File(UPLOAD_DIR));

            System.out.println("Arquivos deletados com sucesso.");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "output_mdiasbranco.txt" + "\"")
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            Files.delete(directory.toPath());
        }
    }
}
