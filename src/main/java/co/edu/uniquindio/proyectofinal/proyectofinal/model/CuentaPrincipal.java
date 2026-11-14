package co.edu.uniquindio.proyectofinal.proyectofinal.model;

import java.time.LocalDateTime;

public class CuentaPrincipal extends Cuenta {

    private static final long serialVersionUID= 1L;

    public CuentaPrincipal(Usuario propietario, LocalDateTime fechaCreacion) {
        super("CP-" + propietario.getId(), propietario, fechaCreacion);
    }

    @Override
    public String getNombre() { return "Disponible"; }

    @Override
    public boolean isActiva() { return true; }

    @Override
    public boolean esPrincipal() { return true; }

}
