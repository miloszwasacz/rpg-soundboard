package com.gmail.dev.wasacz.rpgsoundboard.ui.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.ui.theme.RPGSoundboardTheme

@Composable
fun RoundIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: ButtonColors? = null,
    elevation: ButtonElevation? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val el = elevation?.elevation(enabled, interactionSource)?.value ?: 0.dp
    val elevationOverlay = LocalElevationOverlay.current
    val absoluteElevation = LocalAbsoluteElevation.current + el

    Box(
        modifier = Modifier
            .then(
                if (elevation != null) Modifier.shadow(el, CircleShape, false)
                else Modifier
            )
            .then(
                backgroundColor?.let {
                    val color = it.backgroundColor(enabled).value
                    Modifier.background(
                        elevationOverlay?.apply(color, absoluteElevation) ?: color,
                        CircleShape
                    )
                } ?: Modifier
            )
            .clip(CircleShape)
            .then(modifier)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false, radius = dimensionResource(R.dimen.button_ripple_radius))
            )
            .size(dimensionResource(R.dimen.icon_button_size)),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
        val contentColor = backgroundColor?.let {
            backgroundColor.contentColor(enabled).value
        } ?: LocalContentColor.current
        CompositionLocalProvider(
            LocalContentAlpha provides contentAlpha,
            LocalContentColor provides contentColor,
            content = content
        )
    }
}

//#region Previews
@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    RPGSoundboardTheme {
        Surface {
            Row(Modifier.padding(8.dp)) {
                RoundIconButton(onClick = {}) {
                    Icon(Icons.Rounded.Home, null)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomBackgroundPreview() {
    RPGSoundboardTheme {
        Surface {
            Row(Modifier.padding(8.dp)) {
                RoundIconButton(
                    backgroundColor = ButtonDefaults.buttonColors(),
                    onClick = {}
                ) {
                    Icon(Icons.Rounded.Home, null)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ElevatedPreview() {
    RPGSoundboardTheme {
        Surface {
            Row(Modifier.padding(8.dp)) {
                RoundIconButton(
                    elevation = ButtonDefaults.elevation(),
                    onClick = {}
                ) {
                    Icon(Icons.Rounded.Home, null)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DisabledPreview() {
    RPGSoundboardTheme {
        Surface {
            Row(Modifier.padding(8.dp)) {
                RoundIconButton(
                    backgroundColor = ButtonDefaults.buttonColors(),
                    enabled = false,
                    onClick = {}
                ) {
                    Icon(Icons.Rounded.Home, null)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DarkPreview() {
    RPGSoundboardTheme(true) {
        Surface {
            Row(Modifier.padding(8.dp)) {
                RoundIconButton(onClick = {}) {
                    Icon(Icons.Rounded.Home, null)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomBackgroundDarkPreview() {
    RPGSoundboardTheme(true) {
        Surface {
            Row(Modifier.padding(8.dp)) {
                RoundIconButton(
                    backgroundColor = ButtonDefaults.buttonColors(),
                    onClick = {}
                ) {
                    Icon(Icons.Rounded.Home, null)
                }
            }
        }
    }
}
//#endregion