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

    // Erros (mesmo formato do GlobalExceptionHandler)

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
}
