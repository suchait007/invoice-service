package com.invoice.service.invoice_service;

import co.elastic.apm.attach.ElasticApmAttacher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients("com.invoice.service.invoice_service.client")
@SpringBootApplication
public class InvoiceServiceApplication {

	public static void main(String[] args) {

		ElasticApmAttacher.attach();
		SpringApplication.run(InvoiceServiceApplication.class, args);
	}

}
