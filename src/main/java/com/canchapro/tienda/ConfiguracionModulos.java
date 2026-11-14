package com.canchapro.tienda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Properties;

/** La tienda puede activarse/desactivarse sin recompilar. */
public class ConfiguracionModulos {
    private final Path archivo;
    private boolean tiendaActiva;
    public ConfiguracionModulos(Path carpetaDatos) throws IOException {
        archivo = carpetaDatos.resolve("configuracion.properties");
        if (Files.exists(archivo)) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(archivo)) { p.load(in); }
            String valor = p.getProperty("modulo_tienda", "false").trim();
            if (!valor.equals("true") && !valor.equals("false"))
                throw new IOException("Valor modulo_tienda no valido (usar true o false)");
            tiendaActiva = Boolean.parseBoolean(valor);
        }
    }
    public boolean isTiendaActiva() { return tiendaActiva; }
    public void configurarTienda(boolean activar) throws IOException {
        Files.createDirectories(archivo.getParent());
        Properties p = new Properties(); p.setProperty("modulo_tienda", Boolean.toString(activar));
        Path temporal = Files.createTempFile(archivo.getParent(), "config_", ".tmp");
        try {
            try (OutputStream salida = Files.newOutputStream(temporal)) {
                p.store(salida, "Modulos opcionales de CanchaPro");
            }
            try { Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporal); }
        tiendaActiva=activar;
    }
}
