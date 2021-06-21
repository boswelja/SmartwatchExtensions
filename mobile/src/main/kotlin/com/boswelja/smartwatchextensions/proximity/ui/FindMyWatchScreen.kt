package com.boswelja.smartwatchextensions.proximity.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.CircularCountDownTimer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalAnimationApi
@Preview(showBackground = true)
@ExperimentalTime
@Composable
fun FindMyWatchScreen(
    modifier: Modifier = Modifier,
    onFindWatch: () -> Unit = { },
    onCancel: () -> Unit = { }
) {
    var isRinging by remember { mutableStateOf(false) }
    Row(
        modifier = modifier.padding(16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Crossfade(isRinging) {
            if (it) {
                Icon(
                    painterResource(R.drawable.ic_find_watch),
                    contentDescription = "Ringing Watch",
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .aspectRatio(1f)
                )
            } else {
                CircularCountDownTimer(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    time = Duration.seconds(5),
                    onFinished = {
                        isRinging = true
                        onFindWatch()
                    }
                )
            }
        }
        AnimatedVisibility(visible = !isRinging) {
            Button(
                onClick = onCancel
            ) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        }
    }
}
