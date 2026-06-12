package in.codekerdos.demo;

import in.codekerdos.demo.config.AppConfig;
import in.codekerdos.demo.scope.RequestLogger;
import in.codekerdos.demo.service.GreetingService;
import in.codekerdos.demo.service.NotificationService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        System.out.println("=== CodeKerdos Week 1 — Day 1 Demo ===\n");

        // 1. @Bean from @Configuration
        GreetingService greetingService = context.getBean(GreetingService.class);
        System.out.println("1. GreetingService (via @Bean):");
        System.out.println("   " + greetingService.getGreeting() + "\n");

        // 2. Constructor injection via @Service
        NotificationService notificationService = context.getBean(NotificationService.class);
        System.out.println("2. NotificationService (Constructor Injection):");
        notificationService.notifyUser("student@codekerdos.in", "Welcome to Week 1!");
        System.out.println();

        // 3. Prototype scope demo
        RequestLogger logger1 = context.getBean(RequestLogger.class);
        RequestLogger logger2 = context.getBean(RequestLogger.class);
        System.out.println("3. Bean Scope Demo (Prototype):");
        System.out.println("   Logger 1 ID: " + logger1.getId());
        System.out.println("   Logger 2 ID: " + logger2.getId());
        System.out.println("   Same instance? " + (logger1 == logger2));

        System.out.println("\n=== Day 1 Demo Complete ===");
    }
}
