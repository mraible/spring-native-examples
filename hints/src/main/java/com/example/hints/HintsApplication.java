package com.example.hints;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.nativex.hint.*;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;

class C {
}

/*
@NativeHint(
//	trigger = ReactiveSecurityAutoConfiguration.class,
	options = {"--enable-http", "--enable-https"},
//	resources = {@ResourceHint(patterns = "/sa.properties")} ,
	serializables = {@SerializationHint(types = C.class)},
	types = @TypeHint(types = {C.class}, access = AccessBits.ALL),
	jdkProxies = @JdkProxyHint(types = {}),
	aotProxies = @AotProxyHint()
)*/

@SpringBootApplication
public class HintsApplication {

	@Bean
	ApplicationRunner runner() {
		return events -> {
			var resource = new ClassPathResource("/log4j2.springboot");
			try (var in = new InputStreamReader(resource.getInputStream())) {
				System.out.println(FileCopyUtils.copyToString(in));
			}
		};
	}

	public static void main(String[] args) throws Exception {

		SpringApplication.run(HintsApplication.class, args);
	}

}
