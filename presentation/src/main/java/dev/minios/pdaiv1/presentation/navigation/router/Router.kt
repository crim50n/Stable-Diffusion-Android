package dev.minios.pdaiv1.presentation.navigation.router

import dev.minios.pdaiv1.presentation.navigation.NavigationEffect
import io.reactivex.rxjava3.core.Observable

interface Router<T : NavigationEffect>  {
    fun observe(): Observable<T>
}
