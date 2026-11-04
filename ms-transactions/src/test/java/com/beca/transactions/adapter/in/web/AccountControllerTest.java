package com.beca.transactions.adapter.in.web;

import com.beca.transactions.application.port.in.FindAccountBalanceUseCase;
import com.beca.transactions.domain.exception.AccountNotFoundException;
import com.beca.transactions.domain.exception.AccountServiceUnavailableException;
import com.beca.transactions.domain.model.AccountBalance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindAccountBalanceUseCase findAccountBalanceUseCase;

    @Test
    void findBalance_deveRetornar200ComSaldoELimites() throws Exception {
        when(findAccountBalanceUseCase.findByUserId(1L)).thenReturn(new AccountBalance(
                1L, new BigDecimal("1000.00"), new BigDecimal("2000.00"), new BigDecimal("500.00")));

        mockMvc.perform(get("/users/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.dailyLimit").value(2000.00))
                .andExpect(jsonPath("$.perTransactionLimit").value(500.00));
    }

    @Test
    void findBalance_deveRetornar404QuandoUsuarioNaoTemConta() throws Exception {
        when(findAccountBalanceUseCase.findByUserId(99L))
                .thenThrow(new AccountNotFoundException("Conta não encontrada para o usuário: 99"));

        mockMvc.perform(get("/users/99/balance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Conta não encontrada para o usuário: 99"));
    }

    @Test
    void findBalance_deveRetornar503SemExporDetalhesQuandoServicoEstaForaDoAr() throws Exception {
        when(findAccountBalanceUseCase.findByUserId(1L))
                .thenThrow(new AccountServiceUnavailableException("Falha ao consultar serviço de saldo: timeout"));

        mockMvc.perform(get("/users/1/balance"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Serviço de saldo indisponível, tente novamente mais tarde!"));
    }
}
