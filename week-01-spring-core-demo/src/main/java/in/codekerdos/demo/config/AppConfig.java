package in.codekerdos.demo.config;

import in.codekerdos.demo.service.GreetingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "in.codekerdos.demo")
public class AppConfig {

    @Bean
    public GreetingService greetingService() {
        return new GreetingService("CodeKerdos Spring Boot + AI Bootcamp");
    }
}
