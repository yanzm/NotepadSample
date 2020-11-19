/*
 * Copyright (C) 2011 Yuki Anzai, uPhyca Inc.
 *      http://www.uphyca.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package yanzm.sample.notepad.ui.edit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import yanzm.sample.notepad.databinding.FragmentEditBinding

class EditFragment : Fragment() {

    interface EditFragmentListener {
        fun onNoteDeleted()
        fun onNoteSaved()
    }

    private var listener: EditFragmentListener? = null

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? EditFragmentListener
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditBinding.inflate(inflater, container, false)

        binding.titleView.doOnTextChanged { _, _, _, _ ->
            binding.titleTextInputLayout.error = null
            updateSaveButton()
        }

        binding.bodyView.doOnTextChanged { _, _, _, _ ->
            updateSaveButton()
        }

        binding.saveButton.setOnClickListener {
            closeKeyboard(it)
            saveNote()
        }

        binding.deleteButton.setOnClickListener {
            showDeleteConfirmDialog()
        }

        viewModel.setNoteId(arguments?.getLong(ARGS_NOTE_ID, 0L) ?: 0L)

        viewModel.note.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.deleteButton.visibility = View.VISIBLE
                if (binding.titleView.text!!.isEmpty()) {
                    binding.titleView.setText(it.title)
                }
                if (binding.bodyView.text!!.isEmpty()) {
                    binding.bodyView.setText(it.body)
                }
            } else {
                binding.deleteButton.visibility = View.INVISIBLE
            }
            updateSaveButton()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage("Delete this Note?")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                deleteNote()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun deleteNote() {
        viewModel.delete {
            listener?.onNoteDeleted()
        }
    }

    private fun closeKeyboard(view: View) {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun saveNote() {
        val title = binding.titleView.text.toString()
        if (title.isEmpty()) {
            binding.titleTextInputLayout.error = "title must not be null"
            return
        }

        val body = binding.bodyView.text.toString()

        viewModel.save(title, body) {
            Toast.makeText(requireContext(), "Note Saved", Toast.LENGTH_SHORT).show()
            listener?.onNoteSaved()
        }
    }

    private fun updateSaveButton() {
        val title = binding.titleView.text.toString()
        val body = binding.bodyView.text.toString()

        val note = viewModel.note.value

        val originalTitle = note?.title ?: ""
        val originalBody = note?.body ?: ""

        binding.saveButton.isEnabled = title != originalTitle || body != originalBody
    }

    fun confirmDiscard(onDiscarded: () -> Unit) {
        if (!binding.saveButton.isEnabled) {
            onDiscarded()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setMessage("Are you sure to discard changes?")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onDiscarded()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        private const val ARGS_NOTE_ID = "NOTE_ID"

        fun newInstance(noteId: Long): EditFragment = EditFragment().apply {
            arguments = bundleOf(ARGS_NOTE_ID to noteId)
        }
    }
}
