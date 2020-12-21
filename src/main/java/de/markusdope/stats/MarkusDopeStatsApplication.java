package de.markusdope.stats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MarkusDopeStatsApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(MarkusDopeStatsApplication.class, args);
		}
		catch (Exception e) {
			//Shutdown JVM on Spring Context initialization Errror
			System.exit(-1);
		}
	}

}
