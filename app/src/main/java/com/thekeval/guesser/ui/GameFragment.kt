package com.thekeval.guesser.ui

import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.thekeval.guesser.R
import com.thekeval.guesser.databinding.FragmentGameBinding

/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : Fragment() {

    lateinit var binding: FragmentGameBinding
    var gameStarted = false
    var pickedNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate<FragmentGameBinding>(inflater, R.layout.fragment_game, container, false)

        val etNumber = binding.etNumber
        val btnHinde = binding.btnHide

        btnHinde.setOnClickListener {
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
                    pickedNumber =  etNumber.text.toString().toInt()
                    proceed();
                }
            }
            else if (btnHide.text.toString().toLowerCase() == "show") {
                binding.viewHide.visibility = View.GONE
                btnHide.text = "Hide"
                binding.etNumber.isEnabled = true
            }
        }

        return binding.root
    }

    private fun proceed() {
        binding.txtInstructions.visibility = View.VISIBLE
        binding.etSeekerNumber.visibility = View.VISIBLE
        binding.btnCheck.visibility = View.VISIBLE
        binding.rvGuesses.visibility = View.VISIBLE
    }

}