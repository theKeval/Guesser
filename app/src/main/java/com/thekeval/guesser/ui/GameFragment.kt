package com.thekeval.guesser.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.thekeval.guesser.R
import com.thekeval.guesser.adapters.GuessesAdapter
import com.thekeval.guesser.data.UserPreferences
import com.thekeval.guesser.databinding.FragmentGameBinding
import com.thekeval.guesser.viewmodel.GameViewModel
import kotlin.random.Random

class GameFragment : Fragment() {

    private enum class SoundChannel {
        GUESS,
        KEYPAD,
    }

    private lateinit var binding: FragmentGameBinding
    private lateinit var viewModel: GameViewModel
    private val guessesAdapter = GuessesAdapter()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var isAutoMode = true
    private var soundPool: SoundPool? = null
    private var soundRight = 0
    private var soundWrong = 0
    private var soundWin = 0
    private var soundKey = 0
    private var baseScrollBottomPadding = 0
    private var baseKeyboardBottomMargin = 0
    private var baseListBottomPadding = 0
    private var maxGameplayScroll = 0
    private var isKeyboardVisible = false
    private var scrollClampListener: ViewTreeObserver.OnScrollChangedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[GameViewModel::class.java]
        binding.etSeekerNumber.showSoftInputOnFocus = false

        setupSoundPool()
        baseScrollBottomPadding = binding.gameScroll.paddingBottom
        baseKeyboardBottomMargin = (binding.customKeyboardCard.layoutParams as MarginLayoutParams).bottomMargin
        baseListBottomPadding = binding.rvGuesses.paddingBottom

        binding.rvGuesses.adapter = guessesAdapter
        setupKeyboardAwareLayout()
        setupInteractions()
        observeViewModel()
        renderModeOnly()
        renderState(viewModel.gameState.value ?: GameViewModel.GameState.NOT_STARTED)
        binding.root.post { updateGuessListViewport() }

        return binding.root
    }

    private fun setupInteractions() {
        binding.btnAuto.setOnClickListener {
            when (viewModel.gameState.value) {
                GameViewModel.GameState.STARTED -> askToRevealSecret()
                GameViewModel.GameState.NOT_STARTED -> {
                    viewModel.startAutoGame()
                    binding.etNumber.setText(viewModel.getSecretNumber())
                    binding.viewHide.visibility = View.VISIBLE
                }
                else -> Unit
            }
        }

        binding.btnHide.setOnClickListener {
            when (viewModel.gameState.value) {
                GameViewModel.GameState.STARTED -> askToRevealSecret()
                GameViewModel.GameState.NOT_STARTED -> {
                    val secret = binding.etNumber.text.toString().trim()
                    if (!viewModel.startFriendGame(secret)) {
                        showMessage(
                            titleRes = R.string.invalid_secret_title,
                            messageRes = R.string.invalid_secret_message,
                        )
                        binding.etNumber.text?.clear()
                        return@setOnClickListener
                    }
                    binding.viewHide.visibility = View.VISIBLE
                    binding.etNumber.clearFocus()
                }
                else -> Unit
            }
        }

        binding.btnCheck.setOnClickListener {
            val guess = binding.etSeekerNumber.text.toString().trim()
            if (!viewModel.isValidUnique3Digits(guess)) {
                showMessage(
                    titleRes = R.string.invalid_guess_title,
                    messageRes = R.string.invalid_guess_message,
                )
                binding.etSeekerNumber.text?.clear()
                return@setOnClickListener
            }

            val remark = viewModel.submitGuess(guess) ?: return@setOnClickListener
            binding.etSeekerNumber.text?.clear()

            if (remark == getString(R.string.winner_remark)) {
                playWinFeedback()
                showMessage(
                    titleRes = R.string.winner_title,
                    messageRes = R.string.winner_message,
                )
            } else if (remark.contains("R")) {
                playRightGuessFeedback()
            } else {
                playWrongGuessFeedback()
            }

            binding.etSeekerNumber.requestFocus()
            showCustomKeyboard()
            scrollGuessesToBottom()
        }

        binding.btnReset.setOnClickListener {
            if (viewModel.gameState.value == GameViewModel.GameState.STARTED) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.reset_title)
                    .setMessage(R.string.reset_message)
                    .setPositiveButton(R.string.reset_positive) { _, _ ->
                        viewModel.resetGame()
                        clearEntryFields()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            } else {
                viewModel.resetGame()
                clearEntryFields()
            }
        }

        binding.switchMode.setOnCheckedChangeListener { _, checked ->
            val targetAutoMode = !checked
            if (targetAutoMode == isAutoMode) {
                return@setOnCheckedChangeListener
            }

            if (viewModel.gameState.value == GameViewModel.GameState.STARTED) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.switch_mode_title)
                    .setMessage(R.string.switch_mode_message)
                    .setPositiveButton(R.string.switch_mode_positive) { _, _ ->
                        isAutoMode = targetAutoMode
                        viewModel.resetGame()
                        clearEntryFields()
                        renderModeOnly()
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        binding.switchMode.isChecked = !checked
                    }
                    .show()
            } else {
                isAutoMode = targetAutoMode
                viewModel.resetGame()
                clearEntryFields()
                renderModeOnly()
            }
        }

        binding.switchTxtAppMode.setOnClickListener { binding.switchMode.isChecked = false }
        binding.switchTxtFriendMode.setOnClickListener { binding.switchMode.isChecked = true }

        setupCustomKeyboard()

        binding.etSeekerNumber.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showCustomKeyboard()
                postKeyboardScroll()
            } else {
                hideCustomKeyboard()
            }
        }

        binding.etSeekerNumber.setOnClickListener {
            binding.etSeekerNumber.requestFocus()
            showCustomKeyboard()
            postKeyboardScroll()
        }
    }

    private fun observeViewModel() {
        viewModel.guesses.observe(viewLifecycleOwner) { guesses ->
            val newestFirst = guesses.asReversed()
            guessesAdapter.submitList(newestFirst)
            binding.rvGuesses.visibility = if (guesses.isEmpty()) View.GONE else View.VISIBLE
            if (guesses.isNotEmpty()) {
                binding.rvGuesses.post { binding.rvGuesses.smoothScrollToPosition(0) }
            }
        }

        viewModel.gameState.observe(viewLifecycleOwner) { state ->
            renderState(state)
        }
    }

    private fun renderModeOnly() {
        binding.btnAuto.visibility = if (isAutoMode) View.VISIBLE else View.GONE
        binding.btnHide.visibility = if (isAutoMode) View.GONE else View.VISIBLE
        binding.etNumber.isEnabled = !isAutoMode

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.brand_primary)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        binding.switchTxtAppMode.setTextColor(if (isAutoMode) selectedColor else unselectedColor)
        binding.switchTxtFriendMode.setTextColor(if (isAutoMode) unselectedColor else selectedColor)
    }

    private fun renderState(state: GameViewModel.GameState) {
        when (state) {
            GameViewModel.GameState.NOT_STARTED -> {
                binding.txtStatus.setText(if (isAutoMode) R.string.initial_status_app_mode else R.string.initial_status_friend_mode)
                binding.btnReset.isEnabled = false
                binding.btnCheck.isEnabled = false
                binding.etSeekerNumber.isEnabled = false
                binding.guessInputLayout.visibility = View.GONE
                binding.etSeekerNumber.visibility = View.GONE
                binding.btnCheck.visibility = View.GONE
                binding.viewHide.visibility = View.GONE
                binding.btnAuto.setText(R.string.action_auto)
                binding.btnHide.setText(R.string.action_hide)
                binding.btnAuto.isEnabled = true
                binding.btnHide.isEnabled = true
                hideCustomKeyboardAndResetFocus()
            }

            GameViewModel.GameState.STARTED -> {
                binding.txtStatus.setText(R.string.game_on_status)
                binding.btnReset.isEnabled = true
                binding.btnCheck.isEnabled = true
                binding.etSeekerNumber.isEnabled = true
                binding.guessInputLayout.visibility = View.VISIBLE
                binding.etSeekerNumber.visibility = View.VISIBLE
                binding.btnCheck.visibility = View.VISIBLE
                binding.btnAuto.setText(R.string.action_show)
                binding.btnHide.setText(R.string.action_show)
                binding.etSeekerNumber.requestFocus()
                showCustomKeyboard()
                postKeyboardScroll()
            }

            GameViewModel.GameState.WON -> {
                binding.txtStatus.setText(R.string.status_play_again)
                binding.viewHide.visibility = View.GONE
                binding.btnCheck.isEnabled = false
                binding.etSeekerNumber.isEnabled = false
                binding.btnAuto.isEnabled = false
                binding.btnHide.isEnabled = false
                hideCustomKeyboardAndResetFocus()
            }

            GameViewModel.GameState.ABANDONED -> {
                binding.txtStatus.setText(R.string.status_play_again)
                binding.viewHide.visibility = View.GONE
                binding.btnCheck.isEnabled = false
                binding.etSeekerNumber.isEnabled = false
                binding.btnAuto.isEnabled = false
                binding.btnHide.isEnabled = false
                hideCustomKeyboardAndResetFocus()
            }
        }
    }

    private fun setupKeyboardAwareLayout() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val systemBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val safeBottom = maxOf(systemBottom, imeBottom)

            binding.rvGuesses.setPadding(
                binding.rvGuesses.paddingLeft,
                binding.rvGuesses.paddingTop,
                binding.rvGuesses.paddingRight,
                baseListBottomPadding + safeBottom,
            )

            binding.gameScroll.setPadding(
                binding.gameScroll.paddingLeft,
                binding.gameScroll.paddingTop,
                binding.gameScroll.paddingRight,
                baseScrollBottomPadding + safeBottom,
            )

            val keyboardParams = binding.customKeyboardCard.layoutParams as MarginLayoutParams
            keyboardParams.bottomMargin = baseKeyboardBottomMargin + systemBottom
            binding.customKeyboardCard.layoutParams = keyboardParams

            updateGuessListViewport()

            insets
        }
        ViewCompat.requestApplyInsets(binding.root)

        scrollClampListener = ViewTreeObserver.OnScrollChangedListener {
            val scrollY = binding.gameScroll.scrollY
            if (isKeyboardVisible && scrollY > maxGameplayScroll) {
                binding.gameScroll.scrollTo(0, maxGameplayScroll)
            }
        }
        binding.gameScroll.viewTreeObserver.addOnScrollChangedListener(scrollClampListener)

        binding.gameScroll.post {
            maxGameplayScroll = binding.gameInfoCard.bottom
            updateGuessListViewport()
        }
    }

    private fun askToRevealSecret() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.reveal_title)
            .setMessage(R.string.reveal_message)
            .setPositiveButton(R.string.reveal_positive) { _, _ ->
                binding.viewHide.visibility = View.GONE
                binding.etNumber.setText(viewModel.getSecretNumber())
                viewModel.revealAndAbandon()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun clearEntryFields() {
        binding.etNumber.text?.clear()
        binding.etSeekerNumber.text?.clear()
        hideCustomKeyboardAndResetFocus()
    }

    private fun showMessage(titleRes: Int, messageRes: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setPositiveButton(R.string.got_it, null)
            .show()
    }

    private fun postKeyboardScroll() {
        postDelayed(80L) { focusGameAreaForInput() }
        postDelayed(210L) { focusGameAreaForInput() }
    }

    private fun focusGameAreaForInput() {
        binding.gameScroll.post {
            binding.gameScroll.smoothScrollTo(0, binding.linearLayoutViewHide.top)
            scrollGuessesToBottom()
        }
    }

    private fun scrollGuessesToBottom() {
        val size = guessesAdapter.itemCount
        if (size > 0) {
            binding.rvGuesses.post {
                binding.rvGuesses.smoothScrollToPosition(0)
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupCustomKeyboard() {
        val digitButtons = mapOf(
            binding.key0 to '0',
            binding.key1 to '1',
            binding.key2 to '2',
            binding.key3 to '3',
            binding.key4 to '4',
            binding.key5 to '5',
            binding.key6 to '6',
            binding.key7 to '7',
            binding.key8 to '8',
            binding.key9 to '9',
        )

        digitButtons.forEach { (button, digit) ->
            button.setOnClickListener {
                animateKeyPress(button)
                playSound(soundKey, 0.85f, SoundChannel.KEYPAD)
                appendGuessDigit(digit)
            }
        }

        binding.keyDelete.setOnClickListener {
            animateKeyPress(binding.keyDelete)
            playSound(soundKey, 0.85f, SoundChannel.KEYPAD)
            removeLastGuessDigit()
        }
        binding.keyClear.setOnClickListener {
            animateKeyPress(binding.keyClear)
            playSound(soundKey, 0.85f, SoundChannel.KEYPAD)
            binding.etSeekerNumber.text?.clear()
        }
        binding.keyHideKeyboard.setOnClickListener {
            animateKeyPress(binding.keyHideKeyboard)
            playSound(soundKey, 0.85f, SoundChannel.KEYPAD)
            hideCustomKeyboardAndResetFocus()
            scrollGuessesToBottom()
        }
    }

    private fun appendGuessDigit(digit: Char) {
        val current = binding.etSeekerNumber.text?.toString().orEmpty()
        if (current.length >= 3 || current.contains(digit)) {
            playWrongGuessFeedback()
            return
        }

        val updated = current + digit
        binding.etSeekerNumber.setText(updated)
        binding.etSeekerNumber.setSelection(updated.length)
    }

    private fun removeLastGuessDigit() {
        val current = binding.etSeekerNumber.text?.toString().orEmpty()
        if (current.isEmpty()) {
            return
        }
        val updated = current.dropLast(1)
        binding.etSeekerNumber.setText(updated)
        binding.etSeekerNumber.setSelection(updated.length)
    }

    private fun showCustomKeyboard() {
        hideKeyboard(binding.etSeekerNumber)
        isKeyboardVisible = true
        binding.customKeyboardCard.visibility = View.VISIBLE
        maxGameplayScroll = binding.gameInfoCard.bottom
        updateGuessListViewport()
        binding.gameScroll.post {
            val target = maxGameplayScroll.coerceAtLeast(0)
            binding.gameScroll.smoothScrollTo(0, target)
        }
        postKeyboardScroll()
    }

    private fun hideCustomKeyboard() {
        isKeyboardVisible = false
        binding.customKeyboardCard.visibility = View.GONE
        updateGuessListViewport()
    }

    private fun hideCustomKeyboardAndResetFocus() {
        hideKeyboard(binding.etSeekerNumber)
        hideCustomKeyboard()
        binding.etSeekerNumber.clearFocus()
        binding.txtStatus.requestFocus()
        binding.gameScroll.post { binding.gameScroll.smoothScrollTo(0, 0) }
        scrollGuessesToBottom()
    }

    private fun playWinFeedback() {
        playSound(soundWin, 1.0f, SoundChannel.GUESS)
        vibrateIfEnabled(120)

        binding.txtStatus.animate()
            .scaleX(1.09f)
            .scaleY(1.09f)
            .setDuration(160)
            .withEndAction {
                binding.txtStatus.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(140)
                    .start()
            }
            .start()

        binding.gameInfoCard.animate()
            .translationY(-4f)
            .setDuration(120)
            .withEndAction {
                binding.gameInfoCard.animate()
                    .translationY(0f)
                    .setDuration(120)
                    .start()
            }
            .start()

        launchConfettiBurst()
    }

    private fun playRightGuessFeedback() {
        playSound(soundRight, 0.95f, SoundChannel.GUESS)
        vibrateIfEnabled(35)

        binding.guessInputLayout.animate()
            .scaleX(1.02f)
            .scaleY(1.02f)
            .setDuration(80)
            .withEndAction {
                binding.guessInputLayout.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .start()
            }
            .start()
    }

    private fun playWrongGuessFeedback() {
        playSound(soundWrong, 0.9f, SoundChannel.GUESS)
        vibrateIfEnabled(25)

        binding.guessInputLayout.animate()
            .translationX(8f)
            .setDuration(45)
            .withEndAction {
                binding.guessInputLayout.animate()
                    .translationX(-8f)
                    .setDuration(45)
                    .withEndAction {
                        binding.guessInputLayout.animate()
                            .translationX(0f)
                            .setDuration(45)
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun setupSoundPool() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .setMaxStreams(3)
            .build()

        soundRight = soundPool?.load(requireContext(), R.raw.sfx_right, 1) ?: 0
        soundWrong = soundPool?.load(requireContext(), R.raw.sfx_wrong, 1) ?: 0
        soundWin = soundPool?.load(requireContext(), R.raw.sfx_win, 1) ?: 0
        soundKey = soundPool?.load(requireContext(), R.raw.sfx_key, 1) ?: 0
    }

    private fun playSound(
        soundId: Int,
        volume: Float = 0.8f,
        channel: SoundChannel = SoundChannel.GUESS,
    ) {
        val context = context ?: return
        if (soundId == 0) return

        val enabled = when (channel) {
            SoundChannel.GUESS -> UserPreferences.isGuessSoundEnabled(context)
            SoundChannel.KEYPAD -> UserPreferences.isKeypadSoundEnabled(context)
        }
        if (!enabled) return

        soundPool?.play(soundId, volume, volume, 1, 0, 1f)
    }

    private fun updateGuessListViewport() {
        binding.root.post {
            val rootHeight = binding.root.height
            if (rootHeight <= 0) return@post

            val keyboardHeight = if (binding.customKeyboardCard.visibility == View.VISIBLE) {
                binding.customKeyboardCard.height + (binding.customKeyboardCard.layoutParams as MarginLayoutParams).bottomMargin
            } else {
                0
            }

            val bottomInsets = ViewCompat.getRootWindowInsets(binding.root)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())
                ?.bottom ?: 0

            val baseTop = binding.guessRow.bottom
            val available = rootHeight - baseTop - keyboardHeight - bottomInsets - dpToPx(16)
            val targetHeight = available.coerceIn(dpToPx(140), dpToPx(460))

            val params = binding.rvGuesses.layoutParams
            if (params.height != targetHeight) {
                params.height = targetHeight
                binding.rvGuesses.layoutParams = params
            }
        }
    }

    private fun vibrateIfEnabled(durationMs: Long) {
        val context = context ?: return
        if (!UserPreferences.isVibrationEnabled(context)) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager ?: return
            manager.defaultVibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            return
        }

        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    private fun launchConfettiBurst() {
        binding.confettiLayer.removeAllViews()
        binding.confettiLayer.visibility = View.VISIBLE

        val layerWidth = binding.confettiLayer.width.takeIf { it > 0 } ?: binding.root.width
        val layerHeight = binding.confettiLayer.height.takeIf { it > 0 } ?: binding.root.height
        if (layerWidth <= 0 || layerHeight <= 0) {
            return
        }

        val colors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.brand_primary),
            ContextCompat.getColor(requireContext(), R.color.brand_secondary),
            ContextCompat.getColor(requireContext(), R.color.purple_200),
            ContextCompat.getColor(requireContext(), R.color.teal_200),
        )

        repeat(36) {
            val size = dpToPx(Random.nextInt(6, 11))
            val piece = View(requireContext())
            val shape = GradientDrawable().apply {
                this.shape = GradientDrawable.OVAL
                setColor(colors.random())
            }
            piece.background = shape

            val startX = Random.nextInt(0, (layerWidth - size).coerceAtLeast(1)).toFloat()
            val startY = Random.nextInt(dpToPx(36), dpToPx(96)).toFloat()
            val travelY = Random.nextInt((layerHeight * 0.38f).toInt(), (layerHeight * 0.75f).toInt()).toFloat()
            val driftX = Random.nextInt(-dpToPx(80), dpToPx(80)).toFloat()

            val params = FrameLayout.LayoutParams(size, size)
            params.leftMargin = startX.toInt()
            params.topMargin = startY.toInt()
            binding.confettiLayer.addView(piece, params)

            piece.animate()
                .translationYBy(travelY)
                .translationXBy(driftX)
                .rotation(Random.nextInt(180, 720).toFloat())
                .alpha(0f)
                .setDuration(Random.nextLong(900L, 1500L))
                .withEndAction {
                    binding.confettiLayer.removeView(piece)
                    if (binding.confettiLayer.childCount == 0) {
                        binding.confettiLayer.visibility = View.GONE
                    }
                }
                .start()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun animateKeyPress(key: View) {
        key.animate()
            .scaleX(0.94f)
            .scaleY(0.94f)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(55L)
            .withEndAction {
                key.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(DecelerateInterpolator())
                    .setDuration(75L)
                    .start()
            }
            .start()
    }

    private fun postDelayed(delayMs: Long, action: () -> Unit) {
        mainHandler.postDelayed(action, delayMs)
    }

    override fun onDestroyView() {
        scrollClampListener?.let { listener ->
            if (binding.gameScroll.viewTreeObserver.isAlive) {
                binding.gameScroll.viewTreeObserver.removeOnScrollChangedListener(listener)
            }
        }
        scrollClampListener = null
        soundPool?.release()
        soundPool = null
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }
}