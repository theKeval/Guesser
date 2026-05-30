package com.thekeval.guesser.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.thekeval.guesser.data.UserPreferences
import com.thekeval.guesser.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vibrationEnabled = UserPreferences.isVibrationEnabled(requireContext())
        val guessSoundEnabled = UserPreferences.isGuessSoundEnabled(requireContext())
        val keypadSoundEnabled = UserPreferences.isKeypadSoundEnabled(requireContext())
        binding.switchVibration.isChecked = vibrationEnabled
        binding.switchGuessSound.isChecked = guessSoundEnabled
        binding.switchKeypadSound.isChecked = keypadSoundEnabled

        binding.switchGuessSound.setOnCheckedChangeListener { _, isChecked ->
            UserPreferences.setGuessSoundEnabled(requireContext(), isChecked)
        }

        binding.switchKeypadSound.setOnCheckedChangeListener { _, isChecked ->
            UserPreferences.setKeypadSoundEnabled(requireContext(), isChecked)
        }

        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            UserPreferences.setVibrationEnabled(requireContext(), isChecked)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

