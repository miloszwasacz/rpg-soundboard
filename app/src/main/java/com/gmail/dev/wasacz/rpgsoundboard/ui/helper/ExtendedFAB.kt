package com.gmail.dev.wasacz.rpgsoundboard.ui.helper

import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.material.ExtendedFloatingActionButton as MaterialExtendedFloatingActionButton

@Composable
fun ExtendedFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    val backgroundColor =
        if (enabled) MaterialTheme.colors.secondary
        else MaterialTheme.colors.onSurface.copy(0.12f).compositeOver(MaterialTheme.colors.surface)
    val contentColor =
        if (enabled) contentColorFor(backgroundColor)
        else MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled)
    CompositionLocalProvider(LocalRippleTheme provides if (enabled) LocalRippleTheme.current else NoRippleTheme) {
        MaterialExtendedFloatingActionButton(
            onClick = {
                if (enabled) onClick()
            },
            modifier = modifier,
            text = text,
            icon = icon,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
        )
    }
}

private object NoRippleTheme: RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha() = RippleAlpha(0f, 0f, 0f, 0f)
}