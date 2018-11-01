package com.bzzzchat.rxfirebase;

import com.bzzzchat.rxfirebase.auth.TlMaybeOnSubscribe;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import io.reactivex.Maybe;

public class RxFirebaseAuth {
    public static Maybe<AuthResult> loginByEmail(FirebaseAuth auth, String email, String password) {
        return Maybe.create(new TlMaybeOnSubscribe(auth.signInWithEmailAndPassword(email, password)));
    }
}
