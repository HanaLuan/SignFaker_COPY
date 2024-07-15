package com.tencent.mobileqq.dt;

import android.content.Context;
import com.tencent.mobileqq.fe.IFEKitLog;

/* compiled from: Proguard */
/* loaded from: classes.dex */
public class dtn {

    /* renamed from: a, reason: collision with root package name */
    private static dtn f3133a;

    public native void initContext(Context context);

    public native void initLog(IFEKitLog iFEKitLog);

    public native void initUin(String str);

    public static dtn a() {
        if (f3133a == null) {
            synchronized (dtn.class) {
                if (f3133a == null) {
                    f3133a = new dtn();
                }
            }
        }
        return f3133a;
    }
}