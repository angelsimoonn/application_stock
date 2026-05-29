package com.example.application_stock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application_stock.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView txtCargando, txtPorcentaje;
    
    // Uso de AtomicInteger para garantizar la exclusión mutua y seguridad entre hilos (Thread-Safe)
    // Justificación PSP: Punto 1.1 - Manejo de sincronización y evitar condiciones de carrera.
    private AtomicInteger progresoGlobal = new AtomicInteger(0);
    private final int MAX_PROGRESS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBar);
        txtCargando = findViewById(R.id.txtCargando);
        txtPorcentaje = findViewById(R.id.txtPorcentaje);

        iniciarCargaConcurrente();
    }

    /**
     * Implementación de Programación Paralela (PSP - Punto 1.3)
     * Se utiliza un Pool de 4 hilos fijos para simular la carga de servicios independientes.
     */
    private void iniciarCargaConcurrente() {
        // Creamos un pool de hilos para aprovechar los múltiples núcleos del procesador
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Tarea 1: Verificación de Servidor FTP
        executor.execute(() -> simularTarea("Verificando servidor FTP...", 0, 25));

        // Tarea 2: Cifrado de Seguridad (Punto b del documento)
        executor.execute(() -> simularTarea("Preparando algoritmos de cifrado...", 25, 50));

        // Tarea 3: Sincronización de Base de Datos
        executor.execute(() -> simularTarea("Sincronizando inventario local...", 50, 75));

        // Tarea 4: Finalización y limpieza
        executor.execute(() -> simularTarea("Iniciando interfaz de usuario...", 75, 100));

        // Hilo de control para monitorear el fin de la carga
        new Thread(() -> {
            while (progresoGlobal.get() < MAX_PROGRESS) {
                try { Thread.sleep(100); } catch (InterruptedException e) {}
            }
            // Navegación al finalizar todas las tareas paralelas
            new Handler(Looper.getMainLooper()).post(() -> {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            });
        }).start();
        
        executor.shutdown();
    }

    private void simularTarea(String nombreTarea, int inicio, int fin) {
        for (int i = inicio; i < fin; i++) {
            try {
                // Simulación de carga de trabajo pesada
                Thread.sleep((long) (Math.random() * 150)); 
                
                int actual = progresoGlobal.incrementAndGet();
                
                // Actualización de la UI desde el hilo de ejecución principal
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBar.setProgress(actual);
                    txtPorcentaje.setText(actual + "%");
                    txtCargando.setText(nombreTarea);
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
