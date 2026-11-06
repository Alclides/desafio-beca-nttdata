package com.beca.transactions.domain.service;

import com.beca.transactions.domain.exception.ResourceAccessDeniedException;

// Regra de negócio: cada usuário só vê e movimenta o que é dele
public final class OwnershipPolicy {

    private OwnershipPolicy() {
    }

    public static void ensureOwner(Long authenticatedUserId, Long ownerId) {
        if (!authenticatedUserId.equals(ownerId)) {
            throw new ResourceAccessDeniedException("Acesso negado: estes dados pertencem a outro usuário");
        }
    }
}
