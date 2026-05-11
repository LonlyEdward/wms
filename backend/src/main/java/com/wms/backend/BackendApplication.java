package com.wms.backend;

//import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BackendApplication {

	public static void main(String[] args) {

////		make spring boot read my .env file and set all key value pairs and set them as java system properties
//		Dotenv dotenv = Dotenv.configure()
//						.directory("../")
//						.ignoreIfMissing()
//						.load();
//
//		dotenv.entries().forEach(entry ->
//				System.setProperty(entry.getKey(), entry.getValue())
//				);

		SpringApplication.run(BackendApplication.class, args);
	}

}
