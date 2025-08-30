package id.co.awan.walle.service.dao

import id.co.awan.walle.entity.Terminal
import id.co.awan.walle.repository.HsmRepository
import id.co.awan.walle.repository.TerminalRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class TerminalService(
    private val hsmRepository: HsmRepository,
    private val terminalRepository: TerminalRepository
) {


    fun validateTerminal(
        terminalId: String,
        terminalKey: String
    ): Terminal {
        return terminalRepository.findByIdAndKey(terminalId, terminalKey)
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Terminal validation exception"
            )
    }

}