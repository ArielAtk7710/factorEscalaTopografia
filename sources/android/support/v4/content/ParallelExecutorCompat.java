package android.support.v4.content;

import android.os.AsyncTask;
import java.util.concurrent.Executor;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public final class ParallelExecutorCompat {
    @Deprecated
    public static Executor getParallelExecutor() {
        return AsyncTask.THREAD_POOL_EXECUTOR;
    }

    private ParallelExecutorCompat() {
    }
}
