package com.example.reto_3_4 // Asegúrate que el package sea el correcto

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class DifficultyDialogFragment : DialogFragment() {

    // Interfaz para comunicar la selección de vuelta a la Activity
    interface DifficultyDialogListener {
        fun onDifficultySelected(difficultyLevel: TicTacToeGame.DifficultyLevel)
    }

    var listener: DifficultyDialogListener? = null
    private var currentDifficulty: TicTacToeGame.DifficultyLevel = TicTacToeGame.DifficultyLevel.Expert

    companion object {
        private const val ARG_CURRENT_DIFFICULTY = "current_difficulty"

        // Método factoría para crear una instancia y pasar argumentos
        fun newInstance(currentDifficulty: TicTacToeGame.DifficultyLevel): DifficultyDialogFragment {
            val args = Bundle()
            args.putSerializable(ARG_CURRENT_DIFFICULTY, currentDifficulty) // Enum es Serializable
            val fragment = DifficultyDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    fun setDifficultyDialogListener(listener: DifficultyDialogListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentDifficulty = it.getSerializable(ARG_CURRENT_DIFFICULTY) as? TicTacToeGame.DifficultyLevel
                ?: TicTacToeGame.DifficultyLevel.Expert
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val difficultyLevels = TicTacToeGame.DifficultyLevel.entries
        val difficultyLevelStrings = difficultyLevels.map { it.name }.toTypedArray()

        val currentSelectedIndex = difficultyLevels.indexOf(currentDifficulty)

        val builder = AlertDialog.Builder(requireActivity()) // Usa requireActivity() para el contexto
        builder.setTitle(R.string.dialog_difficulty_title) // Define este string en strings.xml
            .setSingleChoiceItems(difficultyLevelStrings, currentSelectedIndex) { dialog, which ->
                // El usuario seleccionó una opción, pero aún no la aplicamos hasta "OK"
                // Puedes guardar 'which' temporalmente si quieres
            }
            .setPositiveButton(R.string.dialog_ok) { dialog, id ->
                val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                if (selectedPosition != -1) {
                    val selectedDifficulty = difficultyLevels[selectedPosition]
                    listener?.onDifficultySelected(selectedDifficulty)
                }
                dismiss() // Cierra el diálogo
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, id ->
                dismiss() // Cierra el diálogo
            }
        return builder.create()
    }
}