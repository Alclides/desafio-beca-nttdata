package com.beca.transactions.adapter.in.web;

import com.beca.transactions.application.port.in.CreateTransactionUseCase;
import com.beca.transactions.application.port.in.FindTransactionUseCase;
import com.beca.transactions.domain.exception.TransactionNotFoundException;
import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionStatus;
import com.beca.transactions.domain.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    private static final String COMPRA_DO_USUARIO_1 = """
            {"userId": 1, "type": "PURCHASE", "amount": 50.00, "description": "Compra", "category": "LAZER"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateTransactionUseCase createTransactionUseCase;

    @MockitoBean
    private FindTransactionUseCase findTransactionUseCase;

    private static Transaction transacaoDoUsuario1() {
        return new Transaction(5L, 1L, TransactionType.PURCHASE, new BigDecimal("50.00"), "Compra", Category.LAZER,
                null, null, TransactionStatus.APPROVED, null, null, Instant.parse("2026-11-05T15:00:00Z"));
    }

    @Test
    void create_devePermitirCriarEmNomeProprio() throws Exception {
        when(createTransactionUseCase.create(any())).thenReturn(transacaoDoUsuario1());

        mockMvc.perform(post("/transactions").with(TestLogin.as(1L)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(COMPRA_DO_USUARIO_1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void create_deveRetornar403EmNomeDeOutroUsuario() throws Exception {
        mockMvc.perform(post("/transactions").with(TestLogin.as(2L)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(COMPRA_DO_USUARIO_1))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(createTransactionUseCase);
    }

    @Test
    void findById_devePermitirODono() throws Exception {
        when(findTransactionUseCase.findById(5L)).thenReturn(transacaoDoUsuario1());

        mockMvc.perform(get("/transactions/5").with(TestLogin.as(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void findById_deveRetornar403ParaTransacaoDeOutroUsuario() throws Exception {
        when(findTransactionUseCase.findById(5L)).thenReturn(transacaoDoUsuario1());

        mockMvc.perform(get("/transactions/5").with(TestLogin.as(2L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findById_deveRetornar404QuandoNaoExiste() throws Exception {
        when(findTransactionUseCase.findById(99L)).thenThrow(new TransactionNotFoundException("Transação não encontrada: 99"));

        mockMvc.perform(get("/transactions/99").with(TestLogin.as(1L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByUser_devePermitirOProprioUsuario() throws Exception {
        when(findTransactionUseCase.findByUser(eq(1L), any(), any(), any())).thenReturn(List.of(transacaoDoUsuario1()));

        mockMvc.perform(get("/transactions?userId=1").with(TestLogin.as(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    void findByUser_deveRetornar403ParaOutroUsuario() throws Exception {
        mockMvc.perform(get("/transactions?userId=1").with(TestLogin.as(2L)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(findTransactionUseCase);
    }
}
