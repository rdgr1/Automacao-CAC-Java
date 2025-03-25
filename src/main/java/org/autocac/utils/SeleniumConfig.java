package org.autocac.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SeleniumConfig {
    public static WebDriver initializeDriver(){
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
        ChromeOptions options = new ChromeOptions();

        // Equivalentes às opções do Python
        options.addArguments("--disable-blink-features=AutomationControlled"); // Evitar detecção de automação
        options.addArguments("--disable-extensions");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-browser-side-navigation");
        options.addArguments("--disable-gpu");
        //options.addArguments("--headless"); // Executar o Chrome em headless mode
        // Equivalente ao useAutomationExtension=false no Python
        options.setExperimentalOption("useAutomationExtension", false);

        // Equivalente ao excludeSwitches no Python
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));

        // Retornar o WebDriver configurado
        return new ChromeDriver(options);
    }
}
