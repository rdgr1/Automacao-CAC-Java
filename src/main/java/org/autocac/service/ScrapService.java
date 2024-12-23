package org.autocac.service;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.autocac.models.PersonCacModel;
import org.autocac.utils.SeleniumConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.time.format.DateTimeFormatter;
import java.util.*;
import org.autocac.utils.LoadingUtils;
public class ScrapService {
    public static void main(String[] args) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int totalDuration = 7500; // Tempo total em milissegundos (5 segundos)
        int interval = 500;      // Intervalo de atualização do loading (500ms)
        int steps = totalDuration / interval;
        WebDriver driver = SeleniumConfig.initializeDriver();
        try {
            System.out.println(Ansi.colorize("Iniciando Scrap...", Attribute.BOLD(),Attribute.BRIGHT_MAGENTA_TEXT()));
            // Executando script
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("window.open('https://servicos.pf.gov.br/epol-sinic-publico/','_blank');");
            LoadingUtils.showLoading("Carregando Página",steps,interval);
           // Alternando aba
            String newTabHandle = driver.getWindowHandles().toArray()[1].toString();
            driver.switchTo().window(newTabHandle);
            // Criar uma instância da classe Actions
            Actions actions = new Actions(driver);
            LoadingUtils.showLoading("Clicando captcha!",steps,interval);
            // Clique em coordenadas específicas
            actions.moveByOffset(200, 300).click().perform();
            // Aguardando carregar
            LoadingUtils.showLoading("Validando Captcha",steps,interval);
            // Verificando Página
            System.out.println(Ansi.colorize("Tiítulo:",Attribute.BOLD(),Attribute.BRIGHT_MAGENTA_TEXT()) + driver.getTitle());
            // Map de cookies
            Map<String, String> sessionCookies = new HashMap<>();
            // Salvando cookies
            for (Cookie cookie : driver.manage().getCookies()){
                sessionCookies.put(cookie.getName(), cookie.getValue());
            }
            System.out.println(Ansi.colorize("Cookies Coletados!",Attribute.BRIGHT_MAGENTA_TEXT()));
            DataCacService data = new DataCacService();
            List<PersonCacModel> listFunc = data.create();
            for (PersonCacModel personCacModel : listFunc){
                // Filtrando Dados
                String nome_func = personCacModel.getName().trim();
                String cpf_func = personCacModel.getCpf().replace(".","").replace("-","");
                String nome_mom = personCacModel.getMotherName().trim();
                String dataBorn = personCacModel.getDateOfBirth().formatted(dateTimeFormatter);
                // Criando header get
                Map<String,Object> headerGet = new HashMap<>();
                headerGet.put("User-Agent", ((JavascriptExecutor) driver).executeScript("return navigator.userAgent"));
                headerGet.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                // Criando header post
                Map<String,Object> headerPost = new HashMap<>();
                headerPost.put("Content-Type", "application/json");
                headerPost.put("User-Agent", ((JavascriptExecutor) driver).executeScript("return navigator.userAgent"));
                headerPost.put("Accept", "application/json, text/plain, */*");
                headerPost.put("Accept-Language","pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3");
                headerPost.put("Accept-Encoding","gzip, deflate, br, zstd");
                headerPost.put("Origin","https://servicos.pf.gov.br");
                headerPost.put("Referer","https://servicos.pf.gov.br/epol-sinic-publico/");
                headerPost.put("Connection","keep-alive");
                // Criando payload
                Map<String, Object> payload = new HashMap<>();
                // Inserindo dados na payload
                payload.put("cpf",cpf_func);
                payload.put("nome",nome_func);
                payload.put("listaNacionalidade",24);
                payload.put("dtNascimento",dataBorn);
                payload.put("coPaisNascimento",24);
                payload.put("noUfNascimento","");
                payload.put("noMunicipioNascimento","");
                payload.put("coMunicipioNascimento","");
                payload.put("nomePai","");
                payload.put("nomeMae",nome_mom);
                payload.put("documentoCAC",new ArrayList<String>());
                // Fazendo Requisição GET
                try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpGet getRequest = new HttpGet("");
                    for (Map.Entry<String,Object> entry : headerGet.entrySet()) {
                        getRequest.setHeader(entry.getKey(), entry.getValue().toString());
                    }
                    try(CloseableHttpResponse response = httpClient.execute(getRequest)) {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        System.out.println(Ansi.colorize("Código:",Attribute.BOLD(),Attribute.BRIGHT_MAGENTA_TEXT()) + response.getStatusLine().getStatusCode());
                        System.out.println(Ansi.colorize("Result",Attribute.BOLD(),Attribute.BRIGHT_MAGENTA_TEXT())+ responseBody);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
