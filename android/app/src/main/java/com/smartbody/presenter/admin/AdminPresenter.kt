package com.smartbody.presenter.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.ReporteEfectividadDto
import com.smartbody.model.dto.ResumenAdminDto
import com.smartbody.model.dto.UsuarioAdminDto
import com.smartbody.model.repository.AdminRepository
import com.smartbody.model.repository.Resultado
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PestanaAdmin { RESUMEN, USUARIOS, REPORTES }

data class AdminUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val pestana: PestanaAdmin = PestanaAdmin.RESUMEN,
    val resumen: ResumenAdminDto? = null,
    val usuarios: List<UsuarioAdminDto> = emptyList(),
    val reporte: ReporteEfectividadDto? = null,
    val usuarioActualizando: Long? = null,
    val mensaje: String? = null
)

/**
 * Historia de usuario del administrador: resumen e historial de uso, usuarios y reporte de efectividad.
 * Es un complemento: el administrador tambien usa todos los modulos de la aplicacion como cualquier usuario.
 */
class AdminPresenter(
    private val adminRepository: AdminRepository,
    pestanaInicial: PestanaAdmin = PestanaAdmin.RESUMEN
) : ViewModel() {

    private val _estado = MutableStateFlow(AdminUiState(pestana = pestanaInicial))
    val estado: StateFlow<AdminUiState> = _estado.asStateFlow()

    fun cargar() {
        _estado.update { it.copy(cargando = it.resumen == null, errorCarga = null) }
        viewModelScope.launch {
            val resumen = async { adminRepository.resumen() }
            val usuarios = async { adminRepository.usuarios() }
            val reporte = async { adminRepository.reporteEfectividad() }

            when (val r = resumen.await()) {
                is Resultado.Exito -> _estado.update { it.copy(resumen = r.datos) }
                is Resultado.Error -> {
                    val mensaje = if (r.codigo == 403) "Esta sección es solo para administradores." else r.mensaje
                    _estado.update { it.copy(cargando = false, errorCarga = mensaje) }
                    return@launch
                }
            }
            (usuarios.await() as? Resultado.Exito<List<UsuarioAdminDto>>)?.let { u -> _estado.update { it.copy(usuarios = u.datos) } }
            (reporte.await() as? Resultado.Exito<ReporteEfectividadDto>)?.let { r -> _estado.update { it.copy(reporte = r.datos) } }
            _estado.update { it.copy(cargando = false) }
        }
    }

    fun seleccionarPestana(pestana: PestanaAdmin) {
        _estado.update { it.copy(pestana = pestana) }
    }

    fun cambiarEstado(usuario: UsuarioAdminDto, activo: Boolean) {
        if (_estado.value.usuarioActualizando != null) return
        _estado.update { it.copy(usuarioActualizando = usuario.id) }
        viewModelScope.launch {
            when (val resultado = adminRepository.cambiarEstado(usuario.id, activo)) {
                is Resultado.Exito -> {
                    _estado.update {
                        it.copy(
                            usuarioActualizando = null,
                            usuarios = it.usuarios.map { u -> if (u.id == usuario.id) u.copy(activo = activo) else u },
                            mensaje = "${usuario.nombre}: ${resultado.datos.mensaje.lowercase()}"
                        )
                    }
                    cargar()
                }

                is Resultado.Error -> _estado.update { it.copy(usuarioActualizando = null, mensaje = resultado.mensaje) }
            }
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensaje = null) }
    }
}
