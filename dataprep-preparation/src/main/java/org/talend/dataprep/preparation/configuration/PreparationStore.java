package org.talend.dataprep.preparation.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.dataprep.preparation.store.InMemoryPreparationRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;

@Configuration
public class PreparationStore {

    private static final Log LOGGER = LogFactory.getLog(PreparationStore.class);

    @Bean
    public PreparationRepository getStore() {
        LOGGER.info("Using in-memory preparation store.");
        return new InMemoryPreparationRepository();
    }

}
