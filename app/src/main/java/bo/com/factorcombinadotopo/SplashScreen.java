package bo.com.factorcombinadotopo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SurveyApplication app = (SurveyApplication) getApplication();
        TextView txtStatus = findViewById(R.id.txt_splash_status);
        
        long startTime = System.currentTimeMillis();

        // Observar reactivamente el estado de la aplicación
        app.isReady.observe(this, ready -> {
            if (ready) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long delay = Math.max(0, 4500 - elapsedTime);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (txtStatus != null) txtStatus.setText("Sistemas listos");
                    
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        startActivity(Intent.makeRestartActivityTask(new Intent(SplashScreen.this, MainActivity.class).getComponent()));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }, 200);
                }, delay);
            }
        });
    }
}
