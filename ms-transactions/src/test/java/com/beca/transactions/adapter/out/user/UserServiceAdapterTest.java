package com.beca.transactions.adapter.out.user;

import com.beca.transactions.domain.exception.UserServiceUnavailableException;
import com.beca.transactions.domain.model.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

@RestClientTest(components = UserServiceAdapter.class, properties = "integrations.users-url=http://users.test")
class UserServiceAdapterTest {

    private static final String URL = "http://users.test/users/1";
    private static final String AUTH_URL = "http://users.test/users/authenticate";

    @Autowired
    private UserServiceAdapter adapter;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void exists_deveRetornarTrueQuandoOUsuarioExiste() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id": 1, "name": "Antonia", "email": "antonia@gmail.com"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(adapter.exists(1L)).isTrue();
        server.verify();
    }

    @Test
    void exists_deveRetornarFalseQuandoOServicoResponde404() {
        server.expect(requestTo(URL)).andRespond(withResourceNotFound());

        assertThat(adapter.exists(1L)).isFalse();
    }

    @Test
    void exists_deveLancarExcecaoQuandoOServicoResponde500() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        assertThatThrownBy(() -> adapter.exists(1L))
                .isInstanceOf(UserServiceUnavailableException.class);
    }

    @Test
    void authenticate_deveEnviarAsCredenciaisEDevolverOUsuario() {
        server.expect(requestTo(AUTH_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.email").value("maria@teste.com"))
                .andExpect(jsonPath("$.password").value("senha123"))
                .andRespond(withSuccess("""
                        {"id": 1, "name": "Maria", "email": "maria@teste.com"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(adapter.authenticate("maria@teste.com", "senha123")).get()
                .extracting(AuthenticatedUser::id).isEqualTo(1L);
        server.verify();
    }

    @Test
    void authenticate_deveFicarVazioCom401() {
        server.expect(requestTo(AUTH_URL)).andRespond(
                withUnauthorizedRequest());

        assertThat(adapter.authenticate("maria@teste.com", "errada")).isEmpty();
    }

    @Test
    void authenticate_deveLancarExcecaoCom500() {
        server.expect(requestTo(AUTH_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> adapter.authenticate("maria@teste.com", "senha123"))
                .isInstanceOf(UserServiceUnavailableException.class);
    }
}
