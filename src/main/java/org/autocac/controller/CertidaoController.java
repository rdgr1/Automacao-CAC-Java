package org.autocac.controller;

import org.autocac.models.PersonCacModel;
import org.autocac.service.DataCacService;
import org.autocac.service.ScrapService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@RestController
@RequestMapping(value = "/api/certidao/")
public class CertidaoController {

    private final ScrapService servicePF;
    private final DataCacService dataPF;

    public CertidaoController(ScrapService servicePF, DataCacService dataPF) {
        this.servicePF = servicePF;
        this.dataPF = dataPF;
    }
    @GetMapping("/template/{tipo}")
    public ResponseEntity<Resource> baixarTemplate(@PathVariable String tipo) throws IOException {
        String nomeArquivo;
        switch (tipo.toLowerCase()){
            case "policia":
                nomeArquivo = "modelo_policia.xlsx";
                break;
            case "tjdft":
                nomeArquivo = "modelo_tjdf.csv";
                break;
            default:
                return ResponseEntity.badRequest().build();
        }
        Path path = Paths.get("src/main/resources/" + nomeArquivo);
        if (!Files.exists(path)){
            return ResponseEntity.notFound().build();
        }
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .body(resource);
    }
    @PostMapping("/gerar")
    public ResponseEntity<String> gerarCertidao(@RequestParam("file")MultipartFile file, @RequestParam("tipo") String tipo){
        try{
            if("policia".equalsIgnoreCase(tipo)){
                List<PersonCacModel> pessoas = dataPF.create(file);
                servicePF.gerarCertidaoPF(pessoas);
                return ResponseEntity.ok("Certidões geradas com sucesso!");
            } else {
                return ResponseEntity.badRequest().body("Tipo de certidão não suportado ainda !");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }
}
