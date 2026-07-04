package in.codekerdos.expense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ExpenseApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseApprovalApplication.class, args);
    }
}
