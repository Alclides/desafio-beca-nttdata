package com.beca.transactions.adapter.out.user;

import com.beca.transactions.domain.exception.UserServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(components = UserServiceAdapter.class, properties = "integrations.users-url=http://users.test")
class UserServiceAdapterTest {

    private static final String URL = "http://users.test/users/1";

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
}
