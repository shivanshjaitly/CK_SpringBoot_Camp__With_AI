package in.codekerdos.demo.service;

public class GreetingService {

    private final String message;

    public GreetingService(String message) {
        this.message = message;
    }

    public String getGreeting() {
        return "Hello from " + message + "!";
    }
}
