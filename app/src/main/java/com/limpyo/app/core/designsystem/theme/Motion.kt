package com.limpyo.app.core.designsystem.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/** Turns a spring token from tokens.json into a Compose animation spec. */
fun <T> SpringToken.spec(): SpringSpec<T> = spring(dampingRatio = dampingRatio, stiffness = stiffness)

fun <T> tweenFast(easing: Easing = LimpyoMotion.easeStandard): TweenSpec<T> =
    tween(LimpyoMotion.durationFast, easing = easing)

fun <T> tweenShort(easing: Easing = LimpyoMotion.easeStandard): TweenSpec<T> =
    tween(LimpyoMotion.durationShort, easing = easing)

fun <T> tweenMedium(easing: Easing = LimpyoMotion.easeEnter): TweenSpec<T> =
    tween(LimpyoMotion.durationMedium, easing = easing)
