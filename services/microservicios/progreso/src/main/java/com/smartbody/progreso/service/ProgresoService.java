package com.smartbody.progreso.service;

import com.smartbody.progreso.cliente.ClienteUsuarioYPlanes;
import com.smartbody.progreso.dto.MedidaRequest;
import com.smartbody.progreso.dto.MedidaResponse;
import com.smartbody.progreso.dto.PerfilResponse;
import com.smartbody.progreso.dto.ProgresoResponse;
import com.smartbody.progreso.entity.Medida;
import com.smartbody.progreso.repository.MedidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** RF10: actualizacion de medidas e historial de progreso. */
@Service
public class ProgresoService {

    private final MedidaRepository medidaRepository;
    private final ClienteUsuarioYPlanes cliente;

    public ProgresoService(MedidaRepository medidaRepository, ClienteUsuarioYPlanes cliente) {
        this.medidaRepository = medidaRepository;
        this.cliente = cliente;
    }

    @Transactional(readOnly = true)
    public ProgresoResponse consultar(Long usuarioId) {
        List<MedidaResponse> medidas = medidaRepository.findByUsuarioIdOrderByRegistradoEnAsc(usuarioId).stream()
                .map(MedidaResponse::desde)
                .toList();
        if (medidas.isEmpty()) {
            return new ProgresoResponse(medidas, null, null, null, null, null);
        }
        MedidaResponse primera = medidas.get(0);
        MedidaResponse ultima = medidas.get(medidas.size() - 1);
        double cambio = Math.round((ultima.pesoKg() - primera.pesoKg()) * 100.0) / 100.0;
        return new ProgresoResponse(medidas, primera.pesoKg(), ultima.pesoKg(), cambio, ultima.imc(),
                Imc.clasificar(ultima.imc()));
    }

    /** Medida registrada por el usuario desde la app; el peso nuevo se propaga a su perfil en auth. */
    @Transactional
    public MedidaResponse registrar(Long usuarioId, MedidaRequest peticion, PerfilResponse perfil) {
        Double altura = peticion.alturaCm() != null ? peticion.alturaCm()
                : perfil == null ? null : perfil.alturaCm();
        Medida medida = nuevaMedida(usuarioId, peticion.pesoKg(), altura);
        medida.setCinturaCm(peticion.cinturaCm());
        medida.setCaderaCm(peticion.caderaCm());
        medida.setPechoCm(peticion.pechoCm());
        medida.setBrazoCm(peticion.brazoCm());
        MedidaResponse guardada = MedidaResponse.desde(medidaRepository.save(medida));
        cliente.actualizarPeso(usuarioId, peticion.pesoKg());
        return guardada;
    }

    /** Medida que nace de un cambio de peso o altura en el perfil (la llama auth; no vuelve a avisarle). */
    @Transactional
    public MedidaResponse registrarDesdePerfil(Long usuarioId, Double pesoKg, Double alturaCm) {
        return MedidaResponse.desde(medidaRepository.save(nuevaMedida(usuarioId, pesoKg, alturaCm)));
    }

    private static Medida nuevaMedida(Long usuarioId, Double pesoKg, Double alturaCm) {
        Medida medida = new Medida();
        medida.setUsuarioId(usuarioId);
        medida.setPesoKg(pesoKg);
        medida.setAlturaCm(alturaCm);
        medida.setImc(Imc.calcular(pesoKg, alturaCm));
        return medida;
    }
}
