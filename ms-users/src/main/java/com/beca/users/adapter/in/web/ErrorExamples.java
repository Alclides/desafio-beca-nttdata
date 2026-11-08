package com.beca.users.adapter.in.web;

// Exemplos de resposta de erro exibidos no Swagger (mesmo formato do GlobalExceptionHandler)
final class ErrorExamples {

    private ErrorExamples() {
    }

    static final String VALIDATION = """
            {"status": 400, "error": "Bad Request", "message": "Dados inválidos",
             "fields": {"email": "deve ser um endereço de e-mail bem formado", "password": "o tamanho deve ser entre 6 e 72"},
             "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String NOT_FOUND = """
            {"status": 404, "error": "Not Found", "message": "Usuário não encontrado: 99",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String EMAIL_CONFLICT = """
            {"status": 409, "error": "Conflict", "message": "E-mail já cadastrado: antonia@gmail.com",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String INVALID_CSV = """
            {"status": 400, "error": "Bad Request", "message": "Colunas obrigatórias ausentes no cabeçalho: email. Esperado: name,email,password",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String FILE_TOO_LARGE = """
            {"status": 413, "error": "Payload Too Large", "message": "Arquivo maior que o limite permitido",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String INVALID_CREDENTIALS = """
            {"status": 401, "error": "Unauthorized", "message": "E-mail ou senha incorretos",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String UNAUTHORIZED = """
            {"status": 401, "error": "Unauthorized", "message": "Informe e-mail e senha válidos",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;

    static final String FORBIDDEN = """
            {"status": 403, "error": "Forbidden", "message": "Acesso negado: só é possível alterar a própria conta",
             "fields": null, "timestamp": "2026-11-03T18:00:00Z"}
            """;
}
