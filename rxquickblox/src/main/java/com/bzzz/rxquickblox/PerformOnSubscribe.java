package com.bzzz.rxquickblox;

import android.os.Bundle;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.Lo;
import com.quickblox.core.server.Performer;

import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.exceptions.UndeliverableException;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.CompositeException;
import rx.exceptions.Exceptions;
import rx.plugins.RxJavaPlugins;

/**
 * Created by tuanluong on 2/6/18.
 */

public class PerformOnSubscribe<T> implements FlowableOnSubscribe<T> {
    public static final String TAG = PerformOnSubscribe.class.getSimpleName();
    private Performer<T> originalPerformer;
    private Bundle bundle;

    PerformOnSubscribe(Performer<T> originalPerformer) {
        this.originalPerformer = originalPerformer;
    }

    private void processOnError(FlowableEmitter<T> emitter, QBResponseException e) {
        Lo.g(TAG + ":processOnError ");

        try {
            emitter.onError(e);
        } catch (UndeliverableException e1) {
            e1.printStackTrace();
            CompositeException composite = new CompositeException(e, e1);
            RxJavaPlugins.getInstance().getErrorHandler().handleError(composite);
        } catch (Exception var5) {
            Lo.g(TAG + ":error result " + var5.getLocalizedMessage());
            Exceptions.throwIfFatal(var5);
            CompositeException composite = new CompositeException(e, var5);
            RxJavaPlugins.getInstance().getErrorHandler().handleError(composite);
        }
    }

    @Override
    public void subscribe(final FlowableEmitter<T> emitter) throws Exception {
        if (!emitter.isCancelled() && !this.originalPerformer.isCanceled()) {
            this.originalPerformer.performAsync(new QBEntityCallback<T>() {
                @Override
                public void onSuccess(T t, Bundle bundle) {
                    Lo.g(TAG + ":sucess result");
                    emitter.onNext(t);
                    emitter.onComplete();
                }

                @Override
                public void onError(QBResponseException e) {
                    processOnError(emitter, e);
                }
            });
        }
    }
}
