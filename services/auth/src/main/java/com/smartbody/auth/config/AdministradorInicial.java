package com.smartbody.auth.config;

import com.smartbody.auth.service.CuentaAdministradorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Al arrancar, crea la cuenta de administrador si se configuraron SMARTBODY_ADMIN_EMAIL y SMARTBODY_ADMIN_PASSWORD. */
@Component
public class AdministradorInicial implements ApplicationRunner {

    private final CuentaAdministradorService cuentaAdministradorService;
    private final String email;
    private final String password;
    private final String nombre;

    public AdministradorInicial(CuentaAdministradorService cuentaAdministradorService,
                                @Value("${smartbody.admin.email:}") String email,
                                @Value("${smartbody.admin.password:}") String password,
                                @Value("${smartbody.admin.nombre:Administrador}") String nombre) {
        this.cuentaAdministradorService = cuentaAdministradorService;
        this.email = email;
        this.password = password;
        this.nombre = nombre;
    }

    @Override
    public void run(ApplicationArguments args) {
        cuentaAdministradorService.asegurarAdministrador(email, password, nombre);
    }
}
