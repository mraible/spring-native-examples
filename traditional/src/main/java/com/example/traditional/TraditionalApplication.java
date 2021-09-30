package com.example.traditional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;

@SpringBootApplication
public class TraditionalApplication {

	public static void main(String[] args) {
		SpringApplication.run(TraditionalApplication.class, args);
	}

}


@Controller
@ResponseBody
class CustomerRestController {

	private final CustomerRepository repo;

	CustomerRestController(CustomerRepository repo) {
		this.repo = repo;
	}

	@GetMapping("/customers")
	Collection<Customer> get() {
		return this.repo.findAll();
	}
}

interface CustomerRepository extends JpaRepository<Customer, Integer> {
}

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
class Customer {

	@Id
	@GeneratedValue
	private Integer id;
	private String name;
}