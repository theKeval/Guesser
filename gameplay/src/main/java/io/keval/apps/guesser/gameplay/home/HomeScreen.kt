package io.keval.apps.guesser.gameplay.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.keval.apps.guesser.core.ui.theme.GuesserTheme
import io.keval.apps.guesser.domain.usecase.GetWelcomeMessageUseCase
import io.keval.apps.guesser.gameplay.R

@Composable
fun HomeRoute(
    getWelcomeMessageUseCase: GetWelcomeMessageUseCase,
    onStartClick: (HomeUiState) -> Unit,
    onTutorialClick: () -> Unit,
    onGameplayClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(getWelcomeMessageUseCase),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onPlayerModeSelected = viewModel::onPlayerModeSelected,
        onFriendSecretNumberChanged = viewModel::onFriendSecretNumberChanged,
        onStartClick = {
            if (viewModel.canStartGame()) {
                onStartClick(uiState)
            }
        },
        onTutorialClick = onTutorialClick,
        onGameplayClick = onGameplayClick,
        onAboutClick = onAboutClick,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onPlayerModeSelected: (PlayerMode) -> Unit,
    onFriendSecretNumberChanged: (String) -> Unit,
    onStartClick: () -> Unit,
    onTutorialClick: () -> Unit,
    onGameplayClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val titleWidth = (screenWidth * 0.82f).coerceAtMost(340.dp)
        val selectorWidth = (screenWidth * 0.69f).coerceAtMost(285.dp)
        val inputWidth = (screenWidth * 0.47f).coerceAtLeast(188.dp).coerceAtMost(224.dp)
        val startButtonWidth = (screenWidth * 0.80f).coerceAtMost(332.dp)
        val secondaryButtonWidth = (screenWidth * 0.71f).coerceAtMost(296.dp)
        val validationWidth = (screenWidth * 0.60f).coerceAtMost(240.dp)

        Image(
            painter = painterResource(id = R.drawable.home_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Image(
                painter = painterResource(id = R.drawable.home_title),
                contentDescription = null,
                modifier = Modifier
                    .width(titleWidth)
                    .aspectRatio(1.25f),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlayerModeToggle(
                playerMode = uiState.playerMode,
                modifier = Modifier.width(selectorWidth),
                onPlayerModeSelected = onPlayerModeSelected,
            )

            if (uiState.playerMode == PlayerMode.DOUBLE) {
                Spacer(modifier = Modifier.height(10.dp))
                FriendSecretInput(
                    value = uiState.friendSecretNumber,
                    onValueChange = onFriendSecretNumberChanged,
                    modifier = Modifier.width(inputWidth),
                )

                if (uiState.secretValidationMessage != null) {
                    Text(
                        text = uiState.secretValidationMessage,
                        modifier = Modifier
                            .width(validationWidth)
                            .padding(top = 6.dp),
                        color = Color(0xFFFFE4CA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (uiState.playerMode == PlayerMode.DOUBLE) 14.dp else 22.dp))

            HomeImageButton(
                imageRes = R.drawable.home_button_start,
                contentDescription = stringResource(R.string.cd_start_game),
                aspectRatio = 500f / 120f,
                modifier = Modifier.width(startButtonWidth),
                onClick = onStartClick,
            )

            Spacer(modifier = Modifier.height(14.dp))

            HomeImageButton(
                imageRes = R.drawable.home_button_tutorial,
                contentDescription = stringResource(R.string.cd_tutorial),
                aspectRatio = 5f,
                modifier = Modifier.width(secondaryButtonWidth),
                onClick = onTutorialClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            HomeImageButton(
                imageRes = R.drawable.home_button_gameplay,
                contentDescription = stringResource(R.string.cd_gameplay),
                aspectRatio = 5f,
                modifier = Modifier.width(secondaryButtonWidth),
                onClick = onGameplayClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            HomeImageButton(
                imageRes = R.drawable.home_button_about,
                contentDescription = stringResource(R.string.cd_about),
                aspectRatio = 5f,
                modifier = Modifier.width(secondaryButtonWidth),
                onClick = onAboutClick,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PlayerModeToggle(
    playerMode: PlayerMode,
    modifier: Modifier = Modifier,
    onPlayerModeSelected: (PlayerMode) -> Unit,
) {
    val singleDescription = stringResource(R.string.cd_single_player)
    val doubleDescription = stringResource(R.string.cd_double_player)
    val selectorDescription = stringResource(R.string.cd_player_mode_selector)

    Box(
        modifier = modifier.aspectRatio(500f / 120f),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                id = if (playerMode == PlayerMode.SINGLE) {
                    R.drawable.home_player_mode_single
                } else {
                    R.drawable.home_player_mode_double
                },
            ),
            contentDescription = selectorDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clickable(role = Role.RadioButton) { onPlayerModeSelected(PlayerMode.SINGLE) }
                    .semantics { contentDescription = singleDescription },
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clickable(role = Role.RadioButton) { onPlayerModeSelected(PlayerMode.DOUBLE) }
                    .semantics { contentDescription = doubleDescription },
            )
        }
    }
}

@Composable
private fun FriendSecretInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.aspectRatio(500f / 100f),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.home_secret_input_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )

        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.filter(Char::isDigit)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(
                color = Color(0xFF6B3B13),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.friend_secret_hint),
                            style = LocalTextStyle.current.copy(
                                color = Color(0xFF9A7C52),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                            ),
                            textAlign = TextAlign.Center,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun HomeImageButton(
    imageRes: Int,
    contentDescription: String,
    aspectRatio: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = contentDescription,
        modifier = modifier
            .aspectRatio(aspectRatio)
            .scale(if (isPressed) 0.95f else 1f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentScale = ContentScale.FillBounds,
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    GuesserTheme {
        HomeScreen(
            uiState = HomeUiState(playerMode = PlayerMode.DOUBLE, friendSecretNumber = "258"),
            onPlayerModeSelected = {},
            onFriendSecretNumberChanged = {},
            onStartClick = {},
            onTutorialClick = {},
            onGameplayClick = {},
            onAboutClick = {},
        )
    }
}
