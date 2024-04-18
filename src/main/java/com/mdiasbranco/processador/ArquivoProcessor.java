package com.mdiasbranco.processador;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.*;

public class ArquivoProcessor {
    private static final String REGEX_START_NUMBER = "^\\d.*";
    private static final String REGEX_NUMBER = "[^0-9]+";
    private static final String IDENT_CONTA = "Identificação";
    private static final String OUTPUT_FILE_PATH = "TEMP_DIR/output_mdiasbranco.txt";
    private static final String TXT_DIRECTORY = "TEMP_DIR/TXTs";

    private static final String BASE_DIR = "TEMP_DIR/";

    public static void main(String[] args) {
        try {
            FileWriter writer = new FileWriter(OUTPUT_FILE_PATH, StandardCharsets.UTF_8);

            Path directory = Paths.get(TXT_DIRECTORY);

            processarDiretorio(directory, writer);

            writer.close();

            System.out.println("Arquivo de saída gerado com sucesso.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void processarDiretorio(Path directory, FileWriter writer) throws IOException {
        List<Path> files = Files.list(directory)
                .filter(file -> file.toString().endsWith(".txt"))
                .collect(Collectors.toList());

        processarArquivo(files, writer);
    }

    private static void processarArquivo(List<Path> files, FileWriter writer) {
        String numConta = "";

        boolean header = false;

        for (Path file : files) {
            try (BufferedReader reader = criarBufferedReader(file)) {
                reader.readLine();
                String line;

                while ((line = reader.readLine()) != null) {
                    boolean startsWithNumber = line.matches(REGEX_START_NUMBER);

                    numConta = numConta.isEmpty() ? extrairNumeroConta(line) : numConta;

                    if (line.startsWith("Tel") && !header ) {
                        escreverCabecalho(writer, line);
                        header = true;
                    }else if (startsWithNumber && !numConta.isEmpty()) {
                        escreverLinhaConta(writer, numConta, line);
                    }
                }
                numConta = "";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static BufferedReader criarBufferedReader(Path arquivo) throws IOException {
        return Files.newBufferedReader(arquivo, StandardCharsets.ISO_8859_1);
    }

    private static String extrairNumeroConta(String line) {
        if (line.contains(IDENT_CONTA)) {
            return line.replaceAll(REGEX_NUMBER, "").trim();
        }
        return "";
    }

    private static void escreverCabecalho(FileWriter writer, String line) throws IOException {
        writer.write("Conta;" + line + "\n");
    }

    private static void escreverLinhaConta(FileWriter writer, String numConta, String line) throws IOException {
        line = numConta + ";" + line;
        writer.write(line + "\n");
    }
}
