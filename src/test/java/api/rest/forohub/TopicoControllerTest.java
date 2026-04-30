package api.rest.forohub;

import api.rest.forohub.domain.topicos.Topico;
import api.rest.forohub.domain.topicos.TopicoRepository;
import api.rest.forohub.domain.topicos.dto.DatosRegistroTopico;
import api.rest.forohub.domain.topicos.validacion.ValidadorTopicoDuplicado;
import api.rest.forohub.domain.usuario.Usuario;
import api.rest.forohub.domain.usuario.UsuarioRepository;
import api.rest.forohub.infra.exceptions.RecursoDuplicadoException;
import api.rest.forohub.infra.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TopicoController - Pruebas de Integración")
class TopicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private TopicoRepository topicoRepository;

    @MockitoBean
    private ValidadorTopicoDuplicado validador;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;
    private String token;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 1L);
        ReflectionTestUtils.setField(usuario, "login", "testuser");
        ReflectionTestUtils.setField(usuario, "contrasena", "password");

        token = tokenService.generarToken(usuario);

        when(usuarioRepository.findByLogin("testuser")).thenReturn(usuario);
    }

    private Topico crearTopico() {
        var datos = new DatosRegistroTopico("Titulo test", "Mensaje test", "Curso test");
        var topico = new Topico(datos, usuario);
        ReflectionTestUtils.setField(topico, "id", 1L);
        ReflectionTestUtils.setField(topico, "fecha", LocalDateTime.now());
        return topico;
    }

    // ─── GET /topicos ───

    @Test
    @DisplayName("GET /topicos con usuario autenticado debe retornar 200")
    void listar_topicos_autenticado_retorna_200() throws Exception {
        var topico = crearTopico();
        var page = new PageImpl<>(List.of(topico));
        when(topicoRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/topicos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /topicos sin autenticación debe retornar 401")
    void listar_topicos_sin_auth_retorna_401() throws Exception {
        mockMvc.perform(get("/topicos"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /topicos/{id} ───

    @Test
    @DisplayName("GET /topicos/{id} con ID existente debe retornar 200")
    void detallar_topico_existente_retorna_200() throws Exception {
        var topico = crearTopico();
        when(topicoRepository.findById(1L)).thenReturn(Optional.of(topico));

        mockMvc.perform(get("/topicos/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Titulo test"));
    }

    @Test
    @DisplayName("GET /topicos/{id} con ID inexistente debe retornar 404")
    void detallar_topico_inexistente_retorna_404() throws Exception {
        when(topicoRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/topicos/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /topicos/{id} sin autenticación debe retornar 401")
    void detallar_topico_sin_auth_retorna_401() throws Exception {
        mockMvc.perform(get("/topicos/1"))
                .andExpect(status().isUnauthorized());
    }

    // ─── POST /topicos ───

    @Test
    @DisplayName("POST con datos válidos y usuario autenticado debe retornar 201")
    void registrar_topico_valido_retorna_201() throws Exception {
        var datos = new DatosRegistroTopico("Titulo test", "Mensaje test", "Curso test");
        var topico = crearTopico();

        doNothing().when(validador).validar(any());
        when(topicoRepository.save(any())).thenReturn(topico);

        mockMvc.perform(post("/topicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST con tópico duplicado debe retornar 409")
    void registrar_topico_duplicado_retorna_409() throws Exception {
        var datos = new DatosRegistroTopico("Titulo repetido", "Mensaje repetido", "Curso");

        doThrow(new RecursoDuplicadoException("Ya existe un tópico con el mismo titulo y mensaje"))
                .when(validador).validar(any());

        mockMvc.perform(post("/topicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST sin autenticación debe retornar 401")
    void registrar_topico_sin_auth_retorna_401() throws Exception {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", "Curso");

        mockMvc.perform(post("/topicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST con campos vacíos debe retornar 400")
    void registrar_topico_campos_vacios_retorna_400() throws Exception {
        var datosInvalidos = new DatosRegistroTopico("", "", "");

        mockMvc.perform(post("/topicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosInvalidos)))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /topicos/{id} ───

    @Test
    @DisplayName("PUT por el autor del tópico debe retornar 200")
    void actualizar_topico_por_autor_retorna_200() throws Exception {
        var topico = crearTopico();
        var actualizacion = new api.rest.forohub.domain.topicos.dto.DatosActualizacionTopico(
                "Titulo actualizado", "Mensaje actualizado", "Curso actualizado");

        when(topicoRepository.findById(1L)).thenReturn(Optional.of(topico));

        mockMvc.perform(put("/topicos/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT por usuario distinto al autor debe retornar 403")
    void actualizar_topico_por_otro_usuario_retorna_403() throws Exception {
        var otroUsuario = new Usuario();
        ReflectionTestUtils.setField(otroUsuario, "id", 2L);
        ReflectionTestUtils.setField(otroUsuario, "login", "otrouser");
        ReflectionTestUtils.setField(otroUsuario, "contrasena", "password");

        var topico = crearTopico(); // autor es usuario con id=1
        var actualizacion = new api.rest.forohub.domain.topicos.dto.DatosActualizacionTopico(
                "Titulo", "Mensaje", "Curso");

        var tokenOtroUsuario = tokenService.generarToken(otroUsuario);
        when(usuarioRepository.findByLogin("otrouser")).thenReturn(otroUsuario);
        when(topicoRepository.findById(1L)).thenReturn(Optional.of(topico));

        mockMvc.perform(put("/topicos/1")
                        .header("Authorization", "Bearer " + tokenOtroUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT con ID inexistente debe retornar 404")
    void actualizar_topico_inexistente_retorna_404() throws Exception {
        var actualizacion = new api.rest.forohub.domain.topicos.dto.DatosActualizacionTopico(
                "Titulo", "Mensaje", "Curso");

        when(topicoRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/topicos/99")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT sin autenticación debe retornar 401")
    void actualizar_topico_sin_auth_retorna_401() throws Exception {
        var actualizacion = new api.rest.forohub.domain.topicos.dto.DatosActualizacionTopico(
                "Titulo", "Mensaje", "Curso");

        mockMvc.perform(put("/topicos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isUnauthorized());
    }

    // ─── DELETE /topicos/{id} ───

    @Test
    @DisplayName("DELETE con ID existente debe retornar 204")
    void eliminar_topico_existente_retorna_204() throws Exception {
        var topico = crearTopico();
        when(topicoRepository.findById(1L)).thenReturn(Optional.of(topico));

        mockMvc.perform(delete("/topicos/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE con ID inexistente debe retornar 404")
    void eliminar_topico_inexistente_retorna_404() throws Exception {
        when(topicoRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/topicos/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE sin autenticación debe retornar 401")
    void eliminar_topico_sin_auth_retorna_401() throws Exception {
        mockMvc.perform(delete("/topicos/1"))
                .andExpect(status().isUnauthorized());
    }
}