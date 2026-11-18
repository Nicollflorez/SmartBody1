package com.smartbody.progreso.dto;

import com.smartbody.progreso.entity.Medida;

import java.time.LocalDateTime;

public record MedidaResponse(Long id,
                             Double pesoKg,
                             Double alturaCm,
                             Double cinturaCm,
                             Double caderaCm,
                             Double pechoCm,
                             Double brazoCm,
                             Double imc,
                             LocalDateTime registradoEn) {

    public static MedidaResponse desde(Medida medida) {
        return new MedidaResponse(medida.getId(), medida.getPesoKg(), medida.getAlturaCm(), medida.getCinturaCm(),
                medida.getCaderaCm(), medida.getPechoCm(), medida.getBrazoCm(), medida.getImc(),
                medida.getRegistradoEn());
    }
}
