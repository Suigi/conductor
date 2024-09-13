package ninja.ranner.conductor;

import ninja.ranner.conductor.adapter.out.terminal.Printer;
import ninja.ranner.conductor.domain.Timer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConductorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConductorApplication.class, args);
    }

    @Bean
    CommandLineRunner renderSimpleUi() {
        return (String... args) -> {
            Printer printer = new Printer();
            printer.print(new Timer(10));
        };
    }

}
