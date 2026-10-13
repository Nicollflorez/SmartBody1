package com.smartbody.service;

import com.smartbody.dto.MedidaRequest;
import com.smartbody.dto.MedidaResponse;
import com.smartbody.dto.ProgresoResponse;
import com.smartbody.entity.Medida;
import com.smartbody.entity.Perfil;
import com.smartbody.entity.Usuario;
import com.smartbody.repository.MedidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** RF10: actualizacion de medidas e historial de progreso. */
@Service
public class ProgresoService {

    private final UsuarioService usuarioService;
    private final MedidaRepository medidaRepository;

    public ProgresoService(UsuarioService usuarioService, MedidaRepository medidaRepository) {
        this.usuarioService = usuarioService;
        this.medidaRepository = medidaRepository;
    }

    @Transactional(readOnly = true)
    public ProgresoResponse consultar(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        List<MedidaResponse> medidas = medidaRepository.findByUsuarioIdOrderByRegistradoEnAsc(usuario.getId()).stream()
                .map(MedidaResponse::desde)
                .toList();

        if (medidas.isEmpty()) {
            return new ProgresoResponse(medidas, null, null, null, null, null);
        }
        MedidaResponse primera = medidas.get(0);
        MedidaResponse ultima = medidas.get(medidas.size() - 1);
        double cambio = Math.round((ultima.pesoKg() - primera.pesoKg()) * 100.0) / 100.0;
        return new ProgresoResponse(medidas, primera.pesoKg(), ultima.pesoKg(), cambio, ultima.imc(),
                Perfil.clasificarImc(ultima.imc()));
    }

    @Transactional
    public MedidaResponse registrar(String email, MedidaRequest peticion) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Perfil perfil = usuario.getPerfil();
        Double altura = peticion.alturaCm() != null ? peticion.alturaCm()
                : perfil == null ? null : perfil.getAlturaCm();

        Medida medida = new Medida();
        medida.setUsuario(usuario);
        medida.setPesoKg(peticion.pesoKg());
        medida.setAlturaCm(altura);
        medida.setCinturaCm(peticion.cinturaCm());
        medida.setCaderaCm(peticion.caderaCm());
        medida.setPechoCm(peticion.pechoCm());
        medida.setBrazoCm(peticion.brazoCm());
        medida.setImc(Perfil.calcularImc(peticion.pesoKg(), altura));

        // El perfil siempre refleja las medidas mas recientes para las proximas recomendaciones.
        if (perfil != null) {
            perfil.setPesoKg(peticion.pesoKg());
            perfil.setAlturaCm(altura);
        }
        return MedidaResponse.desde(medidaRepository.save(medida));
    }
}
