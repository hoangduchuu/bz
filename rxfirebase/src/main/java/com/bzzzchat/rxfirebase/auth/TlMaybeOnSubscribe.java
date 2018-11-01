package com.bzzzchat.rxfirebase.auth;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;

public class TlMaybeOnSubscribe implements MaybeOnSubscribe<AuthResult> {
    private final Task<AuthResult> task;

    public TlMaybeOnSubscribe(Task<AuthResult> task) {
        this.task = task;
    }

    @Override
    public void subscribe(MaybeEmitter<AuthResult> emitter) {
        AuthListener listener = new AuthListener(emitter);
        task.addOnSuccessListener(listener);
        task.addOnFailureListener(listener);
    }
}
