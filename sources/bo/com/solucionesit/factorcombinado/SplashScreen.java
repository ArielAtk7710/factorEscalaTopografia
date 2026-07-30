package bo.com.solucionesit.factorcombinado;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/* JADX INFO: loaded from: classes.dex */
public class SplashScreen extends Activity {
    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable() { // from class: bo.com.solucionesit.factorcombinado.SplashScreen.1
            @Override // java.lang.Runnable
            public void run() {
                Intent intent = new Intent(SplashScreen.this, (Class<?>) MainActivity.class);
                SplashScreen.this.startActivity(intent);
                SplashScreen.this.finish();
            }
        }, 3000L);
    }
}
