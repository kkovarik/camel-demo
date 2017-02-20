package cz.kkovarik.demo;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class CamelDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamelDemoApplication.class, args);
    }

    @Bean
    ServletRegistrationBean camelServlet() {
        // because we want to use the camel-servlet component for the Camel REST service
        ServletRegistrationBean mapping = new ServletRegistrationBean();
        mapping.setName("CamelServlet");
        mapping.setLoadOnStartup(1);
        // CamelHttpTransportServlet is the name of the Camel servlet to use
        mapping.setServlet(new CamelHttpTransportServlet());
        mapping.addUrlMappings("/camel/*");
        return mapping;
    }
}
