package com.thekeval.guesser.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.thekeval.guesser.R
import com.thekeval.guesser.adapters.DataItem
import com.thekeval.guesser.adapters.GuessesAdapter
import com.thekeval.guesser.databinding.FragmentGameBinding
import com.thekeval.guesser.viewmodel.GameViewModel
import kotlinx.android.synthetic.main.fragment_game.*

/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : Fragment() {

    enum class GameState {
        NOT_STARTED, STARTED, WON, ABANDONED
    }

    private lateinit var binding: FragmentGameBinding
    private lateinit var viewModel: GameViewModel

    lateinit var etNumber: EditText
    lateinit var btnHide: Button
    lateinit var btnAuto: Button
    lateinit var btnReset: Button
    lateinit var btnCheck: Button
    lateinit var switchMode: SwitchMaterial
    lateinit var etSeekerNumber: EditText
    lateinit var rvGuesses: RecyclerView
    lateinit var txtGameInfo: TextView
    lateinit var viewHide: View
    lateinit var txtStatus: TextView
    lateinit var switchTxtAppMode: TextView
    lateinit var switchTxtFriendMode: TextView


    var gameState = GameState.NOT_STARTED
    var isAutoMode = true
    var pickedNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate<FragmentGameBinding>(
            inflater,
            R.layout.fragment_game,
            container,
            false
        )
        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        var adapter = viewModel.lstGuesses.value?.let { GuessesAdapter(it) }
        binding.rvGuesses.adapter = adapter

        etNumber = binding.etNumber
        btnHide = binding.btnHide
        btnAuto = binding.btnAuto
        btnReset = binding.btnReset
        btnCheck = binding.btnCheck
        switchMode = binding.switchMode
        etSeekerNumber = binding.etSeekerNumber
        rvGuesses = binding.rvGuesses
        txtGameInfo = binding.txtGameInfo
        viewHide = binding.viewHide
        txtStatus = binding.txtStatus
        switchTxtAppMode = binding.switchTxtAppMode
        switchTxtFriendMode = binding.switchTxtFriendMode


        val html = "<b>Hide &amp; Seek</b> a number with 3 unique digits, <b>Picker</b> hides it, <b>Seeker</b> finds it by guessing. Each guess opens clues to solve the puzzle!"
        txtGameInfo.text = Html.fromHtml(html)

        btnHide.setOnClickListener {

            if (etNumber.text.toString().isEmpty() ||
                etNumber.text.toString().toInt() > 999 ||
                viewModel.isNotUnique3D(etNumber.text.toString()) ) {
                AlertDialog.Builder(context)
                    .setTitle("Oops!")
                    .setMessage("Hey PICKER,\nYou must PICK a number with 3 UNIQUE digits.")
                    .setPositiveButton("Got it", DialogInterface.OnClickListener { dialog, which ->
                        etNumber.setText("")
                    })
                    .show()
                return@setOnClickListener
            }

            if (btnHide.text.toString().toLowerCase() == "hide") {
                // game started
                viewHide.visibility = View.VISIBLE
                btnHide.text = "Show"
                etNumber.isEnabled = false

                pickedNumber = etNumber.text.toString()
                setControlsForGameOn();

                gameState = GameState.STARTED

            } else if (btnHide.text.toString().toLowerCase() == "show") {
                AlertDialog.Builder(context)
                    .setTitle("Warning!")
                    .setMessage("Once you see the number, the game will reset.")
                    .setPositiveButton("Got it", DialogInterface.OnClickListener { dialog, which ->
                        // let the user see the number and only let user reset the game.
                        viewHide.visibility = View.GONE
                        btnHide.text = "Hide"
                        btnHide.isEnabled = false
                        etNumber.isEnabled = false

                        txtStatus.setText("Press Reset to play again...")
                        etSeekerNumber.isEnabled = false
                        btnCheck.isEnabled = false

                        gameState = GameState.ABANDONED

                    })
                    .setNegativeButton("Cancel", null)
                    .show()

            }
        }

        btnAuto.setOnClickListener {

            if (btnAuto.text.toString().toLowerCase() == "auto") {
                val autoNum = viewModel.autoGen3D()
                etNumber.setText(autoNum)
                btnAuto.setText("Show")
                // btnHide.callOnClick()

                viewHide.visibility = View.VISIBLE
                etNumber.isEnabled = false
                btnReset.isEnabled = true

                pickedNumber = etNumber.text.toString()
                setControlsForGameOn();

                txtStatus.setText(R.string.gameOnStatus)

                gameState = GameState.STARTED
            }
            else if (btnAuto.text.toString().toLowerCase() == "show") {
                AlertDialog.Builder(context)
                    .setTitle("Warning!")
                    .setMessage("Once you see the number, the game will reset.")
                    .setPositiveButton("Got it", DialogInterface.OnClickListener { dialog, which ->
                        // let the user see the number and only let user reset the game.
                        binding.viewHide.visibility = View.GONE
                        btnAuto.setText("Auto")
                        btnAuto.isEnabled = false
                        binding.txtStatus.setText("Press Reset to play again...")
                        binding.etSeekerNumber.isEnabled = false
                        binding.btnCheck.isEnabled = false

                        gameState = GameState.ABANDONED

                    })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
                        // do nothing
                    })
                    .show()

            }
        }

        btnReset.setOnClickListener {

            // TODO: add a check when we force user to press reset if the game state is abandoned or won
            if (gameState == GameState.WON || gameState == GameState.ABANDONED) {
                processReset()
            }
            else {
                AlertDialog.Builder(context)
                    .setTitle("Warning!")
                    .setMessage("This will reset the game!")
                    .setPositiveButton("Got It", DialogInterface.OnClickListener { dialogInterface, i ->
                        // reset the game for current player mode
                        processReset()

                    })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
                        // do nothing
                    })
                    .show()
            }

        }

        btnCheck.setOnClickListener {

            if (viewModel.isNotUnique3D(binding.etSeekerNumber.text.toString())) {
                AlertDialog.Builder(context)
                    .setTitle("Oops!")
                    .setMessage("Hey SEEKER,\nYou must ENTER a number with 3 UNIQUE digits.")
                    .setPositiveButton("Got it", null).show()
                binding.etSeekerNumber.setText("")
                return@setOnClickListener
            }

            val guessedNumber = binding.etSeekerNumber.text.toString()
            val remark = viewModel.generateRemark(pickedNumber, guessedNumber)

            viewModel.addGuess(guessedNumber, remark)
            adapter?.notifyDataSetChanged()

            binding.etSeekerNumber.setText("")

            rvGuesses.smoothScrollToPosition(viewModel.lstGuesses.value!!.count() - 1)

            if (remark.toLowerCase() == "winner") {
                AlertDialog.Builder(context)
                    .setTitle("Winner!")
                    .setMessage("Well done! You found it!")
                    .setPositiveButton("Got it", DialogInterface.OnClickListener { dialog, i ->
                        // btnAuto.callOnClick()

                        // ------------ todo: make a common method which reveals the number and shift below code in that method.

                        if (isAutoMode) {
                            // winning situation in single player
                            viewHide.visibility = View.GONE
                            btnAuto.setText("Auto")
                            btnAuto.isEnabled = false
                            txtStatus.setText("Press Reset to play again.")
                            etSeekerNumber.isEnabled = false
                            btnCheck.isEnabled = false

                            gameState = GameState.WON
                        }
                        else {
                            // winning situationin double player
                            viewHide.visibility = View.GONE
                            btnHide.setText("Hide")
                            btnHide.isEnabled = false
                            txtStatus.setText("Press Reset to play again.")
                            etSeekerNumber.isEnabled = false
                            btnCheck.isEnabled = false

                            gameState = GameState.WON
                        }



                        // ------------------------------------------------

                    }).show()
            }
        }

        switchMode.setOnCheckedChangeListener { switch, switchOn ->
            if (isAutoMode == !switchOn) {
                // do nothing & return
                return@setOnCheckedChangeListener
            }

            if (gameState == GameState.NOT_STARTED) {
                // change the game mode without asking
                isAutoMode = !switchOn      // switch

                setControlsOnSwitch()
            }
            else {
                AlertDialog.Builder(context)
                    .setTitle("Warning!")
                    .setMessage("Game is ON, changing the mode will reset the game. Do you want to switch mode?")
                    .setPositiveButton("Switch", DialogInterface.OnClickListener { dialogInterface, i ->
                        // reset everything and change the game mode
                        // binding.btnReset.callOnClick()

                        // isAutoMode = !switchOn
                        processSwitch()

//                        gameState = GameState.NOT_STARTED
//
//                        btnAuto.visibility = View.GONE
//                        btnHide.visibility = View.VISIBLE
//                        etNumber.isEnabled = true
                    })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
                        switchMode.isChecked = !switchOn
                        // isAutoMode = !isAutoMode
                    })
                    .show()
            }

        }


        viewModel.lstGuesses.observe(viewLifecycleOwner, Observer { guesses ->
            if (guesses.isNotEmpty()) {
                rvGuesses.visibility = View.VISIBLE
                adapter?.submitList(guesses.map {
                    DataItem.GuessItem(it)
                })
            }
            else {
                rvGuesses.visibility = View.GONE
            }
        })

        viewModel.hideRV.observe(viewLifecycleOwner, Observer { hideRv ->
            if (hideRv) {
                rvGuesses.visibility = View.GONE
            }
            else {
                rvGuesses.visibility = View.VISIBLE
            }
        })

        switchTxtAppMode.setOnClickListener {
            switchMode.isChecked = false
        }

        switchTxtFriendMode.setOnClickListener {
            switchMode.isChecked = true
        }

        return binding.root
    }


    private fun setControlsForGameOn() {
        //binding.txtInstructions.visibility = View.VISIBLE
        etSeekerNumber.visibility = View.VISIBLE
        btnCheck.visibility = View.VISIBLE
        rvGuesses.visibility = View.VISIBLE

        etSeekerNumber.isEnabled = true
        btnCheck.isEnabled = true
        btnReset.isEnabled = true

        etSeekerNumber.requestFocus()
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(yourEditText, InputMethodManager.SHOW_IMPLICIT);
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(etSeekerNumber, InputMethodManager.SHOW_FORCED)
    }

    fun processSwitch() {

        processReset()
        isAutoMode = !isAutoMode

        setControlsOnSwitch()
    }

    fun setControlsOnSwitch() {
        if (isAutoMode) {
            // set the template for App mode
            // this will present the single player mode initial screen
            btnAuto.visibility = View.VISIBLE
            btnHide.visibility = View.GONE
            etNumber.isEnabled = false
            txtStatus.setText(R.string.initialStatusAppMode)

            switchTxtAppMode.setTextColor(resources.getColor(R.color.colorPrimary))
            switchTxtAppMode.setTypeface(null, Typeface.BOLD)
            switchTxtFriendMode.setTextColor(Color.GRAY)
            switchTxtFriendMode.setTypeface(null, Typeface.NORMAL)
        }
        else {
            // set the template for Friend mode
            // this will present the double player mode initial screen
            btnAuto.visibility = View.GONE
            btnHide.visibility = View.VISIBLE
            etNumber.isEnabled = true
            txtStatus.setText(R.string.initialStatusFriendMode)

            switchTxtAppMode.setTextColor(Color.GRAY)
            switchTxtAppMode.setTypeface(null, Typeface.NORMAL)
            switchTxtFriendMode.setTextColor(resources.getColor(R.color.colorPrimary))
            switchTxtFriendMode.setTypeface(null, Typeface.BOLD)
        }
    }

    fun processReset() {
        if (isAutoMode) {
            // reset single player mode
            singleModeReset()
        }
        else {
            // reset double player mode
            doubleModeReset()
        }

        gameState = GameState.NOT_STARTED
    }

    fun doubleModeReset() {
        btnHide.text = "Hide"
        btnHide.isEnabled = true
        btnAuto.visibility = View.GONE
        viewHide.visibility = View.GONE
        etNumber.isEnabled = true
        txtStatus.setText(R.string.initialStatusFriendMode)

        // common things to change
        etSeekerNumber.visibility = View.GONE
        btnCheck.visibility = View.GONE
        viewModel.resetGuesses()
        etNumber.setText("")
        btnReset.isEnabled = false
        etSeekerNumber.setText("")
        etSeekerNumber.isEnabled = false
    }

    fun singleModeReset() {
        btnAuto.isEnabled = true
        txtStatus.setText(R.string.initialStatusAppMode)
        btnAuto.setText("Auto")
        viewHide.visibility = View.GONE
        etNumber.isEnabled = false

        // common things to change
        etSeekerNumber.visibility = View.GONE
        btnCheck.visibility = View.GONE
        viewModel.resetGuesses()
        etNumber.setText("")
        btnReset.isEnabled = false
        etSeekerNumber.setText("")
        etSeekerNumber.isEnabled = false
    }

}