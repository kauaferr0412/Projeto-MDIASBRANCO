package com.mdiasbranco.processador;

import org.springframework.boot.SpringApplication;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipProcessor {

    private static final String ZIP_FILE_PATH = "TEMP_DIR/zip_mdiasbranco.zip";
    private static final String EXTRACT_DIR = "TEMP_DIR/EXTRACT_ZIP";
    private static final String TXT_DIR = "TEMP_DIR/TXTs";

    public static void main(String[] args) {

        ensureDirectoriesExist();

        try {
            extractFiles(ZIP_FILE_PATH, EXTRACT_DIR);
            processTXTFiles(EXTRACT_DIR);
            System.out.println("Processamento concluído.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void ensureDirectoriesExist() {
        try {
            Path extractDirPath = Paths.get(EXTRACT_DIR);
            if (!Files.exists(extractDirPath)) {
                Files.createDirectories(extractDirPath);
                System.out.println("Diretório de extração não encontrado, criando diretório...");
            }

            Path txtDirPath = Paths.get(TXT_DIR);
            if (!Files.exists(txtDirPath)) {
                Files.createDirectories(txtDirPath);
                System.out.println("Diretório de arquivos TXT não encontrado, criando diretório...");
            }

        } catch (IOException e) {
            System.out.println("Ocorreu um erro ao criar os diretórios necessários.");
            e.printStackTrace();
        }
    }

    private static void extractFiles(String zipFilePath, String extractDir) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String filePath = extractDir + File.separator + entry.getName();
                    File file = new File(filePath);
                    file.getParentFile().mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipInputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }

                    if (filePath.toLowerCase().endsWith(".zip")) {
                        extractFiles(filePath, extractDir);
                    }
                }
            }
        }
    }

    private static void processTXTFiles(String dirPath) {
        try {
            Path extractDirPath = Paths.get(dirPath);
            if (!Files.exists(extractDirPath)) {
                System.out.println("O diretório de extração não existe.");
                return;
            }

            Path txtDirPath = Paths.get(TXT_DIR);
            if (!Files.exists(txtDirPath)) {
                Files.createDirectories(txtDirPath);
                System.out.println("Diretório de arquivos TXT não encontrado, criando diretório...");
            }

            Files.walk(extractDirPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                    .forEach(ZipProcessor::copyTXTFile);

            System.out.println("Todas as cópias dos arquivos TXT foram criadas com sucesso.");
        } catch (IOException e) {
            System.out.println("Ocorreu um erro ao criar cópias dos arquivos TXT.");
            e.printStackTrace();
        }
    }

    private static void copyTXTFile(Path txtFilePath) {
        try {
            Path destPath = Paths.get(TXT_DIR, txtFilePath.getFileName().toString());
            Files.copy(txtFilePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Cópia do arquivo TXT criada com sucesso: " + destPath.getFileName());
            processTXTFile(destPath.toFile());
        } catch (IOException e) {
            System.out.println("Não foi possível criar cópia do arquivo TXT: " + txtFilePath.getFileName());
            e.printStackTrace();
        }
    }

    private static void processTXTFile(File txtFile) {
        try {
            ArquivoProcessor.main(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
