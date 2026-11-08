package com.beca.users.domain.service;

import com.beca.users.domain.exception.ResourceAccessDeniedException;

// Regra de negócio: cada usuário só altera ou apaga a própria conta
public final class OwnershipPolicy {

    private OwnershipPolicy() {
    }

    public static void ensureOwner(Long authenticatedUserId, Long ownerId) {
        if (!authenticatedUserId.equals(ownerId)) {
            throw new ResourceAccessDeniedException("Acesso negado: só é possível alterar a própria conta");
        }
    }
}
