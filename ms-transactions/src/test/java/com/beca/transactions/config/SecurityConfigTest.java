package com.beca.transactions.config;

import com.beca.transactions.adapter.in.security.UserServiceAuthenticationProvider;
import com.beca.transactions.adapter.in.web.AccountController;
import com.beca.transactions.adapter.in.web.SecurityErrorResponder;
import com.beca.transactions.application.port.in.FindAccountBalanceUseCase;
import com.beca.transactions.application.port.out.UserAuthenticationPort;
import com.beca.transactions.domain.exception.UserServiceUnavailableException;
import com.beca.transactions.domain.model.AccountBalance;
import com.beca.transactions.domain.model.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, UserServiceAuthenticationProvider.class, SecurityErrorResponder.class})
class SecurityConfigTest {

    private static final String EMAIL = "maria@teste.com";
    private static final String SENHA = "senha123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserAuthenticationPort userAuthenticationPort;

    @MockitoBean
    private FindAccountBalanceUseCase findAccountBalanceUseCase;

    @Test
    void semCredenciais_deveRetornar401EmJson() throws Exception {
        mockMvc.perform(get("/users/1/balance"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Informe e-mail e senha válidos"));

        verifyNoInteractions(userAuthenticationPort, findAccountBalanceUseCase);
    }

    @Test
    void comCredenciaisErradas_deveRetornar401() throws Exception {
        when(userAuthenticationPort.authenticate(EMAIL, "errada")).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/1/balance").with(httpBasic(EMAIL, "errada")))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(findAccountBalanceUseCase);
    }

    @Test
    void comCredenciaisCertas_deveChegarNaRota() throws Exception {
        when(userAuthenticationPort.authenticate(EMAIL, SENHA))
                .thenReturn(Optional.of(new AuthenticatedUser(1L, "Maria", EMAIL)));
        when(findAccountBalanceUseCase.findByUserId(1L)).thenReturn(new AccountBalance(
                1L, new BigDecimal("1000"), new BigDecimal("2000"), new BigDecimal("500")));

        mockMvc.perform(get("/users/1/balance").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void comServicoDeUsuariosForaDoAr_deveRetornar503() throws Exception {
        when(userAuthenticationPort.authenticate(EMAIL, SENHA))
                .thenThrow(new UserServiceUnavailableException("fora do ar"));

        mockMvc.perform(get("/users/1/balance").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));

        verifyNoInteractions(findAccountBalanceUseCase);
    }

    @Test
    void documentacaoEHealth_naoDevemExigirLogin() throws Exception {
        for (String rota : new String[]{"/swagger-ui.html", "/v3/api-docs", "/actuator/health"}) {
            int status = mockMvc.perform(get(rota)).andReturn().getResponse().getStatus();
            assertThat(status).as(rota).isNotEqualTo(401);
        }
        verifyNoInteractions(userAuthenticationPort);
    }

    @Test
    void relatorioSemCredenciais_deveRetornar401() throws Exception {
        mockMvc.perform(get("/transactions/report"))
                .andExpect(status().isUnauthorized());
    }
}
