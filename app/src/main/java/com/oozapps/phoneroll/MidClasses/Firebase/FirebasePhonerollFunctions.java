package com.oozapps.phoneroll.MidClasses.Firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

public class FirebasePhonerollFunctions {
    public static void getServerTime(final onGetServerDate onComplete) {
        com.google.firebase.functions.FirebaseFunctions.getInstance().getHttpsCallable("getServerDate")
                .call()
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        if (task.isSuccessful()) {
                            long timestamp = (long) task.getResult().getData();
                            if (onComplete != null) {
                                onComplete.onSuccess(timestamp);
                            }
                        } else {
                            onComplete.onFailed();
                        }
                    }
                });
    }
}
