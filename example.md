```java
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestExample {

    public static void main(String[] args) throws Exception {
        WebDriver driver = // Inicialize o driver do Selenium aqui

        // Criando header GET
        Map<String, String> headerGet = new HashMap<>();
        headerGet.put("User-Agent", (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent"));
        headerGet.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

        // Criando header POST
        Map<String, String> headerPost = new HashMap<>();
        headerPost.put("Content-Type", "application/json");
        headerPost.put("User-Agent", (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent"));
        headerPost.put("Accept", "application/json, text/plain, */*");
        headerPost.put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3");
        headerPost.put("Accept-Encoding", "gzip, deflate, br, zstd");
        headerPost.put("Origin", "https://servicos.pf.gov.br");
        headerPost.put("Referer", "https://servicos.pf.gov.br/epol-sinic-publico/");
        headerPost.put("Connection", "keep-alive");

        // Criando payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("cpf", "12345678900");
        payload.put("nome", "João Silva");
        payload.put("listaNacionalidade", 24);
        payload.put("dtNascimento", "1990-01-01");
        payload.put("coPaisNascimento", 24);
        payload.put("noUfNascimento", "");
        payload.put("noMunicipioNascimento", "");
        payload.put("coMunicipioNascimento", "");
        payload.put("nomePai", "");
        payload.put("nomeMae", "Maria Silva");
        payload.put("documentoCAC", new ArrayList<>());

        // Fazendo Requisição GET
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Configurar e enviar GET
            HttpGet getRequest = new HttpGet("https://exemplo.com/endpoint"); // Substitua pela URL real
            for (Map.Entry<String, String> entry : headerGet.entrySet()) {
                getRequest.setHeader(entry.getKey(), entry.getValue());
            }

            try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
                System.out.println("GET Status: " + response.getStatusLine().getStatusCode());
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("GET Response: " + responseBody);
            }

            // Configurar e enviar POST
            HttpPost postRequest = new HttpPost("https://exemplo.com/endpointPost"); // Substitua pela URL real
            for (Map.Entry<String, String> entry : headerPost.entrySet()) {
                postRequest.setHeader(entry.getKey(), entry.getValue());
            }

            // Configurar payload JSON
            String jsonPayload = new com.google.gson.Gson().toJson(payload);
            postRequest.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                System.out.println("POST Status: " + response.getStatusLine().getStatusCode());
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("POST Response: " + responseBody);
            }
        }
    }
}

```