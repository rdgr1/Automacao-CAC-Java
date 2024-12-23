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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class DataCacService {
    public List<PersonCacModel> create() throws IOException {

        List<PersonCacModel> persons = new ArrayList<>();

        @Cleanup FileInputStream file = new FileInputStream("src/main/resources/CERTIDÃOPOLICIAFEDERAL.xlsx");

        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        List<Row> rows = (List<Row>) toList(sheet.iterator());
        rows.remove(0);
        rows.forEach(row ->{
         List<Cell> cells = (List<Cell>) toList(row.cellIterator());

         PersonCacModel personCac =  PersonCacModel.builder()
                 .name(cells.get(1).getStringCellValue())
                 .motherName(cells.get(2).getStringCellValue())
                 .cpf(cells.get(3).getStringCellValue())
                 .dateOfBirth(String.valueOf(cells.get(4).getDateCellValue()))
                 .build();

         persons.add(personCac);

        });

        return persons;
    }

    public List<?> toList(Iterator<?> iterator){
        return IteratorUtils.toList(iterator);
    }

    public void scannerCac(List<PersonCacModel> persons){
        Gson gson = new Gson();
        String jsonArray = gson.toJson(persons);
    }
}
