package org.autocac.service;

import org.autocac.models.PersonCacModel;
import com.google.gson.Gson;
import lombok.Cleanup;
import org.apache.commons.collections4.IteratorUtils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class DataCacService {

    public List<PersonCacModel> create() throws IOException {

        List<PersonCacModel> persons = new ArrayList<>();

        @Cleanup FileInputStream file = new FileInputStream("src/main/resources/ModeloPF.xlsx");

        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        List<Row> rows = (List<Row>) toList(sheet.iterator());
        rows.remove(0);

        // Formato desejado para a data (formato americano)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        rows.forEach(row -> {
            List<Cell> cells = (List<Cell>) toList(row.cellIterator());

            // 🔒 Proteção contra linhas incompletas
            if (cells.size() < 5) {
                //System.err.println("Linha ignorada (colunas insuficientes): " + cells.size() + " colunas.");
                return;
            }

            // Obtendo e formatando a data de nascimento
            String formattedDate = "";
            try {
                Date date = cells.get(4).getDateCellValue();
                formattedDate = dateFormat.format(date);
            } catch (Exception e) {
                System.err.println("Erro ao formatar a data de nascimento: " + e.getMessage());
            }

            PersonCacModel personCac = PersonCacModel.builder()
                    .name(cells.get(1).getStringCellValue())
                    .motherName(cells.get(2).getStringCellValue())
                    .cpf(cells.get(3).getStringCellValue())
                    .dateOfBirth(formattedDate)
                    .build();

            persons.add(personCac);
        });
        return persons;
    }

    public List<?> toList(Iterator<?> iterator) {
        return IteratorUtils.toList(iterator);
    }

    public void scannerCac(List<PersonCacModel> persons) {
        Gson gson = new Gson();
        String jsonArray = gson.toJson(persons);
        System.out.println(jsonArray);
    }
}
