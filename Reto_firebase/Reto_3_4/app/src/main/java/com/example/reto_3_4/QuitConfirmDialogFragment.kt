package com.example.reto_3_4

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class QuitConfirmDialogFragment : DialogFragment() {

    interface QuitConfirmDialogListener {
        fun onQuitConfirmed()
        // Opcional: fun onQuitCancelled() si necesitas manejar la cancelación
    }

    private var listener: QuitConfirmDialogListener? = null


    companion object {
        // No se necesitan argumentos para este diálogo simple, pero mantenemos el newInstance por consistencia
        fun newInstance(): QuitConfirmDialogFragment {
            return QuitConfirmDialogFragment()
        }
    }

    // Método más seguro para establecer el listener
    fun setQuitConfirmDialogListener(listener: QuitConfirmDialogListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(R.string.dialog_quit_title)      // "¿Salir del juego?"
            .setPositiveButton(R.string.dialog_ok) { dialog, id ->  // "Sí"
                listener?.onQuitConfirmed()
                dismiss()
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, id -> // "No"
                // listener?.onQuitCancelled() // Opcional
                dismiss()
            }
        return builder.create()
    }
}