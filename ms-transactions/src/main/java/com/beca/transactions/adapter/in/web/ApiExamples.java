package com.beca.transactions.adapter.in.web;

// Exemplos de requisição e resposta exibidos no Swagger
final class ApiExamples {

    private ApiExamples() {
    }

    // Requisições: um exemplo por tipo de transação

    static final String DEPOSIT = """
            {"userId": 1, "type": "DEPOSIT", "amount": 3500.00, "description": "Salário de novembro"}
            """;

    static final String WITHDRAWAL = """
            {"userId": 1, "type": "WITHDRAWAL", "amount": 200.00, "description": "Saque no caixa eletrônico"}
            """;

    static final String TRANSFER = """
            {"userId": 1, "type": "TRANSFER", "amount": 150.00, "description": "Divisão do aluguel",
             "destinationUserId": 2}
            """;

    static final String PURCHASE = """
            {"userId": 1, "type": "PURCHASE", "amount": 89.90, "description": "Mercado do mês",
             "category": "ALIMENTACAO"}
            """;

    static final String CORRECTION = """
            {"userId": 1, "type": "DEPOSIT", "amount": 89.90, "description": "Estorno da compra 4",
             "correctedTransactionId": 4}
            """;

    // Respostas

    static final String APPROVED = """
            {"id": 4, "userId": 1, "type": "PURCHASE", "amount": 89.90, "description": "Mercado do mês",
             "category": "ALIMENTACAO", "destinationUserId": null, "correctedTransactionId": null,
             "status": "APPROVED", "rejectedReason": null, "createdAt": "2026-11-04T18:00:00Z",
             "conversion": {"available": true, "reason": null, "rates": [
               {"currency": "USD", "rate": 5.4935, "rateDate": "2026-11-03", "convertedAmount": 16.36},
               {"currency": "EUR", "rate": 6.1500, "rateDate": "2026-11-03", "convertedAmount": 14.62}
             ]}}
            """;

    static final String REJECTED = """
            {"id": 5, "userId": 1, "type": "WITHDRAWAL", "amount": 800.00, "description": "Saque",
             "category": "OUTROS", "destinationUserId": null, "correctedTransactionId": null,
             "status": "REJECTED", "rejectedReason": "Valor excede o limite por transação",
             "createdAt": "2026-11-03T18:00:00Z",
             "conversion": {"available": false, "reason": "Cotação indisponível no momento", "rates": []}}
            """;

    static final String BALANCE = """
            {"userId": 1, "balance": 1000.00, "dailyLimit": 2000.00, "perTransactionLimit": 500.00}
            """;

    static final String EXPENSES_BY_CATEGORY = """
            {"from": "2026-11-01", "to": "2026-11-30", "lines": [
              {"key": "ALIMENTACAO", "total": 150.00},
              {"key": "LAZER", "total": 30.00}
            ], "total": 180.00}
            """;

    static final String EXPENSES_BY_DAY = """
            {"from": "2026-11-01", "to": "2026-11-30", "lines": [
              {"key": "2026-11-03", "total": 100.00},
              {"key": "2026-11-05", "total": 80.00}
            ], "total": 180.00}
            """;

    static final String EXPENSES_BY_MONTH = """
            {"from": "2026-10-01", "to": "2026-11-30", "lines": [
              {"key": "2026-10", "total": 250.00},
              {"key": "2026-11", "total": 180.00}
            ], "total": 430.00}
            """;

    // Erros (mesmo formato do GlobalExceptionHandler)

    static final String USER_NOT_FOUND = """
            {"status": 404, "error": "Not Found", "message": "Usuário não encontrado: 99",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String ACCOUNT_NOT_FOUND = """
            {"status": 404, "error": "Not Found", "message": "Conta não encontrada para o usuário: 99",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String BALANCE_UNAVAILABLE = """
            {"status": 503, "error": "Service Unavailable",
             "message": "Serviço de saldo indisponível, tente novamente mais tarde!",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String VALIDATION = """
            {"status": 400, "error": "Bad Request", "message": "Dados inválidos",
             "fields": {"amount": "deve ser maior que 0"}, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String BUSINESS_RULE = """
            {"status": 400, "error": "Bad Request", "message": "Compra exige categoria",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String INVALID_PERIOD = """
            {"status": 400, "error": "Bad Request", "message": "Data inicial não pode ser depois da data final",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String NOT_FOUND = """
            {"status": 404, "error": "Not Found", "message": "Transação não encontrada: 99",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String FORBIDDEN = """
            {"status": 403, "error": "Forbidden", "message": "Acesso negado: estes dados pertencem a outro usuário",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String REPORT_CSV = """
        data;tipo;categoria;descrição;valor em real;status;motivo
        05/11/2026;PURCHASE;LAZER;Cinema;150,50;APPROVED;
        05/11/2026;WITHDRAWAL;OUTROS;Saque;9000,00;REJECTED;Saldo insuficiente
        """;

    static final String UNAUTHORIZED = """
        {"status": 401, "error": "Unauthorized", "message": "Informe e-mail e senha válidos",
         "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
        """;
}
