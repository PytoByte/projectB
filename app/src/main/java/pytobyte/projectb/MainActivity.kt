package pytobyte.projectb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import pytobyte.projectb.client.DTO.AgentDTO
import pytobyte.projectb.client.MyClient
import pytobyte.projectb.ui.theme.ProjectBTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val screenState = remember {mutableStateOf("intro")}

            ProjectBTheme {
                Crossfade(
                    targetState = screenState,
                    animationSpec = tween(durationMillis = 10000),
                    label = "") { screen ->
                    when (screen.value) {
                        "intro" -> Screen1(screenState)
                        "main"  -> Screen2()
                    }
                }
            }
        }
    }
}
@Composable
fun Screen1(screenState:MutableState<String>) {
    val state = remember {
        MutableTransitionState(false)
    }

    LaunchedEffect(Unit) {
        state.targetState = true
        delay(2000)
        state.targetState = false
        delay(2000)
        screenState.value = "main"
    }

    AnimatedVisibility(
        visibleState = state,
        enter = slideInVertically(animationSpec = tween(durationMillis = 1000)) + fadeIn(animationSpec = tween(durationMillis = 1000)),
        exit = slideOutVertically(animationSpec = tween(durationMillis = 1000), targetOffsetY = {fullHeight ->  fullHeight}) + fadeOut(animationSpec = tween(durationMillis = 1000))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Демонстрация анимации",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )
        }
    }
}

@Composable
fun Screen2() {
    val agents = remember { mutableStateListOf<AgentDTO>() }

    LaunchedEffect(Unit) {
        val client = MyClient()
        client.getAgents(agents).forEach {
            agents.add(Json(builderAction = {ignoreUnknownKeys = true}).decodeFromJsonElement(it))
            delay(10)
        }
        client.close()
    }

    LazyColumn(
        Modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            agents,
            key = { index, item -> item.uuid },
            contentType = {index, item -> item::class}
        ) { index, item ->
            val animationState = remember {
                MutableTransitionState(false)
            }

            val successImageState = remember { mutableStateOf(true) }

            AnimatedVisibility(animationState,
                Modifier.fillMaxSize(),
                enter = slideInHorizontally(),
                exit = slideOutHorizontally()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    if (successImageState.value) {
                        AsyncImage(modifier=Modifier.size(50.dp), model = item.displayIcon, contentDescription = "AgentImage", error = painterResource(id = R.drawable.cringe))
                    } else {
                        Image(modifier=Modifier.size(50.dp), painter = painterResource(id = R.drawable.cringe2), contentDescription = "UnknownAgentImage")
                    }
                    Text(text = item.displayName, fontSize = 20.sp)
                }
            }

            LaunchedEffect(Unit) {
                animationState.targetState = true
            }
        }
    }

    /*AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(id = R.drawable.cringe2), contentDescription = "э")
        }
    }*/
}
