package org.autocac.service;

import com.google.gson.Gson;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.autocac.models.PersonCacModel;
import org.autocac.utils.SeleniumConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.io.IOException;
import java.util.*;
import org.autocac.utils.LoadingUtils;
import org.openqa.selenium.Cookie;

public class ScrapService {

    private static final Logger logger = LogManager.getLogger(ScrapService.class);

    public static void main(String[] args) {
        int interval = 500; // Intervalo de atualização do loading (500ms)
        WebDriver driver = SeleniumConfig.initializeDriver();

        try {
            logger.info("Iniciando Scrap...");
            LoadingUtils.showLoading("Carregando...",7000, interval);
            // Executando script
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("window.open('https://servicos.pf.gov.br/epol-sinic-publico/', '_blank');");
            LoadingUtils.showLoading("Carregando Página", 7500, interval);

            // Alternando aba
            String newTabHandle = driver.getWindowHandles().toArray()[1].toString();
            driver.switchTo().window(newTabHandle);

            // Criar uma instância da classe Actions
            Actions actions = new Actions(driver);
            LoadingUtils.showLoading("Clicando captcha!", 7500, interval);

            // Clique em coordenadas específicas
            actions.moveByOffset(200, 300).click().perform();

            // Aguardando carregar
            LoadingUtils.showLoading("Validando Captcha", 7500, interval);

            // Verificando Página
            logger.info("Título da página: " + driver.getTitle());

            // Map de cookies
            Map<String, String> sessionCookies = new HashMap<>();

            // Salvando cookies
            for (Cookie cookie : driver.manage().getCookies()) {
                sessionCookies.put(cookie.getName(), cookie.getValue());
            }
            LoadingUtils.showLoading("Salvando cookies...",5000,interval);
            String formattedCookies = "_cfuvid=" + sessionCookies.get("_cfuvid") + "; " +
                    "cf_clearance=" + sessionCookies.get("cf_clearance") + ";";

            logger.info("Cookies coletados com sucesso.");
            LoadingUtils.showLoading("Aguardando tempo!", 5000, interval);

            DataCacService data = new DataCacService();
            List<PersonCacModel> listFunc = data.create();

            for (PersonCacModel personCacModel : listFunc) {
                String nome_func = personCacModel.getName().trim();
                String cpf_func = personCacModel.getCpf().replace(".", "").replace("-", "").trim();
                String nome_mom = personCacModel.getMotherName().trim();
                String dataBorn = personCacModel.getDateOfBirth().trim();

                // Criando header GET
                Map<String, Object> headerGet = new HashMap<>();
                headerGet.put("User-Agent", ((JavascriptExecutor) driver).executeScript("return navigator.userAgent"));
                headerGet.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                headerGet.put("Cookie", formattedCookies);

                // Fazendo Requisição GET
                logger.info("Iniciando requisição GET para obter chave do site.");
                LoadingUtils.showLoading("Iniciando requisição GET",7000,interval);
                OkHttpClient client = new OkHttpClient();
                Request.Builder requestBuilderGet = new Request.Builder()
                        .url("https://servicos.pf.gov.br/sinic2-publico-rest/api/siteKey")
                        .get();

                for (Map.Entry<String, Object> header : headerGet.entrySet()) {
                    requestBuilderGet.addHeader(header.getKey(), header.getValue().toString());
                }

                Request requestGet = requestBuilderGet.build();

                try (Response response = client.newCall(requestGet).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        logger.info("Requisição GET bem-sucedida. Código de resposta: " + response.code());
                        logger.info("Resposta GET: " + response.body().string());
                    } else {
                        logger.error("Erro na requisição GET. Código: " + response.code());
                    }
                } catch (IOException e) {
                    logger.error("Erro ao executar requisição GET", e);
                }
                Map<String, Object> headerPost = new HashMap<>();
                headerPost.put("Content-Type", "application/json");
                headerPost.put("User-Agent", ((JavascriptExecutor) driver).executeScript("return navigator.userAgent"));
                headerPost.put("Accept", "application/json, text/plain, */*");
                headerPost.put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3");
                headerPost.put("Accept-Encoding", "gzip, deflate, br, zstd");
                headerPost.put("Origin", "https://servicos.pf.gov.br");
                headerPost.put("Referer", "https://servicos.pf.gov.br/epol-sinic-publico/");
                headerPost.put("Connection", "keep-alive");
                headerPost.put("Cookie", formattedCookies);
                // Fazendo Requisição POST
                logger.info("Preparando dados para requisição POST.");
                LoadingUtils.showLoading("Preparando dados para requisição POST",7000,interval);
                Map<String, Object> payload = new HashMap<>();
                payload.put("cpf", cpf_func);
                payload.put("nome", nome_func);
                payload.put("listaNacionalidade", Collections.singletonList(24)); // Lista com único elemento
                payload.put("dtNascimento", dataBorn);
                payload.put("coPaisNascimento", 24);
                payload.put("noUfNascimento", null); // Equivalente ao None em Python
                payload.put("noMunicipioNascimento", null); // Equivalente ao None em Python
                payload.put("ufNascimento", null); // Equivalente ao None em Python
                payload.put("coMunicipioNascimento", null);
                payload.put("nomePai", null); // Equivalente ao None em Python
                payload.put("nomeMae", nome_mom);
                payload.put("documentoCAC", new ArrayList<>()); // Lista vazia

                String jsonPayload = new Gson().toJson(payload);
                RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));

                Request.Builder requestBuilderPost = new Request.Builder()
                        .url("https://servicos.pf.gov.br/sinic2-publico-rest/api/cac/gerar-cac-pdf")
                        .post(body);

                for (Map.Entry<String, Object> header : headerPost.entrySet()) {
                    requestBuilderPost.addHeader(header.getKey(), header.getValue().toString());
                }

                Request requestPost = requestBuilderPost.build();
                LoadingUtils.showLoading("Fazendo a requisição...",7000,interval);
                try (Response response = client.newCall(requestPost).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        logger.info("Requisição POST bem-sucedida. Código: " + response.code());
                        String responseBody = response.body().string();
                        logger.info("Resposta POST: " + responseBody);
                    } else {
                        logger.error("Erro na requisição POST. Código: " + response.code());
                        logger.error("Resposta:"+ response.code());
                    }
                } catch (IOException e) {
                    logger.error("Erro ao executar requisição POST", e);
                }
            }
        } catch (Exception e) {
            logger.error("Erro no processo de scraping", e);
        } finally {
            driver.quit();
        }
    }
}
