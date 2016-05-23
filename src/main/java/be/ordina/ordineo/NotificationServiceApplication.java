package be.ordina.ordineo;

import be.ordina.ordineo.security.JwtFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.retry.annotation.EnableRetry;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL;

@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
@EnableRetry
@EnableHypermediaSupport(type = HAL)
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

	@Bean
	public FilterRegistrationBean jwtFilter() {
		final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(new JwtFilter());
		registrationBean.addUrlPatterns("/api/*");

		return registrationBean;
	}
}
