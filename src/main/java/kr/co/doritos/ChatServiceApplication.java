package kr.co.doritos;

import kr.co.doritos.daemon.GarbageCollector;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatServiceApplication implements CommandLineRunner {

	private final GarbageCollector garbageCollector;

	public ChatServiceApplication(GarbageCollector garbageCollector) {
		this.garbageCollector = garbageCollector;
	}

	@Override
	public void run(String... args) throws Exception {
		System.err.println("링크챗 Daemon-Service Run");

		Thread thread = new Thread(garbageCollector);
		thread.setDaemon(true);
		thread.start();
	}

	public static void main(String[] args) {
		SpringApplication.run(ChatServiceApplication.class, args);
	}
}
