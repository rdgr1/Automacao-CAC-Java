package  org.autocac.utils;
import java.io.*;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

public class FileUtils {
    public static void savePdfFromBase64(String pdfBase64, String fileName, String directory) {
        try {
            // Certifique-se de que o diretório existe
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // Caminho completo do arquivo
            String filePath = Paths.get(directory, fileName + ".pdf").toString();
            // Decodificar e salvar o arquivo
            byte[] decodedBytes = Base64.getDecoder().decode(pdfBase64);
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(decodedBytes);
            }
            System.out.println("PDF salvo como: " + filePath);
        } catch (Exception e) {
            System.err.println("Erro ao salvar o PDF: " + e.getMessage());
        }
    }
    public static void savePdfFromBytes(byte[] pdfBytes, String fileName, String outputDirectory) {
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File outputFile = new File(dir, fileName);
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            out.write(pdfBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static byte[] decompressGzip(byte[] compressedData) throws IOException {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipIn = new GZIPInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) > 0) {
                byteOut.write(buffer, 0, len);
            }
            return byteOut.toByteArray();
        }
    }
}
