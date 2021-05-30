package com.thekeval.guesser.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.opengl.Visibility
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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

    private lateinit var binding: FragmentGameBinding
    private lateinit var viewModel: GameViewModel

    var gameStarted = false
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

        val etNumber = binding.etNumber
        val btnHide = binding.btnHide
        val btnAuto = binding.btnAuto

        val html = "<b>Hide &amp; Seek</b> a number with 3 unique digits, <b>Picker</b> hides it, <b>Seeker</b> finds it by guessing. Each guess opens clues to solve the puzzle!"
        binding.txtGameInfo.text = Html.fromHtml(html)

        btnHide.setOnClickListener {

            if (etNumber.text.toString().isEmpty() ||
                etNumber.text.toString().toInt() > 999 ||
                viewModel.isNotUnique3D(etNumber.text.toString())
            ) {
                //  Toast.makeText(context,"Number must be less than 999",Toast.LENGTH_LONG).show()
                AlertDialog.Builder(context)
                    .setTitle("Oops!")
                    .setMessage("Hey PICKER,\nYou must PICK a number with 3 UNIQUE digits.")
                    .setPositiveButton("Got it", DialogInterface.OnClickListener { dialog, which ->
                        binding.etNumber.setText("")
                    }).show()
                //        .setPositiveButton("Got it", null).show()
                //    binding.etNumber.setText("")
                return@setOnClickListener
            }

            val btnHide = binding.btnHide

            if (btnHide.text.toString().toLowerCase() == "hide") {
                // game started
                binding.viewHide.visibility = View.VISIBLE
                btnHide.text = "Show"
                binding.etNumber.isEnabled = false

                pickedNumber = etNumber.text.toString()
                updateUi();

                gameStarted = true

            } else if (btnHide.text.toString().toLowerCase() == "show") {
                binding.viewHide.visibility = View.GONE
                btnHide.text = "Hide"
                binding.etNumber.isEnabled = true
            }
        }

        btnAuto.setOnClickListener {

            if (btnAuto.text.toString().toLowerCase() == "auto") {
                val autoNum = viewModel.autoGen3D()
                binding.etNumber.setText(autoNum)
                btnAuto.setText("Show")
                // btnHide.callOnClick()

                binding.viewHide.visibility = View.VISIBLE
                binding.etNumber.isEnabled = false
                binding.btnReset.isEnabled = true

                pickedNumber = etNumber.text.toString()
                updateUi();

                binding.txtStatus.setText("Game on, make a guess and press Check!")
            }
            else if (btnAuto.text.toString().toLowerCase() == "show") {
                binding.viewHide.visibility = View.GONE
                btnAuto.setText("Auto")
                btnAuto.isEnabled = false
                binding.txtStatus.setText("Press Reset to play again...")
                binding.etSeekerNumber.isEnabled = false
                binding.btnCheck.isEnabled = false
            }
        }

        binding.btnReset.setOnClickListener {

            if (binding.switchMode.isChecked) {
                // picker friend
                btnHide.text = "Hide"
                btnAuto.visibility = View.GONE
                binding.viewHide.visibility = View.GONE
                etNumber.isEnabled = true
                binding.txtStatus.setText(R.string.txtGameStatus_double)

            }
            else {
                // auto mode
                btnAuto.isEnabled = true
                binding.txtStatus.setText(R.string.txtGameStatus)
                btnAuto.setText("Auto")
                binding.viewHide.visibility = View.GONE
                etNumber.isEnabled = false
            }

            // common things to change
            et_seeker_number.visibility = View.GONE
            btnCheck.visibility = View.GONE
            viewModel.resetGuesses()
            etNumber.setText("")
            binding.btnReset.isEnabled = false

        }

        binding.btnCheck.setOnClickListener {

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
                        btnAuto.callOnClick()
                    }).show()
            }
        }

        binding.switchMode.setOnCheckedChangeListener { switch, switchOn ->
            if (switchOn) {
                if (binding.etSeekerNumber.visibility == View.VISIBLE) {
                    // game started
                    AlertDialog.Builder(context)
                        .setTitle("Warning!")
                        .setMessage("Game is ON, changing the mode will reset the game. Do you want to switch mode?")
                        .setPositiveButton("Switch", DialogInterface.OnClickListener { dialogInterface, i ->
                            // reset everything and change the game mode
                            binding.btnReset.callOnClick()

                            btnAuto.visibility = View.GONE
                            btnHide.visibility = View.VISIBLE
                            etNumber.isEnabled = true
                        })
                        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
                            // do nothing

                        })
                        .show()
                }
                else {
                    // game not started
                    binding.btnReset.callOnClick()

                    btnAuto.visibility = View.GONE
                    btnHide.visibility = View.VISIBLE
                    etNumber.isEnabled = true
                }

            }
            else {
                btnAuto.visibility = View.VISIBLE
                btnHide.visibility = View.GONE
                etNumber.isEnabled = false
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



        return binding.root
    }

    private fun updateUi() {
        //binding.txtInstructions.visibility = View.VISIBLE
        binding.etSeekerNumber.visibility = View.VISIBLE
        binding.btnCheck.visibility = View.VISIBLE
        binding.rvGuesses.visibility = View.VISIBLE

        binding.etSeekerNumber.isEnabled = true
        binding.btnCheck.isEnabled = true
    }

}