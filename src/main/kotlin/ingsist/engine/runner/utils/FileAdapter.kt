package ingsist.engine.runner.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import java.io.File
import java.util.UUID

@Component
class FileAdapter {
    private val mapper = jacksonObjectMapper()

    fun <T> withTempFile(
        content: String,
        suffix: String,
        block: (File) -> T,
    ): T {
        val file = createTempFile(content, suffix)

        try {
            return block(file)
        } finally {
            // Borro el archvio al terminar
            file.delete()
        }
    }

    fun <T> withTempFiles(
        codeContent: String,
        configContent: Any,
        block: (codeFile: File, configFile: File) -> T,
    ): T {
        val codeFile = createTempFile(codeContent, ".ps")
        val configFile = createTempFile(mapper.writeValueAsString(configContent), ".json")

        try {
            return block(codeFile, configFile)
        } finally {
            codeFile.delete()
            configFile.delete()
        }
    }

    private fun createTempFile(
        content: String,
        suffix: String,
    ): File {
        return File.createTempFile(
            UUID.randomUUID().toString(),
            suffix,
        )
            .apply {
                writeText(content)
            }
    }
}
