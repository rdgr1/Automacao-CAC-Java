package service;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.DeathByCaptcha.Captcha;
import com.DeathByCaptcha.Client;
import com.DeathByCaptcha.Exception;
import com.DeathByCaptcha.SocketClient;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;

public class DeathByCaptchaUtils {

    public static File targetFolder = new File("/home/rrxx/Área de trabalho/Certidões");

    public static String[] headers = {"Nome", "CPF", "Mãe", "Código de Status", "Certidão Existente", "ID", "Data da Solicitação", "Data de Validade", "Data de Emissão", "Tipo de Certidão", "Status", "URL da Certidão"};
    public static Workbook workbook;
    public static Sheet sheet;
    public static CellStyle style;
    public static Font font;
    public static Row headerRow;
    public static int rowNum;


    public static void startDocumentTasks() {
        if (!targetFolder.exists()) {
            targetFolder.mkdirs(); // Cria o diretório caso não exista
        }
        rowNum = 1;

        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Exportação Certidões");
        style = workbook.createCellStyle();
        font = workbook.createFont();
        headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        String username = "zemmus";
        String password = "wstar640";
        String proxy = "";
        String proxytype = "";
        String googlekey = "6LdF880UAAAAAH3wiwYD1ZjpMsR2gkXmvs_wy38W";
        String pageurl = "https://cnc.tjdft.jus.br/solicitacao-externa";
        //String nome = "";
        //String cpf = "";
        //String mae = "";

        String csvFile = "/home/rrxx/Projetos/IdeaProjects/nada_consta_tjdft/src/main/resources/nadaconsta.csv";
        String line = "";
        String csvSplitBy = ";";
        Semaphore semaphore = new Semaphore(10); // Semaphore with 10 permits

        //Executor executor = Executors.newCachedThreadPool();
        ExecutorService executor = Executors.newCachedThreadPool();
        //int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            boolean isFirstLine = true; // Flag to check if it's the first line

            while ((line = br.readLine()) != null) {

                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] fields = line.split(csvSplitBy);

                // Assuming fields are in the order: nome, cpf, mae
                String nome = fields[1];
                String cpf = fields[2];
                String mae = fields[3];
                String registro = fields[0];
                String cpfFormatted = cpf.replace("-","").replace(".","").trim();
                String nomeFormatted = nome.trim();
                String maeFormatted = mae.trim();
                semaphore.acquire(); // Acquire a permit before submitting the task

                executor.submit(() -> {
                    try {
                        resolveCaptcha(proxy, proxytype, googlekey, pageurl, username, password, nomeFormatted, cpfFormatted, maeFormatted, registro);
                    } catch (java.lang.Exception e) {
                        e.printStackTrace();
                    } finally {
                        System.out.print("Complete");
                        semaphore.release(); // Release the permit after task completion
                    }
                });
            }

            File excelFile = new File(targetFolder, "nadaconsta.xlsx");
            try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                workbook.write(fileOut);
                System.out.println("Arquivo Excel salvo em: " + excelFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            executor.shutdown();
        }
        //context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Executor de tarefas", "ok."));
    }

    //private static final Client client = (Client) new SocketClient("zemmus", "wstar640");
    public static String resolveCaptcha(String proxy, String proxytype, String googlekey, String pageurl, String username, String password, String nome, String cpf, String mae, String registro) {

        Client client = (Client) (new SocketClient(username, password));
        // Death By Captcha http Client
        // Client client = (Client) (new HttpClient(username, password));
        //client.isVerbose = true;

        /* Using authtoken
           Client client = (Client) new HttpClient(authtoken); */
        try {
            try {
                System.out.println("Your balance is " + client.getBalance() + " US cents");
            } catch (IOException e) {
                System.out.println("Failed fetching balance: " + e.toString());
                //return;
            }

            Captcha captcha = null;
            try {
                // Proxy and reCAPTCHA v2 token data
                // String proxy = "http://user:password@127.0.0.1:1234";
                // String proxytype = "http";

                //String proxy = "";
                //String proxytype = "";
                //String googlekey = "6LdF880UAAAAAH3wiwYD1ZjpMsR2gkXmvs_wy38W";
                //String pageurl = "https://cnc.tjdft.jus.br/solicitacao-externa";
                /* Upload a reCAPTCHA v2 and poll for its status with 120 seconds timeout.
                   Put the token params and timeout (in seconds)
                   0 or nothing for the default timeout value. */
                captcha = client.decode(4, proxy, proxytype, googlekey, pageurl);

                //other method is to send a json with the parameters
                // JSONObject json_params = new JSONObject();
                // json_params.put("proxy", proxy);
                // json_params.put("proxytype", proxytype);
                // json_params.put("googlekey", googlekey);
                // json_params.put("pageurl", pageurl);
                // captcha = client.decode(4, json_params);
            } catch (IOException e) {
                System.out.println(e);
                System.out.println("Failed uploading CAPTCHA");
                //return;
            } catch (InterruptedException ex) {
                Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (null != captcha) {
                System.out.println("CAPTCHA " + captcha.id + " solved: " + captcha.text);
                processWebTasks(nome, cpf, mae, captcha.text, registro);

                // Report incorrectly solved CAPTCHA if necessary.
                // Make sure you've checked if the CAPTCHA was in fact incorrectly
                // solved, or else you might get banned as abuser.
                /*try {
                    if (client.report(captcha)) {
                        System.out.println("Reported as incorrectly solved");
                    } else {
                        System.out.println("Failed reporting incorrectly solved CAPTCHA");
                    }
                } catch (IOException e) {
                    System.out.println("Failed reporting incorrectly solved CAPTCHA: " + e.toString());
                }*/
            } else {
                System.out.println("Failed solving CAPTCHA");
            }
        } catch (com.DeathByCaptcha.Exception e) {
            System.out.println(e);
        }

        return null;
    }

    public static void processWebTasks(String nome, String cpf, String mae, String captcha, String registro) {

        try (WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED)) {
            // Optionally, configure webClient settings here
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);

            // URL for the GET request
            String url = "https://cnc-api.tjdft.jus.br/pessoas/" + cpf + "/externo?captcha=" + captcha;

            // Making the request and retrieving the page
            Page page = webClient.getPage(url);

            // Getting the response from the page
            WebResponse response = page.getWebResponse();
            //WebResponse response = webClient.loadWebResponse(page);

            // Retrieving response headers
            String token = response.getResponseHeaderValue("x-cnc-tkse");

            String content = response.getContentAsString(StandardCharsets.UTF_8);

            String jsonResponse = content;

            System.out.print("Content 1: " + content);

            JSONObject jsonObj = new JSONObject(jsonResponse);
            String nomeReceita = jsonObj.getString("nome_receita");
            String tipoPessoa = jsonObj.getString("tipo_pessoa");
            String cpfCnpj = jsonObj.getString("cpf_cnpj");
            String situacao = jsonObj.getString("situacao");

            System.out.println("Token: " + token);

            url = "https://cnc-api.tjdft.jus.br/solicitacoes/externo";

//            Thread.sleep(5000L);

            WebRequest request = new WebRequest(new URL(url), com.gargoylesoftware.htmlunit.HttpMethod.POST);
            request.setAdditionalHeader("X-CNC-TKSE", token);
            request.setAdditionalHeader("Content-Type", "application/json, text/plain, /");

            String body = "{\"nome\":\"" + nomeReceita + "\",\"cpf_cnpj\":\"" + cpfCnpj + "\",\"nome_mae\":\"" + mae + "\",\"tipo_certidao\":\"ESPECIAL\"}";
            System.out.println(body);
            request.setRequestBody(body);

            // Send the request and receive the response
            response = webClient.loadWebResponse(request);
            int statusCode = response.getStatusCode();
            System.out.println("Status Code: " + statusCode);

            if (statusCode != 200) {

                content = response.getContentAsString(StandardCharsets.UTF_8);

                jsonResponse = content;

                Row row = sheet.createRow(rowNum++);
                Cell cellNome = row.createCell(0);
                Cell cellCPF = row.createCell(1);
                Cell cellMae = row.createCell(2);
                Cell cellCodStatus = row.createCell(3);
                Cell cellCertidaoExistente = row.createCell(4);
                Cell cellId = row.createCell(5);
                Cell cellDataSolicitacao = row.createCell(6);
                Cell cellDataValidade = row.createCell(7);
                Cell cellDataEmissao = row.createCell(8);
                Cell cellTipoCertidao = row.createCell(9);
                Cell cellStatus = row.createCell(10);
                Cell cellUrlCertidao = row.createCell(11);

                switch (statusCode) {
                    case 422:

                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String mensagem = jsonObject.getString("mensagem");

                        cellNome.setCellValue(nomeReceita);
                        cellCPF.setCellValue(cpf);
                        cellMae.setCellValue(mae);
                        cellCodStatus.setCellValue(statusCode);
                        cellCertidaoExistente.setCellValue("");
                        cellId.setCellValue("");
                        cellDataSolicitacao.setCellValue("");
                        cellDataValidade.setCellValue("");
                        cellDataEmissao.setCellValue("");
                        cellTipoCertidao.setCellValue("");
                        cellStatus.setCellValue(mensagem);
                        cellUrlCertidao.setCellValue("");
                        break;
                    default:
                        cellNome.setCellValue(nomeReceita);
                        cellCPF.setCellValue(cpf);
                        cellMae.setCellValue(mae);
                        cellCodStatus.setCellValue(statusCode);
                        cellCertidaoExistente.setCellValue("");
                        cellId.setCellValue("");
                        cellDataSolicitacao.setCellValue("");
                        cellDataValidade.setCellValue("");
                        cellDataEmissao.setCellValue("");
                        cellTipoCertidao.setCellValue("");
                        cellStatus.setCellValue("");
                        cellUrlCertidao.setCellValue("");
                        break;
                }
                //public static String[] headers = {"Nome", "CPF", "Mãe", "Código de Status", "Certidão Existente", "ID", "Data da Solicitação", "Data de Validade", "Data de Emissão", "Tipo de Certidão", "Status", "URL da Certidão"};
                //Row row = sheet.createRow(rowNum++);

            } else {

                // Get the response content
                content = response.getContentAsString(StandardCharsets.UTF_8);

                jsonResponse = content;

                jsonObj = new JSONObject(jsonResponse);
                boolean certidaoExistente = jsonObj.getBoolean("certidao_existente");
                JSONObject certidao = jsonObj.getJSONObject("certidao");
                int idCertidao = certidao.getInt("id");
                String dataSolicitacao = "";
                //String dataValidade = certidao.getString("data_validade");
                String dataValidade = "";
                String dataEmissao = "";
                String tipoCertidao = certidao.getString("tipo_certidao");
                String status = certidao.getString("status");
                String urlCertidao = certidao.getString("url_certidao");

                //response = webClient.loadWebResponse(urlCertidao).getWebResponse();
                request = new WebRequest(new URL(urlCertidao));
                response = webClient.loadWebResponse(request);

                System.out.println("Response Body: " + content);

                File pdfFile = new File(targetFolder, nomeReceita + "_" + registro + "_" + cpf + ".pdf");
                try (InputStream contentAsStream = response.getContentAsStream();
                     OutputStream outputStream = new FileOutputStream(pdfFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = contentAsStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    System.out.println("Arquivo PDF salvo em: " + pdfFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Row row = sheet.createRow(rowNum++);
                Cell cellNome = row.createCell(0);
                Cell cellCPF = row.createCell(1);
                Cell cellMae = row.createCell(2);
                Cell cellCodStatus = row.createCell(3);
                Cell cellCertidaoExistente = row.createCell(4);
                Cell cellId = row.createCell(5);
                Cell cellDataSolicitacao = row.createCell(6);
                Cell cellDataValidade = row.createCell(7);
                Cell cellDataEmissao = row.createCell(8);
                Cell cellTipoCertidao = row.createCell(9);
                Cell cellStatus = row.createCell(10);
                Cell cellUrlCertidao = row.createCell(11);
                cellNome.setCellValue(nomeReceita);
                cellCPF.setCellValue(cpf);
                cellMae.setCellValue(mae);
                cellCodStatus.setCellValue(statusCode);
                cellCertidaoExistente.setCellValue(certidaoExistente);
                cellId.setCellValue(idCertidao);
                cellDataSolicitacao.setCellValue(dataSolicitacao);
                cellDataValidade.setCellValue(dataValidade);
                cellDataEmissao.setCellValue(dataEmissao);
                cellTipoCertidao.setCellValue(tipoCertidao);
                cellStatus.setCellValue(status);
                cellUrlCertidao.setCellValue(urlCertidao);
            }
            System.out.println(rowNum);
        } catch (IOException ex) {
            Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FailingHttpStatusCodeException ex) {
            Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public static String resolveCaptcha(byte[] input) {

        Client client = (Client) new SocketClient("zemmus", "wstar640");

        try {
            double balance = client.getBalance();

            /* Put your CAPTCHA file name, or file object, or arbitrary input stream,
            or an array of bytes, and optional solving timeout (in seconds) here: */
            Captcha captcha = client.decode(input, 30);
            if (null != captcha) {
                /* The CAPTCHA was solved; captcha.id property holds its numeric ID,
                and captcha.text holds its text. */
                System.out.println("CAPTCHA " + captcha.id + " solved: " + captcha.text);

                return captcha.text;
            }
        } catch (IOException ex) {
            Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}