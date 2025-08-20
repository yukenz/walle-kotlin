package id.co.awan.walle.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Hackathon BlockDev Id",
        description = "Simulasi Hackathon",
        version = "v1.0",
        contact = Contact(
            name = "Yuyun Purniawan",
            email = "yuyun.purniawan@gmail.com"
        ),
        license = License(name = "MIT"),
        summary = "In Development",
        termsOfService = "Nothing"
    )
)
class OpenAPIConfig {
}