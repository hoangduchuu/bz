package com.bzzzchat.rxfirebase.auth;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;

import io.reactivex.MaybeEmitter;

public class AuthListener implements OnSuccessListener<AuthResult>, OnFailureListener {
    private final MaybeEmitter<AuthResult> emitter;

    public AuthListener(MaybeEmitter<AuthResult> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onSuccess(AuthResult authResult) {
        emitter.onSuccess(authResult);
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        emitter.onError(e);
    }
}
