package api.rest.forohub.infra.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {
    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Foro Hub API")
                        .version("1.0.0")
                        .description("""
                                API REST para gestión de foros de discusión.
                                
                                ## Autenticación
                                Esta API usa **JWT (JSON Web Tokens)** con un esquema de doble token:
                                - **Access Token** (2 horas): se envía en el header **Authorization: Bearer <token>**
                                - **Refresh Token** (30 días): se gestiona automáticamente mediante cookie HttpOnly
                                
                                ## Cómo autenticarse en Swagger
                                1. Ejecuta **/auth/login** o **/auth/register**
                                2. Copia el **accessToken** de la respuesta
                                3. Haz clic en el botón **Authorize 🔒** en la parte superior
                                4. Pega el token y confirma
                                """)
                )
                .components(new Components()
                        .addSecuritySchemes("bearer-key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa el accessToken obtenido en /auth/login o /auth/register")
                        )
                );
    }
}
