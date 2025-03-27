package org.autocac.service;

import com.DeathByCaptcha.Captcha;
import com.DeathByCaptcha.Client;
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

    public static File targetFolder = new File("output_directory");

    public static String[] headers = {"Nome", "CPF", "Mãe", "Código de Status", "Certidão Existente", "ID", "Data da Solicitação", "Data de Validade", "Data de Emissão", "Tipo de Certidão", "Status", "URL da Certidão"};
    public static Workbook workbook;
    public static Sheet sheet;
    public static CellStyle style;
    public static Font font;
    public static Row headerRow;
    public static int rowNum;

    public static void main(String[] args) throws Exception {
        startDocumentTasks();
    }

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

        String csvFile = "src/main/resources/modelo_tjdf.csv";
        String line = "";
        String csvSplitBy = ";";
        Semaphore semaphore = new Semaphore(10); // limite de 10 tarefas simultâneas

        ExecutorService executor = Executors.newCachedThreadPool();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] fields = line.split(csvSplitBy);
                String nome = fields[1];
                String cpf = fields[2];
                String mae = fields[3];
                String registro = fields[0];

                String cpfFormatted = cpf.replace("-", "").replace(".", "").trim();
                String nomeFormatted = nome.trim();
                String maeFormatted = mae.trim();

                semaphore.acquire();
                executor.submit(() -> {
                    try {
                        resolveCaptcha(proxy, proxytype, googlekey, pageurl, username, password,
                                nomeFormatted, cpfFormatted, maeFormatted, registro);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        System.out.print("Complete");
                        semaphore.release();
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
    }

    public static String resolveCaptcha(
            String proxy, String proxytype, String googlekey, String pageurl,
            String username, String password,
            String nome, String cpf, String mae, String registro) {

        Client client = new SocketClient(username, password);
        try {
            try {
                System.out.println("Your balance is " + client.getBalance() + " US cents");
            } catch (IOException e) {
                System.out.println("Failed fetching balance: " + e.toString());
            }

            // Tenta resolver o reCAPTCHA
            Captcha captcha = null;
            try {
                captcha = client.decode(4, proxy, proxytype, googlekey, pageurl);
            } catch (IOException e) {
                System.out.println("Failed uploading CAPTCHA: " + e);
            } catch (InterruptedException ex) {
                Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (captcha != null) {
                System.out.println("CAPTCHA " + captcha.id + " solved: " + captcha.text);
                processWebTasks(nome, cpf, mae, captcha.text, registro);
            } else {
                System.out.println("Failed solving CAPTCHA");
            }

        } catch (com.DeathByCaptcha.Exception e) {
            System.out.println("DeathByCaptcha error: " + e);
        }
        return null;
    }

    public static void processWebTasks(String nome, String cpf, String mae, String captcha, String registro) {
        try (WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED)) {

            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);

            // 1) GET em /pessoas/{cpf}/externo
            String urlGet = "https://cnc-api.tjdft.jus.br/pessoas/" + cpf + "/externo?captcha=" + captcha;
            Page page = webClient.getPage(urlGet);
            WebResponse responseGet = page.getWebResponse();
            int statusCodeGet = responseGet.getStatusCode();
            String contentGet = responseGet.getContentAsString(StandardCharsets.UTF_8);

            System.out.println("GET status code: " + statusCodeGet);
            System.out.println("Content GET: " + contentGet);

            // Cria sempre a row pra registrar o resultado
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

            // Preenche algumas colunas básicas antes
            cellNome.setCellValue(nome);
            cellCPF.setCellValue(cpf);
            cellMae.setCellValue(mae);
            cellCodStatus.setCellValue(statusCodeGet);

            // Se o GET não for 200, provavelmente veio erro/HTML
            if (statusCodeGet != 200) {
                // Nao faça parse de JSON (é HTML ou erro)
                // Apenas registra sem parse
                cellCertidaoExistente.setCellValue("");
                cellId.setCellValue("");
                cellDataSolicitacao.setCellValue("");
                cellDataValidade.setCellValue("");
                cellDataEmissao.setCellValue("");
                cellTipoCertidao.setCellValue("");
                cellStatus.setCellValue("Erro GET");
                cellUrlCertidao.setCellValue("");
                return;
            }

            // Se for 200, tenta parsear como JSON
            JSONObject jsonObjGet;
            try {
                jsonObjGet = new JSONObject(contentGet);
            } catch (JSONException e) {
                // Se falhar parse, registra e sai
                cellStatus.setCellValue("Falha parse GET");
                return;
            }

            // Se conseguiu parse, extrai dados
            String nomeReceita = jsonObjGet.optString("nome_receita", "");
            String cpfCnpj = jsonObjGet.optString("cpf_cnpj", "");
            String situacao = jsonObjGet.optString("situacao", "");
            // Header "x-cnc-tkse"
            String token = responseGet.getResponseHeaderValue("x-cnc-tkse");
            System.out.println("Token: " + token);

            // 2) POST em /solicitacoes/externo
            String urlPost = "https://cnc-api.tjdft.jus.br/solicitacoes/externo";
            WebRequest request = new WebRequest(new URL(urlPost), com.gargoylesoftware.htmlunit.HttpMethod.POST);
            if (token != null) {
                request.setAdditionalHeader("X-CNC-TKSE", token);
            }
            request.setAdditionalHeader("Content-Type", "application/json, text/plain, */*");

            // Monta body para o POST
            String body = "{\"nome\":\"" + nomeReceita + "\","
                    + "\"cpf_cnpj\":\"" + cpfCnpj + "\","
                    + "\"nome_mae\":\"" + mae + "\","
                    + "\"tipo_certidao\":\"ESPECIAL\"}";
            request.setRequestBody(body);

            WebResponse responsePost = webClient.loadWebResponse(request);
            int statusCodePost = responsePost.getStatusCode();
            System.out.println("POST status code: " + statusCodePost);

            cellCodStatus.setCellValue(statusCodePost); // substitui o valor do GET pelo do POST

            String contentPost = responsePost.getContentAsString(StandardCharsets.UTF_8);
            System.out.println("POST content: " + contentPost);

            // Se diferente de 200, possivelmente erro
            if (statusCodePost != 200) {
                // Alguns casos podem ter JSON de erro (ex: 422). Tente parsear
                if (statusCodePost == 422) {
                    try {
                        JSONObject jsonError = new JSONObject(contentPost);
                        String mensagem = jsonError.optString("mensagem", "Erro 422");
                        cellStatus.setCellValue(mensagem);
                    } catch (JSONException e) {
                        // Conteúdo não era JSON real
                        cellStatus.setCellValue("422 sem JSON: " + contentPost);
                    }
                } else {
                    // Trata genericamente os outros status
                    cellStatus.setCellValue("Erro POST");
                }
                return;
            }

            // Se 200 no POST, parse JSON de sucesso
            JSONObject jsonObjPost;
            try {
                jsonObjPost = new JSONObject(contentPost);
            } catch (JSONException e) {
                cellStatus.setCellValue("Falha parse POST");
                return;
            }

            // Extração do JSON de resposta
            boolean certidaoExistente = jsonObjPost.optBoolean("certidao_existente", false);
            JSONObject certidao = jsonObjPost.optJSONObject("certidao");

            if (certidao == null) {
                // Nada para ler
                cellStatus.setCellValue("certidao nula na resposta");
                return;
            }

            int idCertidao = certidao.optInt("id", -1);
            String dataSolicitacao = ""; // se não vier no JSON, fica vazio
            String dataValidade = "";
            String dataEmissao = "";
            String tipoCertidao = certidao.optString("tipo_certidao", "");
            String status = certidao.optString("status", "");
            String urlCertidao = certidao.optString("url_certidao", "");

            cellCertidaoExistente.setCellValue(certidaoExistente);
            cellId.setCellValue(idCertidao);
            cellDataSolicitacao.setCellValue(dataSolicitacao);
            cellDataValidade.setCellValue(dataValidade);
            cellDataEmissao.setCellValue(dataEmissao);
            cellTipoCertidao.setCellValue(tipoCertidao);
            cellStatus.setCellValue(status);
            cellUrlCertidao.setCellValue(urlCertidao);

            // Por fim, se tem link PDF, faz GET no PDF e salva
            if (urlCertidao != null && !urlCertidao.isEmpty()) {
                WebRequest pdfRequest = new WebRequest(new URL(urlCertidao));
                WebResponse pdfResponse = webClient.loadWebResponse(pdfRequest);
                // status do PDF
                if (pdfResponse.getStatusCode() == 200) {
                    File pdfFile = new File(targetFolder, nomeReceita + "_" + registro + "_" + cpf + ".pdf");
                    try (
                            InputStream contentAsStream = pdfResponse.getContentAsStream();
                            OutputStream outputStream = new FileOutputStream(pdfFile)
                    ) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = contentAsStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        System.out.println("Arquivo PDF salvo em: " + pdfFile.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Falha ao baixar PDF, status=" + pdfResponse.getStatusCode());
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FailingHttpStatusCodeException ex) {
            Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String resolveCaptcha(byte[] input) {
        Client client = new SocketClient("zemmus", "wstar640");
        try {
            double balance = client.getBalance();
            System.out.println("Balance: " + balance);

            Captcha captcha = client.decode(input, 30);
            if (captcha != null) {
                System.out.println("CAPTCHA " + captcha.id + " solved: " + captcha.text);
                return captcha.text;
            }
        } catch (IOException | com.DeathByCaptcha.Exception | InterruptedException ex) {
            Logger.getLogger(DeathByCaptchaUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}