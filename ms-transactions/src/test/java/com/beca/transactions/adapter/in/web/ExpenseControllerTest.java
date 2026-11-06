package com.beca.transactions.adapter.in.web;

import com.beca.transactions.application.port.in.AnalyzeExpensesUseCase;
import com.beca.transactions.domain.exception.InvalidTransactionException;
import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.ExpenseReport;
import com.beca.transactions.domain.model.Period;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    private static final Period NOVEMBRO = new Period(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyzeExpensesUseCase useCase;

    private static <K extends Comparable<? super K>> ExpenseReport<K> relatorio(K chave) {
        TreeMap<K, BigDecimal> totais = new TreeMap<>();
        totais.put(chave, new BigDecimal("150.00"));
        return new ExpenseReport<>(NOVEMBRO, totais, new BigDecimal("150.00"));
    }

    @Test
    void summary_deveRetornarOsTotaisPorCategoria() throws Exception {
        when(useCase.byCategory(eq(1L), isNull(), isNull())).thenReturn(relatorio(Category.ALIMENTACAO));

        mockMvc.perform(get("/users/1/expenses/summary").with(TestLogin.as(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("2026-11-01"))
                .andExpect(jsonPath("$.to").value("2026-11-30"))
                .andExpect(jsonPath("$.lines[0].key").value("ALIMENTACAO"))
                .andExpect(jsonPath("$.lines[0].total").value(150.00))
                .andExpect(jsonPath("$.total").value(150.00));
    }

    @Test
    void summary_devePassarOPeriodoInformado() throws Exception {
        when(useCase.byCategory(1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(relatorio(Category.LAZER));

        mockMvc.perform(get("/users/1/expenses/summary?from=2026-10-01&to=2026-10-31").with(TestLogin.as(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].key").value("LAZER"));
    }

    @Test
    void daily_deveRetornarADataComoTexto() throws Exception {
        when(useCase.byDay(eq(1L), any(), any())).thenReturn(relatorio(LocalDate.of(2026, 11, 5)));

        mockMvc.perform(get("/users/1/expenses/daily").with(TestLogin.as(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].key").value("2026-11-05"));
    }

    @Test
    void monthly_deveRetornarOMesComoTexto() throws Exception {
        when(useCase.byMonth(eq(1L), any(), any())).thenReturn(relatorio(YearMonth.of(2026, 11)));

        mockMvc.perform(get("/users/1/expenses/monthly").with(TestLogin.as(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].key").value("2026-11"));
    }

    @Test
    void summary_deveRetornar400ComPeriodoInvertido() throws Exception {
        when(useCase.byCategory(eq(1L), any(), any()))
                .thenThrow(new InvalidTransactionException("Data inicial não pode ser depois da data final"));

        mockMvc.perform(get("/users/1/expenses/summary?from=2026-11-30&to=2026-11-01").with(TestLogin.as(1L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void summary_deveRetornar400ComDataInvalida() throws Exception {
        mockMvc.perform(get("/users/1/expenses/summary?from=ontem").with(TestLogin.as(1L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void asTresRotas_devemRetornar403ParaOutroUsuario() throws Exception {
        for (String rota : new String[]{"summary", "daily", "monthly"}) {
            mockMvc.perform(get("/users/1/expenses/" + rota).with(TestLogin.as(2L)))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(useCase);
    }
}
