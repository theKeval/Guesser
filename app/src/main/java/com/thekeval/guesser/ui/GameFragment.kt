package com.thekeval.guesser.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate<FragmentGameBinding>(inflater, R.layout.fragment_game, container, false)
        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val etNumber = binding.etNumber
        val btnHide = binding.btnHide

        btnHide.setOnClickListener {

            if (etNumber.text.toString().isEmpty()) {
                Toast.makeText(context, "Pick a 3 digit number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val btnHide = binding.btnHide

            if (btnHide.text.toString().toLowerCase() == "hide") {
                binding.viewHide.visibility = View.VISIBLE
                btnHide.text = "Show"
                binding.etNumber.isEnabled = false

                if (!gameStarted) {
                    pickedNumber =  etNumber.text.toString()
                    updateUi();
                    gameStarted = true
                }
            }
            else if (btnHide.text.toString().toLowerCase() == "show") {
                binding.viewHide.visibility = View.GONE
                btnHide.text = "Hide"
                binding.etNumber.isEnabled = true
            }
        }

        var adapter = viewModel.lstGuesses.value?.let { GuessesAdapter(it) }
        binding.rvGuesses.adapter = adapter

        viewModel.lstGuesses.observe(viewLifecycleOwner, Observer { guesses ->
            if (guesses.isNotEmpty()) {
                adapter?.submitList(guesses.map {
                    DataItem.GuessItem(it)
                })
            }
        })

        binding.btnCheck.setOnClickListener {
            val guessedNumber = binding.etSeekerNumber.text.toString()
            val remark = generateRemark(pickedNumber, guessedNumber)

            viewModel.addGuess(guessedNumber, remark)
            adapter?.notifyDataSetChanged()

            binding.etSeekerNumber.setText("")
        }

        return binding.root
    }

    private fun updateUi() {
        binding.txtInstructions.visibility = View.VISIBLE
        binding.etSeekerNumber.visibility = View.VISIBLE
        binding.btnCheck.visibility = View.VISIBLE
        binding.rvGuesses.visibility = View.VISIBLE
    }

    private fun generateRemark(pickedNumber: String, guessedNumber: String): String {
        var remark = ""

        val abc = pickedNumber.toCharArray()
        val xyz = guessedNumber.toCharArray()

        var containCount = 0
        var rCount = 0
        var wCount = 0

        for (i in xyz) {
            if (abc.contains(i)) {
                containCount++
            }
        }

        xyz.forEachIndexed { index, c ->
            if (abc[index] == c) {
                rCount++
            }
        }

        if (containCount == 0) {
            remark = "SMYLE!"   // \nSimply Make Your Life Easy
        }
        else if (rCount == 3) {
            // Winner
            remark = "Winner"
        }
        else if (rCount == containCount) {
            remark = rCount.toString() + "R"
        }
        else if (rCount == 0 && containCount > 0) {
            remark = containCount.toString() + "W"
        }
        else {
            remark = rCount.toString() + "R, " + (containCount - rCount).toString() + "W"
        }

        return remark
    }

}