package com.ua.bikeshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входу застосунку «VeloUA» — навчальна велокрамниця.
 *
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan:
 * піднімає IoC-контейнер, сканує пакет com.ua.bikeshop і запускає вбудований Tomcat.
 */
@SpringBootApplication
public class BikeshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(BikeshopApplication.class, args);
    }

}
