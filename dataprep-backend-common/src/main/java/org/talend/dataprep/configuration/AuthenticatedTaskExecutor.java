package org.talend.dataprep.configuration;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Thread Task Executor that replicate current security context
 */
class AuthenticatedTaskExecutor extends ThreadPoolTaskExecutor {
    @Override
    public void execute(Runnable task) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        super.execute(() -> {
            try {
                final SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                ctx.setAuthentication(authentication);
                SecurityContextHolder.setContext(ctx);
                task.run();
            }
            finally {
                SecurityContextHolder.clearContext();
            }
        });
    }
}