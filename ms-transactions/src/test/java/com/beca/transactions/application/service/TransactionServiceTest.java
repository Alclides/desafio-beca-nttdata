package com.beca.transactions.application.service;

import com.beca.transactions.application.port.in.CreateTransactionUseCase.CreateTransactionCommand;
import com.beca.transactions.application.port.out.AccountBalancePort;
import com.beca.transactions.application.port.out.ExchangeRatePort;
import com.beca.transactions.application.port.out.TransactionRepositoryPort;
import com.beca.transactions.domain.exception.AccountNotFoundException;
import com.beca.transactions.domain.exception.AccountServiceUnavailableException;
import com.beca.transactions.domain.exception.ExchangeRateUnavailableException;
import com.beca.transactions.domain.exception.InvalidTransactionException;
import com.beca.transactions.domain.exception.TransactionNotFoundException;
import com.beca.transactions.domain.model.AccountBalance;
import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.ExchangeRate;
import com.beca.transactions.domain.model.ExchangeRates;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionStatus;
import com.beca.transactions.domain.model.TransactionType;
import com.beca.transactions.domain.service.TransactionApprovalPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    @Mock
    private TransactionRepositoryPort repository;

    @Mock
    private AccountBalancePort accountBalancePort;

    @Mock
    private ExchangeRatePort exchangeRatePort;

    private TransactionService service;

    private static final ExchangeRate USD = new ExchangeRate("USD", new BigDecimal("5.4935"), LocalDate.of(2026, 11, 3));
    private static final ExchangeRate EUR = new ExchangeRate("EUR", new BigDecimal("6.1500"), LocalDate.of(2026, 11, 3));

    @BeforeEach
    void setUp() {
        // Só as portas são mocks; a policy é a real, para o teste exercitar as regras de verdade
        service = new TransactionService(repository, accountBalancePort, new TransactionApprovalPolicy(), exchangeRatePort);

        // Por padrão a BrasilAPI está fora do ar; os testes de câmbio sobrescrevem com cambioDisponivel()
        lenient().when(exchangeRatePort.findLatest(any(), any()))
                .thenThrow(new ExchangeRateUnavailableException("fora do ar"));
    }

    private void cambioDisponivel() {
        doReturn(USD).when(exchangeRatePort).findLatest(eq("USD"), any());
        doReturn(EUR).when(exchangeRatePort).findLatest(eq("EUR"), any());
    }

    private static Transaction transacaoGravada(Long id, ExchangeRates rates, Instant createdAt) {
        return new Transaction(id, 1L, TransactionType.PURCHASE, new BigDecimal("50"), "Compra",
                Category.LAZER, null, null, TransactionStatus.APPROVED, null, rates, createdAt);
    }

    // saldo 1000, limite diário 2000, limite por transação 500
    private static AccountBalance conta() {
        return new AccountBalance(1L, new BigDecimal("1000"), new BigDecimal("2000"), new BigDecimal("500"));
    }

    private static CreateTransactionCommand comando(TransactionType type, String valor, Category category) {
        return new CreateTransactionCommand(1L, type, new BigDecimal(valor), "Teste", category, null, null);
    }

    private static CreateTransactionCommand correcao(Long correctedTransactionId) {
        return new CreateTransactionCommand(1L, TransactionType.DEPOSIT, new BigDecimal("50"), "Estorno",
                null, null, correctedTransactionId);
    }

    private static Transaction transacaoGravada(Long id, Long userId) {
        return new Transaction(id, userId, TransactionType.PURCHASE, new BigDecimal("50"), "Compra",
                Category.LAZER, null, null, TransactionStatus.APPROVED, null, null, Instant.now());
    }

    private void saveDevolveOQueRecebeu() {
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void create_deveAprovarDepositoSemConsultarSaldo() {
        saveDevolveOQueRecebeu();

        Transaction result = service.create(comando(TransactionType.DEPOSIT, "100", null));

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.APPROVED);
        verify(accountBalancePort, never()).findByUserId(anyLong());
        verify(repository, never()).sumApprovedOutgoing(anyLong(), any(), any());
        verify(repository).save(any(Transaction.class));
    }

    @Test
    void create_deveAprovarCompraDentroDosLimites() {
        when(accountBalancePort.findByUserId(1L)).thenReturn(conta());
        when(repository.sumApprovedOutgoing(eq(1L), any(), any())).thenReturn(BigDecimal.ZERO);
        saveDevolveOQueRecebeu();

        Transaction result = service.create(comando(TransactionType.PURCHASE, "200", Category.LAZER));

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.APPROVED);
        assertThat(result.getRejectedReason()).isNull();
    }

    @Test
    void create_deveRejeitarRetiradaComSaldoInsuficienteEGravar() {
        AccountBalance saldoBaixo = new AccountBalance(1L, new BigDecimal("50"), new BigDecimal("2000"), new BigDecimal("500"));
        when(accountBalancePort.findByUserId(1L)).thenReturn(saldoBaixo);
        when(repository.sumApprovedOutgoing(eq(1L), any(), any())).thenReturn(BigDecimal.ZERO);
        saveDevolveOQueRecebeu();

        Transaction result = service.create(comando(TransactionType.WITHDRAWAL, "100", null));

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.REJECTED);
        assertThat(result.getRejectedReason()).isEqualTo("Saldo insuficiente");
        verify(repository).save(result);
    }

    @Test
    void create_deveRejeitarAcimaDoLimitePorTransacao() {
        when(accountBalancePort.findByUserId(1L)).thenReturn(conta());
        when(repository.sumApprovedOutgoing(eq(1L), any(), any())).thenReturn(BigDecimal.ZERO);
        saveDevolveOQueRecebeu();

        Transaction result = service.create(comando(TransactionType.PURCHASE, "800", Category.LAZER));

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.REJECTED);
        assertThat(result.getRejectedReason()).isEqualTo("Valor excede o limite por transação");
    }

    @Test
    void create_deveRejeitarQuandoPassaDoLimiteDiario() {
        when(accountBalancePort.findByUserId(1L)).thenReturn(conta());
        when(repository.sumApprovedOutgoing(eq(1L), any(), any())).thenReturn(new BigDecimal("1950"));
        saveDevolveOQueRecebeu();

        Transaction result = service.create(comando(TransactionType.PURCHASE, "100", Category.LAZER));

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.REJECTED);
        assertThat(result.getRejectedReason()).isEqualTo("Valor excede o limite diário");
    }

    @Test
    void create_deveSomarGastosDeHojeNoFusoDoBrasil() {
        when(accountBalancePort.findByUserId(1L)).thenReturn(conta());
        when(repository.sumApprovedOutgoing(eq(1L), any(), any())).thenReturn(BigDecimal.ZERO);
        saveDevolveOQueRecebeu();

        service.create(comando(TransactionType.PURCHASE, "100", Category.LAZER));

        LocalDate hoje = LocalDate.now(ZONE);
        ArgumentCaptor<Instant> start = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> end = ArgumentCaptor.forClass(Instant.class);
        verify(repository).sumApprovedOutgoing(eq(1L), start.capture(), end.capture());
        assertThat(start.getValue()).isEqualTo(hoje.atStartOfDay(ZONE).toInstant());
        assertThat(end.getValue()).isEqualTo(hoje.plusDays(1).atStartOfDay(ZONE).toInstant());
    }

    @Test
    void create_deveRejeitarQuandoServicoDeSaldoEstaForaDoAr() {
        when(accountBalancePort.findByUserId(1L)).thenThrow(new AccountServiceUnavailableException("timeout"));
        saveDevolveOQueRecebeu();

        Transaction result = service.create(comando(TransactionType.WITHDRAWAL, "100", null));

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.REJECTED);
        assertThat(result.getRejectedReason()).startsWith("BALANCE_SERVICE_UNAVAILABLE").contains("indisponível");
        verify(repository, never()).sumApprovedOutgoing(anyLong(), any(), any());
        verify(repository).save(result);
    }

    @Test
    void create_deveRejeitarEGravarQuandoUsuarioNaoTemConta() {
        when(accountBalancePort.findByUserId(1L)).thenThrow(new AccountNotFoundException("sem conta"));
        saveDevolveOQueRecebeu();

        Transaction result = service.create(comando(TransactionType.WITHDRAWAL, "100", null));

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.REJECTED);
        assertThat(result.getRejectedReason()).startsWith("ACCOUNT_NOT_FOUND");
        verify(repository, never()).sumApprovedOutgoing(anyLong(), any(), any());
        verify(repository).save(result);
    }

    @Test
    void create_devePropagarErroDeValidacaoDoDominioSemGravar() {
        assertThatThrownBy(() -> service.create(comando(TransactionType.PURCHASE, "100", null)))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Compra exige categoria");

        verifyNoInteractions(accountBalancePort);
        verify(repository, never()).save(any());
    }

    @Test
    void create_deveAceitarCorrecaoDeTransacaoPropria() {
        when(repository.findById(10L)).thenReturn(Optional.of(transacaoGravada(10L, 1L)));
        saveDevolveOQueRecebeu();

        Transaction result = service.create(correcao(10L));

        assertThat(result.getCorrectedTransactionId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.APPROVED);
    }

    @Test
    void create_deveFalharAoCorrigirTransacaoDeOutroUsuario() {
        when(repository.findById(10L)).thenReturn(Optional.of(transacaoGravada(10L, 2L)));

        assertThatThrownBy(() -> service.create(correcao(10L)))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Só é possível corrigir transações próprias");

        verify(repository, never()).save(any());
    }

    @Test
    void create_deveFalharAoCorrigirTransacaoInexistente() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(correcao(10L)))
                .isInstanceOf(TransactionNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void findById_deveRetornarTransacao() {
        Transaction gravada = transacaoGravada(5L, 1L);
        when(repository.findById(5L)).thenReturn(Optional.of(gravada));

        assertThat(service.findById(5L)).isSameAs(gravada);
    }

    @Test
    void findById_deveFalharQuandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transação não encontrada: 99");
    }

    @Test
    void findByUser_deveUsarMesAtualQuandoPeriodoNaoInformado() {
        LocalDate hoje = LocalDate.now(ZONE);

        service.findByUser(1L, null, null, null);

        ArgumentCaptor<Instant> start = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> end = ArgumentCaptor.forClass(Instant.class);
        verify(repository).findByUser(eq(1L), start.capture(), end.capture(), isNull());
        assertThat(start.getValue()).isEqualTo(hoje.withDayOfMonth(1).atStartOfDay(ZONE).toInstant());
        assertThat(end.getValue()).isEqualTo(hoje.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZONE).toInstant());
    }

    @Test
    void findByUser_deveIncluirODiaFinalInteiro() {
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 11, 30);
        when(repository.findByUser(eq(1L), any(), any(), eq(TransactionType.PURCHASE))).thenReturn(List.of());

        service.findByUser(1L, from, to, TransactionType.PURCHASE);

        // fim exclusivo = meia-noite do dia seguinte, para as 23:59 do dia 30 entrarem
        verify(repository).findByUser(1L,
                from.atStartOfDay(ZONE).toInstant(),
                LocalDate.of(2026, 12, 1).atStartOfDay(ZONE).toInstant(),
                TransactionType.PURCHASE);
    }

    @Test
    void findByUser_deveFalharComPeriodoInvertido() {
        assertThatThrownBy(() -> service.findByUser(1L, LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1), null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Data inicial não pode ser depois da data final");

        verifyNoInteractions(repository);
    }

    @Test
    void create_deveGravarAsTaxasDasDuasMoedas() {
        cambioDisponivel();
        saveDevolveOQueRecebeu();

        Transaction result = service.create(comando(TransactionType.DEPOSIT, "100", null));

        assertThat(result.getExchangeRates()).isEqualTo(new ExchangeRates(USD, EUR));
        verify(repository).save(result);
    }

    @Test
    void create_deveGravarSemTaxaComBrasilApiFora() {
        saveDevolveOQueRecebeu();

        Transaction result = service.create(comando(TransactionType.DEPOSIT, "100", null));

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.APPROVED);
        assertThat(result.hasExchangeRates()).isFalse();
        verify(repository).save(result);
    }

    @Test
    void create_deveGravarTaxaTambemNaTransacaoRejeitada() {
        cambioDisponivel();
        when(accountBalancePort.findByUserId(1L)).thenReturn(conta());
        when(repository.sumApprovedOutgoing(eq(1L), any(), any())).thenReturn(BigDecimal.ZERO);
        saveDevolveOQueRecebeu();

        Transaction result = service.create(comando(TransactionType.PURCHASE, "800", Category.LAZER));

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.REJECTED);
        assertThat(result.hasExchangeRates()).isTrue();
    }

    @Test
    void create_naoDeveBuscarCambioDaOriginalAoValidarCorrecao() {
        when(repository.findById(10L)).thenReturn(Optional.of(transacaoGravada(10L, 1L)));
        saveDevolveOQueRecebeu();

        service.create(correcao(10L));

        // Uma única chamada (USD da transação nova, que falha e encerra a busca)
        verify(exchangeRatePort, times(1)).findLatest(any(), any());
        verify(repository, times(1)).save(any());
    }

    @Test
    void findById_naoDeveChamarApiQuandoJaTemTaxa() {
        Transaction gravada = transacaoGravada(5L, new ExchangeRates(USD, EUR), Instant.now());
        when(repository.findById(5L)).thenReturn(Optional.of(gravada));

        assertThat(service.findById(5L)).isSameAs(gravada);

        verifyNoInteractions(exchangeRatePort);
        verify(repository, never()).save(any());
    }

    @Test
    void findById_deveBuscarEGravarQuandoFaltaTaxa() {
        cambioDisponivel();
        Transaction gravada = transacaoGravada(5L, null, Instant.now());
        when(repository.findById(5L)).thenReturn(Optional.of(gravada));
        saveDevolveOQueRecebeu();

        Transaction result = service.findById(5L);

        assertThat(result.getExchangeRates()).isEqualTo(new ExchangeRates(USD, EUR));
        verify(repository).save(gravada);
    }

    @Test
    void findById_deveResponderSemTaxaComBrasilApiFora() {
        Transaction gravada = transacaoGravada(5L, null, Instant.now());
        when(repository.findById(5L)).thenReturn(Optional.of(gravada));

        Transaction result = service.findById(5L);

        assertThat(result.hasExchangeRates()).isFalse();
        verify(repository, never()).save(any());
    }

    @Test
    void findById_naoDeveGravarTaxaParcial() {
        // USD responde, EUR continua com o padrão de falha
        doReturn(USD).when(exchangeRatePort).findLatest(eq("USD"), any());
        Transaction gravada = transacaoGravada(5L, null, Instant.now());
        when(repository.findById(5L)).thenReturn(Optional.of(gravada));

        Transaction result = service.findById(5L);

        assertThat(result.hasExchangeRates()).isFalse();
        verify(repository, never()).save(any());
    }

    @Test
    void findById_deveUsarADataDeCriacaoNoFusoDoBrasil() {
        cambioDisponivel();
        // 01:00 UTC do dia 16 ainda é 22:00 do dia 15 em São Paulo
        Transaction gravada = transacaoGravada(5L, null, Instant.parse("2026-10-16T01:00:00Z"));
        when(repository.findById(5L)).thenReturn(Optional.of(gravada));
        saveDevolveOQueRecebeu();

        service.findById(5L);

        verify(exchangeRatePort).findLatest("USD", LocalDate.of(2026, 10, 15));
        verify(exchangeRatePort).findLatest("EUR", LocalDate.of(2026, 10, 15));
    }

    @Test
    void findByUser_naoDeveChamarBrasilApi() {
        when(repository.findByUser(eq(1L), any(), any(), isNull()))
                .thenReturn(List.of(transacaoGravada(5L, null, Instant.now())));

        service.findByUser(1L, null, null, null);

        verifyNoInteractions(exchangeRatePort);
    }
}
