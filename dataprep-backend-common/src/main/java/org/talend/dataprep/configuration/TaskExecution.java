//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.talend.dataprep.schema.csv.CSVSerializer;

@Configuration
@SuppressWarnings("InsufficientBranchCoverage")
public class TaskExecution {

    /**
     * @return A {@link TaskExecutor} for non-blocking CSV serialization.
     * @see CSVSerializer
     */
    @Bean(name = "serializer#csv#executor")
    TaskExecutor getCsvTaskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        return executor;
    }

    /**
     * @return A {@link TaskExecutor} for non-blocking HTML serialization.
     * @see CSVSerializer
     */
    @Bean(name = "serializer#html#executor")
    TaskExecutor getHtmlTaskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        return executor;
    }

    /**
     * @return A {@link TaskExecutor} for non-blocking CSV serialization.
     * @see org.talend.dataprep.schema.xls.XlsSerializer
     */
    @Bean(name = "serializer#excel#executor")
    TaskExecutor getExcelTaskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        return executor;
    }

    /**
     * @return A {@link TaskExecutor} for non-blocking JSON serialization.
     */
    @Bean(name = "serializer#json#executor")
    TaskExecutor getJsonTaskExecutor() {
        final ThreadPoolTaskExecutor executor = new AuthenticatedTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        return executor;
    }
}
