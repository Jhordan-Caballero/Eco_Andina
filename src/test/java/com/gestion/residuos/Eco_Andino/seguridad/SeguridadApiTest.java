package com.gestion.residuos.Eco_Andino.seguridad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de seguridad de la API (RNF002) basadas en el OWASP Top 10.
 * Levantan la aplicación completa en un puerto aleatorio y la atacan por HTTP como lo haría un cliente real,
 * con los usuarios de prueba que crea DataSeeder. Necesitan PostgreSQL encendido (igual que la aplicación).
 * Cada caso corresponde a una fila de la tabla de casos de prueba del informe (PS-01 ... PS-12).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SeguridadApiTest {
    private static final String CLAVE = "EcoAndina2026!";
    private static final Pattern TOKEN = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");

    static {
        // Permite enviar la cabecera Origin (PS-11) en cualquier versión del cliente HTTP de Java
        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "origin");
    }

    @Value("${local.server.port}")
    private int puerto;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    @DisplayName("PS-01 (A07) Sin token, los endpoints protegidos responden 401")
    void sinTokenResponde401() throws Exception {
        assertEquals(401, get("/api/auditoria", null).statusCode());
        assertEquals(401, get("/api/recolecciones", null).statusCode());
    }

    @Test
    @DisplayName("PS-02 (A07) Un JWT con firma falsa responde 401")
    void tokenFalsificadoResponde401() throws Exception {
        String falso = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIkFETUlOSVNUUkFET1IiXX0.firmaFalsa";
        assertEquals(401, get("/api/auditoria", falso).statusCode());
    }

    @Test
    @DisplayName("PS-03 (A07) Con contraseña incorrecta responde 401 y no entrega token")
    void claveIncorrectaResponde401() throws Exception {
        HttpResponse<String> respuesta = login("admin", "ClaveIncorrecta1");
        assertEquals(401, respuesta.statusCode());
        assertFalse(respuesta.body().contains("accessToken"));
    }

    @Test
    @DisplayName("PS-04 (A03) Una inyección SQL en el login no permite entrar")
    void inyeccionSqlEnLogin() throws Exception {
        HttpResponse<String> respuesta = login("admin' OR '1'='1' --", "x' OR '1'='1");
        assertEquals(401, respuesta.statusCode());
        assertFalse(respuesta.body().contains("accessToken"));
    }

    @Test
    @DisplayName("PS-05 (A07) Un usuario desactivado no puede iniciar sesión aunque su clave sea correcta")
    void usuarioDesactivadoNoEntra() throws Exception {
        HttpResponse<String> respuesta = login("jperez", CLAVE);
        assertEquals(403, respuesta.statusCode());
        assertFalse(respuesta.body().contains("accessToken"));
    }

    @Test
    @DisplayName("PS-06 (A01) Un generador no puede ver auditoría ni reportes: 403")
    void generadorSinPermisoResponde403() throws Exception {
        String token = token("rhuaman");
        assertEquals(403, get("/api/auditoria", token).statusCode());
        assertEquals(403, get("/api/reportes", token).statusCode());
    }

    @Test
    @DisplayName("PS-07 (A01) El auditor puede consultar la auditoría pero no cambiar su configuración")
    void auditorNoGestionaAuditoria() throws Exception {
        String token = token("psalas");
        assertEquals(200, get("/api/auditoria", token).statusCode());
        String configuracion = "{\"eventos\":{\"validaciones\":true,\"modificaciones\":true,\"cierres\":true,"
                + "\"accesos\":false},\"retencionDias\":\"1\"}";
        assertEquals(403, enviar("PUT", "/api/auditoria/configuracion", token, configuracion).statusCode());
    }

    @Test
    @DisplayName("PS-08 (A01) Un transportista no puede consultar el catálogo de residuos: 403")
    void transportistaSinCatalogo() throws Exception {
        assertEquals(403, get("/api/residuos", token("jccori")).statusCode());
    }

    @Test
    @DisplayName("PS-09 (A05) Las métricas de /actuator solo las ve el administrador")
    void actuatorSoloAdministrador() throws Exception {
        assertEquals(200, get("/actuator/health", null).statusCode());
        assertEquals(401, get("/actuator/metrics", null).statusCode());
        assertEquals(403, get("/actuator/metrics", token("rhuaman")).statusCode());
        assertEquals(200, get("/actuator/metrics", token("admin")).statusCode());
    }

    @Test
    @DisplayName("PS-10 (A07) Tras 5 intentos fallidos, el sexto recibe 429 (fuerza bruta)")
    void bloqueoPorFuerzaBruta() throws Exception {
        String usuario = "atacante" + System.nanoTime();
        for (int i = 0; i < 5; i++) {
            assertEquals(401, login(usuario, "clave" + i).statusCode());
        }
        assertEquals(429, login(usuario, "otra").statusCode());
    }

    @Test
    @DisplayName("PS-11 (A05) CORS rechaza una petición desde un origen no permitido")
    void corsRechazaOrigenDesconocido() throws Exception {
        HttpRequest preflight = HttpRequest.newBuilder(uri("/api/auditoria"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", "http://sitio-malicioso.example")
                .header("Access-Control-Request-Method", "GET")
                .build();
        HttpResponse<String> respuesta = http.send(preflight, HttpResponse.BodyHandlers.ofString());
        assertEquals(403, respuesta.statusCode());
        assertTrue(respuesta.headers().firstValue("Access-Control-Allow-Origin").isEmpty());
    }

    @Test
    @DisplayName("PS-12 (A05) Un JSON inválido devuelve un mensaje genérico, sin detalles internos")
    void errorSinDetallesInternos() throws Exception {
        HttpResponse<String> respuesta = enviar("POST", "/api/auth/login", null, "{malformado");
        assertEquals(400, respuesta.statusCode());
        assertFalse(respuesta.body().contains("Exception"), respuesta.body());
        assertFalse(respuesta.body().contains("at com."), respuesta.body());
    }

    // ------------------------------------------------------------ utilidades HTTP

    private String token(String usuario) throws IOException, InterruptedException {
        HttpResponse<String> respuesta = login(usuario, CLAVE);
        assertEquals(200, respuesta.statusCode(), "no se pudo iniciar sesión como " + usuario);
        Matcher m = TOKEN.matcher(respuesta.body());
        assertTrue(m.find(), "la respuesta del login no trae accessToken");
        return m.group(1);
    }

    private HttpResponse<String> login(String usuario, String clave) throws IOException, InterruptedException {
        String cuerpo = "{\"username\":\"" + usuario.replace("\"", "\\\"") + "\",\"password\":\""
                + clave.replace("\"", "\\\"") + "\"}";
        return enviar("POST", "/api/auth/login", null, cuerpo);
    }

    private HttpResponse<String> get(String ruta, String token) throws IOException, InterruptedException {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(uri(ruta)).GET();
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return http.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String token, String json)
            throws IOException, InterruptedException {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(uri(ruta))
                .header("Content-Type", "application/json")
                .method(metodo, HttpRequest.BodyPublishers.ofString(json));
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return http.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String ruta) {
        return URI.create("http://localhost:" + puerto + ruta);
    }
}
