package  org.autocac.utils;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.Base64;

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
}
