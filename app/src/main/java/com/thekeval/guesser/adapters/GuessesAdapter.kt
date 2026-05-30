package com.thekeval.guesser.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.thekeval.guesser.databinding.GuessViewBinding
import com.thekeval.guesser.model.GuessModel

class GuessesAdapter :
    androidx.recyclerview.widget.ListAdapter<GuessModel, GuessesAdapter.GuessViewHolder>(GuessDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuessViewHolder {
        return GuessViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: GuessViewHolder, position: Int) {
        holder.bind(getItem(position))
    }



    class GuessViewHolder private constructor(val binding: GuessViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): GuessViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GuessViewBinding.inflate(layoutInflater, parent, false)

                return GuessViewHolder(binding)
            }
        }

        fun bind(guess: GuessModel) {
            binding.guessModel = guess
            binding.executePendingBindings()
        }


    }

}
class GuessDiffCallback : DiffUtil.ItemCallback<GuessModel>() {
    override fun areItemsTheSame(oldItem: GuessModel, newItem: GuessModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: GuessModel, newItem: GuessModel): Boolean {
        return oldItem == newItem
    }

}
