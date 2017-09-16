package comp_sci_squad.com.github.url_irl;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public class App extends Application {

    private RefWatcher refWatcher;
    static App instance;

    public static RefWatcher getRefWatcher(Context context) {
        App application = (App) context.getApplicationContext();
        return application.refWatcher;
    }

    @Override public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        instance = this;
        refWatcher = LeakCanary.install(this);
    }

    public void mustDie(Object object) {
        if (refWatcher != null) {
            refWatcher.watch(object);
        }
    }
}