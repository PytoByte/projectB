package pytobyte.projectb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import pytobyte.projectb.client.DTO.AbilitiesDTO
import pytobyte.projectb.client.DTO.AgentDTO
import pytobyte.projectb.client.MyClient
import pytobyte.projectb.ui.theme.ProjectBTheme
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val screenState = remember { mutableStateOf("intro") }

            ProjectBTheme {
                Crossfade(
                    targetState = screenState,
                    animationSpec = tween(durationMillis = 1000),
                    label = ""
                ) { screen ->
                    when (screen.value) {
                        "intro" -> Screen1(screenState)
                        "main" -> Screen2()
                    }
                }
            }
        }
    }
}

@Composable
fun Screen1(screenState: MutableState<String>) {
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
        enter = slideInVertically(animationSpec = tween(durationMillis = 1000)) + fadeIn(
            animationSpec = tween(durationMillis = 1000)
        ),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 1000),
            targetOffsetY = { fullHeight -> fullHeight }) + fadeOut(
            animationSpec = tween(
                durationMillis = 1000
            )
        )
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
    val agents = remember { mutableStateListOf<Pair<MutableState<Boolean>, AgentDTO>>() }
    val selected = remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        val client = MyClient()
        client.getAgents().forEach {
            agents.add(
                Pair(
                    mutableStateOf(false),
                    Json(builderAction = { ignoreUnknownKeys = true }).decodeFromJsonElement(it)
                )
            )
            delay(50)
        }
        client.close()
    }
    val screenState = remember { mutableStateOf("column") }
    Crossfade(
        targetState = screenState,
        label = ""
    ) { screen ->
        when (screen.value) {
            "column" -> agentsList(screenState, agents, selected)
            "agent" -> AgentScreen(screenState, agents[selected.value].second)
        }
    }
}

@Composable
fun agentsList(
    screenState: MutableState<String>,
    agents: MutableList<Pair<MutableState<Boolean>, AgentDTO>>,
    selected: MutableState<Int>
) {
    val columnAnimationState = remember {
        MutableTransitionState(false)
    }
    val startTransition = remember {
        mutableStateOf(false)
    }

    val lazyScrollState:LazyListState  = rememberLazyListState(selected.value)

    AnimatedVisibility(
        columnAnimationState,
        Modifier.fillMaxSize(),
        enter = slideInHorizontally(),
        exit = slideOutHorizontally() + fadeOut()
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            state = lazyScrollState
        ) {
            itemsIndexed(
                agents,
                key = { index, item -> item.second.uuid }
            ) { index, item ->

                val changeScreenState = remember {
                    mutableStateOf(false)
                }
                val animationState = remember {
                    MutableTransitionState(item.first.value)
                }

                AnimatedVisibility(
                    animationState,
                    Modifier
                        .fillMaxSize()
                        .clickable {
                            selected.value = index
                            changeScreenState.value = true
                        },
                    enter = slideInHorizontally() + fadeIn(),
                    exit = slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 500)
                    ) + fadeOut(animationSpec = tween(durationMillis = 600, easing = LinearEasing))
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp)
                            .height(80.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            modifier = Modifier.size(70.dp),
                            model = item.second.displayIcon,
                            contentDescription = "AgentImage",
                            error = painterResource(id = R.drawable.unknow)
                        )
                        //Image(modifier=Modifier.size(100.dp), painter = painterResource(id = R.drawable.cringe), contentDescription = "CRINGE")
                        Spacer(modifier = Modifier.padding(2.dp))
                        Column {
                            Row {
                                Text(text = item.second.displayName, fontSize = 30.sp)
                                Text(
                                    modifier = Modifier.padding(start = 2.dp),
                                    text = "(${item.second.developerName})",
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = item.second.description,
                                fontSize = 20.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    animationState.targetState = true
                    item.first.value = true
                }

                LaunchedEffect(changeScreenState.value) {
                    if (changeScreenState.value) {
                        animationState.targetState = false
                        delay(500)
                        startTransition.value = true
                    }
                }
            }
        }
    }

    LaunchedEffect(startTransition.value) {
        if (startTransition.value) {
            columnAnimationState.targetState = false
            delay(1000)
            screenState.value = "agent"
        } else {
            columnAnimationState.targetState = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScreen(screenState: MutableState<String>, agent: AgentDTO) {
    val screenAnimationState = remember { MutableTransitionState(false) }
    val exitState = remember { mutableStateOf(false) }

    AnimatedVisibility(
        screenAnimationState,
        Modifier.fillMaxSize(),
        enter = slideInHorizontally() + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = 500)
        ) + fadeOut(animationSpec = tween(durationMillis = 400, easing = LinearEasing))
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .background(color = Color.Transparent),
            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        screenAnimationState.targetState = false
                        exitState.value = true
                    }) {
                        Text("Назад")
                    }
                }
            }
        )
        { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                Row() {
                    AsyncImage(
                        modifier = Modifier.size(70.dp),
                        model = agent.displayIcon,
                        contentDescription = "AgentImage",
                        error = painterResource(id = R.drawable.unknow)
                    )
                    Column {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(text = agent.displayName, fontSize = 30.sp)
                            Text(
                                modifier = Modifier.padding(start = 2.dp),
                                text = "(${agent.developerName})",
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            modifier = Modifier.padding(start = 2.dp),
                            text = agent.uuid,
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    modifier = Modifier,
                    text = agent.description,
                    fontSize = 20.sp,
                    color = Color.Black
                )
                Row() {
                    agent.characterTags?.forEach {
                        rotatingCard(it)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        screenAnimationState.targetState = true
    }

    LaunchedEffect(exitState.value) {
        if (exitState.value) {
            delay(500)
            screenState.value = "column"
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rotatingCard(tag: String) {

    val state = remember { mutableStateOf(true) }
    val contentState = remember { mutableStateOf(true) }

    val rotateValue = animateFloatAsState(
        targetValue = if (state.value) 0f else 180f,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing), label = ""
    )

    LaunchedEffect(rotateValue.value) {
        contentState.value = rotateValue.value <= 90f
    }

    Card(
        modifier = Modifier
            .graphicsLayer(rotationY = rotateValue.value)
            .size(100.dp),
        onClick = { state.value=state.value.not() }
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            if (contentState.value) {
                Text(text="Тег")
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(modifier= Modifier
                        .fillMaxSize()
                        .graphicsLayer(rotationY = 180f), text=tag)
                    Text(modifier= Modifier
                        .fillMaxSize()
                        .graphicsLayer(rotationY = 180f), text=tag)
                }
            }
        }
    }
}