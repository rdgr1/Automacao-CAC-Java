---

# **Automação CAC Java**  

Este projeto é uma aplicação desenvolvida para **automatizar o processamento e manipulação de PDFs**, além de gerenciar fluxos administrativos. Ele inclui uma **barra de loading, um sistema de login robusto e um tratamento de erros aprimorado** para garantir uma experiência mais estável.

---

## **📂 Estrutura do Projeto**  

```
Automacao-CAC-Java
├── src/
│   ├── main/
│   │   ├── java/com/projeto/
│   │   ├── resources/
│   ├── test/
│       ├── java/com/projeto/
├── logs/
│   ├── erros.log
├── Pdfs/
│   ├── [arquivos PDFs para processamento]
├── pom.xml
├── README.md
└── .gitignore
```

- **`src/main/java/com/projeto/`** → Código principal da automação.  
- **`src/main/resources/`** → Recursos auxiliares, como configurações e templates.  
- **`logs/erros.log`** → Arquivo contendo logs e mensagens de erro registradas.  
- **`Pdfs/`** → Pasta contendo arquivos PDFs que serão processados.  
- **`pom.xml`** → Gerenciamento de dependências via Maven.  

---

## **🛠 Requisitos**  

- **Java 11+**  
- **Maven** instalado para gerenciamento de dependências.  

---

## **🚀 Instalação e Execução**  

### **1️⃣ Clone o repositório:**  
```bash
git clone https://github.com/rdgr1/Automacao-CAC-Java.git
cd Automacao-CAC-Java
```

### **2️⃣ Compile o projeto:**  
```bash
mvn clean install
```

### **3️⃣ Execute a aplicação:**  
```bash
mvn spring-boot:run
```

Ou execute diretamente com:  
```bash
java -jar target/automacao-cac.jar
```

### **4️⃣ Login e Acompanhamento**  
- O sistema possui uma **interface robusta de login** para acesso seguro.  
- Há uma **barra de loading interativa** para indicar o progresso da automação.  

---

## **⚡ Funcionalidades**  

✅ **Processamento automatizado de PDFs**  
✅ **Sistema de Login robusto**  
✅ **Tratamento avançado de erros com logs detalhados**  
✅ **Interface com Barra de Loading** para indicar o progresso  
✅ **Geração de arquivos de log (`logs/erros.log`)**  

---

## **📝 Observações**  

🔹 **Verifique os PDFs antes da execução** → Certifique-se de que os arquivos necessários estão na pasta `Pdfs/`.  

🔹 **Logs de erros** → Qualquer problema encontrado durante a execução será registrado no arquivo `logs/erros.log`.  

🔹 **Compatibilidade** → O projeto requer **Java 11 ou superior** para funcionamento adequado.  

---

## **🤝 Contribuições**  

Contribuições são bem-vindas! Para contribuir, siga os passos abaixo:  

1️⃣ **Fork o repositório**  
2️⃣ **Crie um branch para sua feature**:  
```bash
git checkout -b feature/nova-feature
```
3️⃣ **Faça commit das mudanças**:  
```bash
git commit -m "Adicionei uma nova feature"
```
4️⃣ **Envie o código para o GitHub**:  
```bash
git push origin feature/nova-feature
```
5️⃣ **Abra um Pull Request** 🚀  

---

## **📜 Licença**  

Este projeto está licenciado sob a **Licença MIT**. Consulte o arquivo `LICENSE` para mais detalhes.  

---

