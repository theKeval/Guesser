package com.thekeval.guesser.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.thekeval.guesser.databinding.GuessViewBinding
import com.thekeval.guesser.model.GuessModel

class GuessesAdapter(val guesses: List<GuessModel>) :
    androidx.recyclerview.widget.ListAdapter<DataItem, GuessesAdapter.GuessViewHolder>(GuessDiffCallback()) {
    // RecyclerView.Adapter<GuessesAdapter.GuessViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuessViewHolder {
        return GuessViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: GuessViewHolder, position: Int) {
        val guess = guesses.get(position)
        holder.bind(guess)
    }

    override fun getItemCount(): Int {
        return guesses.count()
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

sealed class DataItem {
    abstract val id: Long

    data class GuessItem(val guess: GuessModel) : DataItem() {
        override val id: Long
            get() = guess.id
    }

}

class GuessDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }

}
