package ingsist.engine.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import progress.ProgressReporter

@Configuration
class PrintScriptConfig {
    // Creo las Beans necesarias para usar el engine
    @Bean
    fun progressReporter(): ProgressReporter {
        return object : ProgressReporter {
            override fun reportProgress(
                message: String,
                percentage: Int?,
            ) {
                // No hace nada
            }

            override fun reportError(message: String) {
                // No hace nada
            }

            override fun reportSuccess(message: String) {
                // No hace nada
            }
        }
    }
}
