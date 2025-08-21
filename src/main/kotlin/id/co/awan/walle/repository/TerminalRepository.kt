package id.co.awan.walle.repository

import id.co.awan.walle.entity.Terminal
import org.springframework.data.jpa.repository.JpaRepository

interface TerminalRepository : JpaRepository<Terminal, String> {

    fun findByIdAndKey(id: String, key: String): Terminal?

}