package org.autocac.models;



import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Builder
@Getter
@Setter
public class PersonCacModel {

    private String name;
    private String cpf;
    private String motherName;
    private String dateOfBirth;

    public PersonCacModel() {
    }

    public PersonCacModel(String name, String cpf, String motherName, String dateOfBirth) {
        this.name = name;
        this.cpf = cpf;
        this.motherName = motherName;
        this.dateOfBirth = dateOfBirth;
    }

}
