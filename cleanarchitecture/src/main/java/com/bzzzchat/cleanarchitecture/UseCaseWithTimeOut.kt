package com.bzzzchat.cleanarchitecture

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * Created by Huu Hoang on 11/23/17.
 */

/**
 * the same with UseeCase.tk,
 * this class just apply the timeOut to handle the timeout while do some work.
 * let's see line 49
 */

/**
 * Abstract class for a Use Case (Interactor in terms of Clean Architecture).
 * This interface represents a execution unit for different use cases (this means any use case
 * in the application should implement this contract).
 *
 * By convention each UseCase implementation will return the result using a {@link DisposableObserver}
 * that will execute its job in a background thread and will post the result in the UI thread.
 */
abstract class UseCaseWithTimeOut<T, in Params>(private val threadExecutor: ThreadExecutor, private val postExecutionThread: PostExecutionThread) {
    private var disposables = CompositeDisposable()
    private var lastDisposable: Disposable? = null

    /**
     * Builds an {@link Observable} which will be used when executing the current {@link UseCase}.
     */
    abstract fun buildUseCaseObservable(params: Params): Observable<T>


    /**
     * Executes the current use case.
     *
     * @param observer [DisposableObserver] which will be listening to the observable build
     * by [.buildUseCaseObservable] ()} method.
     * @param params   Parameters (Optional) used to build/execute this use case.
     */
    fun <O> execute(observer: O, params: Params) where O: DisposableObserver<T> {
        val observable = this.buildUseCaseObservable(params)
                .timeout(BuildConfig.TIMEOUT_INTERVAL_INTERNET_CONNECTION,TimeUnit.SECONDS)
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.scheduler)
        addDisposable(observable.subscribeWith(observer))
    }

    /**
     * Dispose from current [CompositeDisposable].
     */
    fun dispose() {
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
    }

    fun unsubscribe() {
        if (lastDisposable != null) {
            disposables.remove(lastDisposable!!)
        }
    }

    /**
     * Dispose from current [CompositeDisposable].
     */
    private fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
        lastDisposable = disposable
    }
}